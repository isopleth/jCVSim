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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * This provides a points list as a shape
 *
 * @author Jason Leake
 */
public class ShapeList implements Shape {

    private final AffineTransform transform;
    private final PointsList list;

    public ShapeList(PointsList list, AffineTransform transform) {
        this.list = list;
        this.transform = transform;
    }

    @Override
    public boolean contains(double x, double y) {
        return (list.getMinX() <= x)
                && (list.getMaxX() >= x)
                && (list.getMinY() <= y)
                && (list.getMaxY() >= y);
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return (list.getMinX() <= x)
                && (list.getMinX() <= x + w)
                && (list.getMaxX() >= x)
                && (list.getMaxX() >= x + w)
                && (list.getMinY() <= y)
                && (list.getMinY() <= y + h)
                && (list.getMaxY() >= y)
                && (list.getMaxY() >= y + h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        int minRelLoc = r.outcode(list.getMinX(),
                list.getMinY());
        int maxRelLoc = r.outcode(list.getMaxX(),
                list.getMaxY());
        return (((minRelLoc & (Rectangle2D.OUT_BOTTOM | Rectangle2D.OUT_LEFT)) != 0)
                && ((maxRelLoc & (Rectangle2D.OUT_TOP | Rectangle2D.OUT_RIGHT)) != 0));
    }

    @Override
    public Rectangle getBounds() {
        // x, y, width, height
        return new Rectangle((int) Math.round(list.getMinX()),
                (int) Math.round(list.getMinY()),
                (int) Math.round(list.getMaxX() - list.getMinX()),
                (int) Math.round(list.getMaxY() - list.getMinY()));
    }

    @Override
    public Rectangle2D getBounds2D() {
        // x, y, width, height
        return new Rectangle2D.Double(list.getMinX(),
                list.getMinY(),
                list.getMaxX() - list.getMinX(),
                list.getMaxY() - list.getMinY());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new TracePathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return (contains(x, y) || contains(x, y + h)
                || contains(x + w, y) || contains(x + w, y + h));
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        // x,y,w,h
        return r.intersects(list.getMinX(), list.getMinY(),
                list.getMaxX() - list.getMinX(),
                list.getMaxY() - list.getMinY());

    }

    private class TracePathIterator implements PathIterator {

        private AffineTransform affineTransform;
        private final Iterator<Point2D.Double> pointsIterator;
        private Point2D.Double currentPoint;
        private int currentSegmentType;

        TracePathIterator(AffineTransform at) {
            // if no default transform, use the supplied one, else
            // combine them together
            affineTransform = transform;
            if (transform == null) {
                affineTransform = at;
            } else if (at != null) {
                affineTransform = new AffineTransform(transform);
                affineTransform.concatenate(at);
            }
            pointsIterator = list.listIterator();
            if (pointsIterator.hasNext()) {
                currentPoint = pointsIterator.next();
            } else {
                currentPoint = new Point2D.Double(0., 0.);
            }
            currentSegmentType = PathIterator.SEG_MOVETO;
        }

        @Override
        public int currentSegment(double[] coords) {
            coords[0] = currentPoint.getX();
            coords[1] = currentPoint.getY();
            // srcCoords, srcOffset, dstCoords, dstOffset, numPts
            if (affineTransform != null) {
                affineTransform.transform(coords, 0, coords, 0, 1);
            }
            return currentSegmentType;
        }

        @Override
        public int currentSegment(float[] coords) {
            coords[0] = (float) currentPoint.getX();
            coords[1] = (float) currentPoint.getY();
            // srcCoords, srcOffset, dstCoords, dstOffset, numPts
            if (affineTransform != null) {
                affineTransform.transform(coords, 0, coords, 0, 1);
            }
            return currentSegmentType;
        }

        @Override
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }

        @Override
        public boolean isDone() {
            return !(pointsIterator.hasNext());
        }

        @Override
        public void next() {
            currentPoint = pointsIterator.next();
            currentSegmentType = PathIterator.SEG_LINETO;
        }
    }
}
