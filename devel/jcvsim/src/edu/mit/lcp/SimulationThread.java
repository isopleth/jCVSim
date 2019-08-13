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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is the simulation thread
 *
 * @author Jason Leake
 */
class SimulationThread extends Thread {

    private final AtomicLong periodMs;
    private final AtomicBoolean simulationEnableFlag;
    private final AtomicBoolean realTimeSimulationSpeedFlag;
    private final AtomicBoolean resetFlag;
    private final CSimulation sim;

    /**
     * Constructor
     *
     * @param sim
     * @param simulationEnableFlag
     * @param periodMs
     */
    SimulationThread(CSimulation sim,
            AtomicBoolean simulationEnableFlag,
            AtomicBoolean resetFlag,
            AtomicBoolean RealTimeSimulationSpeedFlag,
            AtomicLong periodMs) {
        this.sim = sim;
        this.simulationEnableFlag = simulationEnableFlag;
        this.realTimeSimulationSpeedFlag = RealTimeSimulationSpeedFlag;
        this.resetFlag = resetFlag;
        this.periodMs = periodMs;
        setName("SimulationThread");
    }

    /**
     * Step the simulation by periodMs at a time, and try to take the same clock
     * time over it.
     */
    @Override
    public void run() {
        try {
            while (true) {
                if (simulationEnableFlag.get()) {
                    long periodInMilliseconds = periodMs.get();
                    long millis = System.currentTimeMillis();
                    long endTime = millis + periodInMilliseconds;
                    sim.step(periodInMilliseconds);
                    long currentTime = System.currentTimeMillis();
                    // Run at realtime speed if flag set, otherwise no delays
                    if (realTimeSimulationSpeedFlag.get()) {
                        if (currentTime < endTime) {
                            Thread.sleep(endTime - currentTime);
                        }
                    }
                } else {
                    // Only check the run flag periodically if thread is not
                    // running
                    Thread.sleep(100);
                }

                if (resetFlag.get()) {
                    sim.reset();
                    resetFlag.set(false);
                    simulationEnableFlag.set(true);
                    realTimeSimulationSpeedFlag.set(true);
                }
            }
        } catch (InterruptedException ex) {
        }
    }
}
