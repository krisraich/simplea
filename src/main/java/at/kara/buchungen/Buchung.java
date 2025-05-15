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

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@MongoEntity(collection="buchungen")
public class Buchung implements Comparable<Buchung> {

    @Data
    @Accessors(chain = true)
    public static class Attachment {

        private String fileName;
        private String fileType;
        @ToString.Exclude
        private byte[] data;
    }

    public ObjectId id;

    private LocalDate datum;

    private int buchungsNummer;

    private String beschreibung;

    private int kontoNummer;

    private boolean brutto;

    private BigDecimal betrag;

    private Steuersatz steuerSatz;

    private boolean einnahme;

    private Attachment beleg;

    @BsonIgnore
    @Override
    public int compareTo(Buchung o) {
        int dateCompare = datum.compareTo(o.datum);
        if(dateCompare != 0) {
            return dateCompare;
        }
        return Integer.compare(buchungsNummer, o.buchungsNummer);
    }

}
