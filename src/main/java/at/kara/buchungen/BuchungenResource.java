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

package at.kara.buchungen;

import at.kara.common.BaseResource;
import at.kara.konten.KontenPlan;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.server.multipart.FileItem;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static at.kara.common.Util.processValue;

@Produces(MediaType.TEXT_HTML)
@ApplicationScoped
@Path("/buchungen")
public class BuchungenResource extends BaseResource {

    @Inject
    BuchungenRepository buchungenRepository;

    @Inject
    @Location("buchungen/tableRow.html")
    Template tableRow;

    public BuchungenResource() {
        super("buchungen");
    }

    @GET
    @Override
    public TemplateInstance index() {
        int currentYear = getCurrentUser().getCurrentYear();
        List<Buchung> buchungen = buchungenRepository
                .streamAll()
                .filter(buchung -> buchung.getDatum().getYear() == currentYear)
                .sorted(Collections.reverseOrder())
                .toList();

        return getTemplate()
                .data("buchungen", buchungen)
                .data("konten", KontenPlan.EA_LISTE)
                .data("steuerCodes", Steuersatz.values());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TemplateInstance create(MultipartFormDataInput input) {
        Buchung buchung = new Buchung();
        validateNewBuchung(input, buchung);
        buchung.setBuchungsNummer(getCurrentUser().getAndIncrementNextBuchungsNummer(buchung.getDatum()));
        buchungenRepository.persist(buchung);
        return tableRow.data(
                "buchung", buchung,
                "state", "Neu: "
        );
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TemplateInstance update(@PathParam("id") String id, MultipartFormDataInput input) {
        Buchung buchung = buchungenRepository.findByIdOptional(new ObjectId(id)).orElseThrow(NotFoundException::new);
        validateNewBuchung(input, buchung);
        buchungenRepository.update(buchung);
        return tableRow.data(
                "buchung", buchung,
                "state", "Geändert: "
        );
    }


    @SneakyThrows
    private void validateNewBuchung(MultipartFormDataInput input, Buchung buchung) {

        Map<String, Collection<FormValue>> values = input.getValues();

        buchung
                .setDatum(processValue("datum", values, LocalDate::parse))
                .setBeschreibung(processValue("beschreibung", values, s -> s))
                .setBetrag(processValue("betrag", values, BigDecimal::new))
                .setBrutto(values.containsKey("brutto"))
                .setEinnahme(processValue("einnahme", values, Boolean::parseBoolean))
                .setKontoNummer(processValue("kontoNummer", values, Integer::valueOf))
                .setSteuerSatz(processValue("steuerSatz", values, Steuersatz::valueOf));

        FormValue beleg = values.get("beleg").iterator().next();
        FileItem fileItem = beleg.getFileItem();
        byte[] bytes = Files.readAllBytes(fileItem.getFile());
        if (bytes.length > 0) {
            buchung.setBeleg(new Buchung.Attachment()
                    .setFileName(beleg.getFileName())
                    .setFileType(beleg.getHeaders().getFirst("Content-Type"))
                    .setData(bytes));
        }
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Buchung getSingle(@PathParam("id") String id) {
        return this.buchungenRepository.findByIdOptional(new ObjectId(id)).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        this.buchungenRepository.deleteById(new ObjectId(id));
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/attachment")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response show(@PathParam("id") String id) {
        Buchung.Attachment beleg = buchungenRepository.findByIdOptional(new ObjectId(id)).orElseThrow(NotFoundException::new).getBeleg();
        Response.ResponseBuilder response = Response.ok(beleg.getData());
        response.header("Content-Disposition", "attachment;filename=" + beleg.getFileName());
        response.header("Content-Type", beleg.getFileType());
        return response.build();
    }

}
