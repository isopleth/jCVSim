package edu.mit.lcp;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class OutputPanel extends JPanel {
    
    private static final String CHOICE_ALL = "ALL";
    
    public OutputVariableTableModel model;
    
    // Tool Bar Elements
    private final JButton selectButton;
    private final JMenu selectButtonMenu;
    private final JMenu selectMenu;

    private final JComboBox<String> categoryComboBox;
    private JComboBox<String> typeComboBox;
    private final JTable table;
    private final JMenu actionsMenu;
    
    public OutputPanel() {
	this.setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();

	model = new OutputVariableTableModel(CVSim.sim.getOutputVariables());

	actionsMenu = new JMenu("Outputs");

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
	typeComboBox = new JComboBox<>(getModelOutputTypeChoices());
	typeComboBox.addActionListener(TypeComboBoxListener);
	topToolBar.add(typeComboBox);

	// bottom toolbar
	JToolBar bottomToolBar = new JToolBar();
	bottomToolBar.setFloatable(false);
	bottomToolBar.setRollover(true);

	selectButtonMenu = new JMenu("Select");
	JMenuItem selectButtonAllMenuItem = new JMenuItem("Select All");
	selectButtonAllMenuItem.addActionListener(new SelectAllOutputsAction(this));
	JMenuItem selectButtonVisibleMenuItem = new JMenuItem("Select Visible");
	selectButtonVisibleMenuItem.addActionListener(new SelectVisibleOutputsAction(this));
	JMenuItem selectButtonNoneMenuItem = new JMenuItem("Select None");
	selectButtonNoneMenuItem.addActionListener(new SelectNoneOutputsAction(this));
	selectButtonMenu.add(selectButtonAllMenuItem);
	selectButtonMenu.add(selectButtonVisibleMenuItem);
	selectButtonMenu.add(selectButtonNoneMenuItem);
	selectButton = new DropDownToolbarButton(selectButtonMenu);
	bottomToolBar.add(selectButton);
	// Need duplicate menu items for menubar
	selectMenu = new JMenu("Select");
	JMenuItem selectAllMenuItem = new JMenuItem("Select All");
	selectAllMenuItem.addActionListener(new SelectAllOutputsAction(this));
	JMenuItem selectVisibleMenuItem = new JMenuItem("Select Visible");
	selectVisibleMenuItem.addActionListener(new SelectVisibleOutputsAction(this));
	JMenuItem selectNoneMenuItem = new JMenuItem("Select None");
	selectNoneMenuItem.addActionListener(new SelectNoneOutputsAction(this));
	selectMenu.add(selectAllMenuItem);
	selectMenu.add(selectVisibleMenuItem);
	selectMenu.add(selectNoneMenuItem);
	actionsMenu.add(selectMenu);
	
	CVSim.sim.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
		public void propertyChange(PropertyChangeEvent e) {
		    model.fireTableDataChanged();
		}
	    });
	
	// table
	table = new DisableableJTable(model);
	table.setCellSelectionEnabled(true);

	JScrollPane scrollPane = new JScrollPane(table);

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.weightx = 0.5;
	constraints.weighty = 0.0;
	constraints.anchor = GridBagConstraints.FIRST_LINE_START;
	add(topToolBar, constraints);

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 0.5;
	constraints.weighty = 0.0;
	constraints.anchor = GridBagConstraints.FIRST_LINE_START;
	add(bottomToolBar, constraints);

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.weightx = 0.5;
	constraints.weighty = 1.0;
	constraints.fill = GridBagConstraints.BOTH;
	add(scrollPane, constraints);
    }


    ///////
    
    public JMenu getMenuOfActions() {
	return actionsMenu;
    }
    
    private class SelectAllOutputsAction extends AbstractAction {
	private final Component pc;
	public SelectAllOutputsAction(Component c) {
	    super("Select All");
	    pc = c;
	}
        @Override
	public void actionPerformed(ActionEvent e) {
	    for (SimulationOutputVariable v: model.getOutputList())
		model.setOutputSelected(v, true);
	}
    }

    private class SelectVisibleOutputsAction extends AbstractAction {
	private final Component pc;
	public SelectVisibleOutputsAction(Component c) {
	    super("Select Visible");
	    pc = c;
	}
        @Override
	public void actionPerformed(ActionEvent e) {
	    for (SimulationOutputVariable v: model.getFilteredOutputList())
		model.setOutputSelected(v, true);
	}
    }

    private class SelectNoneOutputsAction extends AbstractAction {
	private final Component pc;
	public SelectNoneOutputsAction(Component c) {
	    super("Select None");
	    pc = c;
	}
        @Override
	public void actionPerformed(ActionEvent e) {
	    for (SimulationOutputVariable v: model.getOutputList())
		model.setOutputSelected(v, false);
	}
    }
    
    private String[] getModelCategoryChoices() {
	List<String> choices = model.getCategoryNames();
	choices.add(0,CHOICE_ALL);
	String[] choicesArray = new String[choices.size()];
	choices.toArray(choicesArray);
	// alphabetize
	Arrays.sort(choicesArray);
	return choicesArray;
    }

    private String[] getModelOutputTypeChoices() {
	List<String> choices = model.getFilteredOutputTypeNames();
	choices.add(0,CHOICE_ALL);
	String[] choicesArray = new String[choices.size()];
	choices.toArray(choicesArray);
	// alphabetize
	Arrays.sort(choicesArray);
	return choicesArray;
    }

    private final ActionListener CategoryComboBoxListener = new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent e) {
		String selection = (String)((JComboBox)e.getSource()).getSelectedItem();
		if (selection.equals(CHOICE_ALL)) selection = null;
		model.setCategoryFilter(selection);

		String oldTypeSelection = (String)typeComboBox.getSelectedItem();
		typeComboBox.setModel(new DefaultComboBoxModel<>(getModelOutputTypeChoices()));
		typeComboBox.setSelectedItem(oldTypeSelection);
		if (!((String)typeComboBox.getSelectedItem()).equals(oldTypeSelection))
		    typeComboBox.setSelectedItem(CHOICE_ALL);
		    
	    }};

    private final ActionListener TypeComboBoxListener = new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent e) {
		String selection = (String)((JComboBox)e.getSource()).getSelectedItem();
		if (selection.equals(CHOICE_ALL)) selection = null;
		model.setTypeFilter(selection);
	    }};

    private class DropDownToolbarButton extends JButton {
	
	private JPopupMenu buttonMenu;
	
	public DropDownToolbarButton(JMenu menu) {
	    super(menu.getText());	  
	    buttonMenu = menu.getPopupMenu();
        
	    this.addActionListener(new ActionListener() {
                    @Override
		    public void actionPerformed(ActionEvent e) {
			Component c = (Component)e.getSource();
			buttonMenu.show(c, 0, c.getHeight());
		    }
		});
	}
    }

}
   
    


