package jcvsim.compartment21;

/*
 * Header file for the file simulator.c which is the top-level file for the
 * simulation module.
 *
 * Thomas Heldt March 2nd, 2002
 * Last modified April 11th, 2002
 */
// Converted to Java   Jason Leake December 2016

/*
 * Definition of the tilt structure. It contains the leakage flow variables
 * that need to be passed from the tilt() or lbnp() routines to the eqns()
 * routine.
 */
public class TiltLeakageFlows {

    public double splanchnicFlow;
    public double legFlow;
    public double abdominalFlow;

    public TiltLeakageFlows() {
        splanchnicFlow = 0;
        legFlow = 0;
        abdominalFlow = 0;
    }

}
