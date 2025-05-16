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

        Map<Konto.Vorsteuer, BigDecimal> vorsteuer = new HashMap<>();
        BigDecimal vorsteuerTotal = new BigDecimal(0);

        Map<Konto.Umsatzsteuer, BigDecimal> umsatzsteuer = new HashMap<>();
        BigDecimal umsatzsteuerTotal = new BigDecimal(0);

        Map<Konto.InnergemeinschaftlicherErwerb, BigDecimal> innergemeinschaftlich = new HashMap<>();
        BigDecimal innergemeinschaftlichTotal = new BigDecimal(0);

        for (Buchung buchung : list) {
            Optional<Konto.Steuer> kontoOptional = Util.determineSteuerkontoFromBuchung(buchung);
            if (kontoOptional.isEmpty()) {
                continue;
            }
            Konto.Steuer konto = kontoOptional.get();
            BigDecimal steuerBetrag;
            if (buchung.isBrutto()) {
                steuerBetrag = Calc.taxFromBrutto(buchung.getBetrag(), konto);
            } else {
                steuerBetrag = buchung.getBetrag().multiply(konto.getAmount(), Calc.DEFAULT_CONTEXT);
            }
            if (steuerBetrag.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Steuerbetrag <= 0 for buchung '{}' with konto {}", buchung.getBeschreibung(), konto);
                continue;
            }

            switch (konto) {
                case Konto.Vorsteuer vorsteuerKonto -> {
                    BigDecimal sum = vorsteuer.getOrDefault(vorsteuerKonto, new BigDecimal(0)).add(steuerBetrag);
                    vorsteuer.put(vorsteuerKonto, sum);
                    vorsteuerTotal = vorsteuerTotal.add(steuerBetrag);
                }
                case Konto.Umsatzsteuer umsatzsteuerKonto -> {
                    BigDecimal sum = umsatzsteuer.getOrDefault(umsatzsteuerKonto, new BigDecimal(0)).add(steuerBetrag);
                    umsatzsteuer.put(umsatzsteuerKonto, sum);
                    umsatzsteuerTotal = umsatzsteuerTotal.add(steuerBetrag);
                }
                case Konto.InnergemeinschaftlicherErwerb innergemeinschaftlichKonto -> {
                    BigDecimal sum = innergemeinschaftlich.getOrDefault(innergemeinschaftlichKonto, new BigDecimal(0)).add(steuerBetrag);
                    innergemeinschaftlich.put(innergemeinschaftlichKonto, sum);
                    innergemeinschaftlichTotal = innergemeinschaftlichTotal.add(steuerBetrag);
                }
                default -> throw new IllegalStateException("Unknown Steuerkonto type: " + konto.getClass().getName());
            }
        }
        // viel aufwende -> mehr Ust -> mehr Guthaben
        // viel erträge -> mehr VorSt. -> weniger Guthaben
        BigDecimal zahllast = vorsteuerTotal.subtract(umsatzsteuerTotal);

        vorsteuerTotal = vorsteuerTotal.add(innergemeinschaftlichTotal);
        umsatzsteuerTotal = umsatzsteuerTotal.add(innergemeinschaftlichTotal);


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(umsatzsteuerReport.render(
                    Map.of(
                            "startDate", start,
                            "endDate", end,
                            "vorsteuer", flattenMap(vorsteuer),
                            "vorsteuerTotal", Calc.formatToCurrency(vorsteuerTotal),
                            "umsatzsteuer", flattenMap(umsatzsteuer),
                            "umsatzsteuerTotal", Calc.formatToCurrency(umsatzsteuerTotal),
                            "innergemeinschaftlich", flattenMap(innergemeinschaftlich),
                            "innergemeinschaftlichTotal", Calc.formatToCurrency(innergemeinschaftlichTotal),
                            "zahllast", Calc.formatToCurrency(zahllast)
                    )
            ), null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }

    private List<Map<String, String>> flattenMap(Map<? extends Konto.Steuer, BigDecimal> steuerMap) {
        List<Map<String, String>> list = new LinkedList<>();
        steuerMap.forEach((konto, betrag) -> {
            list.add(Map.of(
                    "name", konto.getBeschreibung(),
                    "betrag", Calc.formatToCurrency(betrag)
            ));
        });
        return list;
    }

}
