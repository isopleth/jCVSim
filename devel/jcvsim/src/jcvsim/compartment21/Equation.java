package jcvsim.compartment21;

import static java.lang.Math.PI;
import jcvsim.common.ControlState;
import static jcvsim.common.Maths.atan;
import static jcvsim.common.Maths.cos;
import static jcvsim.common.Maths.exp;
import static jcvsim.common.Maths.sin;
import jcvsim.common.Reflex_vector;
import static jcvsim.compartment21.Data_vector.CompartmentIndex.*;
import static jcvsim.compartment21.Data_vector.ComplianceIndex.*;
import static jcvsim.compartment21.Data_vector.TimeIndex.*;


/*
 * This file contains the subroutines necessary to evaluate the time-varying
 * capacitance values, their derivatives, the flows in the hemodynamic
 * system, and the pressure derivatives. Furthermore, it contains the volume
 * corrention routine.
 *
 * ALTHOUGH ELASTANCE() AND EQNS() ONLY REQUIRE A SMALL SUBSET OF ALL
 * PARAMETERS, CURRENTLY THE ENTIRE PARAMETER VECTOR IS PASSED TO THESE
 * ROUTINES AS ARGUMENTS. THIS CERTAINLY COMPROMISES PERFORMANCE AND
 * SHOULD BE RECONSIDERED IN FUTURE ITERATIONS OF THE PROGRAM.
 *
 *
 * Thomas Heldt March 30th, 2002
 * Last modified November 2, 2002
 */
// Converted to Java Jason Leake Decembr 2016
class Equation {

// Modifies ONLY Data_vector
    // Note by JLL :-
    // This method handles the pumping action of the heart.  In the model pumping
    // is implemented by dynamically changing the compliances of the four chambers
    // of the heart. It uses a set of different functions which are selected
    // according to the time into the cardiac cycle.  It actually works in terms
    // of elastances, and these are converted to compliances at the end of the
    // method.
    //
    // For the atria the elastances the functions are selected as follows:
    //
    // time < atrial systole time -- elastances appropriate for atrial contraction
    //
    // time < atrial systole time * 1.5 -- early atrial relaxation
    //
    // time > atrial systole time * 1.5 -- atrial relaxation
    //
    // And simultaneously for the ventricles the functions are selected as follows:
    //
    // time < ventricle contraction time -- fixed elastances
    //
    // time > venticle contraction time but less than ventricle systole time
    //
    // time > ventricle systole time until 1.5 x ventricle systole time
    //
    // time > 1.5 * ventricle systole time
    public static void elastance_ptr(Data_vector dataVector,
            Parameters parameterVector) {

        // These are the elastances, and their rates of change, for the four
        // compartments. They are the outputs from the method (and copied into
        // the appropriate Data_vector elements when computed)
        double elastanceLA = 0.0; // LA elastanace
        double elastanceRA = 0.0;
        double elastanceLV = 0.0;
        double elastanceRV = 0.0;
        double dElastanceLA = 0.0; // Rate of change of LA elastance
        double dElastanceRA = 0.0;
        double dElastanceLV = 0.0;
        double dElastanceRV = 0.0;

        // These are the inputs used in the algorithm
        final double timeFromStartOfCardiacCycle = dataVector.time[SECONDS_INTO_CARDIAC_CYCLE];
        //double PR_delay = p.time[2];  // unused
        final double timeOfAtrialSystole = dataVector.time[ATRIAL_SYSTOLE_TIME_OFFSET];
        final double timeOfVentricularSystole = dataVector.time[VENTRICULAR_SYSTOLE_TIME_OFFSET];

        final double raDiastolicCompliance = parameterVector.get(ParameterName.RA_DIASTOLIC_COMPLIANCE);
        final double raSystolicCompliance = parameterVector.get(ParameterName.RA_SYSTOLIC_COMPLIANCE);
        final double rvDiastolicCompliance = parameterVector.get(ParameterName.RV_DIASTOLIC_COMPLIANCE);
        final double rvEndSystoleCompliance = dataVector.compliance[RV_END_SYSTOLIC_COMPL];

        final double laDiastolicCompliance = parameterVector.get(ParameterName.LA_DIASTOLIC_COMPLIANCE);
        final double laSystolicCompliance = parameterVector.get(ParameterName.LA_SYSTOLIC_COMPLIANCE);
        final double lvDiastolicCompliance = parameterVector.get(ParameterName.LV_DIASTOLIC_COMPLIANCE);
        final double lvEndSystoleCompliance = dataVector.compliance[LV_END_SYSTOLIC_COMPL];

        final double v_time = dataVector.time[SECONDS_INTO_VENTRICULAR_CONTRACTION];

        if (timeFromStartOfCardiacCycle <= timeOfAtrialSystole) {
            // This is the calculation of the elastances before and up to
            // atrial systole
            elastanceLA = 0.5 * (1 / laSystolicCompliance - 1 / laDiastolicCompliance)
                    * (1 - cos(PI * timeFromStartOfCardiacCycle / timeOfAtrialSystole))
                    + 1 / laDiastolicCompliance;
            elastanceRA = 0.5 * (1 / raSystolicCompliance - 1 / raDiastolicCompliance)
                    * (1 - cos(PI * timeFromStartOfCardiacCycle / timeOfAtrialSystole))
                    + 1 / raDiastolicCompliance;
            dElastanceLA = 0.5 * PI * (1 / laSystolicCompliance - 1 / laDiastolicCompliance)
                    * sin(PI * timeFromStartOfCardiacCycle / timeOfAtrialSystole) / timeOfAtrialSystole;
            dElastanceRA = 0.5 * PI * (1 / raSystolicCompliance - 1 / raDiastolicCompliance)
                    * sin(PI * timeFromStartOfCardiacCycle / timeOfAtrialSystole) / timeOfAtrialSystole;
        } else if (timeFromStartOfCardiacCycle <= 1.5 * timeOfAtrialSystole) {
            // This represents the early atrial relaxation which follows just
            // after atrial systole.
            elastanceLA = 0.5 * (1 / laSystolicCompliance - 1 / laDiastolicCompliance)
                    * (1 + cos(2.0 * PI * (timeFromStartOfCardiacCycle - timeOfAtrialSystole) / timeOfAtrialSystole))
                    + 1 / laDiastolicCompliance;
            elastanceRA = 0.5 * (1 / raSystolicCompliance - 1 / raDiastolicCompliance)
                    * (1 + cos(2.0 * PI * (timeFromStartOfCardiacCycle - timeOfAtrialSystole) / timeOfAtrialSystole))
                    + 1 / raDiastolicCompliance;
            dElastanceLA = -1.0 * PI * (1 / laSystolicCompliance - 1 / laDiastolicCompliance)
                    * sin(2.0 * PI * (timeFromStartOfCardiacCycle - timeOfAtrialSystole) / timeOfAtrialSystole) / timeOfAtrialSystole;
            dElastanceRA = -1.0 * PI * (1 / raSystolicCompliance - 1 / raDiastolicCompliance)
                    * sin(2.0 * PI * (timeFromStartOfCardiacCycle - timeOfAtrialSystole) / timeOfAtrialSystole) / timeOfAtrialSystole;
        } else {
            // This is the calculation of elastance after atrial systole as
            // the atria relax to atrial diastole
            elastanceLA = 1 / laDiastolicCompliance;
            elastanceRA = 1 / raDiastolicCompliance;
            dElastanceLA = 0.0;
            dElastanceRA = 0.0;
        }

        // Ventricular contraction.
        if (v_time <= 0.0) {
            // This is the time before ventricular contraction starts, i.e.
            // before the PR-interval has expired
            elastanceLV = 1 / lvDiastolicCompliance;
            elastanceRV = 1 / rvDiastolicCompliance;
            dElastanceLV = 0.0;
            dElastanceRV = 0.0;
        } else if (v_time <= timeOfVentricularSystole) {
            // This is the time from ventricular contraction start up to
            // ventricular systole when the contraction is at its maximum
            elastanceLV = 0.5 * (1 / lvEndSystoleCompliance - 1 / lvDiastolicCompliance)
                    * (1 - cos(PI * v_time / timeOfVentricularSystole))
                    + 1 / lvDiastolicCompliance;
            elastanceRV = 0.5 * (1 / rvEndSystoleCompliance - 1 / rvDiastolicCompliance)
                    * (1 - cos(PI * v_time / timeOfVentricularSystole))
                    + 1 / rvDiastolicCompliance;
            dElastanceLV = 0.5 * PI * (1 / lvEndSystoleCompliance - 1 / lvDiastolicCompliance)
                    * sin(PI * v_time / timeOfVentricularSystole) / timeOfVentricularSystole;
            dElastanceRV = 0.5 * PI * (1 / rvEndSystoleCompliance - 1 / rvDiastolicCompliance)
                    * sin(PI * v_time / timeOfVentricularSystole) / timeOfVentricularSystole;
        } else if (v_time <= 1.5 * timeOfVentricularSystole) {
            // This is the time shortly after ventricular systole when the
            // ventricles start to relax
            
            // I guess we would add a multiplier value for the Frank-Starling effect here - JLL
            
            elastanceLV = 0.5 * (1 / lvEndSystoleCompliance - 1 / lvDiastolicCompliance)
                    * (1 + cos(2.0 * PI * (v_time - timeOfVentricularSystole) / timeOfVentricularSystole))
                    + 1 / lvDiastolicCompliance;
            elastanceRV = 0.5 * (1 / rvEndSystoleCompliance - 1 / rvDiastolicCompliance)
                    * (1 + cos(2.0 * PI * (v_time - timeOfVentricularSystole) / timeOfVentricularSystole))
                    + 1 / rvDiastolicCompliance;
            dElastanceLV = -1.0 * PI * (1 / lvEndSystoleCompliance - 1 / lvDiastolicCompliance)
                    * sin(2.0 * PI * (v_time - timeOfVentricularSystole) / timeOfVentricularSystole) / timeOfVentricularSystole;
            dElastanceRV = -1.0 * PI * (1 / rvEndSystoleCompliance - 1 / rvDiastolicCompliance)
                    * sin(2.0 * PI * (v_time - timeOfVentricularSystole) / timeOfVentricularSystole) / timeOfVentricularSystole;
        } else {
            // This is the rest of the ventricular relaxation, to ventricular
            // diastole
            
           // I guess we would reset the accumulators for the ventricular diastole
           // volume for the Frank-Starling effect here - JLL
           
            elastanceLV = 1 / lvDiastolicCompliance;
            elastanceRV = 1 / rvDiastolicCompliance;
            dElastanceLV = 0.0;
            dElastanceRV = 0.0;
        }

        // Compliance is the reciprocal of the elastance
        dataVector.compliance[RA_COMPL] = 1 / elastanceRA;
        dataVector.compliance[RV_COMPL] = 1 / elastanceRV;
        dataVector.compliance[LA_COMPL] = 1 / elastanceLA;
        dataVector.compliance[LV_COMPL] = 1 / elastanceLV;

        dataVector.dComplianceDt[RA_COMPL] = -1.0 / (elastanceRA * elastanceRA) * dElastanceRA;
        dataVector.dComplianceDt[RV_COMPL] = -1.0 / (elastanceRV * elastanceRV) * dElastanceRV;
        dataVector.dComplianceDt[LA_COMPL] = -1.0 / (elastanceLA * elastanceLA) * dElastanceLA;
        dataVector.dComplianceDt[LV_COMPL] = -1.0 / (elastanceLV * elastanceLV) * dElastanceLV;

    }


