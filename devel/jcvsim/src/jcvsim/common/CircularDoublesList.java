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

import java.util.LinkedList;

/**
 * A circular list
 *
 * @author Jason Leake
 */
public class CircularDoublesList {

    /**
     * This is the list. The front of the list contains the newest element and
     * the back of the list contains the oldest element.
     */
    private final LinkedList<Double> list = new LinkedList<>();
    public final int maxLength;
    SingleElementCache<Double> cache = new SingleElementCache<>();

    /**
     * Constructor
     *
     * @param length maximum number of elements in the list
     */
    public CircularDoublesList(int length) {
        maxLength = length;
        zero();
    }

    /**
     * Fill list with zeros
     *
     */
    public final void zero() {
        while (list.size() < maxLength) {
            list.addFirst(0.0);
        }
        cache.invalidate();
    }

    /**
     * Update the list with a new element. Adds the element to the back of the
     * list and deletes the element at the front if the length limit has been
     * exceeded.
     *
     * @param value value to add
     */
    public void push(Double value) {
        list.addFirst(value);
        if (list.size() > maxLength) {
            list.removeLast();
        }
        cache.invalidate();
    }

    /**
     * Get a specified element
     *
     * @param index index number. 0 fetches the oldest element
     * @return element
     */
    public double get(int index) {
        Double value = cache.get(index);
        if (value == null) {
            return cache.set(index, list.get(index));
        } else {
            return value;
        }
    }

    public double getMean() {
        double sum = 0;
        for (double value : list) {
            sum += value;
        }
        return sum / maxLength;
    }

}
