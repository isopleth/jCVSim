package jcvsim.compartment21;

import edu.mit.lcp.CVSim;
import edu.mit.lcp.SpringUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;

public class TiltTestFrame extends JFrame implements PropertyChangeListener {

    public static final String TILTTEST = "TILTTEST";

    private static final ParameterName tiltTimeIndex = ParameterName.TIME_TO_MAX_TILT_ANGLE;
    private static final ParameterName tiltAngleIndex = ParameterName.TILT_ANGLE;
    private static final ParameterName maxVolumeLossIndex = ParameterName.MAXIMAL_BLOOD_VOLUME_LOSS_DURINT_TILT;

    private final JFrame frame;
    private final JToggleButton tiltTestOnButton;
    private final JToggleButton tiltTestOffButton;
    private final JFormattedTextField tiltTimeField;
    private final JFormattedTextField tiltAngleField;
    private final JFormattedTextField maxVolumeLossField;

    private final CSimulation21C sim = (CSimulation21C) (CVSim.sim);
    private final Parameters pvec = sim.getParameterVector();

    private double tiltTime;
    private double tiltAngle;
    private double maxVolumeLoss;

    private boolean tiltTest = false;
    private double tiltStartTime = 0.0;
    private double tiltStopTime = 0.0;

    public TiltTestFrame() {

        frame = new JFrame("Perform Tilt Test");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        NumberFormat numberFormat = NumberFormat.getNumberInstance();

        // tilt time
        JLabel tiltTimeLabel = new JLabel("Tilt Time:");
        tiltTime = pvec.get(tiltTimeIndex);
        tiltTimeField = new JFormattedTextField(numberFormat);
        tiltTimeField.setValue(tiltTime);
        tiltTimeField.setColumns(3);
        tiltTimeField.addPropertyChangeListener(this);

        // tilt angle
        JLabel tiltAngleLabel = new JLabel("Tilt Angle:");
        tiltAngle = pvec.get(tiltAngleIndex);
        tiltAngleField = new JFormattedTextField(numberFormat);
        tiltAngleField.setValue(tiltAngle);
        tiltAngleField.setColumns(3);
        tiltAngleField.addPropertyChangeListener(this);

        // max volume loss
        JLabel maxVolumeLossLabel = new JLabel("Max. Volume Loss:");
        maxVolumeLoss = pvec.get(maxVolumeLossIndex);
        maxVolumeLossField = new JFormattedTextField(numberFormat);
        maxVolumeLossField.setValue(maxVolumeLoss);
        maxVolumeLossField.setColumns(3);
        maxVolumeLossField.addPropertyChangeListener(this);

        tiltTestOnButton = new JToggleButton("Start Tilt");
        tiltTestOnButton.addActionListener(new tiltTestOnAction(this));
        tiltTestOffButton = new JToggleButton("Stop Tilt");
        tiltTestOffButton.addActionListener(new tiltTestOffAction(this));

        JPanel contentPane = new JPanel(new SpringLayout());
        contentPane.add(tiltTimeLabel);
        contentPane.add(tiltTimeField);
        contentPane.add(new JLabel("seconds"));
        contentPane.add(tiltAngleLabel);
        contentPane.add(tiltAngleField);
        contentPane.add(new JLabel("degrees"));
        contentPane.add(maxVolumeLossLabel);
        contentPane.add(maxVolumeLossField);
        contentPane.add(new JLabel("mL"));
        contentPane.add(tiltTestOnButton);
        contentPane.add(tiltTestOffButton);
        contentPane.add(new JLabel(""));
        SpringUtilities.makeCompactGrid(contentPane,
                4, 3, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        frame.setContentPane(contentPane);
        frame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation(screenSize.width / 2 - size.width / 2,
                screenSize.height / 2 - size.height / 2);
    }

    public void setTiltTest(boolean b) {
        boolean old = getTiltTest();
        tiltTest = b;
        firePropertyChange(TILTTEST, old, b);
    }

    public boolean getTiltTest() {
        return tiltTest;
    }

    public double getTiltStartTime() {
        return tiltStartTime;
    }

    public void setTiltStartTime() {
        tiltStartTime = sim.getOutput(0);
    }

    public double getTiltStopTime() {
        return tiltStopTime;
    }

    public void setTiltStopTime() {
        tiltStopTime = sim.getOutput(0);
    }

    public void performTiltTest() {
        if (!(frame.isVisible())) {
            frame.setVisible(true);
        }
        if (frame.getExtendedState() == JFrame.ICONIFIED) {
            frame.setExtendedState(JFrame.NORMAL);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        CSimulation21C simulation = (CSimulation21C) CVSim.sim;
        Simulation main = simulation.simulation;
        if (source == tiltTimeField) {
            tiltTime = ((Number) tiltTimeField.getValue()).doubleValue();
            main.updateParameter(tiltTime,
                    pvec,
                    tiltTimeIndex);
        }
        if (source == tiltAngleField) {
            tiltAngle = ((Number) tiltAngleField.getValue()).doubleValue();
            main.updateParameter(tiltAngle,
                    pvec,
                    tiltAngleIndex);
        }
        if (source == maxVolumeLossField) {
            maxVolumeLoss = ((Number) maxVolumeLossField.getValue()).doubleValue();
            main.updateParameter(maxVolumeLoss,
                    pvec,
                    maxVolumeLossIndex);
        }
    }

    private class tiltTestOnAction extends AbstractAction {

        private final Component pc;

        public tiltTestOnAction(Component c) {
            super("Tilt Test On");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            tiltTestOnButton.setSelected(true);
            tiltTestOffButton.setSelected(false);
            setTiltStartTime();
            setTiltTest(true);
            System.out.println("Tilt Test ON");
        }
    }

    private class tiltTestOffAction extends AbstractAction {

        private final Component pc;

        public tiltTestOffAction(Component c) {
            super("Tilt Test Off");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            tiltTestOffButton.setSelected(true);
            tiltTestOnButton.setSelected(false);
            setTiltStopTime();
            setTiltTest(false);
            System.out.println("Tilt Test OFF");
        }
    }

}
