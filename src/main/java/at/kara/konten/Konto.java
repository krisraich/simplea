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

package at.kara.konten;

import at.kara.buchungen.Steuersatz;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class Konto implements Comparable<Konto> {


    private final int nummer;

    private final String name;

    @BsonIgnore
    @Override
    public String toString() {
        return nummer + " " + name;
    }

    public String getShortName() {
        return "" + nummer;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Konto && nummer == ((Konto) obj).nummer;
    }

    @Override
    public int compareTo(Konto o) {
        return Integer.compare(nummer, o.nummer);
    }

    /**
     * Konto 7xxx
     */
    @Getter
    @Accessors(chain = true)
    public abstract static class EAKonto extends Konto {

        EAKonto(int betrag, String beschreibung) {
            super(betrag, beschreibung);
        }

        public abstract boolean isEinnahme();

    }

    /**
     * Konto 7xxx
     */
    @Getter
    @Accessors(chain = true)
    public static class Aufwendungen extends EAKonto {

        private final Steuersatz steuerSatz;

        Aufwendungen(int betrag, String beschreibung) {
            this(betrag, beschreibung, Steuersatz.ZWANZIG);
        }

        Aufwendungen(int betrag, String beschreibung, Steuersatz steuerSatz) {
            super(betrag, beschreibung);
            this.steuerSatz = steuerSatz;
        }


        @Override
        public boolean isEinnahme() {
            return false;
        }

    }

    /**
     * Konto 4xxx
     */
    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Ertraege extends EAKonto {

        private final Steuersatz steuerSatz = Steuersatz.ZWANZIG;

        Ertraege(int betrag, String beschreibung) {
            super(betrag, beschreibung);
        }

        @Override
        public boolean isEinnahme() {
            return true;
        }

    }

    /**
     * Konto für predefined splits... eg. PKW Aufwand (75% PKW, 25% Privatentnahme)
     */
    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Split extends Aufwendungen {


        private final Map<Konto, Double> splitMap = new TreeMap<>();

        Split(int betrag, String beschreibung) {
            super(betrag, beschreibung, Steuersatz.OHNE);
        }

    }


}
