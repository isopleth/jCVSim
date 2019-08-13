package edu.mit.lcp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import jcvsim.common.UnitsConversion;

public abstract class Parameter extends Variable {

    private final PropertyChangeSupport _propChangeListeners = new PropertyChangeSupport(this);

    private Double defaultValue;
    private Double patientModeDefaultValue;
    private double percent;
    // The minimum and maximum are in internal units, i.e. the ones used
    // inside the simulation engine
    private double maximumAcceptedValue = Double.POSITIVE_INFINITY;  // max normal physiological range
    private double minimumAcceptedValue = Double.NEGATIVE_INFINITY;  // min normal physiological range 

    /**
     * Constructor
     *
     * @param category
     * @param type
     * @param name
     * @param units
     */
    public Parameter(String category, String type, String name, String units) {
        super(name, category, type, units);
        defaultValue = 0.0;
        patientModeDefaultValue = 0.0;
        percent = 100;
    }

    /**
     * Constructor
     *
     * @param category
     * @param type
     * @param name
     * @param units
     * @param min
     * @param max
     */
    public Parameter(String category, String type, String name, String units,
            double min, double max) {
        super(name, category, type, units);
        defaultValue = 0.0;
        patientModeDefaultValue = 0.0;
        percent = 100;
        minimumAcceptedValue = min;
        maximumAcceptedValue = max;
    }

    /**
     * This is the case where the units are expressed in a different format to
     * that used in the program.
     *
     * @param category
     * @param type
     * @param name
     * @param unitsConversion
     * @param minInExternalUnits minimum accepted value, in external units
     * @param maxInInternalUnits maximum accepted value, in external units
     */
    public Parameter(String category, String type, String name,
            UnitsConversion unitsConversion,
            double minInExternalUnits, double maxInInternalUnits) {
        super(name, category, type, unitsConversion);
        defaultValue = 0.0;
        patientModeDefaultValue = 0.0;
        percent = 100;
        minimumAcceptedValue = unitsConversion.toInternalRepresentation(minInExternalUnits);
        maximumAcceptedValue = unitsConversion.toInternalRepresentation(maxInInternalUnits);
    }

    public abstract void setValue(Double value);

    public abstract Double getValue();

    /**
     * Set the value, as a percent of its default value
     *
     * @param percent
     */
    public void setPercent(Double percent) {
        Double oldPercent = getPercent();
        this.percent = percent;

        if (CVSim.gui.parameterPanel.patientDisplayModeEnabled()) {
            setValue(percent / 100 * getPatientModeDefaultValue());
        } else {
            setValue(percent / 100 * getDefaultValue());
        }

        String str = String.format(getName() + ": Changing percent from " + oldPercent.toString()
                + " to " + percent.toString() + "\n"
                + getName() + ": Changing value from default of "
                + getDefaultValue().toString()
                + " to " + getValue().toString());
        System.out.println(str);
    }

    public Double getPercent() {
        return percent;
    }

    /**
     * Get the default value - currently in internal units
     *
     * @return default value
     */
    public Double getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value, in internal units
     *
     * @param val
     */
    public void setDefaultValue(Double val) {
        defaultValue = val;
    }

    /**
     * Set the default value to its current value
     */
    public final void setDefaultValue() {
        setDefaultValue(getValue());
    }

    public Double getExternalRepresentationOfValue() {
        return toExternalRepresentation(getValue());
    }

    Double getExternalRepresentationOfDefaultValue() {
        return toExternalRepresentation(getDefaultValue());
    }

    public void setExternalRepresentationOfValue(double value) {
        setValue(toInternalRepresentation(value));
    }

    public Double getPatientModeDefaultValue() {
        return patientModeDefaultValue;
    }

    public void setPtModeDefaultValue(Double val) {
        patientModeDefaultValue = val;
    }

    public double getMin() {
        return minimumAcceptedValue;
    }

    public double getMax() {
        return maximumAcceptedValue;
    }

    @Override
    public String toString() {
        return getName();
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

}
