package jcvsim.compartment6;

import jcvsim.common.ControlState;
import jcvsim.common.Output_vector;
import jcvsim.common.Reflex_vector;
import jcvsim.common.Turning;
import static jcvsim.compartment6.Data_vector.CompartmentIndex.*;

/*
 * This file was created in order to run the simulation from the Java GUI.
 * The functions in this file combine the main() function in main.c and
 * the simulator() function in simulator.c. The simulation was broken up into
 * into three parts: an init_sim() function to initialize the simulation,
 * a step_sim() function to advance the simulation one timestep, and a
 * reset_sim()
 * function to reset variables to their initial values.
 *
 * Catherine Dunn July 10, 2006
 * Last modified July 10, 2006
 */
// Converted to Java Jason Leake December 2016
public class Simulation {

    private final int N_OUT = 21;      // Number of output variables in the steady-state
    // analysis

    // Structure handling the flow of data within the simulation.c routine.
    // See simulator.h for the definition.
    private final Data_vector pressure = new Data_vector();

    // Structure for the reflex variables.
    private final Reflex_vector reflex_vector = new Reflex_vector();

    // Output structure - defined in main.h
    private final Output_vector out = new Output_vector();

    private final Reflex reflex;

    // The following definitions pertain to the adaptive stepsize integration
    // routine. See Numerical Recipes in C (p. ???) for details.
    private final double[] hdid = {0.0};
    private double htry = 0.00001;

    private final double[] hnext = {0.0};

    private final double[] result;           // output vector containing the output variables.
    private final Parameters parameterVector;

    public Simulation(Parameters vector) {
        parameterVector = vector;

        // Allocate memory for data structures.
        result = new double[N_OUT];

        // Initialize the parameter structures and the parameter vector.
        new Initial().mapping(parameterVector);

        reflex = new Reflex(parameterVector);

        // The following function calls initialize the data vector. Estimate()
        // initializes the pressure.x[] and pressure.time[] arrays. Elastance()
        // computes the cardiac compliances and their derivatives and stores their
        // values in pressure.c[] and pressure.dcdt[], respectively. Finally,
        // eqns() computes the pressure derivatives and stores them in
        // pressure.dxdt[].
        Estimate.estimate_ptr(pressure, parameterVector, reflex_vector);      // estimate initial pressures

        // Set up the values for the time-varying capacitance values
        Equation.elastance_ptr(pressure, reflex_vector, parameterVector);

        Equation.fixvolume(pressure, reflex_vector, parameterVector);     // fix the total blood volume

        Equation.eqns_ptr(pressure, parameterVector, reflex_vector); // Initialize pressure derivatives

        Simulator_numerics.numerics(pressure, reflex_vector, out, parameterVector); // Initialize the ouput array???

    }

    // This function is needed to update pressure.x[] from the Java code.
    // The output struct copies the values in pressure.x and makes them available to 
    // the Java code. The output struct is in a sense read-only. If the values in the 
    // output struct are changed, the change is not reflected in pressure.x. However, 
    // occasionally (as in the case of parameter updates), information must pass from
    // the GUI to the simulation variables. This function was created to allow 
    // the changes to output.x to be reflected in pressure.x.
    public void updatePressure(int i, double value) {
        pressure.pressure[i] = value;
    }

