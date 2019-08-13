package jcvsim.compartment21;

import jcvsim.common.ControlState;
import static jcvsim.common.Maths.exp;
import static jcvsim.common.Maths.fabs;
import static jcvsim.common.Maths.log;
import static jcvsim.common.Maths.pow;
import jcvsim.common.Reflex_vector;
import jcvsim.compartment21.Data_vector.CompartmentIndex;
import static jcvsim.compartment21.Data_vector.TimeIndex.*;

/*
 * This file contains the two routines needed to advance the solution of the
 * differential equations by one step. We are currently using a fourth-order
 * Runge-Kutta adaptive stepsize integration routine adapted from the Numerical
 * Recipes in C.
 *
 * Thomas Heldt January 31st, 2002
 * Last modified July 13th, 2002
 */
// converted to Java Jason Leake December 2016
public class Rkqc {

    private static final double PGROW = -0.20;
    private static final double PSHRNK = -0.25;
    private static final double SAFETY = 0.9;
    private static final double ERRCON = 6.0e-4;


    /*
     * rk4() is a fourth-order Runge Kutta integrator routine that is based on a
     * similar code in the Numerical Recipies for C. When the function is
     * called,
     * on fourth-order step is taken and the values of the dependent variables
     * are
     * returned upon function exit.
     *
     * On entry:
     *
     * q Data_vector
     * q is the data structure that contains the dependent variables as
     * the q.x array, the time index in the q.time array, and the
     * derivative information in the q.dxdt array.
     *
     * r Reflex_vector
     * r contains the information about the state of the reflex system. In
     * particular, it contains the information about the cummulative heart
     * rate and therefore the onset time for the next cardiac beat.
     *
     * c Parameter_vector
     * c contains the values of the parameters which are needed when
     * calling the subroutines eqns() and elastance().
     *
     * h double
     * h is the size of the step to be taken by the integrator.
     *
     *
     * On return:
     *
     * q the values of the dependent variables in the q.x array have been
     * replaced by the values at the time t_0+h where t_0 was the
     * simulation time before the rk4 routine was called.
     *
     * r the cummulative heart rate entry and the step count entry of the
     * reflex vector are updated upon function exit.
     *
     * Thomas Heldt January 20, 2002
     * Last modified July 13th, 2002
     */
    public static void rk4_ptr(Data_vector p, Reflex_vector reflexVector, 
            Parameters pvec, double h,
            IntraThoracicPressure intraThoracicPressure, 
            ControlState controlState,
            Reflex reflex) {
        Reflex_vector reflexVector_t = new Reflex_vector(reflexVector);
        Reflex_vector reflexVector_s = new Reflex_vector(reflexVector);       // temporary reflex vectors
        Data_vector q_local = new Data_vector(p);
        Data_vector p_local = new Data_vector(p);  // temporary data vectors

        double hh = h * 0.5;
        double h6 = h / 6.0;          // see NRC for explanation of fourth-
        // order RK integration algorithm

        //  printf("Entering rk4\n");
        //  printf("%e %e %e %e %e\n", q.time[0], q_local.time[1], p_local.time[1], q_local.c[1], p_local.c[1]);
        // Take first step of size hh to the mid-point of the interval.
        for (int index : CompartmentIndex.simulatedCompartments()) {
            q_local.pressure[index] = q_local.pressure[index] + hh * p.dPressureDt[index];
        }

        // Check whether a new beat should be initiated given that we have propagated
        // the pressure vector over the time interval hh.
        q_local.time[SECONDS_INTO_CARDIAC_CYCLE] = reflex.sinoatrialNode(q_local, reflexVector_s, pvec, hh);

        // Update the time-varying elastance values and their derivatives.
        Equation.elastance_ptr(q_local, pvec);

        // Update the derivatives for the current copy of the pressure vector.
        Equation.eqns_ptr(q_local, pvec, reflexVector_s, intraThoracicPressure, controlState);

        //  printf("%e %e %e %e %e\n", q.time[0], q_local.time[1], p_local.time[1], q_local.c[1], p_local.c[1]);
        // Take second step (also of size hh) to the mid-point of the interval.
        for (int index : CompartmentIndex.simulatedCompartments()) {
            q_local.pressure[index] = p.pressure[index] + hh * q_local.dPressureDt[index];
        }

        // Compute the derivatives.
        p_local.copyFrom(q_local);
        Equation.eqns_ptr(p_local, pvec, reflexVector_s, intraThoracicPressure, controlState);
        p_local.time[SECONDS_INTO_CARDIAC_CYCLE] = p.time[SECONDS_INTO_CARDIAC_CYCLE];
        p_local.time[SECONDS_INTO_VENTRICULAR_CONTRACTION] = p.time[SECONDS_INTO_VENTRICULAR_CONTRACTION];
        p_local.time_new[SIMULATION_TIME] = p.time_new[SIMULATION_TIME];
        p_local.time_new[MODIFIED_SIMULATION_TIME] = p.time_new[MODIFIED_SIMULATION_TIME];

        //  printf("%e %e %e %e %e\n", q.time[0], q_local.time[1], p_local.time[1], q_local.c[1], p_local.c[1]);
        // Take step of size h to the end of the interval.
        for (int index : CompartmentIndex.simulatedCompartments()) {
            p_local.pressure[index] = p.pressure[index] + h * p_local.dPressureDt[index];
        }

        for (int index : CompartmentIndex.simulatedCompartments()) {
            q_local.dPressureDt[index] = q_local.dPressureDt[index] + p_local.dPressureDt[index];
        }

        p_local.time[SECONDS_INTO_CARDIAC_CYCLE] = reflex.sinoatrialNode(p_local, reflexVector_t, pvec, h);
        Equation.elastance_ptr(p_local, pvec);
        Equation.eqns_ptr(p_local, pvec, reflexVector_t, intraThoracicPressure, controlState);

        //  printf("%e %e %e %e %e\n", q.time[0], q_local.time[1], p_local.time[1], q_local.c[1], p_local.c[1]);
        for (int index : CompartmentIndex.simulatedCompartments()) {
            p_local.pressure[index] = p.pressure[index]
                    + h6 * (p.dPressureDt[index] + p_local.dPressureDt[index]
                    + 2.0 * q_local.dPressureDt[index]);
        }

        Equation.eqns_ptr(p_local, pvec, reflexVector_t, intraThoracicPressure, controlState);
        p_local.time[SIMULATION_TIME] = p_local.time[SIMULATION_TIME] + h;

        //  printf("%e %e %e %e %e\n\n", q.time[0], q_local.time[1], p_local.time[1], q_local.c[1], p_local.c[1]);
        reflexVector.cumulativeHeartRateSignal = reflexVector_t.cumulativeHeartRateSignal;
        reflexVector.stepCount = reflexVector_t.stepCount;

        p.copyFrom(p_local);
    }

// Pass *double as single element array - a bit of a bodge!
    public static void rkqc_ptr(Data_vector p, Reflex_vector r, 
            Parameters pvec,
            double htry, double eps, double[] hdid,
            double[] hnext, 
            IntraThoracicPressure intraThoracicPressure, 
            ControlState controlState,
            Reflex reflex) {
        Data_vector p_local = new Data_vector();
        Data_vector q_local = new Data_vector();
        Reflex_vector s = new Reflex_vector();
        Reflex_vector t = new Reflex_vector();

        double xsav = 0.0;          // dummy variable to save the initial time
        double hh = 0.0;            // variables for step-sizes
        double h = 0.0;             // variables for step-sizes
        double temp = 0.0;          // dummy variable (see below) 
        double errmax = 0.0;        // dummy variable for maximal difference between
        // single step and double step results

        xsav = p.time[SECONDS_INTO_CARDIAC_CYCLE];
        h = htry;

        // Updated dxdt vector needed because of time-varying volume. 
        // Does not affect model performace when bv is constant.
        Equation.eqns_ptr(p, pvec, r, intraThoracicPressure, controlState);

        for (;;) {
            p_local.copyFrom(p);
            q_local.copyFrom(p);
            s.copyFrom(r);
            t.copyFrom(r);
            hh = h * 0.5;

            //    printf("Taking first step of size: %f\n", hh);
            // Take first step of size h/2.
            rk4_ptr(p_local, s, pvec, hh, intraThoracicPressure, controlState, reflex);
            //    printf("Taking second step of size: %f\n", hh);
            // Take second setp of size hh.
            rk4_ptr(p_local, s, pvec, hh, intraThoracicPressure, controlState, reflex);

            if (p_local.time[SECONDS_INTO_CARDIAC_CYCLE] == xsav) {
                break;
            }

            //    printf("Taking step of size: %f\n", h);
            // Take one full step of size h
            rk4_ptr(q_local, t, pvec, h, intraThoracicPressure, controlState,reflex);
            errmax = 0.0;

            for (int index : CompartmentIndex.simulatedCompartments()) {
                q_local.pressure[index] = q_local.pressure[index] - p_local.pressure[index];
                if (errmax < (temp = fabs(q_local.pressure[index]))) {
                    errmax = temp;
                }
            }

            if ((errmax /= eps) <= 1.0) {
                hdid[0] = h;
                if (errmax > ERRCON) {
                    hnext[0] = SAFETY * h * pow(errmax, PGROW);
                } else {
                    hnext[0] = 4.0 * h;
                }
                break;
            }
            h = SAFETY * h * exp(PSHRNK * log(errmax));
            //    printf("Reducing stepsize to: %f\n\n", h);
        }

        for (int index : CompartmentIndex.simulatedCompartments()) {
            p_local.pressure[index] = p_local.pressure[index] + q_local.pressure[index] / 15.0;
            p.pressure[index] = p_local.pressure[index];
        }
    }
}
