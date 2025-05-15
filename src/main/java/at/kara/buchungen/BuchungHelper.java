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

import at.kara.konten.KontenPlan;
import at.kara.konten.Konto;
import at.kara.math.Calc;
import io.quarkus.qute.TemplateExtension;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * haben an soll
 *
 * Darstellung:
 * (1) Aufwand ohne Steuer (Grundumlage zb)
 *  Bank -> 7185
 *
 * (2) Aufwand mit Steuer, gekauft Brutto
 *  Bank -> 7xxx, 2xx
 *
 * (3) Aufwand mit Steuer, gekauft Netto
 *  Bank -> 7xxx (E220)
 *
 * (4) Aufwand KFZ (sonderfall)
 *   Bank -> 7320, 9600
 *
 * (5) Rechnung gestellt
 *   4xxx, 3xx -> Bank
 */
public class BuchungHelper {

    public static final String SEP_NUM = " | ";
    public static final String SEP_TEXT = "&emsp;->&emsp;";

    @TemplateExtension
    static String buchungszeile(Buchung buchung) {

        Konto.EAKonto eakonto = KontenPlan.MAP.get(buchung.getKontoNummer());
        if(buchung.isEinnahme()){

            BigDecimal netto = Calc.bruttoToNetto(buchung.getBetrag(), buchung.getSteuerSatz());
            BigDecimal tax = buchung.getBetrag().subtract(netto);

            Konto steuerKonto = switch (buchung.getSteuerSatz()) {
                case ZEHN -> KontenPlan.VORSTEUER_10;
                case ZWANZIG -> KontenPlan.VORSTEUER_20;
                default -> throw new IllegalArgumentException("Unknown Steuersatz: " + buchung.getSteuerSatz());
            };

            return eakonto.getShortName() + SEP_NUM + steuerKonto.getShortName() + SEP_TEXT + KontenPlan.BANK.getShortName() +  "<br /> " +
                    Calc.formatToCurrency(netto) + SEP_NUM + Calc.formatToCurrency(tax) + SEP_TEXT + Calc.formatToCurrency(buchung.getBetrag());

        }else{
            if(eakonto instanceof Konto.Split split){
                return KontenPlan.BANK.getShortName() + SEP_TEXT +
                        split.getSplitMap().keySet().stream().map(Konto::getShortName).collect(Collectors.joining(SEP_NUM)) + "<br /> " +
                        Calc.formatToCurrency(buchung.getBetrag()) + SEP_TEXT +
                        split.getSplitMap().
                                values()
                                .stream()
                                .map(percent ->
                                        buchung.getBetrag().multiply(new BigDecimal(percent), Calc.DEFAULT_CONTEXT)
                                )
                                .map(Calc::formatToCurrency)
                                .collect(Collectors.joining(SEP_NUM));

            } else if(Steuersatz.OHNE.equals(buchung.getSteuerSatz())){
                return KontenPlan.BANK.getShortName() + SEP_TEXT + eakonto.getShortName() + "<br /> " +
                        Calc.formatToCurrency(buchung.getBetrag()) + SEP_TEXT + Calc.formatToCurrency(buchung.getBetrag());

            } else if(buchung.isBrutto()){
                BigDecimal netto = Calc.bruttoToNetto(buchung.getBetrag(), buchung.getSteuerSatz());
                BigDecimal tax = buchung.getBetrag().subtract(netto);

                Konto steuerKonto = switch (buchung.getSteuerSatz()) {
                    case ZEHN -> KontenPlan.UMSATZSTEUER_10;
                    case ZWANZIG -> KontenPlan.UMSATZSTEUER_20;
                    default -> throw new IllegalArgumentException("Unknown Steuersatz: " + buchung.getSteuerSatz());
                };

                return KontenPlan.BANK.getShortName() + SEP_TEXT + eakonto.getShortName() + SEP_NUM + steuerKonto.getShortName() + "<br /> " +
                        Calc.formatToCurrency(buchung.getBetrag()) + SEP_TEXT + Calc.formatToCurrency(netto) + SEP_NUM + Calc.formatToCurrency(tax);
            } else {
                Konto steuerKonto = switch (buchung.getSteuerSatz()) {
                    case ZEHN -> KontenPlan.INNERGEMEINSCHAFTLICHER_ERWERB_10;
                    case ZWANZIG -> KontenPlan.INNERGEMEINSCHAFTLICHER_ERWERB_20;
                    default -> throw new IllegalArgumentException("Unknown Steuersatz: " + buchung.getSteuerSatz());
                };

                BigDecimal brutto = Calc.nettoToBrutto(buchung.getBetrag(), buchung.getSteuerSatz());
                BigDecimal tax = brutto.subtract(buchung.getBetrag());

                return KontenPlan.BANK.getShortName() + SEP_TEXT + eakonto.getShortName() + SEP_NUM + steuerKonto.getShortName() + "<br /> " +
                        Calc.formatToCurrency(buchung.getBetrag()) + SEP_TEXT + Calc.formatToCurrency(brutto) + SEP_NUM + Calc.formatToCurrency(tax);
            }
        }

    }


}