    /**
     * step(): Advances the simulation one timestep By varying the
     * dataCompressionFactor the data can be compressed before being passed to
     * the GUI. A dataCompressionFactor of 10 means one piece of output data is
     * passed to the GUI for every 10 timesteps. A dataCompressionFactor of 1
     * means every piece of data is passed to the GUI. (One piece of data for
     * every timestep.)
     *
     * @param stepout
     * @param parameters model parameters
     * @param controlState State of the control buttons in the display.
     */
    public void step(Output stepout, Parameters parameters, ControlState controlState) {
        double[] x0 = new double[controlState.dataCompressionFactor];
        double[] x1 = new double[controlState.dataCompressionFactor];
        double[] x2 = new double[controlState.dataCompressionFactor];
        double[] x3 = new double[controlState.dataCompressionFactor];
        double[] x4 = new double[controlState.dataCompressionFactor];
        double[] x5 = new double[controlState.dataCompressionFactor];
        double[] q0 = new double[controlState.dataCompressionFactor];
        double[] q1 = new double[controlState.dataCompressionFactor];
        double[] q2 = new double[controlState.dataCompressionFactor];
        double[] q3 = new double[controlState.dataCompressionFactor];
        double[] q4 = new double[controlState.dataCompressionFactor];
        double[] q5 = new double[controlState.dataCompressionFactor];
        double[] v0 = new double[controlState.dataCompressionFactor];
        double[] v1 = new double[controlState.dataCompressionFactor];
        double[] v2 = new double[controlState.dataCompressionFactor];
        double[] v3 = new double[controlState.dataCompressionFactor];
        double[] v4 = new double[controlState.dataCompressionFactor];
        double[] v5 = new double[controlState.dataCompressionFactor];
        double[] hr = new double[controlState.dataCompressionFactor];
        double[] ar = new double[controlState.dataCompressionFactor];
        double[] vt = new double[controlState.dataCompressionFactor];
        double[] rvc = new double[controlState.dataCompressionFactor];
        double[] lvc = new double[controlState.dataCompressionFactor];
        double[] tbv = new double[controlState.dataCompressionFactor];
        double[] pth = new double[controlState.dataCompressionFactor];

        // Calculate output values
        for (int index = 0; index < controlState.dataCompressionFactor; index++) {
            Rkqc.rkqc_ptr(pressure, reflex_vector, parameters, htry, 0.001, hdid, hnext, reflex);

            pressure.time[1] = reflex.sinoatrialNode(pressure, reflex_vector, parameters, hdid[0]);

            Equation.elastance_ptr(pressure, reflex_vector, parameters);

            Equation.eqns_ptr(pressure, parameters, reflex_vector);

            reflex.queue(pressure, reflex_vector, parameters,
			 hdid[0], controlState);

            pressure.time[0] += hdid[0];

            htry = 0.001;

            Equation.fixvolume(pressure, reflex_vector, parameters);

            Simulator_numerics_new.numerics_new_ptr(pressure, reflex_vector,
						    hdid[0], result);

            Simulator_numerics.numerics(pressure, reflex_vector, out,
					parameters);

            // simulation time
            stepout.time = pressure.time[0];

            x0[index] = pressure.pressure[LEFT_VENTRICULAR_CPI];
            x1[index] = pressure.pressure[ARTERIAL_CPI];
            x2[index] = pressure.pressure[CENTRAL_VENOUS_CPI];
            x3[index] = pressure.pressure[RIGHT_VENTRICULAR_CPI];
            x4[index] = pressure.pressure[PULMONARY_ARTERIAL_CPI];
            x5[index] = pressure.pressure[PULMONARY_VENOUS_CPI];

            q0[index] = pressure.flowRate[LEFT_VENTRICULAR_CPI];
            q1[index] = pressure.flowRate[ARTERIAL_CPI];
            q2[index] = pressure.flowRate[CENTRAL_VENOUS_CPI];
            q3[index] = pressure.flowRate[RIGHT_VENTRICULAR_CPI];
            q4[index] = pressure.flowRate[PULMONARY_ARTERIAL_CPI];
            q5[index] = pressure.flowRate[PULMONARY_VENOUS_CPI];

            v0[index] = pressure.volume[LEFT_VENTRICULAR_CPI];
            v1[index] = pressure.volume[ARTERIAL_CPI];
            v2[index] = pressure.volume[CENTRAL_VENOUS_CPI];
            v3[index] = pressure.volume[RIGHT_VENTRICULAR_CPI];
            v4[index] = pressure.volume[PULMONARY_ARTERIAL_CPI];
            v5[index] = pressure.volume[PULMONARY_VENOUS_CPI];

            hr[index] = reflex_vector.instantaneousHeartRate;
            ar[index] = reflex_vector.resistance[0];
            vt[index] = reflex_vector.volume[0];
            rvc[index] = reflex_vector.rvEndSystolicCompliance;
            lvc[index] = reflex_vector.lvEndSystolicCompliance;
            tbv[index] = parameters.get(ParameterName.TOTAL_BLOOD_VOLUME);
            pth[index] = parameters.get(ParameterName.INTRA_THORACIC_PRESSURE);

            // Check TBV
            //double total = 0;
            //int k;
            //for (k=0; k < 25; k++)
            // total += pressure.v[k]; 
            //System.out.printf("TBV: %.2f, %.2f\n", parameters.get(PVName.PV70), total);
            // Check TZPFV
            //double total = 0;
            //int k;
            //for (k=162; k < 168; k++)
            //  total += parameters.get(k);
            //System.out.printf("TZPFV: %.2f, %.2f\n", parameters.get(PVName.PV75), total);
        } // end for

        // Compress the output by applying turning algorithm
        stepout.leftVentricularPressure = Turning.turning(x0, controlState.dataCompressionFactor);
        stepout.arterialPressure = Turning.turning(x1, controlState.dataCompressionFactor);
        stepout.centralVenousPressure = Turning.turning(x2, controlState.dataCompressionFactor);
        stepout.rightVentricularPressure = Turning.turning(x3, controlState.dataCompressionFactor);
        stepout.pulmonaryArterialPressure = Turning.turning(x4, controlState.dataCompressionFactor);
        stepout.pulmonaryVenousPressure = Turning.turning(x5, controlState.dataCompressionFactor);

        stepout.leftVentricularFlowRate = Turning.turning(q0, controlState.dataCompressionFactor);
        stepout.arterialFlowRate = Turning.turning(q1, controlState.dataCompressionFactor);
        stepout.centralVenousFlowRate = Turning.turning(q2, controlState.dataCompressionFactor);
        stepout.rightVentricularFlowRate = Turning.turning(q3, controlState.dataCompressionFactor);
        stepout.pulmonaryArterialFlowRate = Turning.turning(q4, controlState.dataCompressionFactor);
        stepout.pulmonaryVenousFlowRate = Turning.turning(q5, controlState.dataCompressionFactor);

        stepout.leftVentricularVolume = Turning.turning(v0, controlState.dataCompressionFactor);
        stepout.arterialVolume = Turning.turning(v1, controlState.dataCompressionFactor);
        stepout.centralVenousVolume = Turning.turning(v2, controlState.dataCompressionFactor);
        stepout.rightVentricularVolume = Turning.turning(v3, controlState.dataCompressionFactor);
        stepout.pulmonaryArterialVolume = Turning.turning(v4, controlState.dataCompressionFactor);
        stepout.pulmonaryVenousVolume = Turning.turning(v5, controlState.dataCompressionFactor);

        stepout.heartRate = Turning.turning(hr, controlState.dataCompressionFactor);
        stepout.arteriolarResistance = Turning.turning(ar, controlState.dataCompressionFactor);
        stepout.venousTone = Turning.turning(vt, controlState.dataCompressionFactor);
        stepout.rightVentricleContractility = Turning.turning(rvc, controlState.dataCompressionFactor);
        stepout.leftVentricleContractility = Turning.turning(lvc, controlState.dataCompressionFactor);
        stepout.totalBloodVolume = Turning.turning(tbv, controlState.dataCompressionFactor);
        stepout.IntraThoracicPressure = Turning.turning(pth, controlState.dataCompressionFactor);
    }

