package edu.mit.lcp;

import java.util.List;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Comparator;

public abstract class CSimulation {

    public static final String COMPRESSION = "COMPRESSION";
    public static final String ABREFLEX = "ABREFLEX";
    public static final String CPREFLEX = "CPREFLEX";
    public static final String BREATHING_ENABLED = "BREATHING_ENABLED";
    public static final String LUNG_VOLUME_REFLEX_ENABLED = "LUNG_VOLUME_REFLEX_ENABLED";

    // Count the number of simulation steps
    protected long steps;

    // dataCompressionFactor is the number of steps/values the
    // simulation uses to generate the single output value, value of 1
    // means each step() proceeds 1ms through the simulation?
    private int _dataCompressionFactor = 1;

    // these parameters control which features of the simulations are enabled
    private boolean arterialBaroreflexEnabled;
    private boolean cardioPulmonaryReflexEnabled;
    private boolean breathingEnabled;
    private boolean lungVolumeReflexEnabled;

    protected List<SimulationOutputVariable> varList = new ArrayList<>();

    /**
     * Reset the simulation to its initial state
     */
    public abstract void reset();

    /**
     * Step the simulation forward in time by the specified time increment
     * @param timeIncrementMs 
     */
    public abstract void step(double timeIncrementMs);

    public abstract double getOutput(int index);

    /**
     * Update pressure in specified compartment
     * @param index
     * @param pressure 
     */
    public abstract void updatePressure(int index, double pressure);

    /**
     * Get runtime parameter list
     * @return parameter list
     */
    public abstract List<Parameter> getParameterList();

    /**
     * Get named parameter
     * @param name parameter name
     * @return parameter value
     */
    public abstract Parameter getParameterByName(String name);

    // methods to change the simulation settings
    public void setDataCompressionFactor(int compressionFactor) {
        int old = getDataCompressionFactor();
        _dataCompressionFactor = compressionFactor;
        firePropertyChange(COMPRESSION, old, compressionFactor);
        System.out.println("DataCompressionFactor = " + compressionFactor);
    }

    public int getDataCompressionFactor() {
        return _dataCompressionFactor;
    }

    public void setABReflex(boolean enable) {
        boolean oldState = getABReflex();
        arterialBaroreflexEnabled = enable;
        firePropertyChange(ABREFLEX, oldState, enable);
    }

    public boolean getABReflex() {
        return arterialBaroreflexEnabled;
    }

    public void setCPReflex(boolean enable) {
        boolean oldState = getCPReflex();
        cardioPulmonaryReflexEnabled = enable;
        firePropertyChange(CPREFLEX, oldState, enable);
    }

    public boolean getCPReflex() {
        return cardioPulmonaryReflexEnabled;
    }

    public void setBreathing(boolean enable) {
        boolean oldState = getBreathing();
        breathingEnabled = enable;
        firePropertyChange(BREATHING_ENABLED, oldState, enable);
    }

    public boolean getBreathing() {
        return breathingEnabled;
    }

    void setLungVolumeReflex(boolean enable) {
        boolean oldState = getLungVolumeReflex();
        lungVolumeReflexEnabled = enable;
        firePropertyChange(LUNG_VOLUME_REFLEX_ENABLED, oldState, enable);
    }

    public boolean getLungVolumeReflex() {
        return lungVolumeReflexEnabled;
    }

    public double calculateTotalZeroPressureFillingVolume() {
        double tzpfv = 0;
        for (Parameter p : getParameterList()) {
            if (p.getName().contains("Zero-Pressure Filling Volume")) {
                tzpfv += p.getValue();
            }
        }
        String str = String.format("tzpfv: %.4f", tzpfv);
        System.out.println(str);
        return tzpfv;
    }

    ///////////////////////////////
    // Support for property changes
    private final PropertyChangeSupport _propChangeListeners = new PropertyChangeSupport(this);

    // Add Support for Property Changes
    public PropertyChangeListener SimulationParameterChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            firePropertyChange(new PropertyChangeEvent(e.getSource(),
                    e.getPropertyName() + ((Parameter) e.getSource()).getName(),
                    e.getOldValue(),
                    e.getNewValue()));
        }
    };

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
        _propChangeListeners.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        _propChangeListeners.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove this PropertyChangeListener from the buttons internal list. If the
     * PropertyChangeListener isn't on the list, silently do nothing.
     *
     * @see #addPropertyChangeListener
     * @param l the PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        _propChangeListeners.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String property, Object old, Object current) {
        _propChangeListeners.firePropertyChange(property, old, current);
    }

    protected void firePropertyChange(PropertyChangeEvent e) {
        _propChangeListeners.firePropertyChange(e);
    }

    // Listener Interface for ChangeEvents (data changes)
    //
    private final EventListenerList _changeListeners = new EventListenerList();

    public void addChangeListener(ChangeListener listener) {
        _changeListeners.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        _changeListeners.remove(ChangeListener.class, listener);
    }

    public void dataChanged() {
        fireChangeEvent();
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = _changeListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int index = listeners.length - 2; index >= 0; index -= 2) {
            if (listeners[index] == ChangeListener.class) {
                ((ChangeListener) listeners[index + 1]).stateChanged(event);
            }
        }
    }

    private final List<SimulationOutputVariableBuffer> varRecorders = new CopyOnWriteArrayList<>();

    public void addVariableRecorder(SimulationOutputVariableBuffer recorder) {
        varRecorders.add(recorder);
    }

    public void removeVariableRecorder(SimulationOutputVariableBuffer recorder) {
        varRecorders.remove(recorder);
    }

    protected List<SimulationOutputVariableBuffer> getVariableRecorders() {
        return varRecorders;
    }

    // Write new data out to the recorders
    public void updateRecorders() {
        for (SimulationOutputVariableBuffer recorder : getVariableRecorders()) {
            recorder.addDatum(getOutput(0),
                    getOutput(recorder.getOutputIndex()));
        }
    }

    protected List<SimulationOutputVariable> getOutputVariables() {
        return varList;
    }

    protected SimulationOutputVariable getOutputVariable(String name) {
        for (SimulationOutputVariable v : varList) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    public class ComparatorX implements Comparator {

        @Override
        public int compare(Object p1, Object p2) {
            return ((((Parameter) p1).getName()).compareTo(((Parameter) p2).getName()));
        }
    }

}