    /*
     * The following routine computes the flows and and the pressure
     * derivatives.
     * Currently implemented features include the non-linear pressure-volume
     * relation for the splanchnic, the legs, and the abdominal venous
     * compartments. This routine calls the orthostatic stress routines [lbnp()
     * or
     * tilt()] which update the leakage flows and blood volume reductions.
     */
    public static void eqns_ptr(Data_vector dataVector,
            Parameters parameters,
            Reflex_vector reflexVector,
            IntraThoracicPressure intraThoracicPressure,
            ControlState controlState) {
        // non-linear compliances
        double Csp = 0.0;
        double Cll = 0.0;
        double Cab = 0.0;
        double Vll = 0.0;
        double Vsp = 0.0;
        double Vab = 0.0;
        double con = 0.0;                         // temporary variable
        TiltLeakageFlows tiltVector = new TiltLeakageFlows();

        // call tilt function
        if (controlState.tiltTestEnabled) {
            tilt(dataVector, parameters, tiltVector, intraThoracicPressure, controlState);
        }

        // The following lines compute the non-linear venous compliance values and 
        // the corresponding vascular volumes.
        con = PI * parameters.get(ParameterName.SPLAN_VEN_COMPLIANCE) / 2.0 / parameters.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL);
        Csp = parameters.get(ParameterName.SPLAN_VEN_COMPLIANCE) / (1.0 + con * con * (dataVector.pressure[SPLANCHNIC_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI]) * (dataVector.pressure[SPLANCHNIC_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI]));
        Vsp = 2.0 * parameters.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL) * atan(con * (dataVector.pressure[SPLANCHNIC_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI])) / PI;

        if ((dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI]) > 0.0) {
            con = PI * parameters.get(ParameterName.LBODY_VEN_COMPLIANCE) / 2.0 / parameters.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL);
            Cll = parameters.get(ParameterName.LBODY_VEN_COMPLIANCE) / (1.0 + con * con * (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI]) * (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI]));
            Vll = 2.0 * parameters.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL) * atan(con * (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI])) / PI;
        } else {
            con = PI * parameters.get(ParameterName.LBODY_VEN_COMPLIANCE) / 2.0 / parameters.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL);
            Cll = parameters.get(ParameterName.LBODY_VEN_COMPLIANCE) / (1.0 + con * con * (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI]) * (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI]));
        }

        con = PI * parameters.get(ParameterName.ABDOM_VEN_COMPLIANCE) / 2.0 / parameters.get(ParameterName.MAX_INCREASE_IN_ABDOM_DISTENDING_VOL);
        Cab = parameters.get(ParameterName.ABDOM_VEN_COMPLIANCE) / (1.0 + con * con * (dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI]) * (dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI]));
        Vab = 2.0 * parameters.get(ParameterName.MAX_INCREASE_IN_ABDOM_DISTENDING_VOL) * atan(con * (dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI])) / PI;

        // Computing the flows in the system based on the pressures at the current
        // time step.
        if (dataVector.pressure[LEFT_VENTRICULAR_CPI] > (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.grav[0])) {
            dataVector.flowRate[ASCENDING_AORTIC_CPI] = (dataVector.pressure[LEFT_VENTRICULAR_CPI] - dataVector.pressure[ASCENDING_AORTIC_CPI] - dataVector.grav[0]) / parameters.get(ParameterName.AORTIC_VALVE_RESISTANCE);
        } else {
            dataVector.flowRate[ASCENDING_AORTIC_CPI] = 0.0;                        // left ventricular outflow
        }
        dataVector.flowRate[BRACHIOCEPHALIC_ARTERIAL_CPI] = (dataVector.pressure[ASCENDING_AORTIC_CPI] - dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI] - dataVector.grav[1]) / parameters.get(ParameterName.BRACH_ART_RESISTANCE);
        dataVector.flowRate[UPPER_BODY_ARTERIAL_CPI] = (dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI] - dataVector.pressure[UPPER_BODY_ARTERIAL_CPI] - dataVector.grav[2]) / parameters.get(ParameterName.UBODY_ART_RESISTANCE);
        dataVector.flowRate[UPPER_BODY_VENOUS_CPI] = (dataVector.pressure[UPPER_BODY_ARTERIAL_CPI] - dataVector.pressure[UPPER_BODY_VENOUS_CPI]) / reflexVector.resistance[0];

        // Starling resistor defines the flow into the superior vena cava.
        if ((dataVector.pressure[UPPER_BODY_VENOUS_CPI] + dataVector.grav[3] > dataVector.pressure[SUPERIOR_VENA_CAVA_CPI]) && (dataVector.pressure[SUPERIOR_VENA_CAVA_CPI] > intraThoracicPressure.getValue())) {
            dataVector.flowRate[SUPERIOR_VENA_CAVA_CPI] = (dataVector.pressure[UPPER_BODY_VENOUS_CPI] - dataVector.pressure[SUPERIOR_VENA_CAVA_CPI] + dataVector.grav[3]) / parameters.get(ParameterName.UBODY_VEN_RESISTANCE);
        } else if ((dataVector.pressure[UPPER_BODY_VENOUS_CPI] + dataVector.grav[3] > intraThoracicPressure.getValue()) && (intraThoracicPressure.getValue() > dataVector.pressure[SUPERIOR_VENA_CAVA_CPI])) {
            dataVector.flowRate[SUPERIOR_VENA_CAVA_CPI] = (dataVector.pressure[UPPER_BODY_VENOUS_CPI] - intraThoracicPressure.getValue() + dataVector.grav[3]) / parameters.get(ParameterName.UBODY_VEN_RESISTANCE);
        } else if (intraThoracicPressure.getValue() > dataVector.pressure[UPPER_BODY_VENOUS_CPI] + dataVector.grav[3]) {
            dataVector.flowRate[SUPERIOR_VENA_CAVA_CPI] = 0.0;
        }

        dataVector.flowRate[THORACIC_AORTIC_CPI] = (dataVector.pressure[SUPERIOR_VENA_CAVA_CPI] - dataVector.pressure[RIGHT_ATRIAL_CPI] + dataVector.grav[4]) / parameters.get(ParameterName.SVC_RESISTANCE);
        dataVector.flowRate[ABDOMINAL_AORTIC_CPI] = (dataVector.pressure[ASCENDING_AORTIC_CPI] - dataVector.pressure[THORACIC_AORTIC_CPI] + dataVector.grav[5]) / parameters.get(ParameterName.THORACIC_AORTA_RESISTANCE);
        dataVector.flowRate[RENAL_ARTERIAL_CPI] = (dataVector.pressure[THORACIC_AORTIC_CPI] - dataVector.pressure[ABDOMINAL_AORTIC_CPI] + dataVector.grav[6]) / parameters.get(ParameterName.ABDOM_AORTA_RESISTANCE);
        dataVector.flowRate[RENAL_VENOUS_CPI] = (dataVector.pressure[ABDOMINAL_AORTIC_CPI] - dataVector.pressure[RENAL_ARTERIAL_CPI] + dataVector.grav[7]) / parameters.get(ParameterName.RENAL_ART_RESISTANCE);
        dataVector.flowRate[SPLANCHNIC_ARTERIAL_CPI] = (dataVector.pressure[RENAL_ARTERIAL_CPI] - dataVector.pressure[RENAL_VENOUS_CPI]) / reflexVector.resistance[1];
        dataVector.flowRate[SPLANCHNIC_VENOUS_CPI] = (dataVector.pressure[RENAL_VENOUS_CPI] - dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.grav[8]) / parameters.get(ParameterName.RENAL_VEN_RESISTANCE);
        dataVector.flowRate[LBODY_ARTERIAL_CPI] = (dataVector.pressure[ABDOMINAL_AORTIC_CPI] - dataVector.pressure[SPLANCHNIC_ARTERIAL_CPI] + dataVector.grav[9]) / parameters.get(ParameterName.SPLAN_ART_RESISTANCE);
        dataVector.flowRate[LBODY_VENOUS_CPI] = (dataVector.pressure[SPLANCHNIC_ARTERIAL_CPI] - dataVector.pressure[SPLANCHNIC_VENOUS_CPI]) / reflexVector.resistance[2];
        dataVector.flowRate[ABDOMINAL_VENOUS_CPI] = (dataVector.pressure[SPLANCHNIC_VENOUS_CPI] - dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.grav[10]) / parameters.get(ParameterName.SPLAN_VEN_RESISTANCE);
        dataVector.flowRate[INFERIOR_VENA_CAVA_CPI] = (dataVector.pressure[ABDOMINAL_AORTIC_CPI] - dataVector.pressure[LBODY_ARTERIAL_CPI] + dataVector.grav[11]) / parameters.get(ParameterName.LBODY_ART_RESISTANCE);
        dataVector.flowRate[RIGHT_ATRIAL_CPI] = (dataVector.pressure[LBODY_ARTERIAL_CPI] - dataVector.pressure[LBODY_VENOUS_CPI]) / reflexVector.resistance[3];

        if (dataVector.pressure[LBODY_VENOUS_CPI] > (dataVector.pressure[ABDOMINAL_VENOUS_CPI] + dataVector.grav[12])) {
            dataVector.flowRate[RIGHT_VENTRICULAR_CPI] = (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.grav[12]) / parameters.get(ParameterName.LBODY_VEN_RESISTANCE);
        } else {
            dataVector.flowRate[RIGHT_VENTRICULAR_CPI] = 0.0;
        }

        dataVector.flowRate[PULMONARY_ARTERIAL_CPI] = (dataVector.pressure[ABDOMINAL_VENOUS_CPI] - dataVector.pressure[INFERIOR_VENA_CAVA_CPI] - dataVector.grav[13]) / parameters.get(ParameterName.ABDOM_VEN_RESISTANCE);
        dataVector.flowRate[PULMONARY_VENOUS_CPI] = (dataVector.pressure[INFERIOR_VENA_CAVA_CPI] - dataVector.pressure[RIGHT_ATRIAL_CPI] - dataVector.grav[14]) / parameters.get(ParameterName.IVC_RESISTANCE);

        if (dataVector.pressure[RIGHT_ATRIAL_CPI] > dataVector.pressure[RIGHT_VENTRICULAR_CPI]) {
            dataVector.flowRate[LEFT_ATRIAL_CPI] = (dataVector.pressure[RIGHT_ATRIAL_CPI] - dataVector.pressure[RIGHT_VENTRICULAR_CPI]) / parameters.get(ParameterName.TRICUSPID_VALVE_RESISTANCE);
        } else {
            dataVector.flowRate[LEFT_ATRIAL_CPI] = 0.0;
        }

        if (dataVector.pressure[RIGHT_VENTRICULAR_CPI] > dataVector.pressure[PULMONARY_ARTERIAL_CPI]) {
            dataVector.flowRate[LEFT_VENTRICULAR_CPI] = (dataVector.pressure[RIGHT_VENTRICULAR_CPI] - dataVector.pressure[PULMONARY_ARTERIAL_CPI]) / parameters.get(ParameterName.PUMONIC_VALVE_RESISTANCE);
        } else {
            dataVector.flowRate[LEFT_VENTRICULAR_CPI] = 0.0;
        }

        dataVector.flowRate[BIAS_1_CPI] = (dataVector.pressure[PULMONARY_ARTERIAL_CPI] - dataVector.pressure[PULMONARY_VENOUS_CPI]) / parameters.get(ParameterName.PULM_MICRO_RESISTANCE);
        dataVector.flowRate[BIAS_2_CPI] = (dataVector.pressure[PULMONARY_VENOUS_CPI] - dataVector.pressure[LEFT_ATRIAL_CPI]) / parameters.get(ParameterName.PULM_VEN_RESISTANCE);

        if (dataVector.pressure[LEFT_ATRIAL_CPI] > dataVector.pressure[LEFT_VENTRICULAR_CPI]) {
            dataVector.flowRate[BIAS_3_CPI] = (dataVector.pressure[LEFT_ATRIAL_CPI] - dataVector.pressure[LEFT_VENTRICULAR_CPI]) / parameters.get(ParameterName.MITRAL_VALVE_RESISTANCE);
        } else {
            dataVector.flowRate[BIAS_3_CPI] = 0.0;
        }

        // Computing the pressure derivatives based on the flows and compliance
        // values at the current time step.
        double intraThoracicPressureValue = intraThoracicPressure.getValue();
        double intraThoracicPressureDerivative = intraThoracicPressure.getDerivative();
        dataVector.dPressureDt[ASCENDING_AORTIC_CPI] = (dataVector.flowRate[ASCENDING_AORTIC_CPI] - dataVector.flowRate[BRACHIOCEPHALIC_ARTERIAL_CPI] - dataVector.flowRate[ABDOMINAL_AORTIC_CPI]) / parameters.get(ParameterName.ASCENDING_AORTA_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[BRACHIOCEPHALIC_ARTERIAL_CPI] = (dataVector.flowRate[BRACHIOCEPHALIC_ARTERIAL_CPI] - dataVector.flowRate[UPPER_BODY_ARTERIAL_CPI]) / parameters.get(ParameterName.BRACH_ART_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[UPPER_BODY_ARTERIAL_CPI] = (dataVector.flowRate[UPPER_BODY_ARTERIAL_CPI] - dataVector.flowRate[UPPER_BODY_VENOUS_CPI]) / parameters.get(ParameterName.UBODY_ART_COMPLIANCE);
        dataVector.dPressureDt[UPPER_BODY_VENOUS_CPI] = (dataVector.flowRate[UPPER_BODY_VENOUS_CPI] - dataVector.flowRate[SUPERIOR_VENA_CAVA_CPI]) / parameters.get(ParameterName.UBODY_VEN_COMPLIANCE);
        dataVector.dPressureDt[SUPERIOR_VENA_CAVA_CPI] = (dataVector.flowRate[SUPERIOR_VENA_CAVA_CPI] - dataVector.flowRate[THORACIC_AORTIC_CPI]) / parameters.get(ParameterName.SVC_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[THORACIC_AORTIC_CPI] = (dataVector.flowRate[ABDOMINAL_AORTIC_CPI] - dataVector.flowRate[RENAL_ARTERIAL_CPI]) / parameters.get(ParameterName.THORACIC_AORTA_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[ABDOMINAL_AORTIC_CPI] = (dataVector.flowRate[RENAL_ARTERIAL_CPI] - dataVector.flowRate[RENAL_VENOUS_CPI] - dataVector.flowRate[LBODY_ARTERIAL_CPI] - dataVector.flowRate[INFERIOR_VENA_CAVA_CPI]) / parameters.get(ParameterName.ABDOM_AORTA_COMPLIANCE) + dataVector.dPressureDt[BIAS_1_CPI];
        dataVector.dPressureDt[RENAL_ARTERIAL_CPI] = (dataVector.flowRate[RENAL_VENOUS_CPI] - dataVector.flowRate[SPLANCHNIC_ARTERIAL_CPI]) / parameters.get(ParameterName.RENAL_ART_COMPLIANCE) + dataVector.dPressureDt[BIAS_1_CPI];
        dataVector.dPressureDt[RENAL_VENOUS_CPI] = (dataVector.flowRate[SPLANCHNIC_ARTERIAL_CPI] - dataVector.flowRate[SPLANCHNIC_VENOUS_CPI]) / parameters.get(ParameterName.RENAL_VEN_COMPLIANCE) + dataVector.dPressureDt[BIAS_1_CPI];
        dataVector.dPressureDt[SPLANCHNIC_ARTERIAL_CPI] = (dataVector.flowRate[LBODY_ARTERIAL_CPI] - dataVector.flowRate[LBODY_VENOUS_CPI]) / parameters.get(ParameterName.SPLAN_ART_COMPLIANCE) + dataVector.dPressureDt[BIAS_1_CPI];
        dataVector.dPressureDt[SPLANCHNIC_VENOUS_CPI] = (dataVector.flowRate[LBODY_VENOUS_CPI] - dataVector.flowRate[ABDOMINAL_VENOUS_CPI] - tiltVector.splanchnicFlow) / Csp + dataVector.dPressureDt[BIAS_1_CPI];
        dataVector.dPressureDt[LBODY_ARTERIAL_CPI] = (dataVector.flowRate[INFERIOR_VENA_CAVA_CPI] - dataVector.flowRate[RIGHT_ATRIAL_CPI]) / parameters.get(ParameterName.LBODY_ART_COMPLIANCE) + dataVector.dPressureDt[BIAS_2_CPI];
        dataVector.dPressureDt[LBODY_VENOUS_CPI] = (dataVector.flowRate[RIGHT_ATRIAL_CPI] - dataVector.flowRate[RIGHT_VENTRICULAR_CPI] - tiltVector.legFlow) / Cll + dataVector.dPressureDt[BIAS_2_CPI];
        dataVector.dPressureDt[ABDOMINAL_VENOUS_CPI] = (dataVector.flowRate[SPLANCHNIC_VENOUS_CPI] + dataVector.flowRate[ABDOMINAL_VENOUS_CPI] + dataVector.flowRate[RIGHT_VENTRICULAR_CPI] - dataVector.flowRate[PULMONARY_ARTERIAL_CPI] - tiltVector.abdominalFlow) / Cab + dataVector.dPressureDt[BIAS_1_CPI];
        dataVector.dPressureDt[INFERIOR_VENA_CAVA_CPI] = (dataVector.flowRate[PULMONARY_ARTERIAL_CPI] - dataVector.flowRate[PULMONARY_VENOUS_CPI]) / parameters.get(ParameterName.IVC_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[RIGHT_ATRIAL_CPI] = ((intraThoracicPressureValue - dataVector.pressure[RIGHT_ATRIAL_CPI]) * dataVector.dComplianceDt[RA_COMPL] + dataVector.flowRate[THORACIC_AORTIC_CPI] + dataVector.flowRate[PULMONARY_VENOUS_CPI] - dataVector.flowRate[LEFT_ATRIAL_CPI])
                / dataVector.compliance[RA_COMPL] + intraThoracicPressureDerivative;
        dataVector.dPressureDt[RIGHT_VENTRICULAR_CPI] = ((intraThoracicPressureValue - dataVector.pressure[RIGHT_VENTRICULAR_CPI]) * dataVector.dComplianceDt[RV_COMPL] + dataVector.flowRate[LEFT_ATRIAL_CPI] - dataVector.flowRate[LEFT_VENTRICULAR_CPI])
                / dataVector.compliance[RV_COMPL] + intraThoracicPressureDerivative;
        dataVector.dPressureDt[PULMONARY_ARTERIAL_CPI] = (dataVector.flowRate[LEFT_VENTRICULAR_CPI] - dataVector.flowRate[BIAS_1_CPI]) / parameters.get(ParameterName.PULM_ART_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[PULMONARY_VENOUS_CPI] = (dataVector.flowRate[BIAS_1_CPI] - dataVector.flowRate[BIAS_2_CPI]) / parameters.get(ParameterName.PULM_VEN_COMPLIANCE) + intraThoracicPressureDerivative;
        dataVector.dPressureDt[LEFT_ATRIAL_CPI] = ((intraThoracicPressureValue - dataVector.pressure[LEFT_ATRIAL_CPI]) * dataVector.dComplianceDt[LA_COMPL] + dataVector.flowRate[BIAS_2_CPI] - dataVector.flowRate[BIAS_3_CPI])
                / dataVector.compliance[LA_COMPL] + intraThoracicPressureDerivative;
        dataVector.dPressureDt[LEFT_VENTRICULAR_CPI] = ((intraThoracicPressureValue - dataVector.pressure[LEFT_VENTRICULAR_CPI]) * dataVector.dComplianceDt[LV_COMPL] + dataVector.flowRate[BIAS_3_CPI] - dataVector.flowRate[ASCENDING_AORTIC_CPI])
                / dataVector.compliance[LV_COMPL] + intraThoracicPressureDerivative;

        // Computing the compartmental volumes.
        dataVector.volume[ASCENDING_AORTIC_CPI] = (dataVector.pressure[ASCENDING_AORTIC_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.ASCENDING_AORTA_COMPLIANCE) + parameters.get(ParameterName.ASCENDING_AORTA_VOLUME);
        dataVector.volume[BRACHIOCEPHALIC_ARTERIAL_CPI] = (dataVector.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.BRACH_ART_COMPLIANCE) + parameters.get(ParameterName.BRACH_ART_ZPFV);
        dataVector.volume[UPPER_BODY_ARTERIAL_CPI] = dataVector.pressure[UPPER_BODY_ARTERIAL_CPI] * parameters.get(ParameterName.UBODY_ART_COMPLIANCE) + parameters.get(ParameterName.UBODY_ART_ZPFV);
        dataVector.volume[UPPER_BODY_VENOUS_CPI] = dataVector.pressure[UPPER_BODY_VENOUS_CPI] * parameters.get(ParameterName.UBODY_VEN_COMPLIANCE) + reflexVector.volume[0];
        dataVector.volume[SUPERIOR_VENA_CAVA_CPI] = (dataVector.pressure[SUPERIOR_VENA_CAVA_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.SVC_COMPLIANCE) + parameters.get(ParameterName.SVC_ZPFV);
        dataVector.volume[THORACIC_AORTIC_CPI] = (dataVector.pressure[THORACIC_AORTIC_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.THORACIC_AORTA_COMPLIANCE) + parameters.get(ParameterName.THORACIC_AORTA_ZPFV);
        dataVector.volume[ABDOMINAL_AORTIC_CPI] = (dataVector.pressure[ABDOMINAL_AORTIC_CPI] - dataVector.pressure[BIAS_1_CPI]) * parameters.get(ParameterName.ABDOM_AORTA_COMPLIANCE) + parameters.get(ParameterName.ABDOM_AORTA_ZPFV);
        dataVector.volume[RENAL_ARTERIAL_CPI] = (dataVector.pressure[RENAL_ARTERIAL_CPI] - dataVector.pressure[BIAS_1_CPI]) * parameters.get(ParameterName.RENAL_ART_COMPLIANCE) + parameters.get(ParameterName.RENAL_ART_ZPFV);
        dataVector.volume[RENAL_VENOUS_CPI] = (dataVector.pressure[RENAL_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI]) * parameters.get(ParameterName.RENAL_VEN_COMPLIANCE) + reflexVector.volume[1];
        dataVector.volume[SPLANCHNIC_ARTERIAL_CPI] = (dataVector.pressure[SPLANCHNIC_ARTERIAL_CPI] - dataVector.pressure[BIAS_1_CPI]) * parameters.get(ParameterName.SPLAN_ART_COMPLIANCE) + parameters.get(ParameterName.SPLAN_ART_ZPFV);
        dataVector.volume[SPLANCHNIC_VENOUS_CPI] = Vsp + reflexVector.volume[2];
        dataVector.volume[LBODY_ARTERIAL_CPI] = (dataVector.pressure[LBODY_ARTERIAL_CPI] - dataVector.pressure[BIAS_2_CPI]) * parameters.get(ParameterName.LBODY_ART_COMPLIANCE) + parameters.get(ParameterName.LBODY_ART_ZPFV);
        dataVector.volume[LBODY_VENOUS_CPI] = Vll + reflexVector.volume[3];
        dataVector.volume[ABDOMINAL_VENOUS_CPI] = Vab + parameters.get(ParameterName.ABDOM_VEN_ZPFV);
        dataVector.volume[INFERIOR_VENA_CAVA_CPI] = (dataVector.pressure[INFERIOR_VENA_CAVA_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.IVC_COMPLIANCE) + parameters.get(ParameterName.IVC_ZPFV);
        dataVector.volume[RIGHT_ATRIAL_CPI] = (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressureValue) * dataVector.compliance[RA_COMPL] + parameters.get(ParameterName.RA_ZPFV);
        dataVector.volume[RIGHT_VENTRICULAR_CPI] = (dataVector.pressure[RIGHT_VENTRICULAR_CPI] - intraThoracicPressureValue) * dataVector.compliance[RV_COMPL] + parameters.get(ParameterName.RV_ZPFV);
        dataVector.volume[PULMONARY_ARTERIAL_CPI] = (dataVector.pressure[PULMONARY_ARTERIAL_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.PULM_ART_COMPLIANCE) + parameters.get(ParameterName.PULM_ART_ZPFV);
        dataVector.volume[PULMONARY_VENOUS_CPI] = (dataVector.pressure[PULMONARY_VENOUS_CPI] - intraThoracicPressureValue) * parameters.get(ParameterName.PULM_VEN_COMPLIANCE) + parameters.get(ParameterName.PULN_VEN_ZPFV);
        dataVector.volume[LEFT_ATRIAL_CPI] = (dataVector.pressure[LEFT_ATRIAL_CPI] - intraThoracicPressureValue) * dataVector.compliance[LA_COMPL] + parameters.get(ParameterName.LA_ZPFV);
        dataVector.volume[LEFT_VENTRICULAR_CPI] = (dataVector.pressure[LEFT_VENTRICULAR_CPI] - intraThoracicPressureValue) * dataVector.compliance[LV_COMPL] + parameters.get(ParameterName.LV_ZPFV);

        dataVector.tidalLungVolume = intraThoracicPressure.getTidalLungVolume();
    }

    /**
     * fixvolume
     *
     * The following routine ensures blood volume conservation after every
     * integration step. It computes the blood volume stored in each capacitor,
     * the total zero pressure filling volume, and the total blood volume loss
     * during an orthostatic stress intervention and compares the sum of these
     * three to the total blood volume constant in the parameter vector. Any
     * difference between the two will be corrected for at the inferior vena
     * cava.
     */
    public static void fixvolume(Data_vector p,
            Reflex_vector ref,
            IntraThoracicPressure intraThoracicPressure,
            Parameters parameters) {

        // This function doesn't do anything, so just comment it out! JLL 
        /*
        double diff = 0.0, con = 0.0;           // temporary variables
        double Vsp = 0.0, Vll = 0.0, Vab = 0.0; // non-linear p-v relations
        double Cll = 0.0;

        con = PI * parameters.get(PVName.SPLAN_VEN_COMPLIANCE) / 2.0 / parameters.get(PVName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL);
        Vsp = 2.0 * parameters.get(PVName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL) * atan(con * (p.pressure[SPLANCHNIC_VENOUS_CPI] - p.pressure[BIAS_1_CPI])) / PI;

        if ((p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI]) > 0.0) {
            con = PI * parameters.get(PVName.LBODY_VEN_COMPLIANCE) / 2.0 / parameters.get(PVName.MAX_INCREASE_IN_LEG_DISTENDING_VOL);
            Cll = parameters.get(PVName.LBODY_VEN_COMPLIANCE) / (1.0 + con * con * (p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI]) * (p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI]));
            Vll = 2.0 * parameters.get(PVName.MAX_INCREASE_IN_LEG_DISTENDING_VOL) * atan(con * (p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI])) / PI;
        } else {
            con = PI * parameters.get(PVName.LBODY_VEN_COMPLIANCE) / 2.0 / parameters.get(PVName.MAX_INCREASE_IN_LEG_DISTENDING_VOL);
            Cll = parameters.get(PVName.LBODY_VEN_COMPLIANCE) / (1.0 + con * con * (p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI]) * (p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI]));
            Vll = 2.0 * parameters.get(PVName.MAX_INCREASE_IN_LEG_DISTENDING_VOL) * atan(con * (p.pressure[LBODY_VENOUS_CPI] - p.pressure[BIAS_2_CPI])) / PI;
        }

        con = PI * parameters.get(PVName.ABDOM_VEN_COMPLIANCE) / 2.0 / parameters.get(PVName.MAX_INCREASE_IN_ABDOM_DISTENDING_VOL);
        Vab = 2.0 * parameters.get(PVName.MAX_INCREASE_IN_ABDOM_DISTENDING_VOL) * atan(con * (p.pressure[ABDOMINAL_VENOUS_CPI] - p.pressure[BIAS_1_CPI])) / PI;

        diff = parameters.get(PVName.TOTAL_BLOOD_VOLUME) - parameters.get(PVName.ABDOM_VEN_ZPFV) - parameters.get(PVName.IVC_ZPFV) - parameters.get(PVName.SVC_ZPFV)
                - parameters.get(PVName.RA_ZPFV) - parameters.get(PVName.RV_ZPFV) - parameters.get(PVName.PULM_ART_ZPFV)
                - parameters.get(PVName.PULN_VEN_ZPFV) - parameters.get(PVName.LA_ZPFV) - parameters.get(PVName.LV_ZPFV) - ref.volume[0]
                - parameters.get(PVName.ASCENDING_AORTA_VOLUME) - parameters.get(PVName.BRACH_ART_ZPFV) - parameters.get(PVName.THORACIC_AORTA_ZPFV) - parameters.get(PVName.UBODY_ART_ZPFV)
                - parameters.get(PVName.ABDOM_AORTA_ZPFV) - parameters.get(PVName.RENAL_ART_ZPFV) - parameters.get(PVName.SPLAN_ART_ZPFV) - parameters.get(PVName.LBODY_ART_ZPFV)
                - ref.volume[1] - ref.volume[2] - ref.volume[3] - p.tilt[1]
                - ((p.pressure[ASCENDING_AORTIC_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.ASCENDING_AORTA_COMPLIANCE)
                + (p.pressure[BRACHIOCEPHALIC_ARTERIAL_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.BRACH_ART_COMPLIANCE)
                + (p.pressure[INFERIOR_VENA_CAVA_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.IVC_COMPLIANCE)
                + (p.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * p.compliance[RA_COMPL]
                + (p.pressure[RIGHT_VENTRICULAR_CPI] - intraThoracicPressure.getValue()) * p.compliance[RV_COMPL]
                + (p.pressure[PULMONARY_ARTERIAL_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.PULM_ART_COMPLIANCE)
                + (p.pressure[PULMONARY_VENOUS_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.PULM_VEN_COMPLIANCE)
                + (p.pressure[LEFT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * p.compliance[LA_COMPL]
                + (p.pressure[LEFT_VENTRICULAR_CPI] - intraThoracicPressure.getValue()) * p.compliance[LV_COMPL]
                + (p.pressure[SUPERIOR_VENA_CAVA_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.SVC_COMPLIANCE)
                + (p.pressure[THORACIC_AORTIC_CPI] - intraThoracicPressure.getValue()) * parameters.get(PVName.THORACIC_AORTA_COMPLIANCE)
                + (p.pressure[ABDOMINAL_AORTIC_CPI] - p.pressure[BIAS_1_CPI]) * parameters.get(PVName.ABDOM_AORTA_COMPLIANCE) + Vsp + Vll + Vab
                + (p.pressure[RENAL_ARTERIAL_CPI] - p.pressure[BIAS_1_CPI]) * parameters.get(PVName.RENAL_ART_COMPLIANCE)
                + (p.pressure[RENAL_VENOUS_CPI] - p.pressure[BIAS_1_CPI]) * parameters.get(PVName.RENAL_VEN_COMPLIANCE)
                + (p.pressure[SPLANCHNIC_ARTERIAL_CPI] - p.pressure[BIAS_1_CPI]) * parameters.get(PVName.SPLAN_ART_COMPLIANCE)
                + p.pressure[UPPER_BODY_ARTERIAL_CPI] * parameters.get(PVName.UBODY_ART_COMPLIANCE)
                + p.pressure[UPPER_BODY_VENOUS_CPI] * parameters.get(PVName.UBODY_VEN_COMPLIANCE)
                + (p.pressure[LBODY_ARTERIAL_CPI] - p.pressure[BIAS_2_CPI]) * parameters.get(PVName.LBODY_ART_COMPLIANCE));

        // The original C code has lots of lines commented out, including this one:
        //  p -> x[12] += diff/Cll;
        // In Java this would be p.pressure[LBODY_VENOUS_CPI] += diff/Cll
         */
    }

    /*
     * Three different orthostatic stresses (short radius centrifugation, tilt,
     * and lower body negative pressure) were implemented in the following three
     * subroutines. Each subroutine is called from eqns() and modifies selected
     * elements of the Data_vector and the Stress_vector which are supplied to
     * them.
     */

 /*
     * In the following subroutine the tilt simulation is implemented.
     */
    public static int tilt(Data_vector p, Parameters parameters, TiltLeakageFlows tilt,
            IntraThoracicPressure intraThoracicPressure, ControlState controlState) {
        double alpha = 0.0;               // tilt angle converted to rads

        double act = 0.0;                 // activation function for intra-thoracic
        // and intra-abdominal pressures.
        double act_dt = 0.0;

        double gravity = 0.0;             // activation function of stress
        // onset and offset
        double gravity_dt = 0.0;

        double tilt_time = 0.0;          // time
        double tilt_angle = 0.0;         // instantaneous tilt angle in degrees

        // The following variables are introduced for computation of the blood 
        // volume sequestration into the interstitial fluid compartment.
        double V_loss = 0.0;                       // total blood volume loss as a
        // function of time

        double qsp_loss = 0.0, qll_loss = 0.0;     // leakage currents 
        double qab_loss = 0.0, q_loss = 0.0;
        double q_not = parameters.get(ParameterName.MAXIMAL_BLOOD_VOLUME_LOSS_DURINT_TILT) / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE); // 
        final double TAU = 276.0;    // time constant of interstitial fluid shifts

        // Page 49 of Thomas Heldt’s PhD thesis:
        // "In addition to changes in the luminal pressures described by Ph, Mead
        // and Gaensler [123] and Ferris and co-workers [124] showed that 
        // intra-thoracic pressure, Pth, changed over similarly short periods of 
        // time in response to gravitational stress. Mead and Gaensler reported 
        // intra-thoracic pressure to drop by (3.1±1.0) mm Hg in response to 
        // sitting up from the supine position. Ferris demonstrated a drop of 
        // (3.5±0.7) mm Hg in response to head-up tilts to 90◦. We implemented
        // these changes in intra-thoracic pressure by assuming a time course
        // similar to the one described by Equation 2.4."
        final double INTRATHORACIC_PRESSURE_CHANGE_ON_TILT = -3.5;
        double con1 = INTRATHORACIC_PRESSURE_CHANGE_ON_TILT / 0.738;

        // Define certain dummy variables.
        alpha = Math.toRadians(parameters.get(ParameterName.TILT_ANGLE));
        q_not *= sin(alpha) / sin(Math.toRadians(85.0));
        con1 *= sin(alpha);

        // Tilt from horizontal to head-up position.
        if ((p.time[MODIFIED_SIMULATION_TIME] >= controlState.tiltStartTime)
                && (p.time[MODIFIED_SIMULATION_TIME] <= (controlState.tiltStartTime + parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)))) {
            tilt_time = (p.time[MODIFIED_SIMULATION_TIME] - controlState.tiltStartTime);

            // calculate tilt angle in radians
            tilt_angle = alpha * (1.0 - cos(PI * tilt_time / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE))) / 2.0;
            gravity = 0.738 * sin(tilt_angle);
            gravity_dt = 0.738 * cos(tilt_angle) * alpha / 2.0 * sin(PI * tilt_time / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)) * PI / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE);

            intraThoracicPressure.setTiltEffect(con1 * gravity, con1 * gravity_dt);

            p.grav[0] = parameters.get(ParameterName.ASCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // ascending aorta 
            p.grav[1] = parameters.get(ParameterName.BRACHIOCEPHAL_ART_HEIGHT) / 2.0 * gravity;  // brachiocephal.
            p.grav[2] = parameters.get(ParameterName.UBODY_ART_HEIGHT) / 2.0 * gravity;  // upper body
            p.grav[3] = parameters.get(ParameterName.UBODY_VEN_HEIGHT) / 2.0 * gravity;  // upper body veins
            p.grav[4] = parameters.get(ParameterName.SVC_HEIGHT) / 2.0 * gravity;  // SVC
            p.grav[5] = parameters.get(ParameterName.DESCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // descending aorta
            p.grav[6] = parameters.get(ParameterName.ABDOM_AORTA_HEIGHT) / 3.0 * gravity;  // abdominal aorta
            p.grav[7] = parameters.get(ParameterName.RENAL_ART_HEIGHT) / 2.0 * gravity;  // renal arteries
            p.grav[8] = parameters.get(ParameterName.RENAL_VEN_HEIGHT) / 2.0 * gravity;  // renal veins
            p.grav[9] = parameters.get(ParameterName.SPLAN_ART_HEIGHT) / 2.0 * gravity;  // splanchnic arteries
            p.grav[10] = parameters.get(ParameterName.SPLAN_VEIN_HEIGHT) / 2.0 * gravity;  // splanchnic veins
            p.grav[11] = parameters.get(ParameterName.LBODY_ART_HEIGHT) / 3.0 * gravity;  // leg arteries
            p.grav[12] = parameters.get(ParameterName.LBODY_VEN_HEIGHT) / 3.0 * gravity;  // leg veins
            p.grav[13] = parameters.get(ParameterName.ABDOM_IVC_HEIGHT) / 3.0 * gravity;  // abdominal IVC
            p.grav[14] = parameters.get(ParameterName.THORACIC_IVC_HEIGHT) / 2.0 * gravity;  // thoracic IVC

            q_loss = q_not * (1.0 - exp(-tilt_time / TAU));
            qsp_loss = 7.0 / (63.0) * q_loss;
            qll_loss = 40.0 / (63.0) * q_loss;
            qab_loss = 16 / (63.0) * q_loss;

            V_loss = q_not * (tilt_time - TAU * (1.0 - exp(-tilt_time / TAU)));
        } // Head-up position
        else if ((p.time[MODIFIED_SIMULATION_TIME] > (parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) + controlState.tiltStartTime))
                && (p.time[MODIFIED_SIMULATION_TIME] <= (parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) + controlState.tiltStartTime
                + parameters.get(ParameterName.DURATION_IN_UPRIGHT_POSTURE)))) {
            tilt_time = (p.time[MODIFIED_SIMULATION_TIME] - controlState.tiltStartTime - parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE));
            tilt_angle = alpha;
            gravity = 0.738 * sin(alpha);
            gravity_dt = 0.0;

            intraThoracicPressure.setTiltEffect(con1 * gravity, con1 * gravity_dt);

            p.grav[0] = parameters.get(ParameterName.ASCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // ascending aorta
            p.grav[1] = parameters.get(ParameterName.BRACHIOCEPHAL_ART_HEIGHT) / 2.0 * gravity;  // brachiocephal.
            p.grav[2] = parameters.get(ParameterName.UBODY_ART_HEIGHT) / 2.0 * gravity;  // upper body
            p.grav[3] = parameters.get(ParameterName.UBODY_VEN_HEIGHT) / 2.0 * gravity;  // upper body veins
            p.grav[4] = parameters.get(ParameterName.SVC_HEIGHT) / 2.0 * gravity;  // SVC
            p.grav[5] = parameters.get(ParameterName.DESCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // descending aorta
            p.grav[6] = parameters.get(ParameterName.ABDOM_AORTA_HEIGHT) / 3.0 * gravity;  // abdominal aorta
            p.grav[7] = parameters.get(ParameterName.RENAL_ART_HEIGHT) / 2.0 * gravity;  // renal artieries
            p.grav[8] = parameters.get(ParameterName.RENAL_VEN_HEIGHT) / 2.0 * gravity;  // renal veins
            p.grav[9] = parameters.get(ParameterName.SPLAN_ART_HEIGHT) / 2.0 * gravity;  // splanchnic arteries
            p.grav[10] = parameters.get(ParameterName.SPLAN_VEIN_HEIGHT) / 2.0 * gravity;  // splanchnic veins
            p.grav[11] = parameters.get(ParameterName.LBODY_ART_HEIGHT) / 3.0 * gravity;  // leg arteries
            p.grav[12] = parameters.get(ParameterName.LBODY_VEN_HEIGHT) / 3.0 * gravity;  // leg veins
            p.grav[13] = parameters.get(ParameterName.ABDOM_IVC_HEIGHT) / 3.0 * gravity;  // abdominal IVC
            p.grav[14] = parameters.get(ParameterName.THORACIC_IVC_HEIGHT) / 2.0 * gravity;  // thoracic IVC

            q_loss = q_not * (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU)) * exp(-tilt_time / TAU);
            qsp_loss = 7.0 / (63.0) * q_loss;
            qll_loss = 40.0 / (63.0) * q_loss;
            qab_loss = 16 / (63.0) * q_loss;

            V_loss = q_not * parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)
                    * (1.0 - TAU * (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU))
                    * exp(-tilt_time / TAU) / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE));
        } // Tilt back to the supine position
        else if ((p.time[MODIFIED_SIMULATION_TIME] >= controlState.tiltStopTime)
                && (p.time[MODIFIED_SIMULATION_TIME] < (controlState.tiltStopTime + parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)))) {
            tilt_time = (p.time[MODIFIED_SIMULATION_TIME] - controlState.tiltStartTime - parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)
                    - (controlState.tiltStopTime - controlState.tiltStartTime));

            tilt_angle = alpha * (1.0 - cos(PI * (1 - tilt_time / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)))) / 2.0;
            gravity = 0.738 * sin(tilt_angle);
            gravity_dt = 0.738 * cos(tilt_angle) * alpha / 2.0 * sin(PI * (1.0 - tilt_time / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE))) * PI / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) * (-1.0);

            intraThoracicPressure.setTiltEffect(con1 * gravity, con1 * gravity_dt);

            p.grav[0] = parameters.get(ParameterName.ASCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // ascending aorta
            p.grav[1] = parameters.get(ParameterName.BRACHIOCEPHAL_ART_HEIGHT) / 2.0 * gravity;  // brachiocephal.
            p.grav[2] = parameters.get(ParameterName.UBODY_ART_HEIGHT) / 2.0 * gravity;  // upper body
            p.grav[3] = parameters.get(ParameterName.UBODY_VEN_HEIGHT) / 2.0 * gravity;  // upper body veins
            p.grav[4] = parameters.get(ParameterName.SVC_HEIGHT) / 2.0 * gravity;  // SVC
            p.grav[5] = parameters.get(ParameterName.DESCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // descending aorta
            p.grav[6] = parameters.get(ParameterName.ABDOM_AORTA_HEIGHT) / 3.0 * gravity;  // abdominal aorta
            p.grav[7] = parameters.get(ParameterName.RENAL_ART_HEIGHT) / 2.0 * gravity;  // renal artieries
            p.grav[8] = parameters.get(ParameterName.RENAL_VEN_HEIGHT) / 2.0 * gravity;  // renal veins
            p.grav[9] = parameters.get(ParameterName.SPLAN_ART_HEIGHT) / 2.0 * gravity;  // splanchnic arteries
            p.grav[10] = parameters.get(ParameterName.SPLAN_VEIN_HEIGHT) / 2.0 * gravity;  // splanchnic veins
            p.grav[11] = parameters.get(ParameterName.LBODY_ART_HEIGHT) / 3.0 * gravity;  // leg arteries
            p.grav[12] = parameters.get(ParameterName.LBODY_VEN_HEIGHT) / 3.0 * gravity;  // leg veins
            p.grav[13] = parameters.get(ParameterName.ABDOM_IVC_HEIGHT) / 3.0 * gravity;  // abdominal IVC
            p.grav[14] = parameters.get(ParameterName.THORACIC_IVC_HEIGHT) / 2.0 * gravity;  // thoracic IVC

            q_loss = q_not * (1.0 + (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU))
                    * exp(-parameters.get(ParameterName.DURATION_IN_UPRIGHT_POSTURE) / TAU)) * exp(-tilt_time / TAU) - q_not;
            qsp_loss = 7.0 / (63.0) * q_loss;
            qll_loss = 40.0 / (63.0) * q_loss;
            qab_loss = 16 / (63.0) * q_loss;

            V_loss = (1.0 + (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU)) * exp(-parameters.get(ParameterName.DURATION_IN_UPRIGHT_POSTURE) / TAU))
                    * (1.0 - exp(-tilt_time / TAU)) * q_not * TAU
                    - q_not * TAU * (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU)) * exp(-parameters.get(ParameterName.DURATION_IN_UPRIGHT_POSTURE) / TAU)
                    + q_not * parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) * (1.0 - tilt_time / parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE));
        } // Supine position
        else if (p.time[MODIFIED_SIMULATION_TIME] > (controlState.tiltStopTime + parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE))) {
            tilt_time = (p.time[MODIFIED_SIMULATION_TIME] - controlState.tiltStartTime - 2.0 * parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)
                    - (controlState.tiltStopTime - controlState.tiltStartTime));

            gravity = 0.0;
            gravity_dt = 0.0;
            tilt_angle = 0.0;

            intraThoracicPressure.setTiltEffect(con1 * gravity, con1 * gravity_dt);

            p.grav[0] = parameters.get(ParameterName.ASCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // ascending aorta
            p.grav[1] = parameters.get(ParameterName.BRACHIOCEPHAL_ART_HEIGHT) / 2.0 * gravity;  // brachiocephal.
            p.grav[2] = parameters.get(ParameterName.UBODY_ART_HEIGHT) / 2.0 * gravity;  // upper body
            p.grav[3] = parameters.get(ParameterName.UBODY_VEN_HEIGHT) / 2.0 * gravity;  // upper body veins
            p.grav[4] = parameters.get(ParameterName.SVC_HEIGHT) / 2.0 * gravity;  // SVC
            p.grav[5] = parameters.get(ParameterName.DESCENDING_AORTA_HEIGHT) / 2.0 * gravity;  // descending aorta
            p.grav[6] = parameters.get(ParameterName.ABDOM_AORTA_HEIGHT) / 3.0 * gravity;  // abdominal aorta
            p.grav[7] = parameters.get(ParameterName.RENAL_ART_HEIGHT) / 2.0 * gravity;  // renal artieries
            p.grav[8] = parameters.get(ParameterName.RENAL_VEN_HEIGHT) / 2.0 * gravity;  // renal veins
            p.grav[9] = parameters.get(ParameterName.SPLAN_ART_HEIGHT) / 2.0 * gravity;  // splanchnic arteries
            p.grav[10] = parameters.get(ParameterName.SPLAN_VEIN_HEIGHT) / 2.0 * gravity;  // splanchnic veins
            p.grav[11] = parameters.get(ParameterName.LBODY_ART_HEIGHT) / 3.0 * gravity;  // leg arteries
            p.grav[12] = parameters.get(ParameterName.LBODY_VEN_HEIGHT) / 3.0 * gravity;  // leg veins
            p.grav[13] = parameters.get(ParameterName.ABDOM_IVC_HEIGHT) / 3.0 * gravity;  // abdominal IVC
            p.grav[14] = parameters.get(ParameterName.THORACIC_IVC_HEIGHT) / 2.0 * gravity;  // thoracic IVC

            q_loss = -q_not * (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU)) * exp(-tilt_time / TAU)
                    * (1.0 - exp(-(parameters.get(ParameterName.DURATION_IN_UPRIGHT_POSTURE) + parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)) / TAU));
            qsp_loss = 7.0 / (63.0) * q_loss;
            qll_loss = 40.0 / (63.0) * q_loss;
            qab_loss = 16 / (63.0) * q_loss;

            V_loss = q_not * TAU * (1.0 - exp(-parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE) / TAU)) * exp(-tilt_time / TAU)
                    * (1.0 - exp(-(parameters.get(ParameterName.DURATION_IN_UPRIGHT_POSTURE) + parameters.get(ParameterName.TIME_TO_MAX_TILT_ANGLE)) / TAU));
        }

        // Write the computed values for the flows, the volume loss, and the carotid
        // sinus offset pressure to the respective structures so they can be passed
        // across subroutine boundaries.
        tilt.splanchnicFlow = qsp_loss;
        tilt.legFlow = qll_loss;
        tilt.abdominalFlow = qab_loss;

        p.tilt[0] = parameters.get(ParameterName.SENSED_PRESSURE_OFFSET_DURING_TILT) * gravity;
        p.tilt[1] = V_loss;

        // convert tilt angle to degrees
        p.tilt_angle = Math.toDegrees(tilt_angle);

        return 0;
    }

}
