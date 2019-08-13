package edu.mit.lcp;

/**
 * Contains the program entry point. Creates and shows the GUI on the
 * event-dispatching thread
 */
import static edu.mit.lcp.SimulationModelType.SIX_COMPARTMENT_MODEL;
import static edu.mit.lcp.SimulationModelType.TWENTY_ONE_COMPARTMENT_MODEL;
import jcvsim.compartment21.CSimulation21C;
import jcvsim.compartment6.CSimulation6C;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;

public class CVSim {

    public static final VersionNumber versionNumber = new VersionNumber(1, 16);

    // Create the simulation as static to allow it to be accessed in a
    // global manner.  it still needs to be scheduled/started later.
    public static SimulationThreadControl simThreadControl;
    public static CSimulation sim;
    public static MainWindow gui;
    private static SimulationModelType simulationModelType;
    public static AtomicBoolean useExternalUnits = new AtomicBoolean(true);

    public static void main(String[] args) {
        boolean speedTest = false;

        System.out.println("CVSim.main(...)");

        simulationModelType = null;
        for (String s : args) {
            if (s.equals("-m6")) {
                simulationModelType = SIX_COMPARTMENT_MODEL;
            }
            if (s.equals("-m21")) {
                simulationModelType = TWENTY_ONE_COMPARTMENT_MODEL;
            }
            if (s.equals("-speed")) {
                speedTest = true;
            }
        }

        if (simulationModelType == null) {
            Object[] possibilities = {SIX_COMPARTMENT_MODEL, TWENTY_ONE_COMPARTMENT_MODEL};
            // Default the selection to the 21 compartment model
            simulationModelType = (SimulationModelType) JOptionPane.showInputDialog(null,
                    "Please select a simulation model:", "Select Model",
                    JOptionPane.PLAIN_MESSAGE, null, possibilities,
                    TWENTY_ONE_COMPARTMENT_MODEL);
        }

        switch (simulationModelType) {
            case SIX_COMPARTMENT_MODEL:
                sim = new CSimulation6C();
                break;
            case TWENTY_ONE_COMPARTMENT_MODEL:
                sim = new CSimulation21C();
                break;
            default:
                throw new RuntimeException("Undefined model type - neither 6 nor 21 compartments");
        }

        if (speedTest) {
            int[] dcf = {1, 1, 5, 10, 20, 50, 100, 1000};
            int simTime = 500;
            System.out.println("Testing " + simulationModelType + " Model backend speed.");
            System.out.print("Simulating " + simTime + " seconds using DataCompressionFactors: ");
            for (int i : dcf) {
                System.out.print(i + " ");
            }
            System.out.print("\n");

            for (int i : dcf) {
                //System.out.print("DataCompressionFactor = " + i + ", ");
                sim.setDataCompressionFactor(i);
                long startTime = System.currentTimeMillis();
                for (int s = 0; s < ((simTime * 1000) / i); s++) {
                    sim.step(0.01);
                }
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                System.out.println("Wall Clock Time Elapsed: " + (double) duration / 1000
                        + "sec -- (" + (double) simTime / ((double) duration / 1000) + "x realtime)");
                sim.reset();
            }
        } else {
            simThreadControl = new SimulationThreadControl(sim);
            gui = new MainWindow();

            // Setup GUI to run in the event-dispatching-thread by using
            // invokeLater method.
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.createAndShowGUI();
                }
            });
        }
    }

    /**
     * Getter for simulation model. There is multithreaded access to the
     * simulation model, but it is immutable once set by the initial selection
     * dialog so no need to do anything special here.
     *
     * @return simulation model
     */
    public static SimulationModelType getSimulationModel() {
        return simulationModelType;
    }

    public static boolean getTiltTest() {
        if (gui.tiltTestFrame != null) {
            return gui.tiltTestFrame.getTiltTest();
        }
        return false;
    }

    public static double getTiltStartTime() {
        if (gui.tiltTestFrame != null) {
            return gui.tiltTestFrame.getTiltStartTime();
        }
        return 0.;
    }

    public static double getTiltStopTime() {
        if (gui.tiltTestFrame != null) {
            return gui.tiltTestFrame.getTiltStopTime();
        }
        return 0.;
    }

}
