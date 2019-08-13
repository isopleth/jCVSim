package jcvsim.compartment21;

import static java.lang.Math.PI;
import jcvsim.common.ControlState;
import static jcvsim.common.Maths.tan;
import jcvsim.common.Output_vector;
import jcvsim.common.Reflex_vector;
import jcvsim.common.Turning;
import static jcvsim.compartment21.Data_vector.CompartmentIndex.*;
import static jcvsim.compartment21.Data_vector.TimeIndex.*;

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

    // Structure handling the flow of data within the simulation.c routine.
    // See simulator.h for the definition.
    private final Data_vector dataVector = new Data_vector();

    // Structure for the reflex variables.
    private final Reflex_vector reflexVector = new Reflex_vector();

    // Outputs from the simulator
    private final Output_vector out = new Output_vector();

    private final Reflex reflex;

    // The following definitions pertain to the adaptive stepsize integration
    // routine. See Numerical Recipes in C (p. ???) for details.
    // The single element arrays are used to allow the RK method to set multiple
    // return values.
    private final double[] hdid = {0.0};     // hdid[0] is set by the RK method to the stepsize that was accomplished
    private double htry = 0.00001;

    private final double[] hnext = {0.0};    // hnext[0] is set to the RK method to the estimated next stepsize 

    private final Parameters parameterVector;

    public Simulation(Parameters vector) {
        parameterVector = vector;

        // Initialize the parameter structures and the parameter vector.
        new Initial().mapping_ptr(parameterVector);

        reflex = new Reflex(parameterVector);

        // The following function calls initialize the data vector. Estimate()
        // initializes the pressure.x[] and pressure.time[] arrays. Elastance()
        // computes the cardiac compliances and their derivatives and stores their
        // values in pressure.c[] and pressure.dcdt[], respectively. Finally,
        // eqns() computes the pressure derivatives and stores them in 
        // pressure.dxdt[].
        Estimate.estimate_ptr(dataVector, parameterVector, reflexVector);      // estimate initial pressures

        // Set up the values for the time-varying capacitance values
        Equation.elastance_ptr(dataVector, parameterVector);

        Equation.fixvolume(dataVector, reflexVector, new IntraThoracicPressure(parameterVector), parameterVector);     // fix the total blood volume

        Equation.eqns_ptr(dataVector, parameterVector, reflexVector,
                new IntraThoracicPressure(parameterVector), new ControlState()); // Initialize pressure derivatives

        Simulator_numerics.numerics(dataVector, reflexVector, out, parameterVector);

    }

    // This function is needed to update pressure.x[] from the Java code.
    // The output struct copies the values in pressure.x and makes them available to 
    // the Java code. The output struct is in a sense read-only. If the values in the 
    // output struct are changed, the change is not reflected in pressure.x. However, 
    // occasionally (as in the case of parameter updates), information must pass from
    // the GUI to the simulation variables. This function was created to allow 
    // the changes to output.x to be reflected in pressure.x.
    public void updatePressure(int index, double value) {
        dataVector.pressure[index] = value;
    }

