package jcvsim.parameters;


/*
 * This is the top-level header file for the entire program. Here we include
 * the standard libraries and define the global structures Parameter_vector
 * and Output_vector which are passed between the simulation and the estimation
 * modules. All other header files in this program include this header file.
 *
 * Thomas Heldt January 25th, 2002
 * Last modified March 27th, 2004
 */
// Converted to Java Jason Leake December 2016

/*
 * Structure for the microvascular resistance structure. Nominal values for
 * the microvascular resistances are stored in the first entry while their
 * respective variances are stored in the second column;
 */
public class MicrocirculationResistanceParameters {

    // Index [0] is the value for the resistance actually used
    // Index [1] is the standard error for the resistance
    // Index [2] and [3] are values for other datasets
    public double[] upperBodyResistance;
    public double[] kidneyResistance;
    public double[] splanchnicResistance;
    public double[] lowerBodyResistance;
    public double[] pulmonaryResistance;

    public MicrocirculationResistanceParameters() {
        upperBodyResistance = new double[4];
        kidneyResistance = new double[4];
        splanchnicResistance = new double[4];
        lowerBodyResistance = new double[4];
        pulmonaryResistance = new double[4];
    }
}
