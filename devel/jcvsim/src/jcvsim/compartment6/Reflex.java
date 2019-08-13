package jcvsim.compartment6;

import jcvsim.common.CircularDoublesList;
import jcvsim.common.ControlState;
import jcvsim.common.ImpulseResponse;
import static jcvsim.common.Maths.atan;
import static jcvsim.common.Maths.sqrt;
import jcvsim.common.Reflex_vector;
import static jcvsim.compartment6.Data_vector.CompartmentIndex.*;
import static jcvsim.compartment6.Data_vector.ComplianceIndex.*;

/*
 * This files contains the subroutines for the reflex model as well as the
 * IPFM model of the cardiac pacemaker. sinoatrialNode() computes the onset time
 * of the next cardiac contraction based on the history of the autonomic
 * efferent (instantaneous) heart rate signal which is taken to be a measure
 * of autonomic activity. Queue() is the main reflex routine. Here the blood
 * pressure variables are averaged and stored, the convolution integrals are
 * computed and the effector variables are computed and updated. Queue_reset()
 * resets the variables of queue() that have been defined as static variables.
 *
 * Thomas Heldt March 13th, 2002
 * Last modified June 14, 2006
 */
// Converted to Java Jason Leake December 2016
public class Reflex {

    public final int I_LENGTH = 960; // Signal history array size = 60 s / 0.0625 s
    public final double S_GRAN = 0.0625;// Signal history bin size
    public final double S_INT = 0.25;    // Averaging interval chosen such that 4 bins fit into
    // one interval of size S_INT
    public final int S_LENGTH = 4;    // Number of bins of size S_GRAN to fit into averaging
    // interval of length S_INT

    /*
     * The following variables are defined here as static variables to confine
     * their scope to the remainder of this file. They are defined outside the
     * queue() routine such that the routine queue_reset() can be used to
     * reset them to their initial values upon exit of the simulator routine.
     */
    private double signalTimeIntegral = 0.0;               // signal time integral  
    private double arterialPressureIntegral = 0.0;              // arterial pressure integral
    private double rightAtrialPressureIntegral = 0.0;              // rap integral
    private double meanArterialPressure = 0.0;              // average arterial pressure
    private double meanRightAtrialPressure = 0.0;              // average rap integral

    // abp and rap signal queues
    private final CircularDoublesList arterialPressureSignalList = new CircularDoublesList(I_LENGTH);
    private final CircularDoublesList rightAtrialPressureSignalList = new CircularDoublesList(I_LENGTH);

    private final CircularDoublesList arterialPressureBins = new CircularDoublesList(S_LENGTH);
    private final CircularDoublesList rightAtrialPressureBins = new CircularDoublesList(S_LENGTH);

    private double alpha_resp_new = 0.0;
    private double alpha_resp_old = 0.0;
    private double alphav_resp_new = 0.0;
    private double alphav_resp_old = 0.0;
    private double para_resp_new = 0.0;
    private double para_resp_old = 0.0;
    private double beta_resp_new = 0.0;
    private double beta_resp_old = 0.0;

    private double alpha_respv_new = 0.0;
    private double alpha_respv_old = 0.0;
    private double alphav_respv_new = 0.0;
    private double alphav_respv_old = 0.0;

    private final ImpulseResponse parasympatheticImpulseResponse;
    private final ImpulseResponse betaSympatheticImpulseResponse;
    private final ImpulseResponse arterialAlphaSympatheticImpulseResponse;
    private final ImpulseResponse venousAlphaSympatheticImpulseResponse;
    private final ImpulseResponse arterialResistanceAlphaSympatheticImpulseResponse;
    private final ImpulseResponse venousToneAlphaSympatheticImpulseResponse;