// step_sim(): Advances the simulation one timestep
// By varying the dataCompressionFactor the data can be compressed before 
// being passed to the GUI. A dataCompressionFactor of 10 means one piece 
// of output data is passed to the GUI for every 10 timesteps. A 
// dataCompressionFactor of 1 means every piece of data is passed to the GUI.
// (One piece of data for every timestep.)
    public void step(Output stepout, Parameters pvec, ControlState controlState) {

        double[] ascendingAorticPressure = new double[controlState.dataCompressionFactor];
        double[] brachiocephalicArterialPressure = new double[controlState.dataCompressionFactor];
        double[] upperBodyArterialPressure = new double[controlState.dataCompressionFactor];
        double[] upperBodyVenousPressure = new double[controlState.dataCompressionFactor];
        double[] superiorVenaCavaPressure = new double[controlState.dataCompressionFactor];
        double[] thoracicAorticPressure = new double[controlState.dataCompressionFactor];
        double[] abdominalAorticPressure = new double[controlState.dataCompressionFactor];
        double[] renalArterialPressure = new double[controlState.dataCompressionFactor];
        double[] renalVenousPressure = new double[controlState.dataCompressionFactor];
        double[] splanchnicArterialPressure = new double[controlState.dataCompressionFactor];
        double[] splanchnicVenousPressure = new double[controlState.dataCompressionFactor];
        double[] lowerBodyArterialPressure = new double[controlState.dataCompressionFactor];
        double[] lowerBodyVenousPressure = new double[controlState.dataCompressionFactor];
        double[] abdominalVenousPressure = new double[controlState.dataCompressionFactor];
        double[] inferiorVenaCavaPressure = new double[controlState.dataCompressionFactor];
        double[] rightAtrialPressure = new double[controlState.dataCompressionFactor];
        double[] rightVentricularPressure = new double[controlState.dataCompressionFactor];
        double[] pulmonaryArterialPressure = new double[controlState.dataCompressionFactor];
        double[] pulmonaryVenousPressure = new double[controlState.dataCompressionFactor];
        double[] leftAtrialPressure = new double[controlState.dataCompressionFactor];
        double[] leftVentricularPressure = new double[controlState.dataCompressionFactor];

        double[] ascendingAorticFlow = new double[controlState.dataCompressionFactor];
        double[] brachiocephalicArterialFlow = new double[controlState.dataCompressionFactor];
        double[] upperBodyArterialFlow = new double[controlState.dataCompressionFactor];
        double[] upperBodyVenousFlow = new double[controlState.dataCompressionFactor];
        double[] superiorVenaCavaFlow = new double[controlState.dataCompressionFactor];
        double[] thoracicAorticFlow = new double[controlState.dataCompressionFactor];
        double[] abdominalAorticFlow = new double[controlState.dataCompressionFactor];
        double[] renalArterialFlow = new double[controlState.dataCompressionFactor];
        double[] renalVenousFlow = new double[controlState.dataCompressionFactor];
        double[] splanchnicArterialFlow = new double[controlState.dataCompressionFactor];
        double[] splanchnicVenousFlow = new double[controlState.dataCompressionFactor];
        double[] lowerBodyArterialFlow = new double[controlState.dataCompressionFactor];
        double[] lowerBodyVenousFlow = new double[controlState.dataCompressionFactor];
        double[] abdominalVenousFlow = new double[controlState.dataCompressionFactor];
        double[] inferiorVenaCavaFlow = new double[controlState.dataCompressionFactor];
        double[] rightAtrialFlow = new double[controlState.dataCompressionFactor];
        double[] rightVentricularFlow = new double[controlState.dataCompressionFactor];
        double[] pulmonaryArterialFlow = new double[controlState.dataCompressionFactor];
        double[] pulmonaryVenousFlow = new double[controlState.dataCompressionFactor];
        double[] leftAtrialFlow = new double[controlState.dataCompressionFactor];
        double[] leftVentricularFlow = new double[controlState.dataCompressionFactor];

        double[] ascendingAorticVolume = new double[controlState.dataCompressionFactor];
        double[] brachiocephalicArterialVolume = new double[controlState.dataCompressionFactor];
        double[] upperBodyArterialVolume = new double[controlState.dataCompressionFactor];
        double[] upperBodyVenousVolume = new double[controlState.dataCompressionFactor];
        double[] superiorVenaCavaVolume = new double[controlState.dataCompressionFactor];
        double[] thoracicAorticVolume = new double[controlState.dataCompressionFactor];
        double[] abdominalAorticVolume = new double[controlState.dataCompressionFactor];
        double[] renalArterialVolume = new double[controlState.dataCompressionFactor];
        double[] renalVenousVolume = new double[controlState.dataCompressionFactor];
        double[] splanchnicArterialVolume = new double[controlState.dataCompressionFactor];
        double[] splanchnicVenousVolume = new double[controlState.dataCompressionFactor];
        double[] lowerBodyArterialVolume = new double[controlState.dataCompressionFactor];
        double[] lowerBodyVenousVolume = new double[controlState.dataCompressionFactor];
        double[] abdominalVenousVolume = new double[controlState.dataCompressionFactor];
        double[] inferiorVenaCavaVolume = new double[controlState.dataCompressionFactor];
        double[] rightAtrialVolume = new double[controlState.dataCompressionFactor];
        double[] rightVentricularVolume = new double[controlState.dataCompressionFactor];
        double[] pulmonaryArterialVolume = new double[controlState.dataCompressionFactor];
        double[] pulmonaryVenousVolume = new double[controlState.dataCompressionFactor];
        double[] leftAtrialVolume = new double[controlState.dataCompressionFactor];
        double[] leftVentricularVolume = new double[controlState.dataCompressionFactor];

        double[] hr = new double[controlState.dataCompressionFactor];
        double[] ar = new double[controlState.dataCompressionFactor];
        double[] vt = new double[controlState.dataCompressionFactor];
        double[] rvc = new double[controlState.dataCompressionFactor];
        double[] lvc = new double[controlState.dataCompressionFactor];

        double[] totalBloodVolume = new double[controlState.dataCompressionFactor];
        double[] intraThoracicPressureResult = new double[controlState.dataCompressionFactor];
        double[] tidalLungVolume = new double[controlState.dataCompressionFactor];

        double[] tiltAngle = new double[controlState.dataCompressionFactor];

        IntraThoracicPressure intraThoracicPressure = new IntraThoracicPressure(controlState, pvec,
                dataVector);

        // Calculate output values
        for (int index = 0; index < controlState.dataCompressionFactor; index++) {

            // Now adjust the results for the effect of breathing on the ITh pressure
            // hdid[0] and hnext[0] are outputs from this method
            // It is a Runge-Kutta integration step which controls the local error
            // to obtain a specified precision.  It controls the local error by
            // adjusting the timestep, which is consequently an output.
            // hdid[0] is the actual time step used and hnext[0] is the time
            // step suggested for the next round. They are passed as single element
            // arrays as a bodge to make them pass-by-reference. 
            Rkqc.rkqc_ptr(dataVector, reflexVector, pvec, htry, 0.001, hdid, hnext,
                    intraThoracicPressure, controlState, reflex);

            dataVector.time[SECONDS_INTO_CARDIAC_CYCLE] = reflex.sinoatrialNode(dataVector, reflexVector, pvec, hdid[0]);
            Equation.elastance_ptr(dataVector, pvec);
            Equation.eqns_ptr(dataVector, pvec, reflexVector, intraThoracicPressure, controlState);
            dataVector.time[SIMULATION_TIME] += hdid[0];

            //htry = (hnext > 0.001 ? 0.001 : hnext);
            htry = 0.001;
            reflex.queue(dataVector, reflexVector, pvec, hdid[0],
                    intraThoracicPressure, controlState);

            Equation.fixvolume(dataVector, reflexVector, intraThoracicPressure, pvec);

            Simulator_numerics_new.numerics_new_ptr(dataVector, reflexVector, hdid[0]);
            Simulator_numerics.numerics(dataVector, reflexVector, out, pvec);

            // simulation time
            stepout.time = dataVector.time[SIMULATION_TIME];

            ascendingAorticPressure[index] = dataVector.pressure[ASCENDING_AORTIC_CPI];
            brachiocephalicArterialPressure[index] = dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI];
            upperBodyArterialPressure[index] = dataVector.pressure[UPPER_BODY_ARTERIAL_CPI];
            upperBodyVenousPressure[index] = dataVector.pressure[UPPER_BODY_VENOUS_CPI];
            superiorVenaCavaPressure[index] = dataVector.pressure[SUPERIOR_VENA_CAVA_CPI];
            thoracicAorticPressure[index] = dataVector.pressure[THORACIC_AORTIC_CPI];
            abdominalAorticPressure[index] = dataVector.pressure[ABDOMINAL_AORTIC_CPI];
            renalArterialPressure[index] = dataVector.pressure[RENAL_ARTERIAL_CPI];
            renalVenousPressure[index] = dataVector.pressure[RENAL_VENOUS_CPI];
            splanchnicArterialPressure[index] = dataVector.pressure[SPLANCHNIC_ARTERIAL_CPI];
            splanchnicVenousPressure[index] = dataVector.pressure[SPLANCHNIC_VENOUS_CPI];
            lowerBodyArterialPressure[index] = dataVector.pressure[LBODY_ARTERIAL_CPI];
            lowerBodyVenousPressure[index] = dataVector.pressure[LBODY_VENOUS_CPI];
            abdominalVenousPressure[index] = dataVector.pressure[ABDOMINAL_VENOUS_CPI];
            inferiorVenaCavaPressure[index] = dataVector.pressure[INFERIOR_VENA_CAVA_CPI];
            rightAtrialPressure[index] = dataVector.pressure[RIGHT_ATRIAL_CPI];
            rightVentricularPressure[index] = dataVector.pressure[RIGHT_VENTRICULAR_CPI];
            pulmonaryArterialPressure[index] = dataVector.pressure[PULMONARY_ARTERIAL_CPI];
            pulmonaryVenousPressure[index] = dataVector.pressure[PULMONARY_VENOUS_CPI];
            leftAtrialPressure[index] = dataVector.pressure[LEFT_ATRIAL_CPI];
            leftVentricularPressure[index] = dataVector.pressure[LEFT_VENTRICULAR_CPI];

            ascendingAorticFlow[index] = dataVector.flowRate[ASCENDING_AORTIC_CPI];
            brachiocephalicArterialFlow[index] = dataVector.flowRate[BRACHIOCEPHALIC_ARTERIAL_CPI];
            upperBodyArterialFlow[index] = dataVector.flowRate[UPPER_BODY_ARTERIAL_CPI];
            upperBodyVenousFlow[index] = dataVector.flowRate[UPPER_BODY_VENOUS_CPI];
            superiorVenaCavaFlow[index] = dataVector.flowRate[SUPERIOR_VENA_CAVA_CPI];
            thoracicAorticFlow[index] = dataVector.flowRate[THORACIC_AORTIC_CPI];
            abdominalAorticFlow[index] = dataVector.flowRate[ABDOMINAL_AORTIC_CPI];
            renalArterialFlow[index] = dataVector.flowRate[RENAL_ARTERIAL_CPI];
            renalVenousFlow[index] = dataVector.flowRate[RENAL_VENOUS_CPI];
            splanchnicArterialFlow[index] = dataVector.flowRate[SPLANCHNIC_ARTERIAL_CPI];
            splanchnicVenousFlow[index] = dataVector.flowRate[SPLANCHNIC_VENOUS_CPI];
            lowerBodyArterialFlow[index] = dataVector.flowRate[LBODY_ARTERIAL_CPI];
            lowerBodyVenousFlow[index] = dataVector.flowRate[LBODY_VENOUS_CPI];
            abdominalVenousFlow[index] = dataVector.flowRate[ABDOMINAL_VENOUS_CPI];
            inferiorVenaCavaFlow[index] = dataVector.flowRate[INFERIOR_VENA_CAVA_CPI];
            rightAtrialFlow[index] = dataVector.flowRate[RIGHT_ATRIAL_CPI];
            rightVentricularFlow[index] = dataVector.flowRate[RIGHT_VENTRICULAR_CPI];
            pulmonaryArterialFlow[index] = dataVector.flowRate[PULMONARY_ARTERIAL_CPI];
            pulmonaryVenousFlow[index] = dataVector.flowRate[PULMONARY_VENOUS_CPI];
            leftAtrialFlow[index] = dataVector.flowRate[LEFT_ATRIAL_CPI];
            leftVentricularFlow[index] = dataVector.flowRate[LEFT_VENTRICULAR_CPI];

            ascendingAorticVolume[index] = dataVector.volume[ASCENDING_AORTIC_CPI];
            brachiocephalicArterialVolume[index] = dataVector.volume[BRACHIOCEPHALIC_ARTERIAL_CPI];
            upperBodyArterialVolume[index] = dataVector.volume[UPPER_BODY_ARTERIAL_CPI];
            upperBodyVenousVolume[index] = dataVector.volume[UPPER_BODY_VENOUS_CPI];
            superiorVenaCavaVolume[index] = dataVector.volume[SUPERIOR_VENA_CAVA_CPI];
            thoracicAorticVolume[index] = dataVector.volume[THORACIC_AORTIC_CPI];
            abdominalAorticVolume[index] = dataVector.volume[ABDOMINAL_AORTIC_CPI];
            renalArterialVolume[index] = dataVector.volume[RENAL_ARTERIAL_CPI];
            renalVenousVolume[index] = dataVector.volume[RENAL_VENOUS_CPI];
            splanchnicArterialVolume[index] = dataVector.volume[SPLANCHNIC_ARTERIAL_CPI];
            splanchnicVenousVolume[index] = dataVector.volume[SPLANCHNIC_VENOUS_CPI];
            lowerBodyArterialVolume[index] = dataVector.volume[LBODY_ARTERIAL_CPI];
            lowerBodyVenousVolume[index] = dataVector.volume[LBODY_VENOUS_CPI];
            abdominalVenousVolume[index] = dataVector.volume[ABDOMINAL_VENOUS_CPI];
            inferiorVenaCavaVolume[index] = dataVector.volume[INFERIOR_VENA_CAVA_CPI];
            rightAtrialVolume[index] = dataVector.volume[RIGHT_ATRIAL_CPI];
            rightVentricularVolume[index] = dataVector.volume[RIGHT_VENTRICULAR_CPI];
            pulmonaryArterialVolume[index] = dataVector.volume[PULMONARY_ARTERIAL_CPI];
            pulmonaryVenousVolume[index] = dataVector.volume[PULMONARY_VENOUS_CPI];
            leftAtrialVolume[index] = dataVector.volume[LEFT_ATRIAL_CPI];
            leftVentricularVolume[index] = dataVector.volume[LEFT_VENTRICULAR_CPI];

            hr[index] = reflexVector.instantaneousHeartRate;
            ar[index] = reflexVector.resistance[0];
            vt[index] = reflexVector.volume[0];
            rvc[index] = reflexVector.rvEndSystolicCompliance;
            lvc[index] = reflexVector.lvEndSystolicCompliance;

            totalBloodVolume[index] = pvec.get(ParameterName.TOTAL_BLOOD_VOLUME);

            intraThoracicPressureResult[index] = intraThoracicPressure.getValue();
            tidalLungVolume[index] = intraThoracicPressure.getTidalLungVolume();

            tiltAngle[index] = dataVector.tilt_angle;

        } // end for

        // Compress the output by applying turning algorithm
        stepout.ascendingAorticPressure = Turning.turning(ascendingAorticPressure, controlState.dataCompressionFactor);
        stepout.brachiocephalicArterialPressure = Turning.turning(brachiocephalicArterialPressure, controlState.dataCompressionFactor);
        stepout.upperBodyArterialPressure = Turning.turning(upperBodyArterialPressure, controlState.dataCompressionFactor);
        stepout.upperBodyVenousPressure = Turning.turning(upperBodyVenousPressure, controlState.dataCompressionFactor);
        stepout.superiorVenaCavaPressure = Turning.turning(superiorVenaCavaPressure, controlState.dataCompressionFactor);
        stepout.thoracicAorticPressure = Turning.turning(thoracicAorticPressure, controlState.dataCompressionFactor);
        stepout.abdominalAorticPressure = Turning.turning(abdominalAorticPressure, controlState.dataCompressionFactor);
        stepout.renalArterialPressure = Turning.turning(renalArterialPressure, controlState.dataCompressionFactor);
        stepout.renalVenousPressure = Turning.turning(renalVenousPressure, controlState.dataCompressionFactor);
        stepout.splanchnicArterialPressure = Turning.turning(splanchnicArterialPressure, controlState.dataCompressionFactor);
        stepout.splanchnicVenousPressure = Turning.turning(splanchnicVenousPressure, controlState.dataCompressionFactor);
        stepout.lowerBodyArterialPressure = Turning.turning(lowerBodyArterialPressure, controlState.dataCompressionFactor);
        stepout.lowerBodyVenousPressure = Turning.turning(lowerBodyVenousPressure, controlState.dataCompressionFactor);
        stepout.abdominalVenousPressure = Turning.turning(abdominalVenousPressure, controlState.dataCompressionFactor);
        stepout.inferiorVenaCavaPressure = Turning.turning(inferiorVenaCavaPressure, controlState.dataCompressionFactor);
        stepout.rightAtrialPressure = Turning.turning(rightAtrialPressure, controlState.dataCompressionFactor);
        stepout.rightVentricularPressure = Turning.turning(rightVentricularPressure, controlState.dataCompressionFactor);
        stepout.pulmonaryArterialPressure = Turning.turning(pulmonaryArterialPressure, controlState.dataCompressionFactor);
        stepout.pulmonaryVenousPressure = Turning.turning(pulmonaryVenousPressure, controlState.dataCompressionFactor);
        stepout.leftAtrialPressure = Turning.turning(leftAtrialPressure, controlState.dataCompressionFactor);
        stepout.leftVentricularPressure = Turning.turning(leftVentricularPressure, controlState.dataCompressionFactor);

        stepout.ascendingAorticFlow = Turning.turning(ascendingAorticFlow, controlState.dataCompressionFactor);
        stepout.brachiocephalicArterialFlow = Turning.turning(brachiocephalicArterialFlow, controlState.dataCompressionFactor);
        stepout.upperBodyArterialFlow = Turning.turning(upperBodyArterialFlow, controlState.dataCompressionFactor);
        stepout.upperBodyVenousFlow = Turning.turning(upperBodyVenousFlow, controlState.dataCompressionFactor);
        stepout.superiorVenaCavaFlow = Turning.turning(superiorVenaCavaFlow, controlState.dataCompressionFactor);
        stepout.thoracicAorticFlow = Turning.turning(thoracicAorticFlow, controlState.dataCompressionFactor);
        stepout.abdominalAorticFlow = Turning.turning(abdominalAorticFlow, controlState.dataCompressionFactor);
        stepout.renalArterialFlow = Turning.turning(renalArterialFlow, controlState.dataCompressionFactor);
        stepout.renalVenousFlow = Turning.turning(renalVenousFlow, controlState.dataCompressionFactor);
        stepout.splanchnicArterialFlow = Turning.turning(splanchnicArterialFlow, controlState.dataCompressionFactor);
        stepout.splanchnicVenousFlow = Turning.turning(splanchnicVenousFlow, controlState.dataCompressionFactor);
        stepout.lowerBodyArterialFlow = Turning.turning(lowerBodyArterialFlow, controlState.dataCompressionFactor);
        stepout.lowerBodyVenousFlow = Turning.turning(lowerBodyVenousFlow, controlState.dataCompressionFactor);
        stepout.abdominalVenousFlow = Turning.turning(abdominalVenousFlow, controlState.dataCompressionFactor);
        stepout.inferiorVenaCavaFlow = Turning.turning(inferiorVenaCavaFlow, controlState.dataCompressionFactor);
        stepout.rightAtrialFlow = Turning.turning(rightAtrialFlow, controlState.dataCompressionFactor);
        stepout.rightVentricularFlow = Turning.turning(rightVentricularFlow, controlState.dataCompressionFactor);
        stepout.pulmonaryArterialFlow = Turning.turning(pulmonaryArterialFlow, controlState.dataCompressionFactor);
        stepout.pulmonaryVenousFlow = Turning.turning(pulmonaryVenousFlow, controlState.dataCompressionFactor);
        stepout.leftAtrialFlow = Turning.turning(leftAtrialFlow, controlState.dataCompressionFactor);
        stepout.leftVentricularFlow = Turning.turning(leftVentricularFlow, controlState.dataCompressionFactor);

        stepout.ascendingAorticVolume = Turning.turning(ascendingAorticVolume, controlState.dataCompressionFactor);
        stepout.brachiocephalicArterialVolume = Turning.turning(brachiocephalicArterialVolume, controlState.dataCompressionFactor);
        stepout.upperBodyArterialVolume = Turning.turning(upperBodyArterialVolume, controlState.dataCompressionFactor);
        stepout.upperBodyVenousVolume = Turning.turning(upperBodyVenousVolume, controlState.dataCompressionFactor);
        stepout.superiorVenaCavaVolume = Turning.turning(superiorVenaCavaVolume, controlState.dataCompressionFactor);
        stepout.thoracicAorticVolume = Turning.turning(thoracicAorticVolume, controlState.dataCompressionFactor);
        stepout.abdominalAorticVolume = Turning.turning(abdominalAorticVolume, controlState.dataCompressionFactor);
        stepout.renalArterialVolume = Turning.turning(renalArterialVolume, controlState.dataCompressionFactor);
        stepout.renalVenousVolume = Turning.turning(renalVenousVolume, controlState.dataCompressionFactor);
        stepout.splanchnicArterialVolume = Turning.turning(splanchnicArterialVolume, controlState.dataCompressionFactor);
        stepout.splanchnicVenousVolume = Turning.turning(splanchnicVenousVolume, controlState.dataCompressionFactor);
        stepout.lowerBodyArterialVolume = Turning.turning(lowerBodyArterialVolume, controlState.dataCompressionFactor);
        stepout.lowerBodyVenousVolume = Turning.turning(lowerBodyVenousVolume, controlState.dataCompressionFactor);
        stepout.abdominalVenousVolume = Turning.turning(abdominalVenousVolume, controlState.dataCompressionFactor);
        stepout.inferiorVenaCavaVolume = Turning.turning(inferiorVenaCavaVolume, controlState.dataCompressionFactor);
        stepout.rightAtrialVolume = Turning.turning(rightAtrialVolume, controlState.dataCompressionFactor);
        stepout.rightVentricularVolume = Turning.turning(rightVentricularVolume, controlState.dataCompressionFactor);
        stepout.pulmonaryArterialVolume = Turning.turning(pulmonaryArterialVolume, controlState.dataCompressionFactor);
        stepout.pulmonaryVenousVolume = Turning.turning(pulmonaryVenousVolume, controlState.dataCompressionFactor);
        stepout.leftAtrialVolume = Turning.turning(leftAtrialVolume, controlState.dataCompressionFactor);
        stepout.leftVentricularVolume = Turning.turning(leftVentricularVolume, controlState.dataCompressionFactor);

        stepout.HR = Turning.turning(hr, controlState.dataCompressionFactor);
        stepout.AR = Turning.turning(ar, controlState.dataCompressionFactor);
        stepout.VT = Turning.turning(vt, controlState.dataCompressionFactor);
        stepout.RVC = Turning.turning(rvc, controlState.dataCompressionFactor);
        stepout.LVC = Turning.turning(lvc, controlState.dataCompressionFactor);

        stepout.totalBloodVolume = Turning.turning(totalBloodVolume, controlState.dataCompressionFactor);
        stepout.intraThoracicPressure = Turning.turning(intraThoracicPressureResult, controlState.dataCompressionFactor);
        stepout.tidalLungVolume = Turning.turning(tidalLungVolume, controlState.dataCompressionFactor);
        
        stepout.tiltAngle = Turning.turning(tiltAngle, controlState.dataCompressionFactor);

    }

    public void reset_sim() {
        // The following lines either reset variables to their initial values or
        // call functions that do the same in other files. This resets the entire
        // simulation subroutine to its initial state such that two consecutive
        // simulations start with the same numeric parameters.
        //  numerics_reset();

        reflex.queue_reset();
    }

    // Total blood volume update equation
    // Delta P * C_splanchnicVenous = Delta V
    // where Delta P = tan ( Delta V * pi/2*V_max ) * (2*V_Max / pi * C_0 )
    public void updateTotalBloodVolume(double newTotalBloodVolume, Parameters pvec) {
        // splanchnic venous compliance
        double C0 = pvec.get(ParameterName.SPLAN_VEN_COMPLIANCE);
        // old total blood volume 
        double tbv_old = pvec.get(ParameterName.TOTAL_BLOOD_VOLUME);
        // new total blood volume
        pvec.put(ParameterName.TOTAL_BLOOD_VOLUME, newTotalBloodVolume);
        // delta v
        double deltaV = newTotalBloodVolume - tbv_old;
        // Vmax 
        double Vmax = pvec.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL);
        // delta p
        double deltaP = tan((deltaV * PI) / (2 * Vmax)) * (2 * Vmax) / (PI * C0);
        // splanchnic pressure
        double P_old = dataVector.pressure[SPLANCHNIC_VENOUS_CPI];
        // update splanchnic pressure
        dataVector.pressure[SPLANCHNIC_VENOUS_CPI] = P_old + deltaP;
    }

