package jcvsim.compartment21;

import static java.lang.Math.PI;
import jcvsim.common.CircularDoublesList;
import jcvsim.common.ControlState;
import jcvsim.common.ImpulseResponse;
import static jcvsim.common.Maths.atan;
import static jcvsim.common.Maths.periodFromRatePerMinute;
import static jcvsim.common.Maths.ratePerMinuteFromPeriod;
import static jcvsim.common.Maths.sqrt;
import static jcvsim.common.Maths.tan;
import jcvsim.common.OneLevelHistory;
import jcvsim.common.Reflex_vector;
import static jcvsim.compartment21.Data_vector.CompartmentIndex.*;
import static jcvsim.compartment21.Data_vector.ComplianceIndex.*;
import static jcvsim.compartment21.Data_vector.TimeIndex.*;

/*
 * This files contains the subroutines for the reflex model as well as the
 * IPFM model of the cardiac pacemaker. sinoatrialNode() computes the onset time
 * of the next cardiac contraction based on the history of the autonomic
 * efferent (instantaneous) heart rate signal which is taken to be a measure
 * of autonomic activity.  Queue() is the main reflex routine. Here the blood
 * pressure variables are averaged and stored, the convolution integrals are
 * computed and the effector variables are computed and updated. Queue_reset()
 * resets the variables of queue() that have been defined as static variables.
 *
 * Thomas Heldt March 13th, 2002
 * Last modified April 10th, 2003
 */
// Converted to Java Jason Leake December 2016
public class Reflex {

    public static final int I_LENGTH = 960;   // Signal history array size = 60 s / 0.0625 s
    private static final double S_GRAN = 0.0625;  // Signal history bin size, in seconds
    private static final double S_INT = 0.25;    // Averaging interval chosen such that 4 bins fit into
    // one interval of size S_INT
    private static final int S_LENGTH = 4;     // Number of bins of size S_GRAN to fit into averaging
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

    private final CircularDoublesList lungVolumesList = new CircularDoublesList(I_LENGTH);

    // Baroreflex arc responses - latest and previous
    OneLevelHistory paraSympResponse = new OneLevelHistory();
    OneLevelHistory artAlphaSympResponse = new OneLevelHistory();
    OneLevelHistory betaSympResponse = new OneLevelHistory();
    OneLevelHistory artResAlphaSympResponse = new OneLevelHistory();

    // Cardiopulmonary reflex arc responses - latest and previous
    OneLevelHistory venAlphaSympResponse = new OneLevelHistory();
    OneLevelHistory venToneAlphaSympResponse = new OneLevelHistory();

    // Lung volume reflex arc responses - latest and previous
    OneLevelHistory breathingParaSympResponse = new OneLevelHistory();
    OneLevelHistory breathingBetaSympResponse = new OneLevelHistory();

    // These are the fixed impulse response curves for the different reflexes
    private final ImpulseResponse paraSympatheticImpulseResponse;
    private final ImpulseResponse betaSympatheticImpulseResponse;
    private final ImpulseResponse arterialAlphaSympatheticImpulseResponse;
    private final ImpulseResponse venousAlphaSympatheticImpulseResponse;
    private final ImpulseResponse arterialResistanceAlphaSympatheticImpulseResponse;
    private final ImpulseResponse venousToneAlphaSympatheticImpulseResponse;
    private final ImpulseResponse breathingParaSympatheticImpulseResponse;
    private final ImpulseResponse breathingBetaSympatheticImpulseResponse;
    private final boolean DEBUG = false;

