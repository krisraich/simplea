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

package at.kara.common;

import at.kara.buchungen.Buchung;
import at.kara.konten.KontenPlan;
import at.kara.konten.Konto;
import jakarta.ws.rs.BadRequestException;
import org.jboss.resteasy.reactive.server.multipart.FormValue;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Util {

    public static <T> T processValue(String propertyName, Map<String, Collection<FormValue>> values, Function<String, T> supplier) {
        ArrayDeque<FormValue> formValues = (ArrayDeque) values.get(propertyName);
        String val;
        if (formValues == null || (val = formValues.getFirst().getValue()) == null || val.isBlank()) {
            throw new BadRequestException(propertyName + "  fehlt oder ist fehlerhaft!");
        }

        return supplier.apply(val);
    }

    public static Optional<Konto.Steuer> determineSteuerkontoFromBuchung(Buchung buchung) {
        Konto.Steuer konto = null;
        if (buchung.isEinnahme()) {
            konto = switch (buchung.getSteuerSatz()) {
                case ZEHN -> KontenPlan.UMSATZSTEUER_10;
                case ZWANZIG -> KontenPlan.UMSATZSTEUER_20;
                case OHNE -> null;
            };
        } else {
            //Ausgabe + brutto = USt.
            if (buchung.isBrutto()) {
                konto = switch (buchung.getSteuerSatz()) {
                    case ZEHN -> KontenPlan.VORSTEUER_10;
                    case ZWANZIG -> KontenPlan.VORSTEUER_20;
                    case OHNE -> null;
                };
            } else {
                //Ausgabe + netto = Innergemeinschaftlicher Erwerb
                konto = switch (buchung.getSteuerSatz()) {
                    case ZEHN -> KontenPlan.INNERGEMEINSCHAFTLICHER_ERWERB_10;
                    case ZWANZIG -> KontenPlan.INNERGEMEINSCHAFTLICHER_ERWERB_20;
                    default -> null;
                };
            }
        }
        return Optional.ofNullable(konto);
    }
}
