/*
 * Copyright (C) 2017 Jason Leake
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
package jcvsim.common;

/**
 * Converts between pressure in cm H2O and mm Hg. The model uses mm Hg
 * internally but can display in cm H2O
 *
 * @author Jason Leake
 */
public class CmWaterToMmHgConversion implements UnitsConversion {

    /**
     * Convert mmHg to cm H2O
     *
     * @param value in mmHg
     * @return
     */
    @Override
    public double toExternalRepresentation(double value) {
        return 1.359510026 * value;
    }

    /**
     * Convert cm H2O to mm Hg
     *
     * @param value
     * @return
     */
    @Override
    public double toInternalRepresentation(double value) {
        return 0.735559121015 * value;
    }

    @Override
    public String getInternalUnits() {
        return "mmHg";
    }

    @Override
    public String getExternalUnits() {
        return "<html>cmH<sub>2</sub>O</html>";
    }

}