    /**
     * Constructor
     *
     * @param parameterVector
     */
    public Reflex(Parameters parameterVector) {
        /*
         * The ImpulseVectors are time-advanced by 3.0*S_GRAN
         * to take care of time delays encountered by the signal averaging 
         * procedure (2.0*S_GRAN; see below) and to allow for linear 
         * interpolation between successive convolutions (1.0*S_GRAN).
         */

        // Set up parasympathetic impulse response function
        parasympatheticImpulseResponse = new ImpulseResponse("parasympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.ABR_DELAY_PARASYMP),
                parameterVector.get(ParameterName.ABR_PEAK_PARASYMP),
                parameterVector.get(ParameterName.ABR_END_PARASYMP),
                3.0 * S_GRAN);

        // Do the same for the beta-sympathetic impulse response function.
        betaSympatheticImpulseResponse = new ImpulseResponse("betasympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.ABR_DELAY_BETA_SYMP),
                parameterVector.get(ParameterName.ABR_PEAK_BETA_SYMP),
                parameterVector.get(ParameterName.ABR_END_BETA_SYMP),
                3.0 * S_GRAN);

        // Do the same for the venous alpha-sympathetic impulse response function.
        venousAlphaSympatheticImpulseResponse = new ImpulseResponse(
                "venous alphasympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.ABR_ALPHA_SYMP_VEN_DELAY),
                parameterVector.get(ParameterName.ABR_ALPHA_SYMP_VEN_PEAK),
                parameterVector.get(ParameterName.ABR_ALPHA_SYMP_VEN_END),
                3.0 * S_GRAN);

        // Do the same for the arterial alpha-sympathetic impulse response function.
        arterialAlphaSympatheticImpulseResponse = new ImpulseResponse(
                "arterial alphasympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.ABR_ALPHA_SYMP_ART_DELAY),
                parameterVector.get(ParameterName.ABR_ALPHA_SYMP_ART_PEAK),
                parameterVector.get(ParameterName.ABR_ALPHA_SYMP_ART_END),
                3.0 * S_GRAN);

        // Do the same for the cardiopulmonary to venous tone alpha-sympathetic
        // impulse response function.
        venousToneAlphaSympatheticImpulseResponse = new ImpulseResponse(
                "venous tone alpha sympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.ALPHA_CPV_DELAY),
                parameterVector.get(ParameterName.ALPHA_CPV_PEAK),
                parameterVector.get(ParameterName.ALPHA_CPV_END),
                3.0 * S_GRAN);

        // Do the same for the cardiopulmonary to arterial resistance
        // alpha-sympathetic impulse response function.
        arterialResistanceAlphaSympatheticImpulseResponse = new ImpulseResponse(
                "arterial resistance alpha sympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.ALPHA_CPA_DELAY),
                parameterVector.get(ParameterName.ALPHA_CPA_PEAK),
                parameterVector.get(ParameterName.ALPHA_CPA_END),
                3.0 * S_GRAN);
    }

