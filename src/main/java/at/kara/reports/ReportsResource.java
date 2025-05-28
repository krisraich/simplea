/*
 * This file is part of the "SimplEA" application.
 *
 * Copyright (c) 2025 by KaRa Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.kara.reports;

import at.kara.buchungen.Buchung;
import at.kara.buchungen.BuchungenRepository;
import at.kara.common.Calc;
import at.kara.common.Util;
import at.kara.konten.Konto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static at.kara.common.Util.processValue;

@Path("/reports")
@Authenticated
@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
@Slf4j
public class ReportsResource {

    @Inject
    BuchungenRepository buchungenRepository;

    @Inject
    @Location("reports/ansicht.html")
    Template ansicht;

    @Inject
    @Location("pdf/umsatzsteuer.pdf.html")
    Template umsatzsteuerReport;

    @GET
    public TemplateInstance anzeigen() {
        return ansicht.instance();
    }

    @POST
    public Response create(MultipartFormDataInput input) {

        String type = processValue("type", input.getValues(), s -> s);

        byte[] pdf = switch (type) {
            case "umsatzsteuer" -> getUmsatzsteuerReport(input.getValues());
            default -> throw new BadRequestException("Report %s not found".formatted(type));
        };

        return Response
                .status(Response.Status.OK)
                .entity(pdf)
                .type("application/pdf")
                .build();
    }

    @SneakyThrows
    private byte[] getUmsatzsteuerReport(Map<String, Collection<FormValue>> values) {
        LocalDate start = processValue("startDate", values, LocalDate::parse);
        LocalDate end = processValue("endDate", values, LocalDate::parse);

        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before end date");
        }

        List<Buchung> list = buchungenRepository.find("{datum: {$gte: ?1, $lte: ?2}}", start, end).list();

        Map<Konto.Umsatzsteuer, BigDecimal> umsatzsteuerMap = new HashMap<>();
        Map<Konto.Vorsteuer, BigDecimal> vorsteuerMap = new HashMap<>();
        Map<Konto.InnergemeinschaftlicherErwerb, BigDecimal> innergemeinschaftlichMap = new HashMap<>();

        for (Buchung buchung : list) {
            Optional<Konto.Steuer> kontoOptional = Util.determineSteuerkontoFromBuchung(buchung);
            if (kontoOptional.isEmpty()) {
                continue;
            }
            Konto.Steuer konto = kontoOptional.get();
            BigDecimal bemessungsgrundlage = buchung.isBrutto() ? Calc.bruttoToNetto(buchung.getBetrag(), konto) : buchung.getBetrag();

            switch (konto) {
                case Konto.Umsatzsteuer UmsatzsteuerKonto -> {
                    BigDecimal sum = umsatzsteuerMap.getOrDefault(UmsatzsteuerKonto, new BigDecimal(0)).add(bemessungsgrundlage);
                    umsatzsteuerMap.put(UmsatzsteuerKonto, sum);
                }
                case Konto.Vorsteuer vorsteuerKonto -> {
                    BigDecimal sum = vorsteuerMap.getOrDefault(vorsteuerKonto, new BigDecimal(0)).add(bemessungsgrundlage);
                    vorsteuerMap.put(vorsteuerKonto, sum);
                }
                case Konto.InnergemeinschaftlicherErwerb innergemeinschaftlichKonto -> {
                    BigDecimal sum = innergemeinschaftlichMap.getOrDefault(innergemeinschaftlichKonto, new BigDecimal(0)).add(bemessungsgrundlage);
                    innergemeinschaftlichMap.put(innergemeinschaftlichKonto, sum);
                }
                default -> throw new IllegalStateException("Unknown Steuerkonto type: " + konto.getClass().getName());
            }
        }
        SteuerDTO vorsteuer = flattenMap(vorsteuerMap);
        SteuerDTO umsatzsteuer = flattenMap(umsatzsteuerMap);
        SteuerDTO innergemeinschaftlich = flattenMap(innergemeinschaftlichMap);

        // viel aufwende -> mehr Ust -> mehr Guthaben
        // viel erträge -> mehr VorSt. -> weniger Guthaben
        BigDecimal summeAbziehbareVorsteuer = vorsteuer.getSummeSteuer().add(innergemeinschaftlich.getSummeSteuer());
        BigDecimal zahllast = umsatzsteuer.getSummeSteuer().subtract(vorsteuer.getSummeSteuer());

        UstDTO ustDTO = new UstDTO()
                .setStartDate(start)
                .setEndDate(end)

                .setUmsatzsteuer(umsatzsteuer)
                .setVorsteuer(vorsteuer)
                .setInnergemeinschaftlich(innergemeinschaftlich)

                .setSummeAbziehbareVorsteuer(Calc.formatToCurrency(summeAbziehbareVorsteuer))
                .setIstZahllast(zahllast.compareTo(BigDecimal.ZERO) > 0)
                .setZahllast(Calc.formatToCurrency(zahllast.abs()));


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(umsatzsteuerReport.render(ustDTO), null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error creating PDF", e);
            throw new InternalServerErrorException("Error creating PDF: " + e.getMessage());
        }
    }

    private SteuerDTO flattenMap(Map<? extends Konto.Steuer, BigDecimal> steuerMap) {
        BigDecimal summeSteuern = new BigDecimal(0);
        BigDecimal summeBemessungsgrundlage = new BigDecimal(0);

        List<Map<String, String>> list = new LinkedList<>();

        for (Map.Entry<? extends Konto.Steuer, BigDecimal> entry : steuerMap.entrySet()) {
            Konto.Steuer konto = entry.getKey();
            BigDecimal betrag = entry.getValue();
            BigDecimal steuer = betrag.multiply(konto.getAmount());

            list.add(Map.of(
                    "name", konto.getBeschreibung(),
                    "konto", konto.getShortName(),
                    "bemessungsgrundlage", Calc.formatToCurrency(betrag),
                    "steuerbetrag", Calc.formatToCurrency(steuer)
            ));

            summeSteuern = summeSteuern.add(steuer);
            summeBemessungsgrundlage = summeBemessungsgrundlage.add(betrag);
        }

        return new SteuerDTO()
                .setSummeSteuer(summeSteuern)
                .setBemessungsgrundlage(summeBemessungsgrundlage)
                .setZeilen(list);
    }

}
