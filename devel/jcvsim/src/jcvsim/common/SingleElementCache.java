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
 * Single element cache. i.e. memory for one element from a list. Stores the
 * element and its index from the list, for faster repeated lookup of the
 * last element accessed from the list.
 * 
 * This is the simplest possible cache - cache one thing. The CircularDoublesList
 * class often interrogated to return a particular value and then again for the
 * same value shortly afterwards.  Perhaps the optimising compiler caches this
 * itself but it is easy to just save the value of the last index interrogated
 * using this class in case it doesn't.
 *
 * @author Jason Leake
 * @param <T>
 */
public class SingleElementCache<T> {

    private int index;
    private T value;

    public SingleElementCache() {
        index = 0;
        value = null;
    }

    /**
     * Set the cache value and the index that refers to it
     * @param listIndex
     * @param newValue
     * @return value stored to cache
     */
    public T set(int listIndex, T newValue) {
        index = listIndex;
        value = newValue;
        return newValue;
    }

    /**
     * Retrieve the caches value if the index matches its index
     * @param listIndex index of required value
     * @return the cached value if it is in the cache, or null if it is not
     */
    public T get(int listIndex) {
        if (value != null && index == listIndex) {
            return value;
        } else {
            return null;
        }
    }

    public void invalidate() {
        value = null;
    }
}
