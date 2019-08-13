package jcvsim.compartment6;

import static java.lang.Math.PI;
import static jcvsim.common.Maths.cos;
import static jcvsim.common.Maths.sin;
import jcvsim.common.Reflex_vector;
import static jcvsim.compartment6.Data_vector.CompartmentIndex.*;
import static jcvsim.compartment6.Data_vector.ComplianceIndex.*;

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
 * Last modified October 19, 2005
 */
// Converted to Java Jason Leake December 2016
class Equation {


// Modifies ONLY Data_vector
    public static void elastance_ptr(Data_vector dataVector, Reflex_vector reflexVector, 
            Parameters parameters) {
        double Elv = 0.0, Erv = 0.0;
        double dElv = 0.0, dErv = 0.0;

        // ventricular timing variables
        double Tvsys = dataVector.time[4];
        double v_time = dataVector.time[6];

        // right and left ventricular diastolic and systolic compliances.
        double Crdias = parameters.get(ParameterName.RV_DIASTOLIC_COMPLIANCE);
        double sigCr = dataVector.compliance[RV_END_SYSTOLIC_COMPL];

        double Cldias = parameters.get(ParameterName.LV_DIASTOLIC_COMPLIANCE);
        double sigCl = dataVector.compliance[LV_END_SYSTOLIC_COMPL];

        // Ventricular contraction. PR-interval has not yet passed.
        if (v_time <= 0.0) {
            Elv = 1 / Cldias;
            Erv = 1 / Crdias;
            dElv = 0.0;
            dErv = 0.0;
        } // Ventricular contraction.
        else if ((0 < v_time) && (v_time <= Tvsys)) {
            Elv = 0.5 * (1 / sigCl - 1 / Cldias) * (1 - cos(PI * v_time / Tvsys)) + 1 / Cldias;
            Erv = 0.5 * (1 / sigCr - 1 / Crdias) * (1 - cos(PI * v_time / Tvsys)) + 1 / Crdias;
            dElv = 0.5 * PI * (1 / sigCl - 1 / Cldias) * sin(PI * v_time / Tvsys) / Tvsys;
            dErv = 0.5 * PI * (1 / sigCr - 1 / Crdias) * sin(PI * v_time / Tvsys) / Tvsys;
        } // Early ventricular relaxation.
        else if ((Tvsys < v_time) && (v_time <= 1.5 * Tvsys)) {
            Elv = 0.5 * (1 / sigCl - 1 / Cldias) * (1 + cos(2.0 * PI * (v_time - Tvsys) / Tvsys))
                    + 1 / Cldias;
            Erv = 0.5 * (1 / sigCr - 1 / Crdias) * (1 + cos(2.0 * PI * (v_time - Tvsys) / Tvsys))
                    + 1 / Crdias;
            dElv = -1.0 * PI * (1 / sigCl - 1 / Cldias) * sin(2.0 * PI * (v_time - Tvsys) / Tvsys) / Tvsys;
            dErv = -1.0 * PI * (1 / sigCr - 1 / Crdias) * sin(2.0 * PI * (v_time - Tvsys) / Tvsys) / Tvsys;
        } // Ventricular diastole.
        else if (v_time > 1.5 * Tvsys) {
            Elv = 1 / Cldias;
            Erv = 1 / Crdias;
            dElv = 0.0;
            dErv = 0.0;
        }

        dataVector.compliance[RV_COMPL] = 1 / Erv;
        dataVector.compliance[LV_COMPL] = 1 / Elv;

        dataVector.dComplianceDt[RV_COMPL] = -1.0 / (Erv * Erv) * dErv;
        dataVector.dComplianceDt[LV_COMPL] = -1.0 / (Elv * Elv) * dElv;
    }


