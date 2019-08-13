package edu.mit.lcp;

import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationOutputVariable extends Variable {

    // Descriptions of the encapsulated data

    private final String description;
    private final int outputIndex;
    private boolean alwaysSelected;  // True if not shown in the selection panel and always selected
    private final Range typicalRange;
    private final AtomicBoolean selected;

    /**
     * Extended constructor allowing object to always be selected automatically
     *
     * @param index
     * @param name
     * @param theDescription
     * @param units
     * @param category
     * @param type
     * @param range
     * @param isAlwaysSelected
     */
    public SimulationOutputVariable(int index, String name, String theDescription,
            String units, String category, String type, Range range,
            boolean isAlwaysSelected) {
        super(name, category, type, units);
        // Names are only set at construction time
        outputIndex = index;
        description = theDescription;
        typicalRange = range;
        alwaysSelected = isAlwaysSelected;
        selected = new AtomicBoolean(false);
    }

    /**
     * Normal constructor
     *
     * @param index
     * @param name
     * @param theDescription
     * @param units
     * @param category
     * @param type
     * @param range
     */
    public SimulationOutputVariable(int index, String name, String theDescription,
            String units, String category, String type, Range range) {
        this(index, name, theDescription, units, category, type, range, false);
    }

    @Override
    public String toString() {
        return description;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public String getDescription() {
        return description;
    }

    public Range getTypicalRange() {
        return typicalRange;
    }

    /**
     * Set/reset always selected flag, which causes the variable to be
     * implicitly selected for output
     *
     * @param state
     */
    public void select(boolean state) {
        selected.set(state);
    }

    /**
     * Test if the object is selected
     *
     * @return true if selected
     */
    public boolean isSelected() {
        return alwaysSelected || selected.get();
    }

}
