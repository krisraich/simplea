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

import at.kara.konten.Konto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class Calc {

    public static final MathContext DEFAULT_CONTEXT = new MathContext(6, RoundingMode.HALF_UP);

    public static final NumberFormat FRENCH_NOTATION;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator(' ');

        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(true);
        FRENCH_NOTATION = format;
    }

    public static BigDecimal nettoToBrutto(BigDecimal netto, Konto.Steuer konto) {
        return netto.add(netto.multiply(konto.getAmount()));
    }

    public static BigDecimal bruttoToNetto(BigDecimal brutto, Konto.Steuer konto) {
        return brutto.divide(BigDecimal.ONE.add(konto.getAmount()), DEFAULT_CONTEXT);
    }
    public static BigDecimal taxFromBrutto(BigDecimal brutto, Konto.Steuer konto) {
        return brutto.subtract(bruttoToNetto(brutto, konto));
    }

    public static String formatToCurrency(BigDecimal value) {
        return FRENCH_NOTATION.format(value.setScale(2, RoundingMode.HALF_UP));
    }

}