// Zero pressure filling volume update equation
// P,new = P,old + (V0,old - V0,new) / C
    /**
     * Update zero pressure filling volume for a specified compartment. Adjusts
     * the pressure in the compartment at the same time
     *
     * @param newZpfv new ZPFV
     * @param pvec data vector
     * @param zpfvName ZPFV name for this compartment in pvec
     * @param complianceName compliance name for this compartment in pvec
     * @param dataVectorIndex index of the pressure for this compartment in
     * (global) data vector
     */
    public void updateZeroPressureFillingVolume(double newZpfv,
            Parameters pvec, ParameterName zpfvName,
            ParameterName complianceName, int dataVectorIndex) {
        // compliance
        double compliance = pvec.get(complianceName);
        // pressure
        double oldPressure = dataVector.pressure[dataVectorIndex];
        // old zero pressure filling volume
        double oldZpfv = pvec.get(zpfvName);
        // new zero pressure filling volume
        pvec.put(zpfvName, newZpfv);
        // recalculate pressure
        dataVector.pressure[dataVectorIndex] = oldPressure + (oldZpfv - newZpfv) / compliance;

        System.out.printf("P,new = P,old + (V0,old - V0,new) / C\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, V0,old: %.2f, V0,new: %.2f, C: %.2f\n",
                dataVector.pressure[dataVectorIndex], oldPressure, oldZpfv, newZpfv, compliance);
    }

