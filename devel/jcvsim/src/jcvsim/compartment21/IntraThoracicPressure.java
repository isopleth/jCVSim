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
package jcvsim.compartment21;

import jcvsim.common.ControlState;
import static jcvsim.compartment21.Data_vector.TimeIndex.SIMULATION_TIME;

/**
 * This class represents the intra-thoracic pressure variation
 *
 * @author Jason Leake
 */
public class IntraThoracicPressure {

    private double tiltEffect;
    private double tiltEffectDerivative;
    private final Parameters parameterVector;
    private final Breathing breathing;
    private Data_vector dataVector;

    /**
     * Constructor
     *
     * @param controlState structure holds the control state of the program -
     * this class needs to know whether breathing is enabled or not
     * @param paramVec parameter vector
     * @param dataVec data vector
     */
    IntraThoracicPressure(ControlState controlState, Parameters paramVec, Data_vector dataVec) {
        tiltEffect = 0;
        breathing = new Breathing(controlState, paramVec);
        parameterVector = paramVec;
        dataVector = dataVec;
    }

    /**
     * This version of the constructor is used to provide a dummy breathing
     * object which has breathing switched off. It is a bit of a bodge used to
     * generate the initial estimate for the simulation model.
     *
     * @param paramVec parameter vector
     */
    IntraThoracicPressure(Parameters paramVec) {
        tiltEffect = 0;
        tiltEffectDerivative = 0;
        breathing = null;
        parameterVector = paramVec;
    }

    /**
     * Set the tilt component of the intra-thoracic pressure. From page 49 of
     * Thomas Heldt’s PhD thesis: "In addition to changes in the luminal
     * pressures described by Ph, Mead and Gaensler [123] and Ferris and
     * co-workers [124] showed that intra-thoracic pressure, Pth, changed over
     * similarly short periods of time in response to gravitational stress. Mead
     * and Gaensler reported intra-thoracic pressure to drop by (3.1±1.0) mm Hg
     * in response to sitting up from the supine position. Ferris demonstrated a
     * drop of (3.5±0.7) mm Hg in response to head-up tilts to 90◦. We
     * implemented these changes in intra-thoracic pressure by assuming a time
     * course similar to the one described by Equation 2.4."
     *
     * @param effect the change in pressure due to tilt, in mmHg
     * @param derivative the derivative of the pressure change due to tilt.
     * Needed because the simulation solves initial value + derivative ODEs
     */
    void setTiltEffect(double effect, double derivative) {
        tiltEffect = effect;
        tiltEffectDerivative = derivative;
    }

    /**
     * Return the current value of the intra-thoracic pressure at the absolute
     * time in seconds from the start of the simulation specified by
     * dataVector.time[ABSOLUTE_TIME]
     *
     * @return intra-thoracic pressure
     */
    double getValue() {
        double retVal = parameterVector.get(ParameterName.INTRA_THORACIC_PRESSURE)
                + tiltEffect;
        if (breathing != null) {
            retVal += breathing.getIntrathoracicTidalPressure(dataVector.time[SIMULATION_TIME]);
        }
        return retVal;
    }

    /**
     * Return the rate of change of the of the intra-thoracic pressure at the
     * absolute time in seconds from the start of the simulation specified by
     * dataVector.time[ABSOLUTE_TIME]
     *
     * @return intra-thoracic pressure
     */
    double getDerivative() {
        double retVal = tiltEffectDerivative;
        if (breathing != null) {
            retVal += breathing.getIntrathoracicTidalPressureDerivative(dataVector.time[SIMULATION_TIME]);
        }
        return retVal;
    }

    /**
     * Get the instantaneous lung volume.
     *
     * @return lung volume
     */
    double getTidalLungVolume() {
        if (breathing != null) {
            return breathing.getTidalLungVolume(dataVector.time[SIMULATION_TIME]);
        } else {
            return parameterVector.get(ParameterName.LUNG_VOLUME_TIDAL) / 2;
        }
    }
}
