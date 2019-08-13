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
 * Interface for a units conversion object
 *
 * @author Jason Leake
 */
public interface UnitsConversion {

    abstract public double toExternalRepresentation(double value);

    abstract public double toInternalRepresentation(double value);

    abstract public String getInternalUnits();

    abstract public String getExternalUnits();
}
