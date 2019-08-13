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

import java.util.ArrayList;
import static jcvsim.common.Maths.rint;

/**
 * The impulse response vectors represent the strength of the baroreflex
 * response over time from a single pressure measurement from the barorecepter
 * system concerned. The pressure measured at time T produces a response over an
 * extended period which gradually rises to a peak and then declines. Hence the
 * response at any given moment is a convolution integral representing the
 * combination of different stages in the response to the series of measurements
 * made over time. As well as representing the response to a single measurement,
 * hence impulse response, this also represents the multiplier for the different
 * measurements to provide the final effect at a given time. This is pretty
 * standard signal processing but I have explained it here as much to remind
 * myself what is happening as to tell you, gentle reader.
 *
 * The vector index is not in seconds, as this is too coarse a granularity,
 * instead each index represents a time step of timeGranularity. The vector thus
 * represents a discrete approximation to a single triangular wave.
 *
 * @author Jason Leake
 */
public final class ImpulseResponse {

    private final double timeGranularity;
    private final ArrayList<Double> data;
    private final String name;
    private final boolean DEBUG = false;

    /**
     * Constructor
     *
     * @param name
     * @param length
     * @param granularity
     * @param responseOnsetDelaySeconds
     * @param responsePeakTimeSeconds
     * @param responseEndTimeSeconds
     * @param timeAdvanceSeconds
     */
    public ImpulseResponse(
            String name,
            int length,
            double granularity,
            double responseOnsetDelaySeconds,
            double responsePeakTimeSeconds,
            double responseEndTimeSeconds,
            double timeAdvanceSeconds) {
        this.name = name;
        data = new ArrayList<>(length);
        timeGranularity = granularity;
        for (int index = 0; index < length; index++) {
            data.add(0.0);
        }
        generateTriangularImpulseResponse(responseOnsetDelaySeconds,
                responsePeakTimeSeconds,
                responseEndTimeSeconds,
                timeAdvanceSeconds);
    }

    /**
     * Initialises the specified vector with a triangular signal, starting at
     * time responseOnsetDelaySeconds - timeAdvanceSeconds, rising to a maximum
     * at responsePeakTimeSeconds - timeAdvanceSeconds, and declining to zero at
     * responseEndTimeSeconds - timeAdvanceSeconds. The amplitude is selected
     * such that the integral of the response triangle is 1.
     *
     * The timeAdvanceSeconds parameter causes the response to happens a little
     * earlier than specified. This represents delays in the simulation engine,
     * as the effect is that the response has decayed slightly, and a later
     * value is provided, at a given time. This is needed to compensate for
     * various simulation engine delays.
     *
     * The code in this function is strongly based on the original C code by
     * Thomas Heldt March 2nd, 2002
     *
     * @param responseOnsetDelaySeconds response start time in seconds
     * @param responsePeakTimeSeconds response peak time in seconds
     * @param responseEndTimeSeconds response end time in seconds
     * @param timeAdvanceSeconds advance added to each time
     */
    public void generateTriangularImpulseResponse(
            double responseOnsetDelaySeconds,
            double responsePeakTimeSeconds,
            double responseEndTimeSeconds,
            double timeAdvanceSeconds) {
        double sum = 0.0;                   // local normalization variable

        // Set up impulse response function
        int start = (int) rint((responseOnsetDelaySeconds - timeAdvanceSeconds) / timeGranularity);
        int peak = (int) rint((responsePeakTimeSeconds - timeAdvanceSeconds) / timeGranularity);
        int end = (int) rint((responseEndTimeSeconds - timeAdvanceSeconds) / timeGranularity);

        // Stop the code crashing with negative array indexes if the response times
        // are zero. Temporary bodge for when parameters have been set to zero.
        if (start >= 0 && peak >= 0 & end >= 0) {
            if (DEBUG) {
                System.out.println(name + " start is " + start);
                System.out.println(name + " peak is " + peak);
                System.out.println(name + " end is " + end);
            }
            for (int timeslot = start; timeslot < peak; timeslot++) {
                double value = (double) (timeslot - start) / (peak - start);
                data.set(timeslot, value);
                sum += value;
            }
            for (int timeslot = peak; timeslot <= end; timeslot++) {
                double value = (double) (end - timeslot) / (end - peak);
                data.set(timeslot, value);
                sum += value;
            }
            // Normalise so that the total, analogous to the integral, is 1
            for (int timeslot = start; timeslot <= end; timeslot++) {
                data.set(timeslot, data.get(timeslot) / sum);
            }
        } else {
            // The effect of this will be to disable this reflex entirely
            System.out.println("Impulse vector " + name + " is set to zero");
            System.out.println("start is " + start);
            System.out.println("peak is " + peak);
            System.out.println("end is " + end);

        }
    }

    /**
     * Getter
     *
     * @param timeslot, timeslot number, units of timeGranularity
     * @return value for this timeslot
     */
    public double get(int timeslot) {
        return data.get(timeslot);
    }

}
