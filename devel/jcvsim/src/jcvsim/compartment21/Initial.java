package jcvsim.compartment21;

import jcvsim.parameters.CardiacCompartmentParameters;
import jcvsim.parameters.CompartmentParameters;
import jcvsim.parameters.MicrocirculationResistanceParameters;
import jcvsim.parameters.ReflexParameters;
import jcvsim.parameters.ReflexTimingParameters;
import jcvsim.parameters.SystemParameters;

class Initial {

    private final CompartmentParameters[] compartmentParameters;
    private final CardiacCompartmentParameters cardiac[];
    private final MicrocirculationResistanceParameters micro_r;
    private final SystemParameters system;
    private final ReflexParameters abrReflex;
    private final ReflexParameters cprReflex;

    private final ReflexTimingParameters reflexTiming;

    /**
     * Constructor
     */
    Initial() {

        // Declare parameter value structures.
        compartmentParameters = new CompartmentParameters[17];
        for (int index = 0; index < compartmentParameters.length; index++) {
            compartmentParameters[index] = new CompartmentParameters();
        }
        cardiac = new CardiacCompartmentParameters[2];
        cardiac[0] = new CardiacCompartmentParameters();
        cardiac[1] = new CardiacCompartmentParameters();
        abrReflex = new ReflexParameters();
        cprReflex = new ReflexParameters();
        micro_r = new MicrocirculationResistanceParameters();
        system = new SystemParameters();
        reflexTiming = new ReflexTimingParameters();

        double alpha = 1.;
        double beta = 1.;

        // Ascending aorta compartment
        compartmentParameters[0].compliance[0][0] = 0.28;
        compartmentParameters[0].compliance[0][1] = 0.04;
        compartmentParameters[0].compliance[0][2] = 0.16;
        compartmentParameters[0].compliance[0][3] = 0.40;

        compartmentParameters[0].zpfv[0][0] = 21.0;
        compartmentParameters[0].zpfv[0][1] = 3.0;
        compartmentParameters[0].zpfv[0][2] = 10.0;
        compartmentParameters[0].zpfv[0][3] = 32.0;

        compartmentParameters[0].resistance[0][0] = 0.007;
        compartmentParameters[0].resistance[0][1] = 0.002;
        compartmentParameters[0].resistance[0][2] = 0.0;
        compartmentParameters[0].resistance[0][3] = 0.013;

        compartmentParameters[0].height[0][0] = 10.0;
        compartmentParameters[0].height[0][1] = 0.5;
        compartmentParameters[0].height[0][2] = 8.0;
        compartmentParameters[0].height[0][3] = 12.0;

        // Brachiocephalic arterial compartment
        compartmentParameters[1].compliance[0][0] = 0.13;
        compartmentParameters[1].compliance[0][1] = 0.02;
        compartmentParameters[1].compliance[0][2] = 0.07;
        compartmentParameters[1].compliance[0][3] = 0.20;

        compartmentParameters[1].zpfv[0][0] = 5.0;
        compartmentParameters[1].zpfv[0][1] = 1.0;
        compartmentParameters[1].zpfv[0][2] = 2.0;
        compartmentParameters[1].zpfv[0][3] = 8.0;

        compartmentParameters[1].resistance[0][0] = 0.003;
        compartmentParameters[1].resistance[0][1] = 0.001;
        compartmentParameters[1].resistance[0][2] = 0.0;
        compartmentParameters[1].resistance[0][3] = 0.006;

        compartmentParameters[1].height[0][0] = 4.5;
        compartmentParameters[1].height[0][1] = 0.5;
        compartmentParameters[1].height[0][2] = 3.0;
        compartmentParameters[1].height[0][3] = 6.0;

        // Upper body arterial compartment
        compartmentParameters[2].compliance[0][0] = 0.2;
        compartmentParameters[2].compliance[0][1] = 0.1;
        compartmentParameters[2].compliance[0][2] = 0.1;
        compartmentParameters[2].compliance[0][3] = 0.7;

        compartmentParameters[2].zpfv[0][0] = 200.0;
        compartmentParameters[2].zpfv[0][1] = 40.0;
        compartmentParameters[2].zpfv[0][2] = 80.0;
        compartmentParameters[2].zpfv[0][3] = 320.0;

        compartmentParameters[2].resistance[0][0] = 0.014;
        compartmentParameters[2].resistance[0][1] = 0.004;
        compartmentParameters[2].resistance[0][2] = 0.002;
        compartmentParameters[2].resistance[0][3] = 0.026;

        compartmentParameters[2].height[0][0] = 20.0;
        compartmentParameters[2].height[0][1] = 1.0;
        compartmentParameters[2].height[0][2] = 17.0;
        compartmentParameters[2].height[0][3] = 23.0;

        // Descending aorta compartment
        compartmentParameters[3].compliance[0][0] = 0.1;
        compartmentParameters[3].compliance[0][1] = 0.03;
        compartmentParameters[3].compliance[0][2] = 0.05;
        compartmentParameters[3].compliance[0][3] = 0.30;

        compartmentParameters[3].zpfv[0][0] = 16.0;
        compartmentParameters[3].zpfv[0][1] = 2.0;
        compartmentParameters[3].zpfv[0][2] = 10.0;
        compartmentParameters[3].zpfv[0][3] = 32.0;

        compartmentParameters[3].resistance[0][0] = 0.011;
        compartmentParameters[3].resistance[0][1] = 0.002;
        compartmentParameters[3].resistance[0][2] = 0.005;
        compartmentParameters[3].resistance[0][3] = 0.017;

        compartmentParameters[3].height[0][0] = 16.0;
        compartmentParameters[3].height[0][1] = 0.8;
        compartmentParameters[3].height[0][2] = 13.4;
        compartmentParameters[3].height[0][3] = 18.6;

        // Abdominal aorta compartment
        compartmentParameters[4].compliance[0][0] = 0.10;
        compartmentParameters[4].compliance[0][1] = 0.01;
        compartmentParameters[4].compliance[0][2] = 0.07;
        compartmentParameters[4].compliance[0][3] = 0.13;

        compartmentParameters[4].zpfv[0][0] = 10.0;
        compartmentParameters[4].zpfv[0][1] = 1.0;
        compartmentParameters[4].zpfv[0][2] = 7.0;
        compartmentParameters[4].zpfv[0][3] = 13.0;

        compartmentParameters[4].resistance[0][0] = 0.01;
        compartmentParameters[4].resistance[0][1] = 0.003;
        compartmentParameters[4].resistance[0][2] = 0.0;
        compartmentParameters[4].resistance[0][3] = 0.02;

        compartmentParameters[4].height[0][0] = 14.5;
        compartmentParameters[4].height[0][1] = 0.5;
        compartmentParameters[4].height[0][2] = 13.0;
        compartmentParameters[4].height[0][3] = 16.0;

        // Renal arterial compartment
        compartmentParameters[5].compliance[0][0] = 0.21;
        compartmentParameters[5].compliance[0][1] = 0.05;
        compartmentParameters[5].compliance[0][2] = 0.10;
        compartmentParameters[5].compliance[0][3] = 0.30;

        compartmentParameters[5].zpfv[0][0] = 20.0;
        compartmentParameters[5].zpfv[0][1] = 5.0;
        compartmentParameters[5].zpfv[0][2] = 5.0;
        compartmentParameters[5].zpfv[0][3] = 35.0;

        compartmentParameters[5].resistance[0][0] = 0.10;
        compartmentParameters[5].resistance[0][1] = 0.05;
        compartmentParameters[5].resistance[0][2] = 0.0;
        compartmentParameters[5].resistance[0][3] = 0.25;

        compartmentParameters[5].height[0][0] = 0.0;
        compartmentParameters[5].height[0][1] = 0.0;
        compartmentParameters[5].height[0][2] = 0.0;
        compartmentParameters[5].height[0][3] = 0.0;

        // Splanchnic arterial compartment
        compartmentParameters[6].compliance[0][0] = 0.2;
        compartmentParameters[6].compliance[0][1] = 0.10;
        compartmentParameters[6].compliance[0][2] = 0.10;
        compartmentParameters[6].compliance[0][3] = 0.70;

        compartmentParameters[6].zpfv[0][0] = 300.0;
        compartmentParameters[6].zpfv[0][1] = 50.0;
        compartmentParameters[6].zpfv[0][2] = 150.0;
        compartmentParameters[6].zpfv[0][3] = 450.0;

        compartmentParameters[6].resistance[0][0] = 0.07;
        compartmentParameters[6].resistance[0][1] = 0.04;
        compartmentParameters[6].resistance[0][2] = 0.0;
        compartmentParameters[6].resistance[0][3] = 0.19;

        compartmentParameters[6].height[0][0] = 5.0;
        compartmentParameters[6].height[0][1] = 0.5;
        compartmentParameters[6].height[0][2] = 8.5;
        compartmentParameters[6].height[0][3] = 11.5;

        // Leg arterial compartment
        compartmentParameters[7].compliance[0][0] = 0.2;
        compartmentParameters[7].compliance[0][1] = 0.10;
        compartmentParameters[7].compliance[0][2] = 0.1;
        compartmentParameters[7].compliance[0][3] = 0.70;

        compartmentParameters[7].zpfv[0][0] = 200.0;
        compartmentParameters[7].zpfv[0][1] = 20.0;
        compartmentParameters[7].zpfv[0][2] = 140.0;
        compartmentParameters[7].zpfv[0][3] = 260.0;

        compartmentParameters[7].resistance[0][0] = 0.09;
        compartmentParameters[7].resistance[0][1] = 0.05;
        compartmentParameters[7].resistance[0][2] = 0.0;
        compartmentParameters[7].resistance[0][3] = 0.24;

        compartmentParameters[7].height[0][0] = 106.0;
        compartmentParameters[7].height[0][1] = 6.0;
        compartmentParameters[7].height[0][2] = 88.0;
        compartmentParameters[7].height[0][3] = 124.0;

        // Pulmonary arterial compartment
        compartmentParameters[8].compliance[0][0] = 3.4;
        compartmentParameters[8].compliance[0][1] = 1.8;
        compartmentParameters[8].compliance[0][2] = 1.5;
        compartmentParameters[8].compliance[0][3] = 7.2;

        compartmentParameters[8].zpfv[0][0] = 160.0;
        compartmentParameters[8].zpfv[0][1] = 20.0;
        compartmentParameters[8].zpfv[0][2] = 100.0;
        compartmentParameters[8].zpfv[0][3] = 220.0;

        compartmentParameters[8].resistance[0][0] = 0.006;
        compartmentParameters[8].resistance[0][1] = 0.003;
        compartmentParameters[8].resistance[0][2] = 0.0;
        compartmentParameters[8].resistance[0][3] = 0.015;

        compartmentParameters[8].height[0][0] = 0.0;
        compartmentParameters[8].height[0][1] = 0.0;
        compartmentParameters[8].height[0][2] = 0.0;
        compartmentParameters[8].height[0][3] = 0.0;

        // Upper body venous compartment
        compartmentParameters[9].compliance[0][0] = 7.0;
        compartmentParameters[9].compliance[0][1] = 2.0;
        compartmentParameters[9].compliance[0][2] = 1.0;
        compartmentParameters[9].compliance[0][3] = 13.0;

        compartmentParameters[9].zpfv[0][0] = 645.0;
        compartmentParameters[9].zpfv[0][1] = 40.0;
        compartmentParameters[9].zpfv[0][2] = 425.0;
        compartmentParameters[9].zpfv[0][3] = 765.0;

        compartmentParameters[9].resistance[0][0] = 0.11;
        compartmentParameters[9].resistance[0][1] = 0.05;
        compartmentParameters[9].resistance[0][2] = 0.0;
        compartmentParameters[9].resistance[0][3] = 0.26;

        compartmentParameters[9].height[0][0] = 20.0;
        compartmentParameters[9].height[0][1] = 1.0;
        compartmentParameters[9].height[0][2] = 18.5;
        compartmentParameters[9].height[0][3] = 21.5;

        // Superior vena cava compartment
        compartmentParameters[10].compliance[0][0] = 1.3;
        compartmentParameters[10].compliance[0][1] = 0.1;
        compartmentParameters[10].compliance[0][2] = 1.0;
        compartmentParameters[10].compliance[0][3] = 1.6;

        compartmentParameters[10].zpfv[0][0] = 16.0;
        compartmentParameters[10].zpfv[0][1] = 4.0;
        compartmentParameters[10].zpfv[0][2] = 4.0;
        compartmentParameters[10].zpfv[0][3] = 28.0;

        compartmentParameters[10].resistance[0][0] = 0.028;
        compartmentParameters[10].resistance[0][1] = 0.014;
        compartmentParameters[10].resistance[0][2] = 0.0;
        compartmentParameters[10].resistance[0][3] = 0.056;

        compartmentParameters[10].height[0][0] = 14.5;
        compartmentParameters[10].height[0][1] = 0.5;
        compartmentParameters[10].height[0][2] = 3.0;
        compartmentParameters[10].height[0][3] = 6.0;

        // Renal venous compartment
        compartmentParameters[11].compliance[0][0] = 5.0;
        compartmentParameters[11].compliance[0][1] = 1.0;
        compartmentParameters[11].compliance[0][2] = 2.0;
        compartmentParameters[11].compliance[0][3] = 8.0;

        compartmentParameters[11].zpfv[0][0] = 30.0;
        compartmentParameters[11].zpfv[0][1] = 10.0;
        compartmentParameters[11].zpfv[0][2] = 10.0;
        compartmentParameters[11].zpfv[0][3] = 60.0;

        compartmentParameters[11].resistance[0][0] = 0.11;
        compartmentParameters[11].resistance[0][1] = 0.05;
        compartmentParameters[11].resistance[0][2] = 0.0;
        compartmentParameters[11].resistance[0][3] = 0.26;

        compartmentParameters[11].height[0][0] = 0.0;
        compartmentParameters[11].height[0][1] = 0.0;
        compartmentParameters[11].height[0][2] = 0.0;
        compartmentParameters[11].height[0][3] = 0.0;

        // Splanchnic venous compartment
        compartmentParameters[12].compliance[0][0] = 60.0;
        compartmentParameters[12].compliance[0][1] = 7.5;
        compartmentParameters[12].compliance[0][2] = 27.5;
        compartmentParameters[12].compliance[0][3] = 72.5;

        compartmentParameters[12].zpfv[0][0] = 1146.0;
        compartmentParameters[12].zpfv[0][1] = 100.0;
        compartmentParameters[12].zpfv[0][2] = 850.0;
        compartmentParameters[12].zpfv[0][3] = 1450.0;

        compartmentParameters[12].resistance[0][0] = 0.07;
        compartmentParameters[12].resistance[0][1] = 0.04;
        compartmentParameters[12].resistance[0][2] = 0.0;
        compartmentParameters[12].resistance[0][3] = 0.19;

        compartmentParameters[12].height[0][0] = 5.0;
        compartmentParameters[12].height[0][1] = 0.5;
        compartmentParameters[12].height[0][2] = 8.5;
        compartmentParameters[12].height[0][3] = 11.5;

        // Leg venous compartment
        compartmentParameters[13].compliance[0][0] = 20.0;
        compartmentParameters[13].compliance[0][1] = 3.0;
        compartmentParameters[13].compliance[0][2] = 11.0;
        compartmentParameters[13].compliance[0][3] = 29.0;

        compartmentParameters[13].zpfv[0][0] = 716.0;
        compartmentParameters[13].zpfv[0][1] = 50.0;
        compartmentParameters[13].zpfv[0][2] = 666.0;
        compartmentParameters[13].zpfv[0][3] = 866.0;

        compartmentParameters[13].resistance[0][0] = 0.10;
        compartmentParameters[13].resistance[0][1] = 0.05;
        compartmentParameters[13].resistance[0][2] = 0.0;
        compartmentParameters[13].resistance[0][3] = 0.25;

        compartmentParameters[13].height[0][0] = 106.0;
        compartmentParameters[13].height[0][1] = 6.0;
        compartmentParameters[13].height[0][2] = 88.0;
        compartmentParameters[13].height[0][3] = 124.0;

        // Abdominal venous compartment
        compartmentParameters[14].compliance[0][0] = 1.3;
        compartmentParameters[14].compliance[0][1] = 0.1;
        compartmentParameters[14].compliance[0][2] = 1.0;
        compartmentParameters[14].compliance[0][3] = 1.6;

        compartmentParameters[14].zpfv[0][0] = 79.0;
        compartmentParameters[14].zpfv[0][1] = 10.0;
        compartmentParameters[14].zpfv[0][2] = 49.0;
        compartmentParameters[14].zpfv[0][3] = 109.0;

        compartmentParameters[14].resistance[0][0] = 0.019;
        compartmentParameters[14].resistance[0][1] = 0.007;
        compartmentParameters[14].resistance[0][2] = 0.0;
        compartmentParameters[14].resistance[0][3] = 0.040;

        compartmentParameters[14].height[0][0] = 14.5;
        compartmentParameters[14].height[0][1] = 1.5;
        compartmentParameters[14].height[0][2] = 10.0;
        compartmentParameters[14].height[0][3] = 19.0;

        // Inferior vena cava compartment
        compartmentParameters[15].compliance[0][0] = 0.5;
        compartmentParameters[15].compliance[0][1] = 0.1;
        compartmentParameters[15].compliance[0][2] = 0.2;
        compartmentParameters[15].compliance[0][3] = 0.8;

        compartmentParameters[15].zpfv[0][0] = 33.0;
        compartmentParameters[15].zpfv[0][1] = 4.0;
        compartmentParameters[15].zpfv[0][2] = 21.0;
        compartmentParameters[15].zpfv[0][3] = 45.0;

        compartmentParameters[15].resistance[0][0] = 0.008;
        compartmentParameters[15].resistance[0][1] = 0.003;
        compartmentParameters[15].resistance[0][2] = 0.0;
        compartmentParameters[15].resistance[0][3] = 0.017;

        compartmentParameters[15].height[0][0] = 6.0;
        compartmentParameters[15].height[0][1] = 0.5;
        compartmentParameters[15].height[0][2] = 4.5;
        compartmentParameters[15].height[0][3] = 7.5;

        // Pulmonary venous compartment
        compartmentParameters[16].compliance[0][0] = 9.0;
        compartmentParameters[16].compliance[0][1] = 3.7;
        compartmentParameters[16].compliance[0][2] = 5.3;
        compartmentParameters[16].compliance[0][3] = 12.7;

        compartmentParameters[16].zpfv[0][0] = 430.0;
        compartmentParameters[16].zpfv[0][1] = 50.0;
        compartmentParameters[16].zpfv[0][2] = 180.0;
        compartmentParameters[16].zpfv[0][3] = 580.0;

        compartmentParameters[16].resistance[0][0] = 0.006;
        compartmentParameters[16].resistance[0][1] = 0.003;
        compartmentParameters[16].resistance[0][2] = 0.0;
        compartmentParameters[16].resistance[0][3] = 0.015;

        compartmentParameters[16].height[0][0] = 0.0;
        compartmentParameters[16].height[0][1] = 0.0;
        compartmentParameters[16].height[0][2] = 0.0;
        compartmentParameters[16].height[0][3] = 0.0;

        // Microvascular resistances
        micro_r.upperBodyResistance[0] = 4.9;
        micro_r.upperBodyResistance[1] = 1.6;
        micro_r.upperBodyResistance[2] = 3.3;
        micro_r.upperBodyResistance[3] = 6.5;

        micro_r.kidneyResistance[0] = 4.1;
        micro_r.kidneyResistance[1] = 1.0;
        micro_r.kidneyResistance[2] = 3.2;
        micro_r.kidneyResistance[3] = 6.2;

        micro_r.splanchnicResistance[0] = 3.0;
        micro_r.splanchnicResistance[1] = 1.0;
        micro_r.splanchnicResistance[2] = 2.3;
        micro_r.splanchnicResistance[3] = 4.3;

        micro_r.lowerBodyResistance[0] = 4.5;
        micro_r.lowerBodyResistance[1] = 0.5;
        micro_r.lowerBodyResistance[2] = 4.0;
        micro_r.lowerBodyResistance[3] = 10.3;

        micro_r.pulmonaryResistance[0] = 0.07;
        micro_r.pulmonaryResistance[1] = 0.04;
        micro_r.pulmonaryResistance[2] = 0.0;
        micro_r.pulmonaryResistance[3] = 0.19;

        // Cardiac parameters
        cardiac[0].systolicCompliance[0][0] = 1.35;
        cardiac[0].systolicCompliance[0][1] = 0.18;
        cardiac[0].systolicCompliance[0][2] = 0.60;
        cardiac[0].systolicCompliance[0][3] = 2.7;

        cardiac[0].systolicCompliance[1][0] = 1.30;
        cardiac[0].systolicCompliance[1][1] = 0.47;
        cardiac[0].systolicCompliance[1][2] = 0.30;
        cardiac[0].systolicCompliance[1][3] = 2.0;

        cardiac[1].systolicCompliance[0][0] = 1.64;
        cardiac[1].systolicCompliance[0][1] = 0.19;
        cardiac[1].systolicCompliance[0][2] = 0.80;
        cardiac[1].systolicCompliance[0][3] = 3.20;

        cardiac[1].systolicCompliance[1][0] = 0.40;
        cardiac[1].systolicCompliance[1][1] = 0.10;
        cardiac[1].systolicCompliance[1][2] = 0.20;
        cardiac[1].systolicCompliance[1][3] = 0.77;

        cardiac[0].diastolicCompliance[0][0] = 3.33;
        cardiac[0].diastolicCompliance[0][1] = 0.56;
        cardiac[0].diastolicCompliance[0][2] = 1.50;
        cardiac[0].diastolicCompliance[0][3] = 6.0;

        cardiac[0].diastolicCompliance[1][0] = 19.29;
        cardiac[0].diastolicCompliance[1][1] = 5.0;
        cardiac[0].diastolicCompliance[1][2] = 7.0;
        cardiac[0].diastolicCompliance[1][3] = 29.0;

        cardiac[1].diastolicCompliance[0][0] = 2.0;
        cardiac[1].diastolicCompliance[0][1] = 0.4;
        cardiac[1].diastolicCompliance[0][2] = 1.0;
        cardiac[1].diastolicCompliance[0][3] = 4.3;

        cardiac[1].diastolicCompliance[1][0] = 9.69;
        cardiac[1].diastolicCompliance[1][1] = 1.18;
        cardiac[1].diastolicCompliance[1][2] = 3.88;
        cardiac[1].diastolicCompliance[1][3] = 15.11;

        cardiac[0].zpfv[0][0] = 14.0;
        cardiac[0].zpfv[0][1] = 1.0;
        cardiac[0].zpfv[0][2] = 10.0;
        cardiac[0].zpfv[0][3] = 18.0;

        cardiac[0].zpfv[1][0] = 46.0;
        cardiac[0].zpfv[1][1] = 21.0;
        cardiac[0].zpfv[1][2] = 10.0;
        cardiac[0].zpfv[1][3] = 82.0;

        cardiac[1].zpfv[0][0] = 24.0;
        cardiac[1].zpfv[0][1] = 13.0;
        cardiac[1].zpfv[0][2] = 10.0;
        cardiac[1].zpfv[0][3] = 38.0;

        cardiac[1].zpfv[1][0] = 55.0;
        cardiac[1].zpfv[1][1] = 10.0;
        cardiac[1].zpfv[1][2] = 25.0;
        cardiac[1].zpfv[1][3] = 85.0;

        cardiac[0].resistance[0][0] = 0.006;
        cardiac[0].resistance[0][1] = 0.003;
        cardiac[0].resistance[0][2] = 0.0;
        cardiac[0].resistance[0][3] = 0.015;

        cardiac[1].resistance[0][0] = 0.01;
        cardiac[1].resistance[0][1] = 0.001;
        cardiac[1].resistance[0][2] = 0.007;
        cardiac[1].resistance[0][3] = 0.013;

        // System-level parameters
        system.totalBloodVolume[0][0] = 5150.0;
        system.totalBloodVolume[0][1] = 203.0;
        system.totalBloodVolume[0][2] = 4041.0;
        system.totalBloodVolume[0][3] = 6460.0;

        system.nominalHeartRate[0][0] = 70.0;
        system.nominalHeartRate[0][1] = 3.3;
        system.nominalHeartRate[0][2] = 50.0;
        system.nominalHeartRate[0][3] = 85.0;

        system.intraThoracicPressure[0][0] = -4.0; // mmHg
        system.intraThoracicPressure[0][1] = 1.0;  // mmHg
        system.intraThoracicPressure[0][2] = -6.0; // mmHg
        system.intraThoracicPressure[0][3] = -2.0; // mmHg

        system.height[0][0] = 169.3;
        system.height[0][1] = 1.5;
        system.height[0][2] = 164.8;
        system.height[0][3] = 173.8;

        system.weight[0][0] = 60.2;
        system.weight[0][1] = 2.1;
        system.weight[0][2] = 63.9;
        system.weight[0][3] = 76.5;

        system.bodySurfaceArea[0][0] = 1.83;
        system.bodySurfaceArea[0][1] = 0.02;
        system.bodySurfaceArea[0][2] = 1.77;
        system.bodySurfaceArea[0][3] = 1.89;

        system.T[0][0] = 0.2;
        system.T[0][1] = 0.02;
        system.T[0][2] = 0.18;
        system.T[0][3] = 0.30;

        system.T[1][0] = 0.3;
        system.T[1][1] = 0.01;
        system.T[1][2] = 0.30;
        system.T[1][3] = 0.40;

        system.T[2][0] = 0.12;
        system.T[2][1] = 0.002;
        system.T[2][2] = 0.17;
        system.T[2][3] = 0.20;

        // Reflex system parameters
        abrReflex.set[0][0] = 91.0;
        abrReflex.set[0][1] = 3.0;
        abrReflex.set[0][2] = 89.0;
        abrReflex.set[0][3] = 105.0;

        abrReflex.set[1][0] = 18.0;
        abrReflex.set[1][1] = 0.0;
        abrReflex.set[1][2] = 18.0;
        abrReflex.set[1][3] = 18.0;

        cprReflex.set[0][0] = 8.0;
        cprReflex.set[0][1] = 1.0;
        cprReflex.set[0][2] = 4.0;
        cprReflex.set[0][3] = 10.0;

        cprReflex.set[1][0] = 5.0;
        cprReflex.set[1][1] = 0.0;
        cprReflex.set[1][2] = 5.0;
        cprReflex.set[1][3] = 5.0;

        abrReflex.rrSympatheticGain[0] = 0.012;
        abrReflex.rrSympatheticGain[1] = 0.004;
        abrReflex.rrSympatheticGain[2] = 0.005;
        abrReflex.rrSympatheticGain[3] = 0.017;

        abrReflex.rrParaSympatheticGain[0] = 0.009;
        abrReflex.rrParaSympatheticGain[1] = 0.004;
        abrReflex.rrParaSympatheticGain[2] = 0.005;
        abrReflex.rrParaSympatheticGain[3] = 0.017;

        cprReflex.rrSympatheticGain[0] = 0.0;
        cprReflex.rrSympatheticGain[1] = 0.0;
        cprReflex.rrSympatheticGain[2] = 0.0;
        cprReflex.rrSympatheticGain[3] = 0.0;

        cprReflex.rrParaSympatheticGain[0] = 0.0;
        cprReflex.rrParaSympatheticGain[1] = 0.0;
        cprReflex.rrParaSympatheticGain[2] = 0.0;
        cprReflex.rrParaSympatheticGain[3] = 0.0;

        abrReflex.res[0][0] = -0.13;
        abrReflex.res[0][1] = 0.05;
        abrReflex.res[0][2] = -0.15;
        abrReflex.res[0][3] = -0.05;

        abrReflex.res[1][0] = -0.13;
        abrReflex.res[1][1] = 0.05;
        abrReflex.res[1][2] = -0.15;
        abrReflex.res[1][3] = -0.05;

        abrReflex.res[2][0] = -0.13;
        abrReflex.res[2][1] = 0.05;
        abrReflex.res[2][2] = -0.15;
        abrReflex.res[2][3] = -0.05;

        abrReflex.res[3][0] = -0.13;
        abrReflex.res[3][1] = 0.05;
        abrReflex.res[3][2] = -0.15;
        abrReflex.res[3][3] = -0.05;

        cprReflex.res[0][0] = -0.3;
        cprReflex.res[0][1] = 0.05;
        cprReflex.res[0][2] = -0.4;
        cprReflex.res[0][3] = -0.2;

        cprReflex.res[1][0] = -0.3;
        cprReflex.res[1][1] = 0.05;
        cprReflex.res[1][2] = -0.4;
        cprReflex.res[1][3] = -0.2;

        cprReflex.res[2][0] = -0.3;
        cprReflex.res[2][1] = 0.05;
        cprReflex.res[2][2] = -0.4;
        cprReflex.res[2][3] = -0.2;

        cprReflex.res[3][0] = -0.3;
        cprReflex.res[3][1] = 0.05;
        cprReflex.res[3][2] = -0.4;
        cprReflex.res[3][3] = -0.2;

        abrReflex.vt[0][0] = 5.3 * alpha;
        abrReflex.vt[0][1] = 0.85 * alpha;
        abrReflex.vt[0][2] = 3.6 * alpha;
        abrReflex.vt[0][3] = 6.15 * alpha;

        abrReflex.vt[1][0] = 1.3 * alpha;
        abrReflex.vt[1][1] = 0.2;
        abrReflex.vt[1][2] = 0.7 * alpha;
        abrReflex.vt[1][3] = 2.0 * alpha;

        abrReflex.vt[2][0] = 13.3 * alpha;
        abrReflex.vt[2][1] = 2.1;
        abrReflex.vt[2][2] = 9.0 * alpha;
        abrReflex.vt[2][3] = 17.6 * alpha;

        abrReflex.vt[3][0] = 6.7 * alpha;
        abrReflex.vt[3][1] = 1.1;
        abrReflex.vt[3][2] = 4.5 * alpha;
        abrReflex.vt[3][3] = 9.0 * alpha;

        cprReflex.vt[0][0] = 13.5 * beta;
        cprReflex.vt[0][1] = 2.7;
        cprReflex.vt[0][2] = 8.1 * beta;
        cprReflex.vt[0][3] = 19.0 * beta;

        cprReflex.vt[1][0] = 2.7 * beta;
        cprReflex.vt[1][1] = 0.5;
        cprReflex.vt[1][2] = 2.2 * beta;
        cprReflex.vt[1][3] = 3.2 * beta;

        cprReflex.vt[2][0] = 64.0 * beta;
        cprReflex.vt[2][1] = 12.8;
        cprReflex.vt[2][2] = 38.4 * beta;
        cprReflex.vt[2][3] = 90.0 * beta;

        cprReflex.vt[3][0] = 30.0 * beta;
        cprReflex.vt[3][1] = 6.0;
        cprReflex.vt[3][2] = 18.0 * beta;
        cprReflex.vt[3][3] = 42.0 * beta;

        abrReflex.c[0][0] = 0.021;
        abrReflex.c[0][1] = 0.003;
        abrReflex.c[0][2] = 0.007;
        abrReflex.c[0][3] = 0.030;

        abrReflex.c[1][0] = 0.014;
        abrReflex.c[1][1] = 0.001;
        abrReflex.c[1][2] = 0.004;
        abrReflex.c[1][3] = 0.014;

        cprReflex.c[0][0] = 0.0;
        cprReflex.c[0][1] = 0.0;
        cprReflex.c[0][2] = 0.0;
        cprReflex.c[0][3] = 0.0;

        cprReflex.c[1][0] = 0.0;
        cprReflex.c[1][1] = 0.0;
        cprReflex.c[1][2] = 0.0;
        cprReflex.c[1][3] = 0.0;

        // Timing parameters
        reflexTiming.para[0].delay = 0.59;
        reflexTiming.para[1].delay = 0.25;
        reflexTiming.para[2].delay = 0.34;
        reflexTiming.para[3].delay = 0.84;

        reflexTiming.para[0].peak = 0.70;
        reflexTiming.para[1].peak = 0.25;
        reflexTiming.para[2].peak = 0.45;
        reflexTiming.para[3].peak = 0.95;

        reflexTiming.para[0].end = 1.00;
        reflexTiming.para[1].end = 0.25;
        reflexTiming.para[2].end = 0.75;
        reflexTiming.para[3].end = 1.50;

        reflexTiming.beta[0].delay = 2.5;
        reflexTiming.beta[1].delay = 1.0;
        reflexTiming.beta[2].delay = 1.0;
        reflexTiming.beta[3].delay = 4.5;

        reflexTiming.beta[0].peak = 3.5;
        reflexTiming.beta[1].peak = 1.0;
        reflexTiming.beta[2].peak = 2.0;
        reflexTiming.beta[3].peak = 5.0;

        reflexTiming.beta[0].end = 15.0;
        reflexTiming.beta[1].end = 5.0;
        reflexTiming.beta[2].end = 10.0;
        reflexTiming.beta[3].end = 20.0;

        reflexTiming.alpha_r[0].delay = 2.5;
        reflexTiming.alpha_r[1].delay = 1.0;
        reflexTiming.alpha_r[2].delay = 1.0;
        reflexTiming.alpha_r[3].delay = 4.5;

        reflexTiming.alpha_r[0].peak = 3.5;
        reflexTiming.alpha_r[1].peak = 1.0;
        reflexTiming.alpha_r[2].peak = 3.0;
        reflexTiming.alpha_r[3].peak = 6.0;

        reflexTiming.alpha_r[0].end = 30.0;
        reflexTiming.alpha_r[1].end = 5.0;
        reflexTiming.alpha_r[2].end = 25.0;
        reflexTiming.alpha_r[3].end = 35.0;

        reflexTiming.alpha_v[0].delay = 5.0;
        reflexTiming.alpha_v[1].delay = 1.5;
        reflexTiming.alpha_v[2].delay = 2.7;
        reflexTiming.alpha_v[3].delay = 7.3;

        reflexTiming.alpha_v[0].peak = 10.0;
        reflexTiming.alpha_v[1].peak = 1.5;
        reflexTiming.alpha_v[2].peak = 4.7;
        reflexTiming.alpha_v[3].peak = 9.3;

        reflexTiming.alpha_v[0].end = 42.0;
        reflexTiming.alpha_v[1].end = 5.0;
        reflexTiming.alpha_v[2].end = 32.5;
        reflexTiming.alpha_v[3].end = 47.5;

        reflexTiming.alpha_cpr[0].delay = 2.5;
        reflexTiming.alpha_cpr[1].delay = 1.0;
        reflexTiming.alpha_cpr[2].delay = 1.0;
        reflexTiming.alpha_cpr[3].delay = 4.5;

        reflexTiming.alpha_cpr[0].peak = 5.5;
        reflexTiming.alpha_cpr[1].peak = 1.0;
        reflexTiming.alpha_cpr[2].peak = 3.0;
        reflexTiming.alpha_cpr[3].peak = 6.0;

        reflexTiming.alpha_cpr[0].end = 35.0;
        reflexTiming.alpha_cpr[1].end = 5.0;
        reflexTiming.alpha_cpr[2].end = 25.0;
        reflexTiming.alpha_cpr[3].end = 35.0;

        reflexTiming.alpha_cpv[0].delay = 5.0;
        reflexTiming.alpha_cpv[1].delay = 1.5;
        reflexTiming.alpha_cpv[2].delay = 2.7;
        reflexTiming.alpha_cpv[3].delay = 7.3;

        reflexTiming.alpha_cpv[0].peak = 9.0;
        reflexTiming.alpha_cpv[1].peak = 1.5;
        reflexTiming.alpha_cpv[2].peak = 4.7;
        reflexTiming.alpha_cpv[3].peak = 9.3;

        reflexTiming.alpha_cpv[0].end = 40.0;
        reflexTiming.alpha_cpv[1].end = 5.0;
        reflexTiming.alpha_cpv[2].end = 32.5;
        reflexTiming.alpha_cpv[3].end = 47.5;

    }

