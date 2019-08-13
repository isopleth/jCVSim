package edu.mit.lcp;

public class SimulationOutputVariableBuffer {

    private final SimulationOutputVariable simulationOutputVariable;
    private double lastDatum;
    private final SynchronizedCircularBuffer dataBuffer;

    public SimulationOutputVariableBuffer(int sizeLimit,
            SimulationOutputVariable variable) {
        simulationOutputVariable = variable;
        dataBuffer = new SynchronizedCircularBuffer(sizeLimit);
        lastDatum = 0;
    }

    // Override the Object.toString() method to give useful names
    @Override
    public String toString() {
        return simulationOutputVariable.getDescription();
    }

    public int getOutputIndex() {
        return simulationOutputVariable.getOutputIndex();
    }

    public String getName() {
        return simulationOutputVariable.getName();
    }

    public String getDescription() {
        return simulationOutputVariable.getDescription();
    }

    public String getInternalUnits() {
        return simulationOutputVariable.getInternalUnits();
    }

    public int getSize() {
        return dataBuffer.size();
    }

    public SimulationOutputVariable getVar() {
        return simulationOutputVariable;
    }

    public Range getTypicalRange() {
        return simulationOutputVariable.getTypicalRange();
    }

    public double getLastDatum() {
        return lastDatum;
    }

    double getLastDatumInExternalRepresentation() {
        return simulationOutputVariable.toExternalRepresentation(lastDatum);
    }
    
    public void addDatum(double timestamp, double datum) {
        lastDatum = datum;
        dataBuffer.addToRing(timestamp, datum);
    }

    public void resizeRingBuffer(int newSize) {
        dataBuffer.resize(newSize);
    }
    
    /**
     * Get a list of data points with the timestamps
     *
     * @return list of points
     */
    public PointsList getPointsList() {
        return dataBuffer.toPointsList();
    }

}
