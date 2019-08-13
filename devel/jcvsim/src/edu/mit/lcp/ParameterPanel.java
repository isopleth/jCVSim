package edu.mit.lcp;

import static edu.mit.lcp.SimulationModelType.SIX_COMPARTMENT_MODEL;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ParameterPanel extends JPanel {

    private static final String CHOICE_ALL = "ALL";
    private boolean patientDisplayModeFlag = false;
    public ParameterTableModel model;
    private final JTable table;
    private boolean highlightingOff = false;
    private final JScrollPane scrollPane;
    private JMenu actionsMenu;

    // top toolbar
    public JComboBox<String> categoryComboBox;
    public JComboBox<String> typeComboBox;

    // middle toolbar
    private JButton selectButton;
    private JMenu selectButtonMenu;
    private JMenu selectMenu;
    private JButton loadButton;
    private JMenuItem loadMenuItem;
    private JButton saveButton;
    private JMenuItem saveMenuItem;
    private JButton resetButton;
    private JMenuItem resetMenuItem;

    // bottom toolbar
    private JButton clearHighlightingButton;
    private JMenuItem clearHighlightingMenuItem;
    private JCheckBox highlightingOffCheckBox;
    private JCheckBoxMenuItem highlightingOffCheckBoxMenuItem;
    private JButton exitPatientModeButton;
    private JMenuItem exitPatientModeMenuItem;

    public ParameterPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        model = new ParameterTableModel(CVSim.sim.getParameterList(), this);

        actionsMenu = new JMenu("Parameters");

        // top toolbar
        JToolBar topToolBar = new JToolBar();
        topToolBar.setFloatable(false);
        topToolBar.setRollover(true);

        JLabel locationFilterLabel = new JLabel("Show Location:");
        locationFilterLabel.setBorder(new EmptyBorder(0, 0, 0, 7));
        topToolBar.add(locationFilterLabel);
        categoryComboBox = new JComboBox<>(getModelCategoryChoices());
        categoryComboBox.addActionListener(CategoryComboBoxListener);
        topToolBar.add(categoryComboBox);

        JLabel typeFilterLabel = new JLabel("Show Type:");
        typeFilterLabel.setBorder(new EmptyBorder(0, 7, 0, 7));
        topToolBar.add(typeFilterLabel);
        typeComboBox = new JComboBox<>(getModelParameterTypeChoices());
        typeComboBox.addActionListener(TypeComboBoxListener);
        topToolBar.add(typeComboBox);

        // middle toolbar
        JToolBar middleToolBar = new JToolBar();
        middleToolBar.setFloatable(false);
        middleToolBar.setRollover(true);

        selectButtonMenu = new JMenu("Select");
        JMenuItem selectButtonAllMenuItem = new JMenuItem("Select All");
        selectButtonAllMenuItem.addActionListener(new SelectAllParametersAction(this));
        JMenuItem selectButtonVisibleMenuItem = new JMenuItem("Select Visible");
        selectButtonVisibleMenuItem.addActionListener(new SelectVisibleParametersAction(this));
        JMenuItem selectButtonNoneMenuItem = new JMenuItem("Select None");
        selectButtonNoneMenuItem.addActionListener(new SelectNoneParametersAction(this));
        selectButtonMenu.add(selectButtonAllMenuItem);
        selectButtonMenu.add(selectButtonVisibleMenuItem);
        selectButtonMenu.add(selectButtonNoneMenuItem);
        selectButton = new DropDownToolbarButton(selectButtonMenu);
        middleToolBar.add(selectButton);
        // Need duplicate menu items for menubar
        selectMenu = new JMenu("Select");
        JMenuItem selectAllMenuItem = new JMenuItem("Select All");
        selectAllMenuItem.addActionListener(new SelectAllParametersAction(this));
        JMenuItem selectVisibleMenuItem = new JMenuItem("Select Visible");
        selectVisibleMenuItem.addActionListener(new SelectVisibleParametersAction(this));
        JMenuItem selectNoneMenuItem = new JMenuItem("Select None");
        selectNoneMenuItem.addActionListener(new SelectNoneParametersAction(this));
        selectMenu.add(selectAllMenuItem);
        selectMenu.add(selectVisibleMenuItem);
        selectMenu.add(selectNoneMenuItem);
        actionsMenu.add(selectMenu);

        loadButton = new JButton(new LoadParametersFromFileAction(this, "Selected"));
        middleToolBar.add(loadButton);
        loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(new LoadParametersFromFileAction(this, "Selected"));
        actionsMenu.add(loadMenuItem);

        saveButton = new JButton(new SaveParametersToFileAction(this, "Selected"));
        middleToolBar.add(saveButton);
        saveMenuItem = new JMenuItem("Snapshot");
        saveMenuItem.addActionListener(new SaveParametersToFileAction(this, "Selected"));
        actionsMenu.add(saveMenuItem);

        resetButton = new JButton(new ResetParametersAction(resetMenuItem, "Selected"));
        middleToolBar.add(resetButton);
        resetMenuItem = new JMenuItem("Restore Default Values");
        resetMenuItem.addActionListener(new ResetParametersAction(resetMenuItem, "Selected"));
        actionsMenu.add(resetMenuItem);

        // bottom toolbar 
        JToolBar bottomToolBar = new JToolBar();
        bottomToolBar.setFloatable(false);
        bottomToolBar.setRollover(true);

        clearHighlightingButton = new JButton("Clear Highlighting");
        clearHighlightingButton.addActionListener(ClearHighlightingListener);
        bottomToolBar.add(clearHighlightingButton);
        clearHighlightingMenuItem = new JMenuItem("Clear Highlighting");
        clearHighlightingMenuItem.addActionListener(ClearHighlightingListener);
        actionsMenu.add(clearHighlightingMenuItem);

        highlightingOffCheckBox = new JCheckBox("Highlighting Off");
        highlightingOffCheckBox.addItemListener(HighlightingOffListener);
        bottomToolBar.add(highlightingOffCheckBox);
        highlightingOffCheckBoxMenuItem = new JCheckBoxMenuItem("Highlighting Off");
        highlightingOffCheckBoxMenuItem.addItemListener(HighlightingOffListener);
        actionsMenu.add(highlightingOffCheckBoxMenuItem);

        // only in 6C model
        if (CVSim.getSimulationModel() == SIX_COMPARTMENT_MODEL) {
            exitPatientModeButton = new JButton("Exit Patient Mode");
            exitPatientModeButton.addActionListener(new ExitPatientModeAction(this));
            exitPatientModeButton.setEnabled(false);
            bottomToolBar.add(exitPatientModeButton);
            exitPatientModeMenuItem = new JMenuItem("Exit Patient Mode");
            exitPatientModeMenuItem.addActionListener(new ExitPatientModeAction(this));
            exitPatientModeMenuItem.setEnabled(false);
            actionsMenu.add(exitPatientModeMenuItem);
        }

        CVSim.sim.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                model.fireTableDataChanged();
            }
        });

        table = new JTable(model) {
            // In order to highlight the boolean (checkbox) column, you need to override
            // prepareRenderer(); If you try to do this by extending the DefaultCellRenderer
            // class, the checkbox is displayed as a String
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

                Component c = super.prepareRenderer(renderer, row, column);
                Parameter p = (Parameter) (model.getFilteredParameterList().get(row));

                // Highlight row
                if (model.highlightList.contains(p)) {
                    c.setBackground(new Color(250, 250, 126)); //light yellow
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        };

        table.setCellSelectionEnabled(true);

        // set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        table.getColumnModel().getColumn(1).setPreferredWidth(10);
        table.getColumnModel().getColumn(2).setPreferredWidth(10);
        table.getColumnModel().getColumn(3).setPreferredWidth(10);

        // use custom renderer
        ParameterValueCellRenderer customRenderer = new ParameterValueCellRenderer();
        table.setDefaultRenderer(String.class, customRenderer);

        // use custom editor for value column
        // when a user double-clicks on an editable cell and starts typing, 
        // the new value should overwrite the old value, not be appended to it
        table.getColumnModel().getColumn(1).setCellEditor(new ParameterValueCellEditor(new JTextField()));
        scrollPane = new JScrollPane(table);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(topToolBar, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(middleToolBar, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.5;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(bottomToolBar, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 0.5;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, constraints);
    }

    /**
     * Test if patient display mode is enabled
     *
     * @return true if this mode is enabled
     */
    public boolean patientDisplayModeEnabled() {
        return patientDisplayModeFlag;
    }

    /**
     * Enable or disable patient mode
     *
     * @param enable true to enable patient mode, false to disable
     */
    public void enablePatientDisplayMode(boolean enable) {

        // deselect table; otherwise, selected values will not be overwritten
        if (table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }

        final JComponent[] componentsDisabledInPatientMode = {
            saveButton,
            selectButton,
            loadButton,
            saveMenuItem,
            selectMenu,
            loadMenuItem,
            resetMenuItem
        };

        final JComponent[] componentsEnabledInPatientMode = {
            exitPatientModeButton, exitPatientModeMenuItem
        };

        for (JComponent component : componentsDisabledInPatientMode) {
            if (component != null) {
                component.setEnabled(!enable);
            }
        }
        for (JComponent component : componentsEnabledInPatientMode) {
            if (component != null) {
                component.setEnabled(!enable);
            }
        }

        CVSim.gui.toolbar.setPatientModeButtons(enable);
        patientDisplayModeFlag = enable;
    }

    public JMenu getMenuOfActions() {
        return actionsMenu;
    }

    public boolean getHighlightingOff() {
        return highlightingOff;
    }

    private ItemListener HighlightingOffListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == highlightingOffCheckBox) {
                if (((JCheckBox) e.getSource()).isSelected()) {
                    highlightingOff = true;
                    highlightingOffCheckBoxMenuItem.setSelected(true);
                    // clear existing highlighting
                    model.updateHighlightList(null);
                } else {
                    highlightingOff = false;
                    highlightingOffCheckBoxMenuItem.setSelected(false);
                }
            } else if (e.getSource() == highlightingOffCheckBoxMenuItem) {
                if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                    highlightingOff = true;
                    highlightingOffCheckBox.setSelected(true);
                    // clear existing highlighting
                    model.updateHighlightList(null);
                } else {
                    highlightingOff = false;
                    highlightingOffCheckBox.setSelected(false);
                }
            }
        }
    };

    private ActionListener ClearHighlightingListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.updateHighlightList(null);
        }
    };

    private class ExitPatientModeAction extends AbstractAction {

        private Component pc;

        public ExitPatientModeAction(Component c) {
            super("Exit Patient Mode");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // exit patient mode and reset parameters to default values
            enablePatientDisplayMode(false);
            resetParameters(model.getParameterList());
        }
    }

    private class SelectAllParametersAction extends AbstractAction {

        private final Component pc;

        public SelectAllParametersAction(Component c) {
            super("Select All");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectAllParameters();
        }
    }

    private class SelectVisibleParametersAction extends AbstractAction {

        private final Component pc;

        public SelectVisibleParametersAction(Component c) {
            super("Select Visible");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Parameter p : model.getFilteredParameterList()) {
                model.setParameterSelected(p, true);
            }
        }
    }

    private class SelectNoneParametersAction extends AbstractAction {

        private final Component pc;

        public SelectNoneParametersAction(Component c) {
            super("Select None");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Parameter p : model.getParameterList()) {
                model.setParameterSelected(p, false);
            }
        }
    }

    /**
     * Reset the parameters
     *
     * @param list
     */
    public void resetParameters(java.util.List<Parameter> list) {
        Double defaultValue;
        Double oldValue;

        for (Parameter p : list) {
            defaultValue = p.getDefaultValue();
            oldValue = p.getValue();

            if ((!oldValue.equals(defaultValue))) {
                p.setValue(p.getDefaultValue());
                p.setPercent(100.0);
                System.out.println(p.getName() + " reset to " + defaultValue + " (was " + oldValue + ")");
            }

            model.fireTableDataChanged();
        }
        deselectAllParameters();
    }

    private void selectAllParameters() {
        for (Parameter p : model.getParameterList()) {
            model.setParameterSelected(p, true);
        }
    }

    private void deselectAllParameters() {
        for (Parameter p : model.getParameterList()) {
            model.setParameterSelected(p, false);
        }
    }

    private class ResetParametersAction extends AbstractAction {

        private final Component pc;
        private final String subAction;

        public ResetParametersAction(Component c, String action) {
            subAction = action;
            putValue(NAME, "Restore");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            // if the user is trying to reset selected parameters but hasn't selected any,
            // display error message
            if (subAction.equals("Selected") && (model.getSelectedParameterList().isEmpty())) {
                JOptionPane.showMessageDialog(pc,
                        "You have not selected any parameters.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                int ans = JOptionPane.showConfirmDialog(pc, "Do you really want to restore the " + subAction.toLowerCase() + " parameters?",
                        "Restore Parameters?", JOptionPane.YES_NO_OPTION);
                if (ans == JOptionPane.YES_OPTION) {
                    if (subAction.equals("All")) {
                        enablePatientDisplayMode(false);
                        resetParameters(model.getParameterList());
                    } else if (subAction.equals("Selected")) {
                        resetParameters(model.getSelectedParameterList());
                    } else {
                        throw new UnsupportedOperationException();
                    }

                }
            }
        }
    }

    private void WriteParameterFile(java.util.List<Parameter> list, File file) throws Exception {

        Properties prop = new Properties();
        for (Parameter p : list) {
            prop.setProperty(p.getName(), p.getValue().toString());
        }

        FileOutputStream fos = new FileOutputStream(file);
        prop.storeToXML(fos, "jCVSIM Simulation Parameter File. (Written by Version: "
                + CVSim.versionNumber + ", Model: " + CVSim.getSimulationModel() + ")");
        fos.close();

    }

    private class SaveParametersToFileAction extends AbstractAction {

        private JFileChooser fc;
        private final Component pc;
        private final String subAction;

        public SaveParametersToFileAction(Component c, String action) {
            subAction = action;
            putValue(NAME, "Snapshot");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fc = new JFileChooser();

            // if the user is trying to save selected parameters but hasn't selected any,
            // display error message
            if (subAction.equals("Selected") && (model.getSelectedParameterList().isEmpty())) {
                JOptionPane.showMessageDialog(pc,
                        "You have not selected any parameters.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                if (fc.showSaveDialog(pc) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Saving parameters to: " + file);
                    try {
                        if (subAction.equals("All")) {
                            WriteParameterFile(model.getParameterList(), file);
                        } else if (subAction.equals("Selected")) {
                            WriteParameterFile(model.getSelectedParameterList(), file);
                        } else {
                            throw new UnsupportedOperationException();
                        }

                        deselectAllParameters();

                    } catch (Exception ex) {
                        System.out.println("Error saving parameters. " + ex);
                        JOptionPane.showMessageDialog(pc, "Error Saving Parameters to " + file.toString()
                                + "\n-----\nException Says: " + ex,
                                "File Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void ReadParameterFile(java.util.List<Parameter> list, File file) throws Exception {
        double value;
        double oldvalue;
        boolean found = false;
        String prop;

        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(file);

        props.loadFromXML(fis); // throws exception

        for (Enumeration propNames = props.propertyNames(); propNames.hasMoreElements();) {
            prop = (String) propNames.nextElement();
            for (Parameter param : list) {
                if (param.getName().equals(prop)) {
                    found = true;
                    oldvalue = param.getValue();
                    value = Double.valueOf(props.getProperty(prop)); // throws exception
                    if (oldvalue != value) {
                        param.setValue(value);
                        System.out.println("Parameter \"" + param + "\" set to "
                                + value + " (was " + oldvalue + ")");
                    }
                }
            }
            if (!found) {
                System.out.println("\"" + prop + "\" did not match parameter");
            }
        }
    }

    private class LoadParametersFromFileAction extends AbstractAction {

        private JFileChooser fc;
        private final Component pc;
        private final String subAction;

        public LoadParametersFromFileAction(Component c, String action) {
            subAction = action;
            putValue(NAME, "Load");
            pc = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fc = new JFileChooser();

            // if the user is trying to save selected parameters but hasn't selected any,
            // display error message
            if (subAction.equals("Selected") && (model.getSelectedParameterList().isEmpty())) {
                JOptionPane.showMessageDialog(pc,
                        "You have not selected any parameters.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                if (fc.showOpenDialog(pc) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Loading Parameters From: " + file);
                    try {
                        if (subAction.equals("All")) {
                            ReadParameterFile(model.getParameterList(), file);
                        } else if (subAction.equals("Selected")) {
                            ReadParameterFile(model.getSelectedParameterList(), file);
                        } else {
                            throw new UnsupportedOperationException();
                        }

                        deselectAllParameters();

                    } catch (Exception ex) {
                        System.out.println("Error loading parameters. Some parameter values may have changed." + ex);
                        JOptionPane.showMessageDialog(pc, "Error Loading Parameters from " + file.toString()
                                + "\n-----\nException Says: " + ex,
                                "File Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private String[] getModelCategoryChoices() {
        java.util.List<String> choices = model.getCategoryNames();
        choices.add(0, CHOICE_ALL);
        String[] choicesArray = new String[choices.size()];
        choices.toArray(choicesArray);
        // alphabetize
        Arrays.sort(choicesArray);
        return choicesArray;
    }

    private String[] getModelParameterTypeChoices() {
        java.util.List<String> choices = model.getFilteredParameterTypeNames();
        choices.add(0, CHOICE_ALL);
        String[] choicesArray = new String[choices.size()];
        choices.toArray(choicesArray);
        // alphabetize
        Arrays.sort(choicesArray);
        return choicesArray;
    }

    private ActionListener CategoryComboBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selection = (String) ((JComboBox) e.getSource()).getSelectedItem();
            setCategoryComboBoxSelection(selection);
        }
    };

    public void setCategoryComboBoxSelection(String selection) {
        if (selection.equals(CHOICE_ALL)) {
            selection = null;
        }
        model.setCategoryFilter(selection);

        String oldTypeSelection = (String) typeComboBox.getSelectedItem();
        typeComboBox.setModel(new DefaultComboBoxModel<>(getModelParameterTypeChoices()));
        typeComboBox.setSelectedItem(oldTypeSelection);
        if (!((String) typeComboBox.getSelectedItem()).equals(oldTypeSelection)) {
            typeComboBox.setSelectedItem(CHOICE_ALL);
        }
    }

    private ActionListener TypeComboBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selection = (String) ((JComboBox) e.getSource()).getSelectedItem();
            setTypeComboBoxSelection(selection);
        }
    };

    public void setTypeComboBoxSelection(String selection) {
        if (selection.equals(CHOICE_ALL)) {
            selection = null;
        }
        model.setTypeFilter(selection);
    }

    public void recenterTable(int centerRow) {
        // get scrollpane viewport
        JViewport viewport = scrollPane.getViewport();
        // Get x,y location of the upper left corner of the viewport 
        Point startingPoint = viewport.getViewPosition();
        // Convert x,y location to row number
        int oldTopRow = table.rowAtPoint(startingPoint);
        // Get the size of the viewport
        Dimension size = viewport.getExtentSize();
        // Given the size of the viewport, find out how many rows can be displayed
        int numRows = (int) (size.getHeight() / table.getRowHeight());
        // Need to calculate which row will be at the top of the table once
        // the table is recentered
        int newTopRow = (centerRow - (int) (numRows / 2));
        if (newTopRow < 0) {
            newTopRow = 0;
        }
        // Convert row number to y location
        int newY = startingPoint.y + (newTopRow - oldTopRow) * table.getRowHeight();
        // Set the view to the new y position
        viewport.setViewPosition(new Point(startingPoint.x, newY));
    }

    ///////
    ///////
    private class DropDownToolbarButton extends JButton {

        private JPopupMenu buttonMenu;

        public DropDownToolbarButton(JMenu menu) {
            super(menu.getText());
            buttonMenu = menu.getPopupMenu();

            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Component c = (Component) e.getSource();
                    buttonMenu.show(c, 0, c.getHeight());
                }
            });
        }

        public void addMenuItem(JMenuItem mitem) {
            buttonMenu.add(mitem);
        }
    }

    // custom renderer
    // show non-default parameter values in red
    public class ParameterValueCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            Parameter parameter = (Parameter) (model.getFilteredParameterList().get(row));

            // set default colors
            cell.setForeground(Color.BLACK);
            cell.setBackground(Color.WHITE);

            // for all columns
            // Highlight row
            if (model.highlightList.contains(parameter)) {
                cell.setBackground(new Color(250, 250, 126)); //light yellow
            }
            // only for value column
            if (column == ParameterTableModel.COL_VALUE) {
                // right justify
                setHorizontalAlignment(SwingConstants.RIGHT);

                // Set tooltip
                String str = String.format("Allowed range: %.2f to %.2f", parameter.getMin(), parameter.getMax());
                setToolTipText(str);

                // for DEFAULT mode: if the current value is not the default value, 
                // display the value in red
                // for PATIENT mode: if the current value is not 100%, 
                //display the value in red
                if (patientDisplayModeFlag) {
                    if (parameter.getPercent() != 100) {
                        cell.setForeground(Color.RED);
                    }
                } else {
                    Double defaultValue;
                    if (CVSim.useExternalUnits.get()) {
                        defaultValue = parameter.getExternalRepresentationOfDefaultValue();
                    } else {
                        defaultValue = parameter.getDefaultValue();
                    }
                    Double currentValue = Double.valueOf(value.toString());
                    if (!currentValue.equals(defaultValue)) {
                        cell.setForeground(Color.RED);
                        System.out.println(currentValue + " " + defaultValue);
                    }
                }

            } else {
                // if not Value col 
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            return this;
        }
    }

    // custom cell editor
    // when a user double-clicks on an editable cell and starts typing, the
    // new value should overwrite the old value, not be appended to it
    private class ParameterValueCellEditor extends DefaultCellEditor implements FocusListener {

        JTextField tf;

        public ParameterValueCellEditor(JTextField tf) {
            super(tf);
            this.tf = tf;
            tf.setHorizontalAlignment(JTextField.RIGHT);
            tf.addFocusListener(this);
            clickCountToStart = 1;
        }

        // DefaultCellEditor methods
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            tf.setText(value.toString());
            return tf;
        }

        // FocusListener methods
        @Override
        public void focusGained(FocusEvent e) {
            tf.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
    }
}
