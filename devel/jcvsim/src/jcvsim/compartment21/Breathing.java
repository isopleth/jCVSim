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

import static java.lang.Math.PI;
import jcvsim.common.ControlState;
import static jcvsim.common.Maths.cos;
import static jcvsim.common.Maths.periodFromRatePerMinute;
import static jcvsim.common.Maths.sin;

/**
 * This class represents the intrathoracic pressure and lung volume changes due
 * to breathing. It assumes that the pressure inside the lung is the same as the
 * intrathoracic pressure, which is not strictly true.
 *
 * @author Jason Leake
 */
//
// These are the values used in rcvsim:
// 	% Respiration
//
//		% Respiratory cycle period for fixed-rate 
//		% breathing (Def: 5 sec)
//		Tr: 5
//
//		% Tidal volume (Def: 500 ml)
//		Qt: 500
//
//		% Initial functional reserve volume (Def: 2200 ml) {*}
//		Qfr: 2200
//
//		% Step amplitude in functional reserve volume (Def: 0 ml)
//		Qfrs: 0
//
//		% Dead space of airways (Def: 150 ml)
//		Qds: 150
//
//		% Airway resistance (Def: 0.0026 mmHg-s/ml)
//		Rair: 0.0026
//
//		% Lung compliance (Def: 252.5 ml/mmHg)
//		Clu: 252.5
//
//		% Unstressed volume in lungs (Def: 1190 ml)
//		Qluo: 1190
//
//	% Direct neural coupling mechanism between respiration and F
//
//		% Static gain control of parasympathetic limb (Def: 1 unitless)
//		dgainp: 1
//
//		% Static gain control of beta-sympathetic limb (Def: 1 unitless)
//		dgains: 1
public class Breathing {

    private final Parameters parameters;
    private final ControlState state;

    /**
     * Constructor
     *
     * @param parameterVector parameter vector
     */
    Breathing(ControlState controlState, Parameters parameterVector) {
        parameters = parameterVector;
        state = controlState;
    }

    /**
     * Calculate the intra-thoracic overpressure due to breathing at the
     * specified time
     *
     * @param absoluteTimeInSeconds in seconds since simulation start
     * @return new intra-thoracic pressure
     */
    public double getIntrathoracicTidalPressure(double absoluteTimeInSeconds) {
        if (state.breathingEnabled) {

            // Now assume that breathing is a sine wave. Calculate the sine wave
            // which is in range -1->+1, and convert it to the range 0->1.
            double pressure = (1. + sin(2. * PI * absoluteTimeInSeconds / getBreathingPeriodInSeconds())) / 2.;

            // Scale it
            double scale = parameters.get(ParameterName.LUNG_MAX_TIDAL_PRESSURE)
                    - getMinTidalPressure();
            pressure = pressure * scale;

            // Now shift it
            pressure = pressure + getMinTidalPressure();

            return pressure;
        } else {
            // If breathing is switched off in the model then there is no
            // time-dependent overpressure
            return 0.;
        }
    }

    /**
     * Calculate the intra-thoracic overpressure due to breathing at the
     * specified time
     *
     * @param absoluteTimeInSeconds in seconds since simulation start
     * @return new intra-thoracic pressure
     */
    public double getIntrathoracicTidalPressureDerivative(double absoluteTimeInSeconds) {
        if (state.breathingEnabled) {
            double breathingPeriodInSeconds = periodFromRatePerMinute(parameters.get(ParameterName.LUNG_RESPIRATORY_RATE_PER_MINUTE));

            // This is the derivative of the expression in
            // getIntrathoracicTidalPressure()
            double pressure = (2. * PI / getBreathingPeriodInSeconds()) * cos(2. * PI * absoluteTimeInSeconds / breathingPeriodInSeconds) / 2;

            // Scale it
            double scale = parameters.get(ParameterName.LUNG_MAX_TIDAL_PRESSURE)
                    - getMinTidalPressure();
            pressure = pressure * scale;

            return pressure;
        } else {
            // If breathing is switched off in the model then there is no
            // time-dependent overpressure change
            return 0.;
        }
    }

    /**
     * Return the min tidal pressure.
     *
     * If linkITPandLungVolume is not set then it just uses the parameter.
     *
     * If it is set, then it returns a value based on email from Pedro Fernandes
     * Vargas "As per a previous email from February (I think) ITP could be set
     * between -4 mmHg at FRC and around -9-10 mmHg at a volume corresponding to
     * FRC + 30% " forced vital capacity, which is an average volume most people
     * breathe at " when doing SDB at 6 breaths.min-1."
     */
    public double getMinTidalPressure() {
        if (state.linkITPandLungVolume) {
            return -4.
                    - 2. * parameters.get(ParameterName.LUNG_VOLUME_TIDAL)
                    / parameters.get(ParameterName.LUNG_VOLUME_FUNCTIONAL_RESERVE);
        } else {
            return parameters.get(ParameterName.LUNG_MIN_TIDAL_PRESSURE);
        }
    }

    /**
     * Return tidal lung volume. Varies between 0 and PVName.LUNG_VOLUME_TIDAL
     *
     * @param absoluteTimeInSeconds
     * @return lung volume
     */
    public double getTidalLungVolume(double absoluteTimeInSeconds) {
        if (state.breathingEnabled) {
            // Now assume that breathing is a sine wave. Calculate the sine wave
            // which is in range -1->+1, and convert it to the range 0->1.
            // It is pi/2 out of phase with the intrathoracic pressure
            double normalisedInstantaneousTidalVolume = (1.
                    + sin((PI / 2.) + (2. * PI * absoluteTimeInSeconds / getBreathingPeriodInSeconds()))) / 2.;

            double retval = normalisedInstantaneousTidalVolume * parameters.get(ParameterName.LUNG_VOLUME_TIDAL);

            return retval;
        } else {
            return parameters.get(ParameterName.LUNG_VOLUME_TIDAL) / 2;
        }
    }

    /**
     * Get the breathing period in seconds
     *
     * @return period
     */
    private double getBreathingPeriodInSeconds() {
        return periodFromRatePerMinute(parameters.get(ParameterName.LUNG_RESPIRATORY_RATE_PER_MINUTE));
    }
}