    public void reset_sim() {
        // The following lines either reset variables to their initial values or
        // call functions that do the same in other files. This resets the entire
        // simulation subroutine to its initial state such that two consecutive
        // simulations start with the same numeric parameters.
        //  numerics_reset();
        reflex.queue_reset();
    }

// Total blood volume update constraint
// Pv,new = Pv,old + (Vtot,new - Vtot,old) / Cv
    public void updateTotalBloodVolume(double tbv_new, Parameters parameters) {
        // venous compliance
        double Cv = parameters.get(ParameterName.VEN_COMPLIANCE);
        // central venous pressure
        double Pv_old = pressure.pressure[CENTRAL_VENOUS_CPI];
        // old total blood volume 
        double tbv_old = parameters.get(ParameterName.TOTAL_BLOOD_VOLUME);
        // new total blood volume
        parameters.put(ParameterName.TOTAL_BLOOD_VOLUME, tbv_new);
        // recalculate central venous pressure
        pressure.pressure[CENTRAL_VENOUS_CPI] = Pv_old + (tbv_new - tbv_old) / Cv;

        System.out.printf("Pv,new = Pv,old + (Vtot,new - Vtot,old) / Cv\n");
        System.out.printf("Pv,new: %.2f, Pv,old: %.2f, Vtot,new: %.2f, Vtot,old: %.2f, Cv: %.2f\n", pressure.pressure[CENTRAL_VENOUS_CPI], Pv_old, tbv_new, tbv_old, Cv);
    }

// Total zero pressure filling volume update constraint
// Pv,new = Pv,old + (V0,old - V0,new) / Cv
    public void updateTotalZeroPressureFillingVolume(double zpfv_new, Parameters parameters) {
        // venous compliance
        double Cv = parameters.get(ParameterName.VEN_COMPLIANCE);
        // central venous pressure
        double Pv_old = pressure.pressure[CENTRAL_VENOUS_CPI];
        // old total zero pressure filling volume
        double zpfv_old = parameters.get(ParameterName.TOTAL_ZPFV);
        // new total zero pressure filling volume
        parameters.put(ParameterName.TOTAL_ZPFV, zpfv_new);
        // recalculate central venous pressure
        pressure.pressure[CENTRAL_VENOUS_CPI] = Pv_old + (zpfv_old - zpfv_new) / Cv;

        System.out.printf("Pv,new = Pv,old + (V0,old - V0,new) / Cv\n");
        System.out.printf("Pv,new: %.2f, Pv,old: %.2f, V0,old: %.2f, V0,new: %.2f, Cv: %.2f\n", pressure.pressure[CENTRAL_VENOUS_CPI], Pv_old, zpfv_old, zpfv_new, Cv);
    }

// Intra-thoracic pressure update constraint
// For all thoracic compartments.
// P,new = P,old + (Pth,new - Pth,old)
    public void updateIntrathoracicPressure(double Pth_new, Parameters parameters) {
        double P_old;
        System.out.printf("P,new = P,old + (Pth,new - Pth,old)\n");

        // old intra-thoracic pressure
        double Pth_old = parameters.get(ParameterName.INTRA_THORACIC_PRESSURE);
        // new intra-thoracic pressure
        parameters.put(ParameterName.INTRA_THORACIC_PRESSURE, Pth_new);
        pressure.pressure[INTRA_THORACIC_CPI] = Pth_new;

        // left ventricle pressure
        P_old = pressure.pressure[LEFT_VENTRICULAR_CPI];
        // recalculate left ventricle pressure
        pressure.pressure[LEFT_VENTRICULAR_CPI] = P_old + Pth_new - Pth_old;
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n", pressure.pressure[LEFT_VENTRICULAR_CPI], P_old, Pth_new, Pth_old);

        // right ventricle pressure
        P_old = pressure.pressure[RIGHT_VENTRICULAR_CPI];
        // recalculate right ventricle pressure
        pressure.pressure[RIGHT_VENTRICULAR_CPI] = P_old + Pth_new - Pth_old;
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n", pressure.pressure[RIGHT_VENTRICULAR_CPI], P_old, Pth_new, Pth_old);

        // pulmonary arterial pressure
        P_old = pressure.pressure[PULMONARY_ARTERIAL_CPI];
        // recalculate pulmonary arterial pressure
        pressure.pressure[PULMONARY_ARTERIAL_CPI] = P_old + Pth_new - Pth_old;
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n", pressure.pressure[PULMONARY_ARTERIAL_CPI], P_old, Pth_new, Pth_old);

        // pulmonary venous pressure
        P_old = pressure.pressure[PULMONARY_VENOUS_CPI];
        // recalculate pulmonary venous pressure
        pressure.pressure[PULMONARY_VENOUS_CPI] = P_old + Pth_new - Pth_old;
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n", pressure.pressure[PULMONARY_VENOUS_CPI], P_old, Pth_new, Pth_old);
    }

// Compliance parameter update constraint
// P,new = P,old * C,old / C,new + Pth * (1 - C,old / C,new)
    public void updatePulmonaryArterialCompliance(double C_new, Parameters parameters) {
        // old pulmonary arterial compliance
        double C_old = parameters.get(ParameterName.PULM_ART_COMPLIANCE);
        // new pulmonary arterial compliance
        parameters.put(ParameterName.PULM_ART_COMPLIANCE, C_new);
        // old intra-thoracic pressure
        double Pth = parameters.get(ParameterName.INTRA_THORACIC_PRESSURE);
        // old pulmonary arterial pressure
        double P_old = pressure.pressure[PULMONARY_ARTERIAL_CPI];
        // new pulomary arterial compliance
        pressure.pressure[PULMONARY_ARTERIAL_CPI] = P_old * C_old / C_new + Pth * (1 - C_old / C_new);
        System.out.printf("P,new = P,old * C,old / C,new\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, C,old: %.2f, C,new: %.2f\n", pressure.pressure[PULMONARY_ARTERIAL_CPI], P_old, C_old, C_new);
    }

// Compliance parameter update constraint
// P,new = P,old * C,old / C,new + Pth * (1 - C,old / C,new)
    public void updatePulmonaryVenousCompliance(double C_new, Parameters parameters) {
        // old pulmonary arterial compliance
        double C_old = parameters.get(ParameterName.PULM_VEN_COMPLIANCE);
        // new pulmonary arterial compliance
        parameters.put(ParameterName.PULM_VEN_COMPLIANCE, C_new);
        // old intra-thoracic pressure
        double Pth = parameters.get(ParameterName.INTRA_THORACIC_PRESSURE);
        // old pulmonary arterial pressure
        double P_old = pressure.pressure[PULMONARY_VENOUS_CPI];
        // new pulomary arterial compliance
        pressure.pressure[PULMONARY_VENOUS_CPI] = P_old * C_old / C_new + Pth * (1 - C_old / C_new);
        System.out.printf("P,new = P,old * C,old / C,new\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, C,old: %.2f, C,new: %.2f\n", pressure.pressure[PULMONARY_VENOUS_CPI], P_old, C_old, C_new);
    }

// Compliance parameter update constraint
// P,new = P,old * C,old / C,new
    public void updateArterialCompliance(double C_new, Parameters parameters) {
        // old pulmonary arterial compliance
        double C_old = parameters.get(ParameterName.ART_COMPLIANCE);
        // new pulmonary arterial compliance
        parameters.put(ParameterName.ART_COMPLIANCE, C_new);
        // old pulmonary arterial pressure
        double P_old = pressure.pressure[ARTERIAL_CPI];
        // new pulomary arterial compliance
        pressure.pressure[ARTERIAL_CPI] = P_old * C_old / C_new;
        System.out.printf("P,new = P,old * C,old / C,new\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, C,old: %.2f, C,new: %.2f\n", pressure.pressure[ARTERIAL_CPI], P_old, C_old, C_new);
    }

// Compliance parameter update constraint
// P,new = P,old * C,old / C,new
    public void updateVenousCompliance(double C_new, Parameters parameters) {
        // old pulmonary arterial compliance
        double C_old = parameters.get(ParameterName.VEN_COMPLIANCE);
        // new pulmonary arterial compliance
        parameters.put(ParameterName.VEN_COMPLIANCE, C_new);
        // old pulmonary arterial pressure
        double P_old = pressure.pressure[CENTRAL_VENOUS_CPI];
        // new pulomary arterial compliance
        pressure.pressure[CENTRAL_VENOUS_CPI] = P_old * C_old / C_new;
        System.out.printf("P,new = P,old * C,old / C,new\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, C,old: %.2f, C,new: %.2f\n", pressure.pressure[CENTRAL_VENOUS_CPI], P_old, C_old, C_new);
    }

// Update function for parameters with no update constraints
    public void updateParameter(double newValue, Parameters parameters, ParameterName index) {
        parameters.put(index, newValue);
    }

}
