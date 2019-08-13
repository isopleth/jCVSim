package edu.mit.lcp;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Class SimulationThread is a wrapper for the Simulation class so it
// can run independently at a fixed rate in a separate thread.
public class SimulationThreadControl {

    private final SimulationThread simulationThread;
    private final AtomicBoolean simulationEnableFlag = new AtomicBoolean(false);
    private final AtomicBoolean resetFlag = new AtomicBoolean(false);
    private final AtomicBoolean realTimeSimulationSpeedFlag = new AtomicBoolean(true);
    private final AtomicLong periodInMilliseconds = new AtomicLong(0);

    /**
     * Constructor
     *
     * @param sim simulation engine entry point
     */
    public SimulationThreadControl(CSimulation sim) {
        simulationThread = new SimulationThread(sim, simulationEnableFlag, resetFlag,
                realTimeSimulationSpeedFlag, periodInMilliseconds);
    }

    /**
     * Set the gross time step for the simulation thread. It runs until this
     * time step has been reached and then emits a set of outputs
     *
     * @param period time step period
     */
    public void setPeriodInMs(long period) {
        periodInMilliseconds.set(period);
        System.out.println("Simulation Period set to " + periodInMilliseconds + "ms");
    }

    public boolean isRunning() {
        return simulationEnableFlag.get();
    }

    /**
     * Run the thread, or restart it if it has been paused
     */
    public void start() {
        simulationEnableFlag.set(true);
        if (!simulationThread.isAlive()) {
            simulationThread.start();
        }
    }

    /**
     * Pause the thread
     */
    public void pause() {
        simulationEnableFlag.set(false);
    }

    /**
     * Reset the simulation by setting the reset flag. Don't set the flag
     * if the simulation thread isn't running as it will never be cleared, and
     * there is nothing to do anyway.
     */
    public void reset() {
        if (simulationThread.isAlive()) {
            resetFlag.set(true);
        }
    }

    /**
     * Test if the simulation has reset yet
     *
     * @return true if reset has not been actioned yet
     */
    boolean stillResetting() {
        return resetFlag.get();
    }

    /**
     * Enable or disable the slowing down of the simulation to realtime speeds
     *
     * @param selected
     */
    void realTimeDelayEnabled(boolean selected) {
        realTimeSimulationSpeedFlag.set(selected);
    }

}
