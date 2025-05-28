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

package at.kara.reports;

import at.kara.common.Calc;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SteuerDTO {


    private List<Map<String, String>> zeilen;
    private BigDecimal summeSteuer;
    private BigDecimal bemessungsgrundlage;

    public String getSummeSteuerFormat() {
        return "€ " + Calc.formatToCurrency(summeSteuer);
    }

    public String getBemessungsgrundlageFormat() {
        return "€ " + Calc.formatToCurrency(bemessungsgrundlage);
    }

}
