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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KontenPlan {

    public static Konto BANK = new Konto(2800, "Bank") {

        @Override
        public String getShortName() {
            return "BA";
        }
    };


    //steuerkonten
    public static final Konto.Umsatzsteuer UMSATZSTEUER_10 = new Konto.Umsatzsteuer(310, "Umsatzsteuer 10%", Steuersatz.ZEHN, new BigDecimal("0.1"));
    public static final Konto.Umsatzsteuer UMSATZSTEUER_20 = new Konto.Umsatzsteuer(320, "Umsatzsteuer 20%", Steuersatz.ZWANZIG, new BigDecimal("0.2"));

    public static final Konto.Vorsteuer VORSTEUER_10 = new Konto.Vorsteuer(210, "Vorsteuer 10%", Steuersatz.ZEHN, new BigDecimal("0.1"));
    public static final Konto.Vorsteuer VORSTEUER_20 = new Konto.Vorsteuer(220, "Vorsteuer 20%", Steuersatz.ZWANZIG, new BigDecimal("0.2"));

    public static Konto.InnergemeinschaftlicherErwerb INNERGEMEINSCHAFTLICHER_ERWERB_10 = new Konto.InnergemeinschaftlicherErwerb(
            21000, "Innergemeinschaftlicher Erwerb 10%", Steuersatz.ZEHN, new BigDecimal("0.1")) {
        @Override
        public String getShortName() {
            return "E210";
        }
    };
    public static final Konto.InnergemeinschaftlicherErwerb INNERGEMEINSCHAFTLICHER_ERWERB_20 = new Konto.InnergemeinschaftlicherErwerb(
            22000, "Innergemeinschaftlicher Erwerb 20%", Steuersatz.ZWANZIG, new BigDecimal("0.2")) {
        @Override
        public String getShortName() {
            return "E220";
        }
    };

    public static final Konto.Split PKW;

    static {
        Konto.Split pkw = new Konto.Split(7320, "PKW");
        pkw.getSplitMap().put(new Konto(9600, "Privatentnahme"), 0.25);
        pkw.getSplitMap().put(pkw, 0.75);
        PKW = pkw;
    }

    public static final List<Konto.EAKonto> EA_LISTE = List.of(
            new Konto.Ertraege(4000, "Einnahmen AT"),
            new Konto.Ertraege(4010, "Einnahmen EU"),

            new Konto.Aufwendungen(7060, "Geringe Wirtschaftsgüter bis 1000€"),
            new Konto.Aufwendungen(7600, "Büromaterial"),

            PKW,
            new Konto.Aufwendungen(7380, "Mobiltelefon"),

            new Konto.Aufwendungen(7160, "Tourismusabgabe", Steuersatz.OHNE),
            new Konto.Aufwendungen(7185, "Grundumlage WKO", Steuersatz.OHNE),

            new Konto.Aufwendungen(7620, "Fachliteratur", Steuersatz.ZEHN),
            new Konto.Aufwendungen(7670, "Werbung"),

            new Konto.Aufwendungen(7730, "Pflichtversicherung SVS", Steuersatz.OHNE),
            new Konto.Aufwendungen(7755, "Steuerberater")
    );

    public static final Map<Integer, Konto.EAKonto> MAP = EA_LISTE.stream().collect(Collectors.toMap(Konto::getNummer, konto -> konto));


}
