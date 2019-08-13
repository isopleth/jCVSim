package edu.mit.lcp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class Patient {

    private final String name;
    private final String history;
    private final List<Parameter> parameterList;
    private final List<Double> valueList;
    private final JFrame frame;

    public Patient(String name, String history) {
        this.name = name;
        this.history = history;
        parameterList = new ArrayList<>();
        valueList = new ArrayList<>();
        frame = createPatientHistoryFrame();
    }

    private JFrame createPatientHistoryFrame() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setText(history);
        JScrollPane scrollPane = new JScrollPane(textPane);

        JFrame theFrame = new JFrame("Patient History: " + name);
        theFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.setOpaque(true);
        theFrame.setContentPane(contentPane);

        int width = 600;
        int height = 300;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        theFrame.setSize(width, height);
        theFrame.setLocation(screenSize.width / 2 - width / 2,
                screenSize.height / 2 - height / 2);
        return theFrame;
    }

    public void addParameterSetting(String name, Double value) {
        Parameter parameter = CVSim.sim.getParameterByName(name);
        if (parameter != null) {
            parameterList.add(parameter);
            valueList.add(value);
        } else {
            System.out.print("Could not load " + name + " from patient data.\n");
        }
    }

    public void loadParameterSettings() {

        // Reset all parameters
        CVSim.gui.parameterPanel.resetParameters(CVSim.sim.getParameterList());

        // Set parameter table model display mode to PATIENT
        CVSim.gui.parameterPanel.enablePatientDisplayMode(true);

        // load patient parameter settings
        for (int i = 0; i < parameterList.size(); i++) {
            (parameterList.get(i)).setValue(valueList.get(i));
            (parameterList.get(i)).setPtModeDefaultValue(valueList.get(i));
        }
    }

    public JFrame getFrame() {
        return frame;
    }
    
    public String getName() {
        return name;
    }
}
