package edu.mit.lcp;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

// Trace.java
// Contains trace data in the form of x,y points
// Also contains the color of the trace, the name of the x and y data,
// and the units of the x and y data
public class Trace {

    public static final String PROP_COLOR = "COLOR";
    public static final String PROP_STROKE = "STROKE";
    public static final String PROP_ENABLED = "ENABLED";
    public static final String PROP_XRANGE = "XRANGE";
    public static final String PROP_YRANGE = "YRANGE";

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private Color color;
    private AffineTransform transform;
    private final SimulationOutputVariableBuffer xVar;
    private final SimulationOutputVariableBuffer yVar;
    private Range xRange;
    private Range yRange;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public Trace(SimulationOutputVariableBuffer xvar, SimulationOutputVariableBuffer yvar,
            Color colour) {
        xVar = xvar;
        yVar = yvar;
        color = colour;
        transform = new AffineTransform();
        xRange = xVar.getTypicalRange();
        yRange = yVar.getTypicalRange();
    }

    public void setColor(Color newColor) {
        Color oldColor = color;
        color = newColor;
        changes.firePropertyChange(PROP_COLOR, oldColor, newColor);
    }

    public Color getColor() {
        return color;
    }

    /**
     * Thread safe method for getting a copy of the affine transform object
     *
     * @return affine transform object
     */
    public AffineTransform getTransformCopy() {
        synchronized (this) {
            return (AffineTransform) transform.clone();
        }
    }

    /**
     * Thread safe method for setting affine transform object
     *
     * @param newTransform transform
     */
    void setTransform(AffineTransform newTransform) {
        synchronized (this) {
            transform = (AffineTransform) newTransform.clone();
        }
    }

    public void setEnabled(boolean b) {
        boolean oldEnabled = enabled.getAndSet(b);
        changes.firePropertyChange(PROP_ENABLED, oldEnabled, b);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public String getXDescription() {
        return xVar.getDescription();
    }

    public String getYDescription() {
        return yVar.getDescription();
    }

    public Range getXRange() {
        synchronized (this) {
            return xRange;
        }
    }

    public Range getYRange() {
        synchronized (this) {
            return yRange;
        }
    }

    public void setXRange(Range newRange) {
        synchronized (this) {
            Range oldRange = xRange;
            xRange = newRange;
            changes.firePropertyChange(PROP_XRANGE, oldRange, newRange);
        }
    }

    public void setYRange(Range newRange) {
        synchronized (this) {
            Range oldRange = yRange;
            yRange = newRange;
            changes.firePropertyChange(PROP_YRANGE, oldRange, newRange);
        }
    }

    public SimulationOutputVariableBuffer getXVar() {
        return xVar;
    }

    public SimulationOutputVariableBuffer getYVar() {
        return yVar;
    }

    /**
     * Resize the ring buffer to the specified size
     *
     * @param newSize
     */
    public void resizeBuffers(int newSize) {
        xVar.resizeRingBuffer(newSize);
        yVar.resizeRingBuffer(newSize);
    }

    // Override Object.toString() to report useful information
    @Override
    public String toString() {
        return xVar + " / " + yVar;
    }

    ///////////////////////////////
    // Support for property changes
    /**
     * The specified PropertyChangeListeners propertyChange method will be
     * called each time the value of any bound property is changed. The
     * PropertyListener object is added to a list of PropertyChangeListeners
     * managed by this button, it can be removed with
     * removePropertyChangeListener. Note: the JavaBeans specification does not
     * require PropertyChangeListeners to run in any particular order.
     *
     * @see #removePropertyChangeListener
     * @param l the PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changes.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove this PropertyChangeListener from the buttons internal list. If the
     * PropertyChangeListener isn't on the list, silently do nothing.
     *
     * @see #addPropertyChangeListener
     * @param l the PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

    /**
     * Generate a snapshot of the points in the output data lists. Works by lazy
     * evaluation.
     *
     * @return snapshot
     */
    public PointsList getList() {
        PointsList theList;
        synchronized (this) {
            if (xVar.getVar() == CVSim.sim.getOutputVariable("TIME")) {
                // The y var list has time versus data in it, so just use that
                theList = yVar.getPointsList();
            } else {
                // Create a new list containing the two data sets. The data is in
                // the y points of the var lists.
                theList = new PointsList();

                // Handle missing points in either list
                ListIterator<Point2D.Double> xIterator = xVar.getPointsList().listIterator();
                ListIterator<Point2D.Double> yIterator = yVar.getPointsList().listIterator();
                while (xIterator.hasNext() && yIterator.hasNext()) {

                    Point2D.Double xPoint = xIterator.next();
                    Point2D.Double yPoint = yIterator.next();

                    double x = xPoint.y;
                    double xTime = xPoint.x;
                    double y = yPoint.y;
                    double yTime = yPoint.x;

                    if (xTime == yTime) {
                        if (xTime != 0) {
                            theList.add(new Point2D.Double(x, y));
                        }
                    } else if (xTime > yTime) {
                        // Y point missing, so move to the next one
                        xIterator.previous();
                    } else if (yTime > xTime) {
                        // X point missing, so move to the next one
                        yIterator.previous();
                    }

                }
            }
        }
        return theList;
    }

    /**
     * Get points list as a shape implementation
     *
     * @return points list
     */
    Shape getShapeList() {
        return new ShapeList(getList(), transform);
    }

}
