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

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class Konto implements Comparable<Konto> {


    private final int nummer;

    private final String beschreibung;

    @BsonIgnore
    @Override
    public String toString() {
        return nummer + " " + beschreibung;
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

    @Getter
    @Accessors(chain = true)
    public abstract static class Steuer extends Konto {

        private final Steuersatz steuerSatz;
        private final BigDecimal amount;

        Steuer(int nummer, String beschreibung, Steuersatz steuerSatz, BigDecimal amount) {
            super(nummer, beschreibung);
            this.steuerSatz = steuerSatz;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return getBeschreibung();
        }
    }

    @Getter
    @Accessors(chain = true)
    public static class Umsatzsteuer extends Steuer {
        Umsatzsteuer(int nummer, String beschreibung, Steuersatz steuerSatz, BigDecimal amount) {
            super(nummer, beschreibung, steuerSatz, amount);
        }
    }

    @Getter
    @Accessors(chain = true)
    public static class Vorsteuer extends Steuer {
        Vorsteuer(int nummer, String beschreibung, Steuersatz steuerSatz, BigDecimal amount) {
            super(nummer, beschreibung, steuerSatz, amount);
        }
    }

    @Getter
    @Accessors(chain = true)
    public static class InnergemeinschaftlicherErwerb extends Steuer {
        InnergemeinschaftlicherErwerb(int nummer, String beschreibung, Steuersatz steuerSatz, BigDecimal amount) {
            super(nummer, beschreibung, steuerSatz, amount);
        }
    }

    /**
     * Konto 7xxx
     */
    @Getter
    @Accessors(chain = true)
    public abstract static class EAKonto extends Konto {

        EAKonto(int nummer, String beschreibung) {
            super(nummer, beschreibung);
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

        Aufwendungen(int nummer, String beschreibung) {
            this(nummer, beschreibung, Steuersatz.ZWANZIG);
        }

        Aufwendungen(int nummer, String beschreibung, Steuersatz steuerSatz) {
            super(nummer, beschreibung);
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

        Ertraege(int nummer, String beschreibung) {
            super(nummer, beschreibung);
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

        Split(int nummer, String beschreibung) {
            super(nummer, beschreibung, Steuersatz.OHNE);
        }

    }

}
