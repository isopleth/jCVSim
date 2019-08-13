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
package jcvsim.parameters;

/**
 * This parameterises the impulse response to a single reflex.
 *
 * @author Jason Leake
 */
public class ResponseParameters {

    public double delay; // How many seconds before it starts
    public double peak;  // How many seconds before it reaches maximum
    public double end;   // How many seconds before it decays to nothing
}