    /*
     * The following routine computes the flows and the pressure derivatives;
     * it is called from the Runge-Kutta integration routine.
     */
// Modifies ONLY Data_vector
    public static void eqns_ptr(Data_vector dataVector, Parameters parameters, Reflex_vector reflexVector) {

        // Computing the flows in the system based on the pressures at the current
        // time step.
        // Left ventricular outflow
        if (dataVector.pressure[LEFT_VENTRICULAR_CPI] > dataVector.pressure[ARTERIAL_CPI]) {
            dataVector.flowRate[LEFT_VENTRICULAR_CPI] = (dataVector.pressure[LEFT_VENTRICULAR_CPI] - dataVector.pressure[ARTERIAL_CPI]) / parameters.get(ParameterName.AORTIC_VALVE_RESISTANCE);
        } else {
            dataVector.flowRate[LEFT_VENTRICULAR_CPI] = 0.0;
        }

        // Systemic bloodflow 
        dataVector.flowRate[ARTERIAL_CPI] = (dataVector.pressure[ARTERIAL_CPI] - dataVector.pressure[CENTRAL_VENOUS_CPI]) / reflexVector.resistance[0];

        // Right ventricular inflow
        if (dataVector.pressure[CENTRAL_VENOUS_CPI] > dataVector.pressure[RIGHT_VENTRICULAR_CPI]) {
            dataVector.flowRate[CENTRAL_VENOUS_CPI] = (dataVector.pressure[CENTRAL_VENOUS_CPI] - dataVector.pressure[RIGHT_VENTRICULAR_CPI]) / parameters.get(ParameterName.VEN_RESISTANCE);
        } else {
            dataVector.flowRate[CENTRAL_VENOUS_CPI] = 0.0;
        }

        // Right ventricular outflow
        if (dataVector.pressure[RIGHT_VENTRICULAR_CPI] > dataVector.pressure[PULMONARY_ARTERIAL_CPI]) {
            dataVector.flowRate[RIGHT_VENTRICULAR_CPI] = (dataVector.pressure[RIGHT_VENTRICULAR_CPI] - dataVector.pressure[PULMONARY_ARTERIAL_CPI]) / parameters.get(ParameterName.PULMONIC_VALVE_RESISTANCE);
        } else {
            dataVector.flowRate[RIGHT_VENTRICULAR_CPI] = 0.0;
        }

        // Pulmonary bloodflow
        dataVector.flowRate[PULMONARY_ARTERIAL_CPI] = (dataVector.pressure[PULMONARY_ARTERIAL_CPI] - dataVector.pressure[PULMONARY_VENOUS_CPI]) / parameters.get(ParameterName.PULM_MICRO_RESISTANCE);

        // Left ventricular inflow
        if (dataVector.pressure[PULMONARY_VENOUS_CPI] > dataVector.pressure[LEFT_VENTRICULAR_CPI]) {
            dataVector.flowRate[PULMONARY_VENOUS_CPI] = (dataVector.pressure[PULMONARY_VENOUS_CPI] - dataVector.pressure[LEFT_VENTRICULAR_CPI]) / parameters.get(ParameterName.PULM_VEN_RESISTANCE);
        } else {
            dataVector.flowRate[PULMONARY_VENOUS_CPI] = 0.0;
        }

        // Computing the pressure derivatives based on the flows and compliance
        // values at the current time step.
        dataVector.dPressureDt[LEFT_VENTRICULAR_CPI] = ((dataVector.pressure[INTRA_THORACIC_CPI] - dataVector.pressure[LEFT_VENTRICULAR_CPI]) * dataVector.dComplianceDt[LV_COMPL] + dataVector.flowRate[PULMONARY_VENOUS_CPI] - dataVector.flowRate[LEFT_VENTRICULAR_CPI])
                / dataVector.compliance[LV_COMPL] + dataVector.dPressureDt[INTRA_THORACIC_CPI];
        dataVector.dPressureDt[ARTERIAL_CPI] = (dataVector.flowRate[LEFT_VENTRICULAR_CPI] - dataVector.flowRate[ARTERIAL_CPI]) / parameters.get(ParameterName.ART_COMPLIANCE);
        dataVector.dPressureDt[CENTRAL_VENOUS_CPI] = (dataVector.flowRate[ARTERIAL_CPI] - dataVector.flowRate[CENTRAL_VENOUS_CPI]) / parameters.get(ParameterName.VEN_COMPLIANCE);
        dataVector.dPressureDt[RIGHT_VENTRICULAR_CPI] = ((dataVector.pressure[INTRA_THORACIC_CPI] - dataVector.pressure[RIGHT_VENTRICULAR_CPI]) * dataVector.dComplianceDt[RV_COMPL] + dataVector.flowRate[CENTRAL_VENOUS_CPI] - dataVector.flowRate[RIGHT_VENTRICULAR_CPI])
                / dataVector.compliance[RV_COMPL] + dataVector.dPressureDt[INTRA_THORACIC_CPI];
        dataVector.dPressureDt[PULMONARY_ARTERIAL_CPI] = (dataVector.flowRate[RIGHT_VENTRICULAR_CPI] - dataVector.flowRate[PULMONARY_ARTERIAL_CPI]) / parameters.get(ParameterName.PULM_ART_COMPLIANCE) + dataVector.dPressureDt[INTRA_THORACIC_CPI];
        dataVector.dPressureDt[PULMONARY_VENOUS_CPI] = (dataVector.flowRate[PULMONARY_ARTERIAL_CPI] - dataVector.flowRate[PULMONARY_VENOUS_CPI]) / parameters.get(ParameterName.PULM_VEN_COMPLIANCE) + dataVector.dPressureDt[INTRA_THORACIC_CPI];

        // Computing the compartmental volumes.
        dataVector.volume[LEFT_VENTRICULAR_CPI] = (dataVector.pressure[LEFT_VENTRICULAR_CPI] - dataVector.pressure[INTRA_THORACIC_CPI]) * dataVector.compliance[LV_COMPL] + parameters.get(ParameterName.LV_ZPFV);
        dataVector.volume[ARTERIAL_CPI] = dataVector.pressure[ARTERIAL_CPI] * parameters.get(ParameterName.ART_COMPLIANCE) + parameters.get(ParameterName.SYSTEMIC_ART_ZPFV);
        dataVector.volume[CENTRAL_VENOUS_CPI] = dataVector.pressure[CENTRAL_VENOUS_CPI] * parameters.get(ParameterName.VEN_COMPLIANCE) + parameters.get(ParameterName.SYSTEMIC_VEN_ZPFV);
        dataVector.volume[RIGHT_VENTRICULAR_CPI] = (dataVector.pressure[RIGHT_VENTRICULAR_CPI] - dataVector.pressure[INTRA_THORACIC_CPI]) * dataVector.compliance[RV_COMPL] + parameters.get(ParameterName.RV_ZPFV);
        dataVector.volume[PULMONARY_ARTERIAL_CPI] = (dataVector.pressure[PULMONARY_ARTERIAL_CPI] - dataVector.pressure[INTRA_THORACIC_CPI]) * parameters.get(ParameterName.PULM_ART_COMPLIANCE) + parameters.get(ParameterName.PULM_ART_ZPFV);
        dataVector.volume[PULMONARY_VENOUS_CPI] = (dataVector.pressure[PULMONARY_VENOUS_CPI] - dataVector.pressure[INTRA_THORACIC_CPI]) * parameters.get(ParameterName.PULM_VEN_COMPLIANCE) + parameters.get(ParameterName.PULM_VEN_ZPFV);
    }