    public Reflex(Parameters parameterVector) {
        /*
         * The ImpulseVectors are time-advanced by 3.0*S_GRAN
         * to take care of time delays encountered by the signal averaging
         * procedure (2.0*S_GRAN; see below) and to allow for linear
         * interpolation between successive convolutions (1.0*S_GRAN).
         */

        // Set up parasympathetic impulse response function
        paraSympatheticImpulseResponse = new ImpulseResponse("parasympathetic",
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

        /*
        These two vectors exist in rcsvim.  In that program they have a unity
        integral and are scaled as follows.  bres is the beta sympathetic response
        and pres is the parasympathetic response.  rcvsim is written in modsim
        and these are vectors in modsim
        
        % Scaling by supine static gain values.
        %bres = th(38)*((7.2/9)/60000)*sir;
        %pres = th(39)*((15/9)/60000)*vir;

        % Scaling by standing static gain values.
        bres = th(53)*th(38)*0.5*((18/9)/60000)*sir;
        pres = th(52)*th(39)*0.3*((17.6/9)/60000)*vir;
        
        The overall response is just bres-pres
        
        h = bres-pres;
        
        The th() vector contains runtime constants, and is loaded from other
        parameters:
        
        th(52) = dgainp = 1
        th(53) = dgains = 1
        th(38) = bgain = 1
        th(39) = pgain = 1
        
        The time is advanced by 1.5 seconds because in rcvsim "The impulse
        response is defined here by a linear combination of [the two responses],
        each of which are advanced in time by 1.5 s in order to account for the
        noncausality of this mechanism"
         */
        breathingParaSympatheticImpulseResponse = new ImpulseResponse("breathing parasympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.LUNG_VOLUME_DELAY_PARASYMP),
                parameterVector.get(ParameterName.LUNG_VOLUME_PEAK_PARASYMP),
                parameterVector.get(ParameterName.LUNG_VOLUME_END_PARASYMP),
                3.0 * S_GRAN);

        breathingBetaSympatheticImpulseResponse = new ImpulseResponse("breathing betasympathetic",
                I_LENGTH, S_GRAN,
                parameterVector.get(ParameterName.LUNG_VOLUME_DELAY_BETA_SYMP),
                parameterVector.get(ParameterName.LUNG_VOLUME_PEAK_BETA_SYMP),
                parameterVector.get(ParameterName.LUNG_VOLUME_END_BETA_SYMP),
                3.0 * S_GRAN);

    }

    /**
     * The following subroutine is the implementation of the Integral Pulse
     * Frequency Modulation (IPFM) model of the cardiac pacemaker (also known as
     * Integrate To Threshold (ITT) model). The method reads various timing
     * information from the data vector and updates the variable cardiac_time.
     * The history of the autonomic input to the pacemaker is stored in r.hr[2]
     * of the reflex vector.
     *
     * @param dataVector
     * @param reflexVector
     * @param parameterVector
     * @param dt
     * @return
     */
    public double sinoatrialNode(Data_vector dataVector,
            Reflex_vector reflexVector,
            Parameters parameterVector,
            double dt) {

        final double IPFM_INTEGRAL_THRESHOLD = 60.0;

        double cumulativeHeartRateSignal = reflexVector.cumulativeHeartRateSignal;
        double a_time = dataVector.time[SECONDS_INTO_CARDIAC_CYCLE];
        double v_time = dataVector.time[SECONDS_INTO_VENTRICULAR_CONTRACTION];

        double a_time_old = dataVector.time_new[SIMULATION_TIME];
        double v_time_new = dataVector.time_new[MODIFIED_SIMULATION_TIME];

        double PR_new = dataVector.time_new[PR_DELAY_TIME];
        double PR_old = dataVector.time[PR_DELAY_TIME];
        double Tv_old = dataVector.time[VENTRICULAR_SYSTOLE_TIME_OFFSET];

        double t_onset = 0.0;                  // stores RR-interval (cardiac time)
        double hr = reflexVector.afferentHeartRateSignal;                // afferent HR signal from
        // baroreceptors
        int numberOfIntegrationSteps = reflexVector.stepCount;                 // number of integration steps

        // The following two lines define the values of the IPFM integral before
        // (f_old) and after (f_new) a time step of size dt is taken.
        double f_old = cumulativeHeartRateSignal * a_time / numberOfIntegrationSteps;
        double f_new = (cumulativeHeartRateSignal + hr) * (a_time + dt) / (numberOfIntegrationSteps + 1);

        // The following IF-statement determines whether or not a new beat should
        // be initiated. It does so only if the current value of the IPFM 
        // (Integral Pulse Frequency Modulation) integral
        // exceeds the value of 60 AND IF the absolute refractory period of 1.2
        // times the ventricular contraction time has passed since the onset of 
        // the ventricular cardiac contraction.
        if (f_new >= IPFM_INTEGRAL_THRESHOLD) {
            // The following lines compute the time in the cardiac cycle at 
            // which the IPFM integral hits the threshold value of 60.0.
            if (f_old <= IPFM_INTEGRAL_THRESHOLD) {
                t_onset = a_time + dt * (IPFM_INTEGRAL_THRESHOLD - f_old) / (f_new - f_old);
            } else {
                t_onset = a_time + dt;
            }

            // Update the cardiac timing variables for the next beat to be taken.
            dataVector.time_new[PR_DELAY_TIME] = parameterVector.get(ParameterName.PR_INTERVAL) * sqrt(t_onset);  // PR-interval
            dataVector.time_new[ATRIAL_SYSTOLE_TIME_OFFSET] = parameterVector.get(ParameterName.ATRIAL_SYSTOLE_INTERVAL) * sqrt(t_onset);  // atrial systole
            dataVector.time_new[VENTRICULAR_SYSTOLE_TIME_OFFSET] = parameterVector.get(ParameterName.VENTRICULAR_SYSTOLE_INTERVAL) * sqrt(t_onset);  // ventricular systole
        }

        // Determine whether a new atrial or ventricular contraction should be
        // started. A new atrial contraction is initiated by re-setting the time in
        // the cardiac cycle, setting the number of time steps to 1, and re-setting
        // the cummulative heart rate signal. A new ventricular contraction is
        // initiated by re-setting the ventricular time.
        // Check whether atrial and ventricular contractions are completed.
        if ((f_new >= IPFM_INTEGRAL_THRESHOLD) && ((a_time - PR_old) > 1.5 * Tv_old)) {
            a_time = t_onset - a_time;
            a_time_old = t_onset - a_time;
            v_time_new = v_time = a_time - PR_new;

            // Update the cardiac timing variables for the next beat to be taken.
            dataVector.time[PR_DELAY_TIME] = parameterVector.get(ParameterName.PR_INTERVAL) * sqrt(t_onset);  // PR-interval
            dataVector.time[ATRIAL_SYSTOLE_TIME_OFFSET] = parameterVector.get(ParameterName.ATRIAL_SYSTOLE_INTERVAL) * sqrt(t_onset);  // atrial systole
            dataVector.time[VENTRICULAR_SYSTOLE_TIME_OFFSET] = parameterVector.get(ParameterName.VENTRICULAR_SYSTOLE_INTERVAL) * sqrt(t_onset);  // ventricular systole

            // Update the contractility feedback.
            dataVector.compliance[RV_END_SYSTOLIC_COMPL] = reflexVector.rvEndSystolicCompliance;
            dataVector.compliance[LV_END_SYSTOLIC_COMPL] = reflexVector.lvEndSystolicCompliance;

            reflexVector.instantaneousHeartRate = ratePerMinuteFromPeriod(t_onset);
            cumulativeHeartRateSignal = ratePerMinuteFromPeriod(t_onset);
            numberOfIntegrationSteps = 1;
        } else if ((f_new >= IPFM_INTEGRAL_THRESHOLD) && ((a_time - PR_old + PR_new) > 1.5 * Tv_old)) {
            // Ventricular contraction is sufficiently completed for a 
            // new atrial contraction to be initiated.
            a_time = t_onset - a_time;
            v_time_new = a_time - PR_new;
            a_time_old += dt;
            v_time += dt;

            // Update the atrial systolic time interval.
            dataVector.time[ATRIAL_SYSTOLE_TIME_OFFSET] = dataVector.time_new[ATRIAL_SYSTOLE_TIME_OFFSET];  // atrial systole

            reflexVector.instantaneousHeartRate = ratePerMinuteFromPeriod(t_onset);
            cumulativeHeartRateSignal = ratePerMinuteFromPeriod(t_onset);
            numberOfIntegrationSteps = 1;
        } else {
            // Update the timing information if no new beat is to be initiated.
            a_time_old += dt;
            a_time += dt;
            v_time_new += dt;

            if ((a_time_old - PR_old) > 1.5 * Tv_old) {
                v_time = v_time_new;

                // Update the PR delay, the ventricular systolic interval, and the
                // contractility feedback.
                dataVector.time[PR_DELAY_TIME] = dataVector.time_new[PR_DELAY_TIME];  // PR-interval
                dataVector.time[VENTRICULAR_SYSTOLE_TIME_OFFSET] = dataVector.time_new[VENTRICULAR_SYSTOLE_TIME_OFFSET];  // ventricular systole
                dataVector.compliance[RV_END_SYSTOLIC_COMPL] = reflexVector.rvEndSystolicCompliance;
                dataVector.compliance[LV_END_SYSTOLIC_COMPL] = reflexVector.lvEndSystolicCompliance;
                a_time_old = a_time;
            } else {
                v_time += dt;
            }

            // Updates cummulative heart rate signal, time step counter, and cardiac
            // time if no new beat is initiated.
            cumulativeHeartRateSignal += reflexVector.afferentHeartRateSignal;
            numberOfIntegrationSteps++;
        }

        dataVector.time_new[SIMULATION_TIME] = a_time_old;
        dataVector.time_new[MODIFIED_SIMULATION_TIME] = v_time_new;
        dataVector.time[SECONDS_INTO_VENTRICULAR_CONTRACTION] = v_time;
        dataVector.time[MODIFIED_SIMULATION_TIME] = dataVector.time[SIMULATION_TIME] + dt;
        reflexVector.cumulativeHeartRateSignal = cumulativeHeartRateSignal;                // Write new cummulative heart rate and 
        reflexVector.stepCount = numberOfIntegrationSteps;                  // step counter to reflex vector.
        //  System.out.println(v_time + " " + v_time_new);
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
     * @param dataVector
     * @param reflexVector
     * @param pvec
     * @param dt
     * @param intraThoracicPressure
     * @param controlState
     */
    public void queue(Data_vector dataVector,
            Reflex_vector reflexVector,
            Parameters pvec, double dt,
            IntraThoracicPressure intraThoracicPressure,
            ControlState controlState) {

        double con = 0.0;                            // dummy variables

        // Compute the running integral of arterial and right atrial pressures by
        // Backward Euler over an interval of length S_GRAN. 
        // Compute the average over the first S_GRAN seconds of simulation time.
        if (dataVector.time[SIMULATION_TIME] < S_INT) {
            if ((signalTimeIntegral + dt) < S_GRAN) {
                signalTimeIntegral += dt;
                rightAtrialPressureIntegral += (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * dt;
                arterialPressureIntegral += (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.tilt[0]) * dt;
            } else {
                // Account for the fraction of the last time step that fell within
                // the averaging interval - again: Backward Euler integration.
                rightAtrialPressureIntegral += (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * (S_GRAN - signalTimeIntegral);
                arterialPressureIntegral += (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.tilt[0]) * (S_GRAN - signalTimeIntegral);

                arterialPressureBins.push(arterialPressureIntegral / S_GRAN);
                rightAtrialPressureBins.push(rightAtrialPressureIntegral / S_GRAN);

                meanArterialPressure = arterialPressureBins.getMean();
                meanRightAtrialPressure = rightAtrialPressureBins.getMean();

                double sensedArterialPressure = atan((meanArterialPressure - pvec.get(ParameterName.ABR_SET_POINT))
                        / pvec.get(ParameterName.ABR_SCALING_FACTOR))
                        * pvec.get(ParameterName.ABR_SCALING_FACTOR);
                double sensedRightAtrialPressure = atan((meanRightAtrialPressure - pvec.get(ParameterName.CPR_SET_POINT))
                        / pvec.get(ParameterName.CPR_SCALING_FACTOR))
                        * pvec.get(ParameterName.CPR_SCALING_FACTOR);

                lungVolumesList.push(intraThoracicPressure.getTidalLungVolume());

                dataVector.pressure[BIAS_3_CPI] = sensedArterialPressure;

                arterialPressureSignalList.push(sensedArterialPressure);
                rightAtrialPressureSignalList.push(sensedRightAtrialPressure);

                // Account for the fraction of the last time step that fell outside
                // the previous averaging interval - again: Backward Euler
                // integration.
                signalTimeIntegral = signalTimeIntegral + dt - S_GRAN;

                rightAtrialPressureIntegral = (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * signalTimeIntegral;
                arterialPressureIntegral = (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.tilt[0]) * signalTimeIntegral;
            }
        } // Compute the reflex response for the remainder of the simulation time.
        else if ((signalTimeIntegral + dt) < S_GRAN) {
            signalTimeIntegral += dt;

            rightAtrialPressureIntegral += (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * dt;
            arterialPressureIntegral += (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.tilt[0]) * dt;
        } else {
            // Account for the fraction of the last time step that fell within
            // the averaging interval - again: Backward Euler integration.

            rightAtrialPressureIntegral += (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * (S_GRAN - signalTimeIntegral);
            arterialPressureIntegral += (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.tilt[0]) * (S_GRAN - signalTimeIntegral);

            arterialPressureBins.push(arterialPressureIntegral / S_GRAN);
            rightAtrialPressureBins.push(rightAtrialPressureIntegral / S_GRAN);

            meanArterialPressure = arterialPressureBins.getMean();
            meanRightAtrialPressure = rightAtrialPressureBins.getMean();

            double sensedArterialPressure = atan((meanArterialPressure - pvec.get(ParameterName.ABR_SET_POINT)) / pvec.get(ParameterName.ABR_SCALING_FACTOR))
                    * pvec.get(ParameterName.ABR_SCALING_FACTOR);
            double sensedRightAtrialPressure = atan((meanRightAtrialPressure - pvec.get(ParameterName.CPR_SET_POINT)) / pvec.get(ParameterName.CPR_SCALING_FACTOR))
                    * pvec.get(ParameterName.CPR_SCALING_FACTOR);

            lungVolumesList.push(intraThoracicPressure.getTidalLungVolume());

            dataVector.pressure[BIAS_3_CPI] = sensedArterialPressure;

            arterialPressureSignalList.push(sensedArterialPressure);
            rightAtrialPressureSignalList.push(sensedRightAtrialPressure);

            double newParaSympResp = 0.;
            double newBetaSympResp = 0.;
            double newArtAlphaSympResp = 0.;
            double newArtResAlphaSympResp = 0.;
            double newVenAlphaSympResp = 0.;
            double newVenToneAlphaSympResp = 0.;
            double newBreathingParaSympResp = 0.;
            double newBreathingBetaSympResp = 0.;

            // Do the convolution integrals
            for (int timeslot = 0; timeslot < I_LENGTH; timeslot++) {
                newParaSympResp += paraSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                newBetaSympResp += betaSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                newArtAlphaSympResp += arterialAlphaSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                newVenAlphaSympResp += venousAlphaSympatheticImpulseResponse.get(timeslot) * arterialPressureSignalList.get(timeslot);
                newArtResAlphaSympResp += arterialResistanceAlphaSympatheticImpulseResponse.get(timeslot) * rightAtrialPressureSignalList.get(timeslot);
                newVenToneAlphaSympResp += venousToneAlphaSympatheticImpulseResponse.get(timeslot) * rightAtrialPressureSignalList.get(timeslot);
                newBreathingParaSympResp += breathingParaSympatheticImpulseResponse.get(timeslot) * lungVolumesList.get(timeslot);
                newBreathingBetaSympResp += breathingBetaSympatheticImpulseResponse.get(timeslot) * lungVolumesList.get(timeslot);
            }

            paraSympResponse.push(newParaSympResp);
            artAlphaSympResponse.push(newArtAlphaSympResp);
            betaSympResponse.push(newBetaSympResp);
            artResAlphaSympResponse.push(newArtResAlphaSympResp);
            venAlphaSympResponse.push(newVenAlphaSympResp);
            venToneAlphaSympResponse.push(newVenToneAlphaSympResp);
            breathingParaSympResponse.push(newBreathingParaSympResp);
            breathingBetaSympResponse.push(newBreathingBetaSympResp);

            // Account for the fraction of the last time step that fell outside
            // the previous averaging interval - again: Backward Euler
            // integration.
            signalTimeIntegral = signalTimeIntegral + dt - S_GRAN;

            rightAtrialPressureIntegral = (dataVector.pressure[RIGHT_ATRIAL_CPI] - intraThoracicPressure.getValue()) * signalTimeIntegral;
            arterialPressureIntegral = (dataVector.pressure[ASCENDING_AORTIC_CPI] + dataVector.tilt[0]) * signalTimeIntegral;
        }

        // Linear interpolation between successive reflex system updates.
        double artAlphaSympResp = 0.0;
        double artResAlphaSympResp = 0.0;
        double paraSympResp = 0.0;
        double betaSympResp = 0.0;
        double venAlphaSympResp = 0.0;
        double venToneAlphaSympResp = 0.0;
        double breathingParaSympResp = 0.0;
        double breathingBetaSympResp = 0.0;

        // if the arterial baroreflex control system is on...
        if (controlState.abReflexEnabled) {
            paraSympResp = paraSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
            betaSympResp = betaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
            artAlphaSympResp = artAlphaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
            venAlphaSympResp = venAlphaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
        }

        // if the cardiopulmonary reflex control system is on...
        if (controlState.cpReflexEnabled) {
            artResAlphaSympResp = artResAlphaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
            venToneAlphaSympResp = venToneAlphaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
        }

        // if lung volume reflex is turned on
        if (controlState.lungVolumeReflexEnabled) {
            breathingParaSympResp = -breathingParaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
            breathingBetaSympResp = breathingBetaSympResponse.linearInterpolate(signalTimeIntegral / S_GRAN);
        }

        // Heart rate control
        double afferantHeartRatePeriod = periodFromRatePerMinute(pvec.get(ParameterName.NOMINAL_HEART_RATE))
                + pvec.get(ParameterName.ABR_HR_SYMPATHETIC_GAIN) * betaSympResp
                + pvec.get(ParameterName.ABR_HR_PARASYMPATHETIC_GAIN) * paraSympResp
                + pvec.get(ParameterName.LUNG_VOLUME_HR_PARASYMP_GAIN) * breathingParaSympResp
                + pvec.get(ParameterName.LUNG_VOLUME_HR_BETA_SYMP_GAIN) * breathingBetaSympResp;

        if (DEBUG) {

            System.out.println("Breathing effect on afferent heart rate is "
                    + (pvec.get(ParameterName.LUNG_VOLUME_HR_PARASYMP_GAIN) * breathingParaSympResp + " and "
                    + pvec.get(ParameterName.LUNG_VOLUME_HR_BETA_SYMP_GAIN) * breathingBetaSympResp));

            System.out.println("Baroreflex effect on afferent heart rate is "
                    + pvec.get(ParameterName.ABR_HR_PARASYMPATHETIC_GAIN) * paraSympResp + " and "
                    + pvec.get(ParameterName.ABR_HR_SYMPATHETIC_GAIN) * betaSympResp);

        }

        reflexVector.afferentHeartRateSignal = ratePerMinuteFromPeriod(afferantHeartRatePeriod);

        // Contractility feedback. Limit contractility feedback so end-systolic
        // ventricular elastances do not become too large during severe stress.
        reflexVector.rvEndSystolicCompliance = pvec.get(ParameterName.RV_SYSTOLIC_COMPLIANCE)
                + pvec.get(ParameterName.ABR_RV_CONTRACTILITY_SYMPATHETIC_GAIN) * betaSympResp;
        if (reflexVector.rvEndSystolicCompliance > 0.01) {
            reflexVector.rvEndSystolicCompliance = 0.01;
        }

        reflexVector.lvEndSystolicCompliance = pvec.get(ParameterName.LV_SYSTOLIC_COMPLIANCE)
                + pvec.get(ParameterName.ABR_LV_CONTRACTILITY_SYMPATHETIC_GAIN) * betaSympResp;
        if (reflexVector.lvEndSystolicCompliance > 0.3) {
            reflexVector.lvEndSystolicCompliance = 0.3;
        }

        // Peripheral resistance feedback
        reflexVector.resistance[0] = pvec.get(ParameterName.UBODY_MICRO_RESISTANCE)
                + pvec.get(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_UPPER_BODY) * artAlphaSympResp
                + pvec.get(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_UBODY) * artResAlphaSympResp;
        reflexVector.resistance[1] = pvec.get(ParameterName.RENAL_MICRO_RESISTANCE)
                + pvec.get(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_KIDNEY) * artAlphaSympResp
                + pvec.get(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_KIDNEY) * artResAlphaSympResp;
        reflexVector.resistance[2] = pvec.get(ParameterName.SPLAN_MICRO_RESISTANCE)
                + pvec.get(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_SPLANCHNIC) * artAlphaSympResp
                + pvec.get(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_SPLANCHNIC) * artResAlphaSympResp;
        reflexVector.resistance[3] = pvec.get(ParameterName.LBODY_MICRO_RESISTANCE)
                + pvec.get(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_LOWER_BODY) * artAlphaSympResp
                + pvec.get(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_LBODY) * artResAlphaSympResp;

        // Venous tone feedback implementation. First, we compute the amount of 
        // blood stored in the non-linear compartments, then we compute the 
        // new zero pressure filling volumes and update the pressures
        // accordingly.
        con = PI * pvec.get(ParameterName.SPLAN_VEN_COMPLIANCE) / 2.0 / pvec.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL);
        double Vsp = 2.0 * pvec.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL) * atan(con * (dataVector.pressure[SPLANCHNIC_VENOUS_CPI] - dataVector.pressure[BIAS_1_CPI])) / PI;

        con = PI * pvec.get(ParameterName.LBODY_VEN_COMPLIANCE) / 2.0 / pvec.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL);
        double Vll = 2.0 * pvec.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL) * atan(con * (dataVector.pressure[LBODY_VENOUS_CPI] - dataVector.pressure[BIAS_2_CPI])) / PI;

        double Vupold = reflexVector.volume[0];
        double Vkold = reflexVector.volume[1];
        double Vspold = reflexVector.volume[2];
        double Vllold = reflexVector.volume[3];

        reflexVector.volume[0] = pvec.get(ParameterName.UBODY_VEN_ZPFV)
                + pvec.get(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_UPPER_BODY) * venAlphaSympResp
                + pvec.get(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_UBODY) * venToneAlphaSympResp;
        dataVector.pressure[UPPER_BODY_VENOUS_CPI] += (Vupold - reflexVector.volume[0]) / pvec.get(ParameterName.UBODY_VEN_COMPLIANCE);

        reflexVector.volume[1] = pvec.get(ParameterName.RENAL_VEN_ZPFV)
                + pvec.get(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_KIDNEY) * venAlphaSympResp
                + pvec.get(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_KIDNEY) * venToneAlphaSympResp;
        dataVector.pressure[RENAL_VENOUS_CPI] += (Vkold - reflexVector.volume[1]) / pvec.get(ParameterName.RENAL_VEN_COMPLIANCE);

        reflexVector.volume[2] = pvec.get(ParameterName.SPLAN_VEN_ZPFV)
                + pvec.get(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_SPLANCHNIC) * venAlphaSympResp
                + pvec.get(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_SPLANCHNIC) * venToneAlphaSympResp;
        dataVector.pressure[SPLANCHNIC_VENOUS_CPI] = tan(PI * (Vsp + Vspold - reflexVector.volume[2]) / 2.0 / pvec.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL)) / PI
                / pvec.get(ParameterName.SPLAN_VEN_COMPLIANCE) * 2.0 * pvec.get(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL) + dataVector.pressure[BIAS_1_CPI];

        reflexVector.volume[3] = pvec.get(ParameterName.LBODY_VEN_ZPFV)
                + pvec.get(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_LOWER_BODY) * venAlphaSympResp
                + pvec.get(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_LBODY) * venToneAlphaSympResp;
        dataVector.pressure[LBODY_VENOUS_CPI] = tan(PI * (Vll + Vllold - reflexVector.volume[3]) / 2.0 / pvec.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL)) / PI
                / pvec.get(ParameterName.LBODY_VEN_COMPLIANCE) * 2.0 * pvec.get(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL) + dataVector.pressure[BIAS_2_CPI];
    }

    /**
     * The following routine resets the static variables in the queue routine to
     * their initial values. This is required when multiple simulations are
     * being run in sequence - as is the case with the computation of the
     * gradient matrix.
     */
    public void queue_reset() {
        signalTimeIntegral = 0.0;               // signal time integral  

        arterialPressureIntegral = 0.0;              // arterial pressure integral
        rightAtrialPressureIntegral = 0.0;              // rap integral
        meanArterialPressure = 0.0;              // average arterial pressure
        meanRightAtrialPressure = 0.0;              // average rap integral

        // Baroreflex responses - latest and previous
        paraSympResponse = new OneLevelHistory();
        artAlphaSympResponse = new OneLevelHistory();
        betaSympResponse = new OneLevelHistory();
        artResAlphaSympResponse = new OneLevelHistory();

        // Cardiopulmonary reflex arc responses - latest and previous
        venAlphaSympResponse = new OneLevelHistory();
        venToneAlphaSympResponse = new OneLevelHistory();

        // Lung volume reflex arc responses - latest and previous
        breathingParaSympResponse = new OneLevelHistory();
        breathingBetaSympResponse = new OneLevelHistory();

        arterialPressureBins.zero();
        rightAtrialPressureBins.zero();
        arterialPressureSignalList.zero();
        rightAtrialPressureSignalList.zero();
        lungVolumesList.zero();
    }
}
