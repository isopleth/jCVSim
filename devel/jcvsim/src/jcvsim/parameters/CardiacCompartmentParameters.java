package jcvsim.parameters;

/*
 * Definition of the compartment structure which is used to store the nominal
 * parameter values and their variances for  physical parameters describing each 
* compartment: compliance, volume, resistance.
 * Entries v[2][.] contain the volume limits and their variances for the
 * non-linear compartments; otherwise they are set to zero.
*
* Each Cardiac object represents one compartment of the heart, so there
* should be two of them instantiated.
 */
public class CardiacCompartmentParameters {

    public double[][] systolicCompliance;
    public double[][] diastolicCompliance;
    public double[][] zpfv;
    public double[][] resistance;

    public CardiacCompartmentParameters() {
        systolicCompliance = new double[2][];
        diastolicCompliance = new double[2][];
        zpfv = new double[2][];
        resistance = new double[1][];

        systolicCompliance[0] = new double[4];
        systolicCompliance[1] = new double[4];
        diastolicCompliance[0] = new double[4];
        diastolicCompliance[1] = new double[4];
        zpfv[0] = new double[4];
        zpfv[1] = new double[4];
        resistance[0] = new double[4];
    }
}
