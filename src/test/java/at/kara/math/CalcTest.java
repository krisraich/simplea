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

package at.kara.math;

import at.kara.common.Calc;
import at.kara.konten.KontenPlan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class CalcTest {

    @Test
    void testTax() {
        Assertions.assertEquals(
                0,
                Calc.bruttoToNetto(new BigDecimal("120"), KontenPlan.VORSTEUER_20).compareTo(new BigDecimal("100"))
        );

        Assertions.assertEquals(
                0,
                Calc.nettoToBrutto(new BigDecimal("100"), KontenPlan.VORSTEUER_20).compareTo(new BigDecimal("120"))
        );

    }

    @Test
    void testMoneyCalc() {
        Assertions.assertEquals(
                "3 333,33",
                Calc.formatToCurrency(new BigDecimal(10_000).divide(new BigDecimal(3), Calc.DEFAULT_CONTEXT))
        );

        Assertions.assertEquals(
                "1,00",
                Calc.formatToCurrency(new BigDecimal("1"))
        );
    }

}