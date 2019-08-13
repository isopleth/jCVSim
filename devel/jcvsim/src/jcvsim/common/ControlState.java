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
package jcvsim.common;

import edu.mit.lcp.CSimulation;
import edu.mit.lcp.CVSim;

/**
 * Encapsulates the state of the control buttons in the display. It saves
 * having to pass a load of separate flags into methods.
 *
 * @author Jason Leake
 */
public class ControlState {

    public final int dataCompressionFactor;
    public final boolean abReflexEnabled;
    public final boolean cpReflexEnabled;
    public final boolean breathingEnabled;
    public final boolean lungVolumeReflexEnabled;
    
    /**
     * This is a placeholder for linking the ITP to the lung volume
     */
    public final boolean linkITPandLungVolume = false;

    public final boolean tiltTestEnabled;
    public final double tiltStartTime;
    public final double tiltStopTime;

    /**
     * Constructor
     * @param sim Main simulation class, which contains the states of the
     * the features represented by this class.
     */
    public ControlState(CSimulation sim) {
        dataCompressionFactor = sim.getDataCompressionFactor();
        abReflexEnabled = sim.getABReflex();
        cpReflexEnabled = sim.getCPReflex();
        breathingEnabled = sim.getBreathing();
        lungVolumeReflexEnabled = sim.getLungVolumeReflex();
        tiltTestEnabled = CVSim.getTiltTest();
        tiltStartTime = CVSim.getTiltStartTime();
        tiltStopTime = CVSim.getTiltStopTime();
    }

    /**
     * Constructor for "everything disabled" object
     */
    public ControlState() {
        dataCompressionFactor = 1;
        abReflexEnabled = false;
        cpReflexEnabled = false;
        breathingEnabled = false;
        lungVolumeReflexEnabled = false;
        tiltTestEnabled = false;
        tiltStartTime = 0;
        tiltStopTime = 0;
    }

}
