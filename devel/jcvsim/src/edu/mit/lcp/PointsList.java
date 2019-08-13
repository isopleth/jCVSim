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
package edu.mit.lcp;

import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * List of points.
 *
 * @author Jason Leake
 */
public class PointsList extends LinkedList<Point2D.Double> {

    private boolean rangesValid;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;

    @Override
    public boolean add(Point2D.Double val) {
        rangesValid = false;
        return super.add(val);
    }

    @Override
    public void clear() {
        rangesValid = false;
        super.clear();
    }

    /**
     * Return the minimum x value in the list
     *
     * @return minimum x
     */
    double getMinX() {
        calculateRanges();
        return xmin;
    }

    /**
     * Return the maximum x value in the list
     *
     * @return maximum x
     */
    double getMaxX() {
        calculateRanges();
        return xmax;
    }

    /**
     * Return the minimum y value in the list
     *
     * @return minimum y
     */
    double getMinY() {
        calculateRanges();
        return ymin;
    }

    /**
     * Return the maximum y value in the list
     *
     * @return maximum y
     */
    double getMaxY() {
        calculateRanges();
        return ymax;
    }

    /**
     * Get the X min and max as a range
     *
     * @return x min and max
     */
    public Range getXDataRange() {
        return new Range(getMinX(), getMaxX());
    }

    /**
     * Obtain minimum and maximum x and y values. Done by lazy evaluation - just
     * use the existing values unless they are invalid, in which case the
     * invalid flag is set. They are invalidated by the addition of a new value
     * to the list.
     */
    private void calculateRanges() {
        if (!rangesValid) {
            if (isEmpty()) {
                throw new RuntimeException("Undefined");
            }
            xmax = Double.MIN_VALUE;
            xmin = Double.MAX_VALUE;
            ymax = Double.MIN_VALUE;
            ymin = Double.MAX_VALUE;
            for (Point2D.Double value : this) {
                double x = value.getX();
                if (x > xmax) {
                    xmax = x;
                } else if (x < xmin) {
                    xmin = x;
                }
                double y = value.getY();
                if (y > ymax) {
                    ymax = y;
                } else if (y < ymin) {
                    ymin = y;
                }
            }
            rangesValid = true;
        }
    }

}
