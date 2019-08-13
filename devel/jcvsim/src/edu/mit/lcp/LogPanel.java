package edu.mit.lcp;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;

public class LogPanel extends JPanel {

    private final JLabel logFileLabel;
    private final JLabel recordsWrittenLabel;
    private final JScrollPane scrollPane;
    private final JTextArea textArea;
    private OutputFile outputFile;
    private final JToggleButton onSelectButton;
    private final JToggleButton onButton;
    private final JToggleButton offButton;
    private final AtomicBoolean loggingEnabled = new AtomicBoolean(false);
    private final JRadioButton firstButton;
    private final JRadioButton secondButton;
    private AtomicLong recordsWrittenCount = new AtomicLong(0);
    private final Timer timer;

    /**
     * Constructor
     */
    public LogPanel() {
        logFileLabel = new JLabel();
        Box filenameBox = Box.createHorizontalBox();
        filenameBox.add(new JLabel("Log file: "));
        filenameBox.add(logFileLabel);

        recordsWrittenLabel = new JLabel(" ");

        ImageIcon startIcon = new ImageIcon(getClass().getResource("/buttonIcons/go.gif"));
        onButton = new JToggleButton(startIcon);
        onButton.addActionListener(new LogPanel.Start());
        onButton.setToolTipText("Start logging");

        ImageIcon startIconSelectFile = new ImageIcon(getClass().getResource("/buttonIcons/select.gif"));
        onSelectButton = new JToggleButton(startIconSelectFile);
        onSelectButton.addActionListener(new LogPanel.SelectAndStart());
        onSelectButton.setToolTipText("Select output file then start logging");

        ImageIcon stopIcon = new ImageIcon(getClass().getResource("/buttonIcons/stop.gif"));
        offButton = new JToggleButton(stopIcon);
        offButton.addActionListener(new LogPanel.Stop());
        offButton.setToolTipText("Stop logging");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.add(onButton);
        toolBar.add(onSelectButton);
        toolBar.add(offButton);

        firstButton = new JRadioButton("Record every sample");
        secondButton = new JRadioButton("Record a sample every 10 ms");
        secondButton.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(firstButton);
        group.add(secondButton);
        Box sampleRateBox = Box.createVerticalBox();
        sampleRateBox.add(firstButton);
        sampleRateBox.add(secondButton);

        textArea = new JTextArea(5, 20);
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(toolBar);
        add(filenameBox);
        add(sampleRateBox);
        add(recordsWrittenLabel);
        add(scrollPane);
        toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        sampleRateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        recordsWrittenLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filenameBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        offButton.setSelected(true);
        onButton.setSelected(false);
        onSelectButton.setSelected(false);
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordsWrittenLabel.setText(String.valueOf(recordsWrittenCount.get()) + " records written");
            }
        });
        timer.start();
    }

    /**
     * Turn on logging to the specified file.
     *
     * @param file output file, or null to use default name
     */
    void switchLoggingOn(File file) {

        if (!loggingEnabled.get()) {

            onButton.setSelected(true);
            onSelectButton.setSelected(true);
            offButton.setSelected(false);

            textArea.setText(null);
            if (file == null) {
                file = getDefaultFilename();
            }
            if (fileOk(file)) {
                List<Parameter> reorderedParameterList = new ArrayList<>();
                List<SimulationOutputVariable> reorderedOutputList = new ArrayList<>();

                List<Parameter> parameterList = CVSim.gui.parameterPanel.model.getSelectedParameterList();

                // reorder output list to match order of output table
                for (SimulationOutputVariable simulationOutputVariable : CVSim.sim.getOutputVariables()) {
                    if (simulationOutputVariable.isSelected()) {
                        reorderedOutputList.add(simulationOutputVariable);
                    }
                }

                // reorder parameter list to match order of parameter table
                for (Parameter p : CVSim.sim.getParameterList()) {
                    if (parameterList.contains(p)) {
                        reorderedParameterList.add(p);
                    }
                }
                logFileLabel.setText(file.getAbsolutePath() + "  ");

                outputFile = new OutputFile(reorderedParameterList, reorderedOutputList,
                        getMinimumTimeBetweenUpdatesMs(), recordsWrittenCount, file);
                try {
                    outputFile.startLogging();

                    // display list of variables being logged in scrollPane
                    if (reorderedOutputList.size() > 0) {
                        textArea.append("Outputs:\n");
                        for (SimulationOutputVariable o : reorderedOutputList) {
                            textArea.append(o.getDescription() + "\n");
                        }
                    }

                    if (reorderedParameterList.size() > 0) {
                        textArea.append("\nParameters:\n");
                        for (Parameter p : reorderedParameterList) {
                            textArea.append(p.getName() + "\n");
                        }
                    }
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(this, "Cannot start logging " + file.toString(),
                            "File Error", JOptionPane.ERROR_MESSAGE);
                }

                loggingEnabled.set(true);

            }
        }
    }

    /**
     * Switch logging off
     */
    void switchLoggingOff() {
        if (loggingEnabled.get()) {
            offButton.setSelected(true);
            onSelectButton.setSelected(false);
            onButton.setSelected(false);
            logFileLabel.setText("");

            outputFile.stopLogging();
            outputFile = null;
            loggingEnabled.set(false);
        }
    }

    /**
     * Get default filename
     *
     * @return default filename
     */
    private File getDefaultFilename() {
        // Create default filename based on date and time
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMddyyyy_HHmmss");
        String defaultFilename = String.format("jcvsim_output_" + df.format(date) + ".txt");
        return new File(defaultFilename);
    }

    /**
     * Check the selected file can be written
     *
     * @param file file to write
     * @return true if all OK
     */
    private boolean fileOk(File file) {
        try {
            if (file.createNewFile()) {
                if (file.canWrite()) {
                    // then the file is valid
                } else {
                    JOptionPane.showMessageDialog(LogPanel.this,
                            "Cannot write to file " + file.toString(),
                            "File Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                int response = JOptionPane.showConfirmDialog(LogPanel.this, file.toString()
                        + " Already Exists. Do you want to overwrite the file?",
                        "Overwrite File?", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    if (file.canWrite()) {
                        // then the file is valid
                    } else {
                        JOptionPane.showMessageDialog(LogPanel.this,
                                "Cannot write to file " + file.toString(),
                                "File Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(LogPanel.this,
                    "Cannot write to file " + file.toString(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (HeadlessException ex) {
            System.out.println("Headless system");
            return false;
        }
        return true;
    }
    
    /**
     * Get minimum time between updates
     * 
     * @return interval in milliseconds
     */
    private long getMinimumTimeBetweenUpdatesMs() {
        if (secondButton.isSelected()) {
            return 10;
        }
        return 0;
    }

    /**
     * Select filename and start logging button
     */
    private class SelectAndStart implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!loggingEnabled.get()) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(LogPanel.this.getDefaultFilename());
                if (fileChooser.showSaveDialog(LogPanel.this) == JFileChooser.APPROVE_OPTION) {
                    LogPanel.this.switchLoggingOn(fileChooser.getSelectedFile());
                }
            } else {
                onSelectButton.setSelected(false);
            }
        }
    }

    /**
     * Stop logging button
     */
    private class Stop implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            LogPanel.this.switchLoggingOff();
        }
    }

    /**
     * Start logging button
     */
    private class Start implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!loggingEnabled.get()) {
                LogPanel.this.switchLoggingOn(null);
            } else {
                onButton.setSelected(false);
            }
        }
    }

}
