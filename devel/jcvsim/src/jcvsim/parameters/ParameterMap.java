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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Superclass for map used to store runtime parameters
 *
 * @author user
 * @param <T>
 */
public class ParameterMap<T> {

    private final Set<T> defaultedValues = new HashSet<>();
    private final Map<T, Double> map = new HashMap<>();

    /**
     * Just a get() for the map, except that defaults unset parameters to 0,
     * and warns about this the first time that parameter is read.
     *
     * @param key parameter name
     * @return parameter value
     */
    public Double get(T key) {
        Double value = map.get(key);
        if (value == null) {
            value = 0.;
            if (!defaultedValues.contains(key)) {
                System.out.println(key
                        + " has not been set, defaulting to " + value);
                defaultedValues.add(key);
            }
        }
        return value;
    }

    /**
     * Setter
     *
     * @param key
     * @param value
     */
    public void put(T key, Double value) {
        map.put(key, value);
    }

}
