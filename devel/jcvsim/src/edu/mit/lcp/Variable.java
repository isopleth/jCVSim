/*
 * Copyright (C) 2017 user
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
package edu.mit.lcp;

import jcvsim.common.UnitsConversion;

/**
 * A parameter or output. This will allow the parameter to be specified in units
 * which are different from the units which the program uses internally
 *
 * @author Jason Leake
 */
abstract public class Variable {

    private final String name;
    private final String category;
    private final String type;
    private final String units; // These are the external units if the internal units differs
    private final UnitsConversion unitsConversion;

    public Variable(String name, String category, String type, String units) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.units = units;
        this.unitsConversion = null;
    }

    public Variable(String name, String category, String type,
            UnitsConversion unitsConversion) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.units = unitsConversion.getExternalUnits();
        this.unitsConversion = unitsConversion;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getUnits() {
        return units;
    }

    public String getExternalUnits() {
        if (unitsConversion == null) {
            return units;
        } else {
            return unitsConversion.getExternalUnits();
        }
    }

        public String getInternalUnits() {
        if (unitsConversion == null) {
            return units;
        } else {
            return unitsConversion.getInternalUnits();
        }
    }
    
    public double toExternalRepresentation(double value) {
        if (unitsConversion == null) {
            return value;
        } else {
            return unitsConversion.toExternalRepresentation(value);
        }
    }

    public double toInternalRepresentation(double value) {
        if (unitsConversion == null) {
            return value;
        } else {
            return unitsConversion.toInternalRepresentation(value);
        }
    }

}