    /*
     * The following subroutine is the implementation of the Integral Pulse
     * Frequency Modulation (IPFM) model of the cardiac pacemaker (also known as
     * Integrate To Threshold (ITT) model). Sanode() reads various timing
     * information from the data vector and updates the variable cardiac_time.
     * The history of the autonomic input to the pacemaker is stored in r.hr[2]
     * of
     * the reflex vector.
     */
    public double sinoatrialNode(Data_vector p, Reflex_vector r,
            Parameters parameterVector, double dt) {
        double cum_hr = r.cumulativeHeartRateSignal;
        double a_time = p.time[1];
        double v_time = p.time[6];

        double a_time_old = p.time_new[0];
        double v_time_new = p.time_new[5];

        double PR_new = p.time_new[2];
        double PR_old = p.time[2];
        double Tv_old = p.time[4];

        double f_old = 0.0, f_new = 0.0;       // temp storage for IPFM integrants
        double t_onset = 0.0;                  // stores RR-interval (cardiac time)
        double hr = r.afferentHeartRateSignal;                // afferent HR signal from
        // baroreceptors
        int n = r.stepCount;                 // number of integration steps

        // The following two lines define the values of the IPFM integral before
        // (f_old) and after (f_new) a time step of size dt is taken.
        f_old = cum_hr * a_time / n;
        f_new = (cum_hr + hr) * (a_time + dt) / (n + 1);

        //  printf("%e %e %e %e %d\n", p.time[0], f_old, f_new, t_onset, n);
        // The following IF-statement determines whether or not a new beat should
        // be initiated. It does so only if the current value of the IPFM integral
        // exceeds the value of 60 AND IF the absolute refractory period of 1.2
        // times the ventricular contraction time has passed since the onset of 
        // the ventricular cardiac contraction.
        if (f_new >= 60.0) {
            // The following lines compute the time in the cardiac cycle at 
            // which the IPFM integral hits the threshold value of 60.0.
            if (f_old <= 60.0) {
                t_onset = a_time + dt * (60.0 - f_old) / (f_new - f_old);
            } else {
                t_onset = a_time + dt;
            }

            // Update the cardiac timing variables for the next beat to be taken.
            p.time_new[2] = parameterVector.get(ParameterName.RR_INTERVAL) * sqrt(t_onset);  // PR-interval
            p.time_new[3] = parameterVector.get(ParameterName.ATRIAL_SYSTOLE_INTERVAL) * sqrt(t_onset);  // atrial systole
            p.time_new[4] = parameterVector.get(ParameterName.VENTRICULAR_SYSTOLE_INTERVAL) * sqrt(t_onset);  // ventricular systole
        }

        // Determine whether a new atrial or ventricular contraction should be
        // started. A new atrial contraction is initiated by re-setting the time in
        // the cardiac cycle, setting the number of time steps to 1, and re-setting
        // the cummulative heart rate signal. A new ventricular contraction is
        // initiated by re-setting the ventricular time.
        // Check whether atrial and ventricular contractions are completed.
        if ((f_new >= 60.0) && ((a_time - PR_old) > 1.5 * Tv_old)) {
            a_time = t_onset - a_time;
            a_time_old = t_onset - a_time;
            v_time_new = v_time = a_time - PR_new;

            // Update the cardiac timing variables for the next beat to be taken.
            p.time[2] = parameterVector.get(ParameterName.RR_INTERVAL) * sqrt(t_onset);  // PR-interval
            p.time[3] = parameterVector.get(ParameterName.ATRIAL_SYSTOLE_INTERVAL) * sqrt(t_onset);  // atrial systole
            p.time[4] = parameterVector.get(ParameterName.VENTRICULAR_SYSTOLE_INTERVAL) * sqrt(t_onset);  // ventricular systole

            // Update the contractility feedback.
            p.compliance[RV_END_SYSTOLIC_COMPL] = r.rvEndSystolicCompliance;
            p.compliance[LV_END_SYSTOLIC_COMPL] = r.lvEndSystolicCompliance;

            r.instantaneousHeartRate = 60. / t_onset;
            cum_hr = 60. / t_onset;
            n = 1;
        } // Check whether ventricular contraction is sufficiently completed for a 
        // new atrial contraction to be initiated.
        else if ((f_new >= 60.0) && ((a_time - PR_old + PR_new) > 1.5 * Tv_old)) {
            a_time = t_onset - a_time;
            v_time_new = a_time - PR_new;
            a_time_old += dt;
            v_time += dt;

            // Update the atrial systolic time interval.
            p.time[3] = p.time_new[3];  // atrial systole

            r.instantaneousHeartRate = 60. / t_onset;
            cum_hr = 60. / t_onset;
            n = 1;
        } // Update the timing information if no new beat is to be initiated.
        else {
            a_time_old += dt;
            a_time += dt;
            v_time_new += dt;

            if ((a_time_old - PR_old) > 1.5 * Tv_old) {
                v_time = v_time_new;

                // Update the PR delay, the ventricular systolic interval, and the
                // contractility feedback.
                p.time[2] = p.time_new[2];  // PR-interval
                p.time[4] = p.time_new[4];  // ventricular systole
                p.compliance[RV_END_SYSTOLIC_COMPL] = r.rvEndSystolicCompliance;
                p.compliance[LV_END_SYSTOLIC_COMPL] = r.lvEndSystolicCompliance;
                a_time_old = a_time;
            } else {
                v_time += dt;
            }

            // Updates cummulative heart rate signal, time step counter, and cardiac
            // time if no new beat is initiated.
            cum_hr += r.afferentHeartRateSignal;
            n++;
        }

        //  printf("%e %e %e %e %e\n", p.time[0], a_time, a_time_old, v_time, v_time_new);
        p.time_new[0] = a_time_old;
        p.time_new[5] = v_time_new;
        p.time[6] = v_time;
        p.time[5] = p.time[0] + dt;
        r.cumulativeHeartRateSignal = cum_hr;                // Write new cummulative heart rate and 
        r.stepCount = n;                  // step counter to reflex vector.

        return a_time;
    }