    void mapping_ptr(Parameters pvec) {

        pvec.put(ParameterName.ABR_SET_POINT, abrReflex.set[0][0]);     // Arterial set-point
        pvec.put(ParameterName.ABR_SCALING_FACTOR, abrReflex.set[1][0]);     // ABR scaling factor
        pvec.put(ParameterName.ABR_HR_SYMPATHETIC_GAIN, abrReflex.rrSympatheticGain[0]);      // RR-symp. gain
        pvec.put(ParameterName.ABR_HR_PARASYMPATHETIC_GAIN, abrReflex.rrParaSympatheticGain[0]);      // RR-parasymp. gain 
        pvec.put(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_UPPER_BODY, abrReflex.res[0][0]);     // ABR R gain to up compartment
        pvec.put(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_KIDNEY, abrReflex.res[1][0]);     // ABR R gain to k compartment
        pvec.put(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_SPLANCHNIC, abrReflex.res[2][0]);     // ABR R gain to sp compartment
        pvec.put(ParameterName.ABR_ART_RES_SYMPATHETIC_GAIN_TO_LOWER_BODY, abrReflex.res[3][0]);     // ABR R gain to ll compartment
        pvec.put(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_UPPER_BODY, abrReflex.vt[0][0]);      // ABR VT gain to up compartment
        pvec.put(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_KIDNEY, abrReflex.vt[1][0]);      // ABR VT gain to k compartment
        pvec.put(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_SPLANCHNIC, abrReflex.vt[2][0]);      // ABR VT gain to sp compartment 
        pvec.put(ParameterName.ABR_VEN_TONE_SYMPATHETIC_GAIN_TO_LOWER_BODY, abrReflex.vt[3][0]);      // ABR VT gain to ll compartment
        pvec.put(ParameterName.ABR_RV_CONTRACTILITY_SYMPATHETIC_GAIN, abrReflex.c[0][0]);       // ABR RV contractility gain
        pvec.put(ParameterName.ABR_LV_CONTRACTILITY_SYMPATHETIC_GAIN, abrReflex.c[1][0]);       // ABR LV contractility gain
        pvec.put(ParameterName.SENSED_PRESSURE_OFFSET_DURING_TILT, -25.0);      // Sensed pressure off-set during tilt
        pvec.put(ParameterName.CPR_SET_POINT, cprReflex.set[0][0]);     // Cardio-pulmonary set-point
        pvec.put(ParameterName.CPR_SCALING_FACTOR, cprReflex.set[1][0]);     // CPR scaling factor
        pvec.put(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_UBODY, cprReflex.res[0][0]);     // CPR R gain to up compartment   
        pvec.put(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_KIDNEY, cprReflex.res[1][0]);     // CPR R gain to k compartment
        pvec.put(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_SPLANCHNIC, cprReflex.res[2][0]);     // CPR R gain to sp compartment 
        pvec.put(ParameterName.CPR_ART_RES_SYMPATHETIC_GAIN_TO_LBODY, cprReflex.res[3][0]);     // CPR R gain to ll compartment
        pvec.put(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_UBODY, cprReflex.vt[0][0]);      // CPR VT gain to up compartment
        pvec.put(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_KIDNEY, cprReflex.vt[1][0]);      // CPR VT gain to k compartment
        pvec.put(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_SPLANCHNIC, cprReflex.vt[2][0]);      // CPR VT gain to sp compartment
        pvec.put(ParameterName.CPR_VEN_SYMPATHETIC_GAIN_TO_LBODY, cprReflex.vt[3][0]);      // CPR VT gain to ll compartment
        pvec.put(ParameterName.ABR_DELAY_PARASYMP, reflexTiming.para[0].delay);       // Delay parasymp.
        pvec.put(ParameterName.ABR_PEAK_PARASYMP, reflexTiming.para[0].peak);       // Peak parasymp.
        pvec.put(ParameterName.ABR_END_PARASYMP, reflexTiming.para[0].end);       // End parasymp.
        pvec.put(ParameterName.ABR_DELAY_BETA_SYMP, reflexTiming.beta[0].delay);       // Delay beta-sympathetic
        pvec.put(ParameterName.ABR_PEAK_BETA_SYMP, reflexTiming.beta[0].peak);       // Peak beta- sympathetic
        pvec.put(ParameterName.ABR_END_BETA_SYMP, reflexTiming.beta[0].end);       // End beat-sympathetic

        // Respiratory parameters
        // These are just taken from rcvsim. Ran it with Matlab and looked at the
        // impulse responses that it produced.
        pvec.put(ParameterName.LUNG_VOLUME_DELAY_PARASYMP, 7. * 0.0625);
        pvec.put(ParameterName.LUNG_VOLUME_PEAK_PARASYMP, 22. * 0.0625);
        pvec.put(ParameterName.LUNG_VOLUME_END_PARASYMP, 37. * 0.0625);
        pvec.put(ParameterName.LUNG_VOLUME_HR_PARASYMP_GAIN, ((15. / 9.) / 60000.));  // See Reflex constructor for where these numbers came from
        pvec.put(ParameterName.LUNG_VOLUME_DELAY_BETA_SYMP, 31. * 0.0625);
        pvec.put(ParameterName.LUNG_VOLUME_PEAK_BETA_SYMP, 78. * 0.0625);
        pvec.put(ParameterName.LUNG_VOLUME_END_BETA_SYMP, 477. * 0.0625);
        pvec.put(ParameterName.LUNG_VOLUME_HR_BETA_SYMP_GAIN, ((7.2 / 9.) / 60000));// See Reflex constructor for where these numbers came from

        pvec.put(ParameterName.INTRA_THORACIC_PRESSURE, system.intraThoracicPressure[0][0]);        // Intra-thoracic pressure
        pvec.put(ParameterName.LUNG_MAX_TIDAL_PRESSURE, +4.);
        pvec.put(ParameterName.LUNG_MIN_TIDAL_PRESSURE, -4.);
        pvec.put(ParameterName.LUNG_RESPIRATORY_RATE_PER_MINUTE, 14.);
        pvec.put(ParameterName.LUNG_VOLUME_FUNCTIONAL_RESERVE, 2200.);
        pvec.put(ParameterName.LUNG_VOLUME_TIDAL, 500.);

        pvec.put(ParameterName.PV32, 7.0);      // Pbias_max at sp compartment
        pvec.put(ParameterName.PV33, 40.0);      // Pbias_max at ll compartment
        pvec.put(ParameterName.PV34, 5.0);      // Pbias_max at ab compartment
        pvec.put(ParameterName.PV35, 2.0);      // Arterial compliance
        pvec.put(ParameterName.UBODY_VEN_COMPLIANCE, compartmentParameters[9].compliance[0][0]);         // C upper body veins
        pvec.put(ParameterName.RENAL_VEN_COMPLIANCE, compartmentParameters[11].compliance[0][0]);        // C renal veins
        pvec.put(ParameterName.SPLAN_VEN_COMPLIANCE, compartmentParameters[12].compliance[0][0]);        // C splanchnic veins
        pvec.put(ParameterName.LBODY_VEN_COMPLIANCE, compartmentParameters[13].compliance[0][0]);        // C lower body veins
        pvec.put(ParameterName.ABDOM_VEN_COMPLIANCE, compartmentParameters[14].compliance[0][0]);        // C abdominal veins
        pvec.put(ParameterName.IVC_COMPLIANCE, compartmentParameters[15].compliance[0][0]);        // C inferior vena cava
        pvec.put(ParameterName.SVC_COMPLIANCE, compartmentParameters[10].compliance[0][0]);        // C superior vena cava
        pvec.put(ParameterName.RA_DIASTOLIC_COMPLIANCE, cardiac[0].diastolicCompliance[0][0]); // RA diastolic compliance
        pvec.put(ParameterName.RA_SYSTOLIC_COMPLIANCE, cardiac[0].systolicCompliance[0][0]);  // RA systolic compliance
        pvec.put(ParameterName.RV_DIASTOLIC_COMPLIANCE, cardiac[0].diastolicCompliance[1][0]); // RV diastolic compliance
        pvec.put(ParameterName.RV_SYSTOLIC_COMPLIANCE, cardiac[0].systolicCompliance[1][0]);  // RV systolic compliance
        pvec.put(ParameterName.PULM_ART_COMPLIANCE, compartmentParameters[8].compliance[0][0]);         // C pul. arteries
        pvec.put(ParameterName.PULM_VEN_COMPLIANCE, compartmentParameters[16].compliance[0][0]);        // C pul. veins
        pvec.put(ParameterName.LA_DIASTOLIC_COMPLIANCE, cardiac[1].diastolicCompliance[0][0]); // LA diastolic compliance
        pvec.put(ParameterName.LA_SYSTOLIC_COMPLIANCE, cardiac[1].systolicCompliance[0][0]);  // LA systolic compliance
        pvec.put(ParameterName.LV_DIASTOLIC_COMPLIANCE, cardiac[1].diastolicCompliance[1][0]); // LV diastolic compliance
        pvec.put(ParameterName.LV_SYSTOLIC_COMPLIANCE, cardiac[1].systolicCompliance[1][0]);  // LV systolic compliance
        pvec.put(ParameterName.UBODY_MICRO_RESISTANCE, micro_r.upperBodyResistance[0]);         // R upper body
        pvec.put(ParameterName.UBODY_VEN_RESISTANCE, compartmentParameters[9].resistance[0][0]);         // R upper body outflow
        pvec.put(ParameterName.RENAL_MICRO_RESISTANCE, micro_r.kidneyResistance[0]);         // R kidney compartment
        pvec.put(ParameterName.RENAL_VEN_RESISTANCE, compartmentParameters[11].resistance[0][0]);        // R kidney outflow
        pvec.put(ParameterName.SPLAN_MICRO_RESISTANCE, micro_r.splanchnicResistance[0]);         // R splanchnic compartment
        pvec.put(ParameterName.SPLAN_VEN_RESISTANCE, compartmentParameters[12].resistance[0][0]);        // R splanchnic outflow
        pvec.put(ParameterName.LBODY_MICRO_RESISTANCE, micro_r.lowerBodyResistance[0]);         // R lower body compartment
        pvec.put(ParameterName.LBODY_VEN_RESISTANCE, compartmentParameters[13].resistance[0][0]);        // R lower body outflow
        pvec.put(ParameterName.ABDOM_VEN_RESISTANCE, compartmentParameters[14].resistance[0][0]);        // R abdominal venous compartment
        pvec.put(ParameterName.IVC_RESISTANCE, compartmentParameters[15].resistance[0][0]);        // R inferior vena cava comp.
        pvec.put(ParameterName.SVC_RESISTANCE, compartmentParameters[10].resistance[0][0]);        // R superior vena cava comp.
        pvec.put(ParameterName.TRICUSPID_VALVE_RESISTANCE, cardiac[0].resistance[0][0]);      // R tricuspid valve
        pvec.put(ParameterName.PUMONIC_VALVE_RESISTANCE, compartmentParameters[8].resistance[0][0]);         // R right ventricular outflow
        pvec.put(ParameterName.PULM_MICRO_RESISTANCE, micro_r.pulmonaryResistance[0]);         // R pulmonary microcirculation
        pvec.put(ParameterName.PULM_VEN_RESISTANCE, compartmentParameters[16].resistance[0][0]);        // R pulmonary veins / LV inflow
        pvec.put(ParameterName.MITRAL_VALVE_RESISTANCE, cardiac[1].resistance[0][0]);      // R mitral valve
        pvec.put(ParameterName.AORTIC_VALVE_RESISTANCE, compartmentParameters[0].resistance[0][0]);         // R left ventricular outflow
        pvec.put(ParameterName.TOTAL_BLOOD_VOLUME, system.totalBloodVolume[0][0]);         // Total blood volume   
        pvec.put(ParameterName.MAX_INCREASE_IN_SPLAN_DISTENDING_VOL, 1500.0);      // Maximal increase in sp distending vol.
        pvec.put(ParameterName.MAX_INCREASE_IN_LEG_DISTENDING_VOL, 1000.0);      // Maximal increase in ll distending vol.
        pvec.put(ParameterName.MAX_INCREASE_IN_ABDOM_DISTENDING_VOL, 650.0);      // Maximal increase in ab distending vol.
        pvec.put(ParameterName.MAXIMAL_BLOOD_VOLUME_LOSS_DURINT_TILT, 300.0);      // Maximal blood volume loss during tilt
        pvec.put(ParameterName.PV75, 4166.0);      // Total zero pressure filling volume
        pvec.put(ParameterName.PV76, 715.0);      // Volume of arterial compartment
        pvec.put(ParameterName.UBODY_VEN_ZPFV, compartmentParameters[9].zpfv[0][0]);        // ZPFV of upper body compartment
        pvec.put(ParameterName.RENAL_VEN_ZPFV, compartmentParameters[11].zpfv[0][0]);       // ZPFV of kidney compartment
        pvec.put(ParameterName.SPLAN_VEN_ZPFV, compartmentParameters[12].zpfv[0][0]);       // ZPFV of splanchnic compartment
        pvec.put(ParameterName.LBODY_VEN_ZPFV, compartmentParameters[13].zpfv[0][0]);       // ZPFV of lower body compartment
        pvec.put(ParameterName.ABDOM_VEN_ZPFV, compartmentParameters[14].zpfv[0][0]);       // ZPFV of abdominal veins
        pvec.put(ParameterName.IVC_ZPFV, compartmentParameters[15].zpfv[0][0]);       // ZPFV of inferior vena cava
        pvec.put(ParameterName.SVC_ZPFV, compartmentParameters[10].zpfv[0][0]);       // ZPFV of superior vena cava
        pvec.put(ParameterName.RA_ZPFV, cardiac[0].zpfv[0][0]);     // ZPFV of right atrium
        pvec.put(ParameterName.RV_ZPFV, cardiac[0].zpfv[1][0]);     // ZPFV of right ventricle
        pvec.put(ParameterName.PULM_ART_ZPFV, compartmentParameters[8].zpfv[0][0]);        // ZPFV of pulmonary arteries
        pvec.put(ParameterName.PULN_VEN_ZPFV, compartmentParameters[16].zpfv[0][0]);       // ZPFV of pulmonary veins
        pvec.put(ParameterName.LA_ZPFV, cardiac[1].zpfv[0][0]);     // ZPFV of left atrium
        pvec.put(ParameterName.LV_ZPFV, cardiac[1].zpfv[1][0]);     // ZPFV of left ventricle
        pvec.put(ParameterName.NOMINAL_HEART_RATE, system.nominalHeartRate[0][0]);        // Nominal heart rate
        pvec.put(ParameterName.TILT_ANGLE, 0.0);      // Tilt angle
        pvec.put(ParameterName.TIME_TO_MAX_TILT_ANGLE, 2.0);      // Time to max. tilt angle (i.e. tilt time)
        pvec.put(ParameterName.TILT_ONSET_TIME, 200.0);      // Tilt onset time
        pvec.put(ParameterName.DURATION_IN_UPRIGHT_POSTURE, 240.0);      // Duration in upright posture
        pvec.put(ParameterName.MAXIMAL_EXTERNAL_NEGATIVE_PRESSURE, 0.0);      // Maximal external negative pressure
        pvec.put(ParameterName.VOLUME_LOSS_DURING_LBNP, 500.0);      // Volume loss during LBNP
        pvec.put(ParameterName.ASCENDING_AORTA_COMPLIANCE, compartmentParameters[0].compliance[0][0]);        // C aortic arch
        pvec.put(ParameterName.BRACH_ART_COMPLIANCE, compartmentParameters[1].compliance[0][0]);        // C upper thoracic aorta
        pvec.put(ParameterName.THORACIC_AORTA_COMPLIANCE, compartmentParameters[3].compliance[0][0]);        // C lower thoracic aorta
        pvec.put(ParameterName.UBODY_ART_COMPLIANCE, compartmentParameters[2].compliance[0][0]);        // C upper body arteries
        pvec.put(ParameterName.ABDOM_AORTA_COMPLIANCE, compartmentParameters[4].compliance[0][0]);        // C abdominal aorta
        pvec.put(ParameterName.RENAL_ART_COMPLIANCE, compartmentParameters[5].compliance[0][0]);        // C renal arteries
        pvec.put(ParameterName.SPLAN_ART_COMPLIANCE, compartmentParameters[6].compliance[0][0]);        // C splanchnic arteries
        pvec.put(ParameterName.LBODY_ART_COMPLIANCE, compartmentParameters[7].compliance[0][0]);        // C leg arteries and arterioles
        pvec.put(ParameterName.BRACH_ART_RESISTANCE, compartmentParameters[1].resistance[0][0]);        // R upper thoracic aorta
        pvec.put(ParameterName.UBODY_ART_RESISTANCE, compartmentParameters[2].resistance[0][0]);        // R head arteries
        pvec.put(ParameterName.THORACIC_AORTA_RESISTANCE, compartmentParameters[3].resistance[0][0]);        // R lower thoracic aorta
        pvec.put(ParameterName.ABDOM_AORTA_RESISTANCE, compartmentParameters[4].resistance[0][0]);        // R abdominal aorta
        pvec.put(ParameterName.RENAL_ART_RESISTANCE, compartmentParameters[5].resistance[0][0]);        // R renal arteries 
        pvec.put(ParameterName.SPLAN_ART_RESISTANCE, compartmentParameters[6].resistance[0][0]);        // R splanchnic arteries
        pvec.put(ParameterName.LBODY_ART_RESISTANCE, compartmentParameters[7].resistance[0][0]);        // R leg arteries
        pvec.put(ParameterName.ABR_ALPHA_SYMP_VEN_DELAY, reflexTiming.alpha_v[0].delay);   // Delay alpha-symp. to veins
        pvec.put(ParameterName.ABR_ALPHA_SYMP_VEN_PEAK, reflexTiming.alpha_v[0].peak);   // Peak alpha-symp. to veins
        pvec.put(ParameterName.ABR_ALPHA_SYMP_VEN_END, reflexTiming.alpha_v[0].end);   // End alpha-symp. to veins
        pvec.put(ParameterName.ABR_ALPHA_SYMP_ART_DELAY, reflexTiming.alpha_r[0].delay);   // Delay alpha-symp. to arteries
        pvec.put(ParameterName.ABR_ALPHA_SYMP_ART_PEAK, reflexTiming.alpha_r[0].peak);   // Peak alpha-symp. to arteries
        pvec.put(ParameterName.ABR_ALPHA_SYMP_ART_END, reflexTiming.alpha_r[0].end);   // End alpha-symp. to arteries
        pvec.put(ParameterName.PR_INTERVAL, system.T[2][0]);         // PR-interval
        pvec.put(ParameterName.ATRIAL_SYSTOLE_INTERVAL, system.T[0][0]);         // Atrial systole
        pvec.put(ParameterName.VENTRICULAR_SYSTOLE_INTERVAL, system.T[1][0]);         // Ventricular systole
        pvec.put(ParameterName.ASCENDING_AORTA_HEIGHT, compartmentParameters[0].height[0][0]);
        pvec.put(ParameterName.BRACHIOCEPHAL_ART_HEIGHT, compartmentParameters[1].height[0][0]);
        pvec.put(ParameterName.UBODY_ART_HEIGHT, compartmentParameters[2].height[0][0]);
        pvec.put(ParameterName.UBODY_VEN_HEIGHT, compartmentParameters[9].height[0][0]);
        pvec.put(ParameterName.SVC_HEIGHT, compartmentParameters[10].height[0][0]);
        pvec.put(ParameterName.DESCENDING_AORTA_HEIGHT, compartmentParameters[3].height[0][0]);
        pvec.put(ParameterName.ABDOM_AORTA_HEIGHT, compartmentParameters[4].height[0][0]);
        pvec.put(ParameterName.RENAL_ART_HEIGHT, compartmentParameters[5].height[0][0]);
        pvec.put(ParameterName.RENAL_VEN_HEIGHT, compartmentParameters[11].height[0][0]);
        pvec.put(ParameterName.SPLAN_ART_HEIGHT, compartmentParameters[6].height[0][0]);
        pvec.put(ParameterName.SPLAN_VEIN_HEIGHT, compartmentParameters[12].height[0][0]);
        pvec.put(ParameterName.LBODY_ART_HEIGHT, compartmentParameters[7].height[0][0]);
        pvec.put(ParameterName.LBODY_VEN_HEIGHT, compartmentParameters[13].height[0][0]);
        pvec.put(ParameterName.ABDOM_IVC_HEIGHT, compartmentParameters[14].height[0][0]);
        pvec.put(ParameterName.THORACIC_IVC_HEIGHT, compartmentParameters[15].height[0][0]);
        pvec.put(ParameterName.ASCENDING_AORTA_VOLUME, compartmentParameters[0].zpfv[0][0]);       // V aortic arch
        pvec.put(ParameterName.BRACH_ART_ZPFV, compartmentParameters[1].zpfv[0][0]);       // V upper thoracic aorta
        pvec.put(ParameterName.THORACIC_AORTA_ZPFV, compartmentParameters[2].zpfv[0][0]);       // V lower thoracic aorta
        pvec.put(ParameterName.UBODY_ART_ZPFV, compartmentParameters[3].zpfv[0][0]);       // V upper body arteries
        pvec.put(ParameterName.ABDOM_AORTA_ZPFV, compartmentParameters[4].zpfv[0][0]);       // V abdominal aorta
        pvec.put(ParameterName.RENAL_ART_ZPFV, compartmentParameters[5].zpfv[0][0]);       // V renal arteries
        pvec.put(ParameterName.SPLAN_ART_ZPFV, compartmentParameters[6].zpfv[0][0]);       // V splanchnic arteries
        pvec.put(ParameterName.LBODY_ART_ZPFV, compartmentParameters[7].zpfv[0][0]);       // V leg arteries
        pvec.put(ParameterName.PV144, system.height[0][0]);
        pvec.put(ParameterName.PV145, system.weight[0][0]);
        pvec.put(ParameterName.PV146, system.bodySurfaceArea[0][0]);
        pvec.put(ParameterName.ALPHA_CPV_DELAY, reflexTiming.alpha_cpv[0].delay);   // Delay alpha-symp. to veins
        pvec.put(ParameterName.ALPHA_CPV_PEAK, reflexTiming.alpha_cpv[0].peak);   // Peak alpha-symp. to veins
        pvec.put(ParameterName.ALPHA_CPV_END, reflexTiming.alpha_cpv[0].end);   // End alpha-symp. to veins
        pvec.put(ParameterName.ALPHA_CPA_DELAY, reflexTiming.alpha_cpr[0].delay);   // Delay alpha-symp. to arteries
        pvec.put(ParameterName.ALPHA_CPA_PEAK, reflexTiming.alpha_cpr[0].peak);   // Peak alpha-symp. to arteries
        pvec.put(ParameterName.ALPHA_CPA_END, reflexTiming.alpha_cpr[0].end);   // End alpha-symp. to arteries

    }
}
