package edu.mit.lcp;

import static edu.mit.lcp.SimulationModelType.TWENTY_ONE_COMPARTMENT_MODEL;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class ControlToolBar extends JToolBar {

    private final JToggleButton startButton;
    private final JToggleButton stopButton;
    private final JToggleButton abReflexOnButton;
    private final JToggleButton abReflexOffButton;
    private final JToggleButton cpReflexOnButton;
    private final JToggleButton cpReflexOffButton;
    private final JToggleButton breathingOnButton;
    private final JToggleButton breathingOffButton;
    private final JToggleButton lungVolumeReflexOnButton;
    private final JToggleButton lungVolumeReflexOffButton;
    private final JToggleButton resetButton;
    private final JToggleButton fastSpeedButton;
    private final JToggleButton internalUnitsButton;
    private final JToggleButton externalUnitsButton;
    private final JToggleButton limitedPrecisionDisplayButton;
    private final AtomicBoolean limitedPrecisionDisplay = new AtomicBoolean(true);
    public static final int DISPLAY_PRECISION_LIMIT = 5;

    public ControlToolBar() {

        setRollover(true);
        setFloatable(false);
        startButton = initButton("go.gif", new StartSimulationAction(),
                "Start Simulation");
        stopButton = initButton("stop.gif", new StopSimulationAction(),
                "Stop Simulation");

        add(Box.createRigidArea(new Dimension(20, 0)));
        abReflexOnButton = initButton("ABgo.gif", new ABReflexOnAction(),
                "Arterial Baroreflex Control System On");
        abReflexOffButton = initButton("ABstop.gif", new ABReflexOffAction(),
                "Arterial Baroreflex Control System Off");
        cpReflexOnButton = initButton("CPgo.gif", new CPReflexOnAction(),
                "Cardiopulmonary Reflex Control System On");
        cpReflexOffButton = initButton("CPstop.gif", new CPReflexOffAction(),
                "Cardiopulmonary Reflex Control System Off");

        // Breathing present only in 21 compartment model
        if (CVSim.getSimulationModel() == TWENTY_ONE_COMPARTMENT_MODEL) {

            add(Box.createRigidArea(new Dimension(20, 0)));
            breathingOnButton = initButton("BRgo.gif", new BreathingOnAction(), "Breathing On");
            breathingOffButton = initButton("BRstop.gif", new BreathingOffAction(), "Breathing Off");
            breathingOffButton.setSelected(true);

            lungVolumeReflexOnButton = initButton("LVgo.gif",
                    new LungVolumeReflexOnAction(),
                    "Lung volume heart rate reflexes On");
            lungVolumeReflexOffButton = initButton("LVstop.gif",
                    new LungVolumeReflexOffAction(),
                    "Lung volume heart rate reflexes Off");
            lungVolumeReflexOffButton.setSelected(true);

        } else {
            breathingOnButton = null;
            breathingOffButton = null;
            lungVolumeReflexOnButton = null;
            lungVolumeReflexOffButton = null;
        }

        add(Box.createRigidArea(new Dimension(20, 0)));
        resetButton = initButton("reset.gif", new ResetAction(),
                "Reset simulation");
        fastSpeedButton = initButton("fastSpeed.gif", new SimulationSpeedAction(),
                "Run simulation as fast as possible");

        add(Box.createRigidArea(new Dimension(20, 0)));
        internalUnitsButton = initButton("internalUnits.gif", new InternalUnitsAction(),
                "Display all values using the internal simulation representation");
        externalUnitsButton = initButton("externalUnits.gif", new ExternalUnitsAction(),
                "Display all values using the external representation");

        limitedPrecisionDisplayButton = initButton("limitDisplayPrecision.gif", new LimitedPrecisionDisplayAction(),
                "Limit display of values to "
                + DISPLAY_PRECISION_LIMIT + " significant figure precision");

        stopButton.setSelected(true);
        abReflexOffButton.setSelected(true);
        cpReflexOffButton.setSelected(true);
        externalUnitsButton.setSelected(true);
        limitedPrecisionDisplayButton.setSelected(true);
        limitedPrecisionDisplay.set(limitedPrecisionDisplayButton.isSelected());
    }

    /**
     * Create a button and add it to the display
     *
     * @param iconImage
     * @param action
     * @param toolTip
     * @return initialised button
     */
    private JToggleButton initButton(String iconImage, AbstractAction action,
            String toolTip) {
        ImageIcon icon = new ImageIcon(getClass().getResource("/buttonIcons/" + iconImage));
        JToggleButton button = new JToggleButton(icon);
        button.addActionListener(action);
        button.setToolTipText(toolTip);
        add(button);
        return button;

    }

    public JMenu createSimulationMenu() {
        JMenu simMenu = new JMenu("Simulation");
        AbstractAction[] actionList = {
            new StartSimulationAction(),
            new StopSimulationAction(),
            new ABReflexOnAction(),
            new ABReflexOffAction(),
            new CPReflexOnAction(),
            new CPReflexOffAction(),
            new ResetAction()
        };
        for (AbstractAction action : actionList) {
            simMenu.add(action);
        }

        // Only add the breathing controls to the 21 compartment model
        if (CVSim.getSimulationModel() == TWENTY_ONE_COMPARTMENT_MODEL) {
            AbstractAction[] list = {
                new BreathingOnAction(),
                new BreathingOffAction(),
                new LungVolumeReflexOnAction(),
                new LungVolumeReflexOffAction()
            };
            for (AbstractAction action : list) {
                simMenu.add(action);
            }
        }

        return simMenu;
    }

    /**
     * Enable or disable the physiological functionality buttons
     *
     * @param enabled true to enable, false to disable
     */
    public void setPatientModeButtons(boolean enabled) {
        JToggleButton[] buttons = {
            abReflexOnButton,
            abReflexOffButton,
            cpReflexOnButton,
            cpReflexOffButton,
            internalUnitsButton,
            externalUnitsButton,
            limitedPrecisionDisplayButton};
        for (JToggleButton button : buttons) {
            button.setEnabled(!enabled);
        }

        // Only enable the breathing controls to the 21 compartment model
        if (CVSim.getSimulationModel() == TWENTY_ONE_COMPARTMENT_MODEL) {
            breathingOnButton.setEnabled(!enabled);
            breathingOffButton.setEnabled(!enabled);
            lungVolumeReflexOnButton.setEnabled(!enabled);
            lungVolumeReflexOffButton.setEnabled(!enabled);
        }
    }

    /**
     * Test if numbers are to be displayed to a limited precision. Internally
     * they are represented as doubles, this just affects how they are
     * displayed.
     *
     * @return true if they are to be displayed to limited precision
     */
    boolean limitedPrecisionDisplayIsEnabled() {
        return limitedPrecisionDisplay.get();
    }

    // Actions
    private class StartSimulationAction extends AbstractAction {

        public StartSimulationAction() {
            super("Start Simulation");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            startButton.setSelected(true);
            stopButton.setSelected(false);
            CVSim.simThreadControl.setPeriodInMs(1);
            CVSim.simThreadControl.start();
            System.out.println("Start");
        }
    }

    private class StopSimulationAction extends AbstractAction {

        public StopSimulationAction() {
            super("Stop Simulation");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            stopButton.setSelected(true);
            startButton.setSelected(false);
            CVSim.simThreadControl.pause();
            System.out.println("Stop");
        }
    }

    // Arterial Baroreflexes
    private class ABReflexOnAction extends AbstractAction {

        public ABReflexOnAction() {
            super("Arterial Baroreflex Control System ON");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            abReflexOnButton.setSelected(true);
            abReflexOffButton.setSelected(false);
            CVSim.sim.setABReflex(true);
            System.out.println("AB Reflex ON");
        }
    }

    private class ABReflexOffAction extends AbstractAction {

        public ABReflexOffAction() {
            super("Arterial Baroreflex Control System OFF");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            abReflexOffButton.setSelected(true);
            abReflexOnButton.setSelected(false);
            CVSim.sim.setABReflex(false);
            System.out.println("AB Reflex OFF");
        }
    }

    // Cardiopulmonary reflexes
    private class CPReflexOnAction extends AbstractAction {

        public CPReflexOnAction() {
            super("Cardiopulmonary Reflex Control System ON");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            cpReflexOnButton.setSelected(true);
            cpReflexOffButton.setSelected(false);
            CVSim.sim.setCPReflex(true);
            System.out.println("CP Reflex ON");
        }
    }

    private class CPReflexOffAction extends AbstractAction {

        public CPReflexOffAction() {
            super("Cardiopulmonary Reflex Control System OFF");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            cpReflexOffButton.setSelected(true);
            cpReflexOnButton.setSelected(false);
            CVSim.sim.setCPReflex(false);
            System.out.println("CP Reflex OFF");
        }
    }

    // Breathing
    private class BreathingOnAction extends AbstractAction {

        public BreathingOnAction() {
            super("Breathing ON");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            breathingOnButton.setSelected(true);
            breathingOffButton.setSelected(false);
            CVSim.sim.setBreathing(true);
            System.out.println("Breathing ON");
        }
    }

    private class BreathingOffAction extends AbstractAction {

        public BreathingOffAction() {
            super("Breathing OFF");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            breathingOffButton.setSelected(true);
            breathingOnButton.setSelected(false);
            CVSim.sim.setBreathing(false);
            System.out.println("Breathing OFF");
        }
    }

    private class LungVolumeReflexOnAction extends AbstractAction {

        public LungVolumeReflexOnAction() {
            super("Lung volume reflex ON");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            lungVolumeReflexOffButton.setSelected(false);
            lungVolumeReflexOnButton.setSelected(true);
            CVSim.sim.setLungVolumeReflex(true);
            System.out.println("Lung volume reflex ON");
        }
    }

    private class LungVolumeReflexOffAction extends AbstractAction {

        public LungVolumeReflexOffAction() {
            super("Lung volume reflex OFF");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            lungVolumeReflexOffButton.setSelected(true);
            lungVolumeReflexOnButton.setSelected(false);
            CVSim.sim.setLungVolumeReflex(false);
            System.out.println("Lung volume reflex OFF");
        }
    }

    /**
     * Reset the simulation to its starting state
     */
    private class ResetAction extends AbstractAction {

        public ResetAction() {
            super("Reset Simulation");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CVSim.simThreadControl.reset();
            boolean wasRunning = CVSim.simThreadControl.isRunning();
            // Reset will stop the simulation
            resetButton.setSelected(false);
            System.out.println("Reset");
            // Wait for the system to reset
            while (CVSim.simThreadControl.stillResetting()) {
            }
            // Reload initial parameters
            CVSim.gui.parameterPanel.resetParameters(CVSim.gui.parameterPanel.model.getParameterList());
            CVSim.gui.parameterPanel.enablePatientDisplayMode(false);

            // Restart the simulation if it was running
            if (wasRunning) {
                CVSim.simThreadControl.start();
            }
        }
    }

    // Simulation speed    
    private class SimulationSpeedAction extends AbstractAction {

        public SimulationSpeedAction() {
            super("Simulation speed action");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CVSim.simThreadControl.realTimeDelayEnabled(!fastSpeedButton.isSelected());
        }
    }

    private class InternalUnitsAction extends AbstractAction {

        public InternalUnitsAction() {
            super("Internal units action");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            internalUnitsButton.setSelected(true);
            externalUnitsButton.setSelected(false);
            CVSim.useExternalUnits.set(false);
            CVSim.gui.parameterPanel.model.fireTableDataChanged();
            CVSim.gui.outputPanel.model.fireTableDataChanged();
        }
    }

    private class ExternalUnitsAction extends AbstractAction {

        public ExternalUnitsAction() {
            super("External units action");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            internalUnitsButton.setSelected(false);
            externalUnitsButton.setSelected(true);
            CVSim.useExternalUnits.set(true);
            CVSim.gui.parameterPanel.model.fireTableDataChanged();
            CVSim.gui.outputPanel.model.fireTableDataChanged();
        }
    }

    private class LimitedPrecisionDisplayAction extends AbstractAction {

        public LimitedPrecisionDisplayAction() {
            super("Limit display precision action");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            limitedPrecisionDisplay.set(limitedPrecisionDisplayButton.isSelected());
            CVSim.gui.parameterPanel.model.fireTableDataChanged();
            CVSim.gui.outputPanel.model.fireTableDataChanged();
        }
    }

}