    /**
     * queue() is the main reflex routine. It computes the time-averaged
     * pressures, performs the convolutions, and updates the effector variables.
     * It does so by first averaging the pressure signals in bins of size
     * S_GRAN. A running average over S_LENGTH bins returns the final averaged
     * pressure values which are then used from computation of the convolution
     * integral. The effector variables are updated at every integration step
     * through linear interpolation between the respective values determined by
     * the convolution integral.
     *
     * @param p
     * @param r
     * @param pvec
     * @param dt
     * @param controlState
     */
    public void queue(Data_vector p, Reflex_vector r,
            Parameters pvec, double dt,
            ControlState controlState) {
        double s_abp = 0.0, s_rap = 0.0;             // sensed pressures

        // variables for the arterial and cardiopulmonary convolution integrals
        double alpha_resp = 0.0, alphav_resp = 0.0;
        double para_resp = 0.0, beta_resp = 0.0;
        double alpha_respv = 0.0, alphav_respv = 0.0;

        // variables for the effector mechanisms (venous tone and heart rate)
        double Vold = 0.0;
        double I = 0.0;

        int timeslot = 0;

        // Compute the running integral of arterial and right atrial pressures by
        // Backward Euler over an interval of length S_GRAN. 
        // Compute the average over the first S_GRAN seconds of simulation time.
        if (p.time[0] < S_INT) {
            if ((signalTimeIntegral + dt) < S_GRAN) {
                signalTimeIntegral += dt;

                rightAtrialPressureIntegral += (p.pressure[CENTRAL_VENOUS_CPI] - p.pressure[INTRA_THORACIC_CPI]) * dt;
                arterialPressureIntegral += p.pressure[ARTERIAL_CPI] * dt;
            } else {
                // Account for the fraction of the last time step that fell within
                // the averaging interval - again: Backward Euler integration.

                rightAtrialPressureIntegral += (p.pressure[CENTRAL_VENOUS_CPI] - p.pressure[INTRA_THORACIC_CPI]) * (S_GRAN - signalTimeIntegral);
                arterialPressureIntegral += p.pressure[ARTERIAL_CPI] * (S_GRAN - signalTimeIntegral);

                // Update the averaging bins by wrap-around modulus arithmetic.
                arterialPressureBins.push(arterialPressureIntegral / S_GRAN);
                rightAtrialPressureBins.push(rightAtrialPressureIntegral / S_GRAN);

                meanArterialPressure = meanRightAtrialPressure = 0.0;
                for (timeslot = 0; timeslot < S_LENGTH; timeslot++) {
                    meanArterialPressure += arterialPressureBins.get(timeslot);
                    meanRightAtrialPressure += rightAtrialPressureBins.get(timeslot);
                }

                meanArterialPressure /= S_LENGTH;
                meanRightAtrialPressure /= S_LENGTH;

                s_abp = atan((meanArterialPressure - pvec.get(ParameterName.ABR_SET_POINT)) / pvec.get(ParameterName.ABR_SCALING_FACTOR))
                        * pvec.get(ParameterName.ABR_SCALING_FACTOR);
                s_rap = atan((meanRightAtrialPressure - pvec.get(ParameterName.ABR_HR_SYMPATHETIC_GAIN)) / pvec.get(ParameterName.CPR_SCALING_FACTOR))
                        * pvec.get(ParameterName.CPR_SCALING_FACTOR);

                // Wrap-around modulus arithmetic to update the pressure histories.
                arterialPressureSignalList.push(s_abp);
                rightAtrialPressureSignalList.push(s_rap);

                // Account for the fraction of the last time step that fell outside
                // the previous averaging interval - again: Backward Euler
                // integration.
                signalTimeIntegral = signalTimeIntegral + dt - S_GRAN;

                rightAtrialPressureIntegral = (p.pressure[CENTRAL_VENOUS_CPI] - p.pressure[INTRA_THORACIC_CPI]) * signalTimeIntegral;
                arterialPressureIntegral = p.pressure[ARTERIAL_CPI] * signalTimeIntegral;
            }
        } // Compute the reflex response for the remainder of the simulation time.
        else if ((signalTimeIntegral + dt) < S_GRAN) {
            signalTimeIntegral += dt;

            rightAtrialPressureIntegral += (p.pressure[CENTRAL_VENOUS_CPI] - p.pressure[INTRA_THORACIC_CPI]) * dt;
            arterialPressureIntegral += p.pressure[ARTERIAL_CPI] * dt;
        } else {
            // Account for the fraction of the last time step that fell within
            // the averaging interval - again: Backward Euler integration.

            rightAtrialPressureIntegral += (p.pressure[CENTRAL_VENOUS_CPI] - p.pressure[INTRA_THORACIC_CPI]) * (S_GRAN - signalTimeIntegral);
            arterialPressureIntegral += p.pressure[ARTERIAL_CPI] * (S_GRAN - signalTimeIntegral);

            // Update the averaging bins by wrap-around modulus arithmetic.
            arterialPressureBins.push(arterialPressureIntegral / S_GRAN);
            rightAtrialPressureBins.push(rightAtrialPressureIntegral / S_GRAN);

            meanArterialPressure = meanRightAtrialPressure = 0.0;
            for (timeslot = 0; timeslot < S_LENGTH; timeslot++) {
                meanArterialPressure += arterialPressureBins.get(timeslot);
                meanRightAtrialPressure += rightAtrialPressureBins.get(timeslot);
            }

            meanArterialPressure /= S_LENGTH;
            meanRightAtrialPressure /= S_LENGTH;

            s_abp = atan((meanArterialPressure - pvec.get(ParameterName.ABR_SET_POINT)) / pvec.get(ParameterName.ABR_SCALING_FACTOR))
                    * pvec.get(ParameterName.ABR_SCALING_FACTOR);
            s_rap = atan((meanRightAtrialPressure - pvec.get(ParameterName.ABR_HR_SYMPATHETIC_GAIN)) / pvec.get(ParameterName.CPR_SCALING_FACTOR))
                    * pvec.get(ParameterName.CPR_SCALING_FACTOR);

            // Wrap-around modulus arithmetic to update the pressure histories.
            arterialPressureSignalList.push(s_abp);
            rightAtrialPressureSignalList.push(s_rap);

            // Do the convolution integrals
            for (timeslot = 0; timeslot < I_LENGTH; timeslot++) {
                para_resp += parasympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                beta_resp += betaSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                alpha_resp += arterialAlphaSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                alpha_respv += venousAlphaSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                alphav_resp += arterialResistanceAlphaSympatheticImpulseResponse.get(timeslot) * rightAtrialPressureSignalList.get(timeslot);
                alphav_respv += venousToneAlphaSympatheticImpulseResponse.get(timeslot) * rightAtrialPressureSignalList.get(timeslot);
            }

            para_resp_old = para_resp_new;
            para_resp_new = para_resp;
            beta_resp_old = beta_resp_new;
            beta_resp_new = beta_resp;
            alpha_resp_old = alpha_resp_new;
            alpha_resp_new = alpha_resp;
            alphav_resp_old = alphav_resp_new;
            alphav_resp_new = alphav_resp;

            alpha_respv_old = alpha_respv_new;
            alpha_respv_new = alpha_respv;
            alphav_respv_old = alphav_respv_new;
            alphav_respv_new = alphav_respv;

            // Account for the fraction of the last time step that fell outside
            // the previous averaging interval - again: Backward Euler
            // integration.
            signalTimeIntegral = signalTimeIntegral + dt - S_GRAN;

            rightAtrialPressureIntegral = (p.pressure[CENTRAL_VENOUS_CPI] - p.pressure[INTRA_THORACIC_CPI]) * signalTimeIntegral;
            arterialPressureIntegral = p.pressure[ARTERIAL_CPI] * signalTimeIntegral;
        }

        // Linear interpolation between successive reflex system updates.
        para_resp = 0.0;
        beta_resp = 0.0;
        alpha_resp = 0.0;
        alphav_resp = 0.0;
        alpha_respv = 0.0;
        alphav_respv = 0.0;

        // if the arterial baroreflex control system is on...
        if (controlState.abReflexEnabled) {
            para_resp = (para_resp_new - para_resp_old) * signalTimeIntegral / S_GRAN + para_resp_old;
            beta_resp = (beta_resp_new - beta_resp_old) * signalTimeIntegral / S_GRAN + beta_resp_old;
            alpha_resp = (alpha_resp_new - alpha_resp_old) * signalTimeIntegral / S_GRAN + alpha_resp_old;
            alpha_respv = (alpha_respv_new - alpha_respv_old) * signalTimeIntegral / S_GRAN + alpha_respv_old;
        }

        // if the cardiopulmonary reflex control system is on...
        if (controlState.cpReflexEnabled) {
            alphav_resp = (alphav_resp_new - alphav_resp_old) * signalTimeIntegral / S_GRAN + alphav_resp_old;
            alphav_respv = (alphav_respv_new - alphav_respv_old) * signalTimeIntegral / S_GRAN + alphav_respv_old;
        }

        // Heart rate control
        I = 60. / pvec.get(ParameterName.NOMINAL_HEART_RATE)
                + (pvec.get(ParameterName.ABR_HR_SYMPATHETIC_GAIN) * beta_resp + pvec.get(ParameterName.ABR_HR_PARASYMPATHETIC_GAIN) * para_resp);
        r.afferentHeartRateSignal = 60. / I;

        // Contractility feedback. Limit contractility feedback so end-systolic
        // ventricular elastances do not become too large during severe stress.
        // Index-0 represents right ventricle; index-1 is the left ventricle.
        r.rvEndSystolicCompliance = pvec.get(ParameterName.RV_SYSTOLIC_COMPLIANCE) + pvec.get(ParameterName.ABR_CONTRACT_RV_SYMP_GAIN) * beta_resp;
        r.rvEndSystolicCompliance = (r.rvEndSystolicCompliance > 0.3 ? r.rvEndSystolicCompliance : 0.3);

        r.lvEndSystolicCompliance = pvec.get(ParameterName.LV_SYSTOLIC_COMPLIANCE) + pvec.get(ParameterName.ABR_CONTRACT_LV_SYMP_GAIN) * beta_resp;
        r.lvEndSystolicCompliance = (r.lvEndSystolicCompliance > 0.01 ? r.lvEndSystolicCompliance : 0.01);

        // Peripheral resistance feedback
        r.resistance[0] = pvec.get(ParameterName.TOTAL_PERIPHERAL_RESISTANCE) + pvec.get(ParameterName.ABR_ART_RESISTANCE_SYMP_GAIN) * alpha_resp
                + pvec.get(ParameterName.CPR_ART_RESISTANCE_SYMP_GAIN) * alphav_resp;

        // Venous tone feedback implementation. First, we compute the amount of 
        // blood stored in the non-linear compartments, then we compute the 
        // new zero pressure filling volumes and update the pressures
        // accordingly.
        Vold = r.volume[0];

        r.volume[0] = pvec.get(ParameterName.KIDNEY_COMPARTMENT_ZPFV) + pvec.get(ParameterName.SPLAN_COMPARTMENT_ZPFV) + pvec.get(ParameterName.LBODY_COMPARTMENT_ZPFV)
                + pvec.get(ParameterName.ABDOM_VEN_ZPFV) + pvec.get(ParameterName.INFERIOR_VENA_CAVA_ZPFV) + pvec.get(ParameterName.SUPERIOR_VENA_CAVA_ZPFV)
                + pvec.get(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN) * alpha_respv + pvec.get(ParameterName.CPR_VEN_TONE_SYMP_GAIN) * alphav_respv;

        p.pressure[CENTRAL_VENOUS_CPI] += (Vold - r.volume[0]) / pvec.get(ParameterName.VEN_COMPLIANCE);
    }


    /*
     * The following routine resets some of the static variables in the queue
     * routine to their initial values. This is required when multiple
     * simulations
     * are being run in sequence - as is the case with the computation of the
     * gradient matrix.
     */
    public void queue_reset() {

        signalTimeIntegral = 0.0;
        arterialPressureIntegral = 0.0;
        rightAtrialPressureIntegral = 0.0;
        meanArterialPressure = 0.0;
        meanRightAtrialPressure = 0.0;

        alpha_resp_new = alpha_resp_old = 0.0;
        alphav_resp_new = alphav_resp_old = 0.0;
        beta_resp_new = beta_resp_old = 0.0;
        para_resp_new = para_resp_old = 0.0;
        alpha_respv_new = alpha_respv_old = 0.0;
        alphav_respv_new = alphav_respv_old = 0.0;

        arterialPressureBins.zero();
        rightAtrialPressureBins.zero();
        arterialPressureSignalList.zero();
        rightAtrialPressureSignalList.zero();
    }

}