    /*
     * The following routine ensures blood volume conservation after every
     * integration step. It computes the blood volume stored in each capacitor,
     * the total zero pressure filling volume, and the total blood volume loss
     * during an othostatic stress intervention and compares the sum of these
     * three to the total blood volume constant in the parameter vector. Any
     * difference between the two will be corrected for at the inferior vena
     * cava.
     */
// THIS FUNCTION DOESN'T DO ANYTHING
    public static int fixvolume(Data_vector p, Reflex_vector ref, Parameters theta) {

        // This function doesn't do anthing, so just return 0 right away!
        return 0;

        // JLL The rest of the function is commented out because of the above line
/*
         * double diff = 0.0;
         *
         * diff = ((p.x[0]-p.x[24])*p.c[3] + (p.x[3]-p.x[24])*p.c[1] +
         * (p.x[4]-p.x[24])*theta.get(PVName.PV47) +
         * (p.x[5]-p.x[24])*theta.get(PVName.PV48) +
         * p.x[1]*theta.get(PVName.PV153) + p.x[2]*theta.get(PVName.PV154)) +
         * theta.get(PVName.PV75) - theta.get(PVName.PV70);
         *
         * // Correct for any difference in blood volume at the inferior vena
         * cava
         * // compartment.
         * // p . x[12] -= diff/theta.get(PVName.PV154);
         *
         * // printf("%e %e %e %e %e\n", p.time[0], p.time[1], p.c[3], p.c[1],
         * diff);
         *
         * return 0;
         */
    }
}
