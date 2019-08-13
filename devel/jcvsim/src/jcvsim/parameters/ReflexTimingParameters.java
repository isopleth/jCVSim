package jcvsim.parameters;

import jcvsim.parameters.ResponseParameters;

/*
 * Structures containing reflex parameters.  Reflex contains the set-point
 * values and static gain values. Timing contains the information needed to
 * set up the impulse response functions.
 */
public class ReflexTimingParameters {

    public ResponseParameters[] para;
    public ResponseParameters[] beta;
    public ResponseParameters[] alpha_r;
    public ResponseParameters[] alpha_v;
    public ResponseParameters[] alpha_cpr;
    public ResponseParameters[] alpha_cpv;

    public ReflexTimingParameters() {
        para = new ResponseParameters[4];
        beta = new ResponseParameters[4];
        alpha_r = new ResponseParameters[4];
        alpha_v = new ResponseParameters[4];
        alpha_cpr = new ResponseParameters[4];
        alpha_cpv = new ResponseParameters[4];
        for (int index = 0; index < 4; index++) {
            para[index] = new ResponseParameters();
            beta[index] = new ResponseParameters();
            alpha_r[index] = new ResponseParameters();
            alpha_v[index] = new ResponseParameters();
            alpha_cpr[index] = new ResponseParameters();
            alpha_cpv[index] = new ResponseParameters();
        }
    }
}
