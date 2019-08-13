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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Thread safe circular buffer of points. The most recent elements are at the
 * front of the list, and they gradually work their way backwards until they
 * fall off of the end. Uses a linked list rather than an array list to avoid
 * problems with running out of contiguous heap.
 *
 * @author Jason Leake
 */
public class SynchronizedCircularBuffer {

    /**
     * This is the list.  The points represent timestamp = x, datum = y.
     * The newest element is at the front of the list, and the oldest at
     * the back of the list.
     */
    private final LinkedList<Point2D.Double> list;

    /**
     * Constructor
     *
     * @param sizeLimit size of the buffer
     */
    public SynchronizedCircularBuffer(int sizeLimit) {
        list = new LinkedList<>();
        synchronized (this) {
            for (int index = 0; index < sizeLimit; index++) {
                list.add(null);
            }
        }
    }

    /**
     * Get minimum value in vector. Copies into an array to avoid
     * synchronisation locking slowing the whole thing down
     *
     * @return minimum value
     */
    public double getMin() {
        ArrayList<Double> array = toDataArrayList();
        double min = 0;
        for (Double val : array) {
            if (val < min) {
                min = val;
            }
        }
        return min;
    }

    /**
     * Get maximum value in vector. Copies into an array to avoid
     * synchronisation locking slowing the whole thing down
     *
     * @return maximum value
     */
    public double getMax() {
        ArrayList<Double> array = toDataArrayList();
        double max = 0;
        for (Double val : array) {
            if (val > max) {
                max = val;
            }
        }
        return max;
    }

    /**
     * Update the list with a new point.
     * @param timestamp
     * @param datum 
     */
    void addToRing(double timestamp, double datum) {
        synchronized (this) {
            list.removeLast();
            list.addFirst(new Point2D.Double(timestamp, datum));
        }
    }

    /**
     * Number of elements in array
     *
     * @return Size of array
     */
    public int size() {
        synchronized (this) {
            return list.size();
        }
    }

    /**
     * Return an array list containing just the data. Null entries are excluded
     * from the output list.  The newest point is at the start of the return
     * array, and the oldest at the end.
     *
     * @return list of data
     */
    public ArrayList<Double> toDataArrayList() {
        ArrayList<Double> arrayList;
        synchronized (this) {
            int size = 0;
            for (int index = 0; index < list.size(); index++) {
                if (list.get(index) != null) {
                    size++;
                }
            }
            arrayList = new ArrayList<>(size);
            for (Point2D.Double element : list) {
                arrayList.add(element.y);
            }
        }
        return arrayList;
    }

    /**
     * Return a list containing the timestamp/data pairs. Null entries are
     * excluded from the output list.  The newest datum is at the start of the
     * returned array and the oldest at the end.
     *
     * @return list
     */
    public PointsList toPointsList() {
        PointsList pointsList;
        synchronized (this) {
            pointsList = new PointsList();
            for (Point2D.Double element : list) {
                if (element != null) {
                    pointsList.add(element);
                }
            }
        }
        return pointsList;
    }

    /**
     * Change the size of the buffer
     *
     * @param newSize
     */
    void resize(int newSize) {
        synchronized (this) {
            int originalSize = list.size();
            if (newSize < originalSize) {
                // Shrinking list.  Remove oldest elements, at the end of the
                // list.
                for (int count = 0; count < (originalSize - newSize); count++) {
                    list.removeFirst();
                }
            } else {
                // Adding list.  Add "old" elements to the end of the list.
                // As we don't know what they should be, add nulls since they
                // won't be plotted.
                for (int count = 0; count < (newSize - originalSize); count++) {
                    list.addLast(null);
                }
            }

        }
    }
}
