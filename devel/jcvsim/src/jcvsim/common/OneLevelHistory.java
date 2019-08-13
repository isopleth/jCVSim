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
package jcvsim.common;

/**
 * One level history. Like a circular list but with only two elements.
 *
 * @author Jason Leake
 */
public class OneLevelHistory {

    private double previous;
    private double current;

    public OneLevelHistory() {
        previous = 0.;
        current = 0.;
    }

    /**
     * Pushes new value onto history, making this the current value and the
     * previous current value into the previous value.
     *
     * @param newValue value to set to the current value
     */
    public void push(double newValue) {
        if (new Double(newValue).isNaN()) {
            throw new RuntimeException("value being pushed is not a number");
        }
        previous = current;
        current = newValue;
    }

    public double getPrevious() {
        return previous;
    }

    public double getCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "OneLevelHistory: " + Double.toString(current) + " and " + Double.toString(previous);
    }

    /**
     * Linear interpolation between the two values
     *
     * @param proportion How far between the two, 0->1
     * @return linear interpolation of value
     */
    public double linearInterpolate(double proportion) {
        if (proportion < 0.) {
            throw new RuntimeException("proportion " + proportion + " negative");
        } else if (proportion > 1.) {
            throw new RuntimeException("proportion " + proportion + " greater than 1");
        }
        return (current - previous) * proportion + previous;
    }
}
