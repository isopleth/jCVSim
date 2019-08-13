package jcvsim.common;

import java.util.Arrays;

/*
 * Header file for the file simulator.c which is the top-level file for the
 * simulation module.
 *
 * Thomas Heldt March 2nd, 2002
 * Last modified April 11th, 2002
 */
// Converted to Java   Jason Leake December 2016
public final class Reflex_vector {

    /**
     * Cardiac chambers affected by the contractility feedback loop;
     * compartments affected by the resistance and venous tone feedback loops.
     */

    /*
     * Definition of the reflex data structure. This structure contains the
     * values of the parameters affected by the reflex response, namely heart 
     * rate, right and left ventricular end-systolic compliances, arteriolar
     * resistances, and four systemic zero pressure filling volumes.
     */
    /**
     * Afferent (reflex) instantaneous heart rate signal
     */
    public double afferentHeartRateSignal;
    /**
     * Cumulative heart rate signal in a beat (intra-cardiac cycle time)
     */
    public double cumulativeHeartRateSignal;
    /**
     * The actual beat-by-beat heart rate value.
     */
    public double instantaneousHeartRate;
    /**
     * Right and left ventricular end-systolic compliances
     */
    public double rvEndSystolicCompliance;
    public double lvEndSystolicCompliance;

    public double resistance[];
    public double volume[];
    public int stepCount;

    /**
     * Constructor
     */
    public Reflex_vector() {
        resistance = new double[4];
        volume = new double[4];
    }

    /**
     * Copy another object to this one
     *
     * @param other object to copy from
     */
    public void copyFrom(Reflex_vector other) {
        afferentHeartRateSignal = other.afferentHeartRateSignal;
        cumulativeHeartRateSignal = other.cumulativeHeartRateSignal;
        instantaneousHeartRate = other.instantaneousHeartRate;

        rvEndSystolicCompliance = other.rvEndSystolicCompliance;
        lvEndSystolicCompliance = other.lvEndSystolicCompliance;
        resistance = Arrays.copyOf(other.resistance, other.resistance.length);
        volume = Arrays.copyOf(other.volume, other.volume.length);
        stepCount = other.stepCount;

    }

    /**
     * Copy constructor
     *
     * @param other object to copy from
     */
    public Reflex_vector(Reflex_vector other) {
        copyFrom(other);
    }
}
