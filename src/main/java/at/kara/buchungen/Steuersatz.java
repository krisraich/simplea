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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum Steuersatz {

    ZWANZIG(BigDecimal.valueOf(0.20), "20%"),
    ZEHN(BigDecimal.valueOf(0.1), "10%"),
    OHNE(BigDecimal.ZERO, "keine");

    private final BigDecimal prozent;
    private final String name;

    public static Steuersatz findByName(String name) {
        for(Steuersatz satz : Steuersatz.values()) {
            if(satz.getName().equalsIgnoreCase(name)) {
                return satz;
            }
        }
        throw new IllegalArgumentException("Unknown Steuersatz: " + name);
    }

}
