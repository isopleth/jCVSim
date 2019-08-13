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
// converted to Java Jason Leake December 2016

/*
 * Definition of the compartment structure which is used to store the nominal
 * parameter values and their variances for the four physical parameters
 * describing each compartment: compliance, volume, resistance, and length.
 * Entries v[2][.] contain the volume limits and their variances for the
 * non-linear compartments; otherwise they are set to zero.
 */
public class CompartmentParameters {

    public double[][] compliance;    // Compliance    c[1][4]
    public double[][] zpfv;    // Volume        v[2][4]
    public double[][] resistance;    // Resistance    r[1][4]
    public double[][] height;    // length/height h[1][4]

    public CompartmentParameters() {
        compliance = new double[1][];
        compliance[0] = new double[4];
        zpfv = new double[2][];
        zpfv[0] = new double[4];
        zpfv[1] = new double[4];

        resistance = new double[1][];
        resistance[0] = new double[4];
        height = new double[1][4];
        height[0] = new double[4];

    }

    // From reading Initial.c, the indices of the array of 17 Hemo objects are
    // used thus:
    public static final int ASCENDING_AORTA_COMPARTMENT = 0;
    public static final int BRACHIOCEPHALIC_ARTERIAL_COMPARTMENT = 1;
    public static final int UPPER_BODY_ARTERIAL_COMPARTMENT = 2;
    public static final int DESCENDING_AORTA_COMPARTMENT = 3;
    public static final int ABDOMINAL_AORTA_COMPARTMENT = 4;
    public static final int RENAL_ARTERIAL_COMPARTMENT = 5;
    public static final int SPLANCHNIC_ARTERIAL_COMPARTMENT = 6;
    public static final int LEG_ARTERIAL_COMPARTMENT = 7;
    public static final int PULMONARY_ARTERIAL_COMPARTMENT = 8;
    public static final int UPPER_BODY_VENOUS_COMPARTMENT = 9;
    public static final int SUPERIOR_VENA_CAVA_COMPARTMENT = 10;
    public static final int RENAL_VENOUS_COMPARTMENT = 11;
    public static final int SPLANCHNIC_VENOUS_COMPARTMENT = 12;
    public static final int LEG_VENOUS_COMPARTMENT = 13;
    public static final int ABDOMINAL_VENOUS_COMPARTMENT = 14;
    public static final int INFERIOR_VENA_CAVA_COMPARTMENT = 15;
    public static final int PULMONARY_VENOUS_COMPARTMENT = 16;
    // -- End of note
}