// Intrathoracic pressure update equation
// P,new = P,old + (Pth,new - Pth,old)
    /**
     * Update intra-thoracic pressure, adjusting the pressures of all of the
     * compartments in the thorax
     *
     * @param newIntraThoracicPressure
     * @param pvec parameter vector
     */
    public void updateIntrathoracicPressure(double newIntraThoracicPressure, Parameters pvec) {
        double oldPressure;
        System.out.printf("updateIntrathoracicPressure()\n");
        System.out.printf("P,new = P,old + (Pth,new - Pth,old)\n");

        // old intra-thoracic pressure
        double oldIntraThoracicPressure = pvec.get(ParameterName.INTRA_THORACIC_PRESSURE);
        // new intra-thoracic pressure
        pvec.put(ParameterName.INTRA_THORACIC_PRESSURE, newIntraThoracicPressure);

        // update pressure for all compartments inside the thorax
        // ascending aorta
        oldPressure = dataVector.pressure[ASCENDING_AORTIC_CPI];
        dataVector.pressure[ASCENDING_AORTIC_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update ascending aortic pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[ASCENDING_AORTIC_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // brachiocephalic
        oldPressure = dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI];
        dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update brachiocephalic arterial pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // superior vena cava
        oldPressure = dataVector.pressure[SUPERIOR_VENA_CAVA_CPI];
        dataVector.pressure[SUPERIOR_VENA_CAVA_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update superior vena cava pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[SUPERIOR_VENA_CAVA_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // thoracic aorta
        oldPressure = dataVector.pressure[THORACIC_AORTIC_CPI];
        dataVector.pressure[THORACIC_AORTIC_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update thoracic aortic pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[THORACIC_AORTIC_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // inferior vena cava
        oldPressure = dataVector.pressure[INFERIOR_VENA_CAVA_CPI];
        dataVector.pressure[INFERIOR_VENA_CAVA_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update inferior vena cava pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[INFERIOR_VENA_CAVA_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // right atrial
        oldPressure = dataVector.pressure[RIGHT_ATRIAL_CPI];
        dataVector.pressure[RIGHT_ATRIAL_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update right atrial pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[RIGHT_ATRIAL_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // right ventricular
        oldPressure = dataVector.pressure[RIGHT_VENTRICULAR_CPI];
        dataVector.pressure[RIGHT_VENTRICULAR_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update right ventricular pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[RIGHT_VENTRICULAR_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // pulmonary arterial
        oldPressure = dataVector.pressure[PULMONARY_ARTERIAL_CPI];
        dataVector.pressure[PULMONARY_ARTERIAL_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update pulmonary arterial pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[PULMONARY_ARTERIAL_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // pulmonary venous
        oldPressure = dataVector.pressure[PULMONARY_VENOUS_CPI];
        dataVector.pressure[PULMONARY_VENOUS_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update pulmonary venous pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[PULMONARY_VENOUS_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // left atrial
        oldPressure = dataVector.pressure[LEFT_ATRIAL_CPI];
        dataVector.pressure[LEFT_ATRIAL_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update left atrial pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[LEFT_ATRIAL_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

        // left ventricular
        oldPressure = dataVector.pressure[LEFT_VENTRICULAR_CPI];
        dataVector.pressure[LEFT_VENTRICULAR_CPI] = oldPressure + newIntraThoracicPressure - oldIntraThoracicPressure;
        System.out.printf("Update left ventricular pressure\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, Pth,new: %.2f, Pth,old: %.2f\n",
                dataVector.pressure[LEFT_VENTRICULAR_CPI], oldPressure, newIntraThoracicPressure, oldIntraThoracicPressure);

    }

// Compliance update constraint for compartments inside the thorax
// P,new = P,old * C,old / C,new + Pth * (1 - C,old / C,new)
    /**
     * Adjust the compliance of a compartment located in the thorax. It
     * compensates the resulting pressure for the intra-thoracic compliance
     *
     * @param newCompliance new compliance value
     * @param pvec parameter vector
     * @param complianceName compartment compliance name
     * @param dataVectorIndex index of compartment pressure in data vector
     */
    public void updateComplianceInsideThorax(double newCompliance, Parameters pvec,
            ParameterName complianceName, int dataVectorIndex) {
        // old compliance
        double oldCompliance = pvec.get(complianceName);
        // new compliance
        pvec.put(complianceName, newCompliance);
        // intra-thoracic pressure
        double intraThoracicPressure = pvec.get(ParameterName.INTRA_THORACIC_PRESSURE);
        // old pressure
        double oldPressure = dataVector.pressure[dataVectorIndex];
        // new pressure
        dataVector.pressure[dataVectorIndex] = oldPressure * oldCompliance / newCompliance + intraThoracicPressure * (1 - oldCompliance / newCompliance);
        System.out.printf("updateComplianceInsideThorax()\n");
        System.out.printf("P,new = P,old * C,old / C,new + Pth * (1 - C,old / C,new)\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, C,old: %.2f, C,new: %.2f, Pth: %.2f\n",
                dataVector.pressure[dataVectorIndex], oldPressure, oldCompliance, newCompliance, intraThoracicPressure);
    }

// Compliance update constraint for compartments outside the thorax
// P,new = P,old * C,old / C,new
    /**
     * Adjust the compliance of a compartment located outside of the thorax. It
     * does not have to compensate the resulting pressure for the intra-thoracic
     * compliance
     *
     * @param newCompliance new compliance value
     * @param pvec parameter vector
     * @param complianceName compartment compliance name
     * @param dataVectorIndex index of compartment pressure in data vector
     */
    public void updateComplianceOutsideThorax(double newCompliance, Parameters pvec,
            ParameterName complianceName, int dataVectorIndex) {
        // old compliance
        double oldCompliance = pvec.get(complianceName);
        // new compliance
        pvec.put(complianceName, newCompliance);
        // old pressure
        double oldPressure = dataVector.pressure[dataVectorIndex];
        // new pressure
        dataVector.pressure[dataVectorIndex] = oldPressure * oldCompliance / newCompliance;
        System.out.printf("updateComplianceOutsideThorax()\n");
        System.out.printf("P,new = P,old * C,old / C,new\n");
        System.out.printf("P,new: %.2f, P,old: %.2f, C,old: %.2f, C,new: %.2f\n",
                dataVector.pressure[dataVectorIndex], oldPressure, oldCompliance, newCompliance);
    }

// Update function for parameters with no update constraints
    public void updateParameter(double newValue, Parameters pvec, ParameterName parameterName) {
        pvec.put(parameterName, newValue);
    }

}
