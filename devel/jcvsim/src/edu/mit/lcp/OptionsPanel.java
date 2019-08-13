package edu.mit.lcp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OptionsPanel extends JPanel {

    private JSlider speedSlider;

    public OptionsPanel() {

        // Create slider for simulation speed
        speedSlider = new JSlider(1, 10, 1); // min,max,default
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(speedSliderListener);
        speedSlider.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Simulation Speed"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(speedSlider, constraints);
    }

    // Listener
    ChangeListener speedSliderListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (!speedSlider.getValueIsAdjusting()) {
                long period = 10;
                int speed = (int) speedSlider.getValue();
                int comp = (int) ((double) (period * speed));
                CVSim.sim.setDataCompressionFactor(comp);
                CVSim.simThreadControl.setPeriodInMs(period);
            }
        }
    };

}
