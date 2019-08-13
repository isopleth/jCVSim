package edu.mit.lcp;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class OutputVariableTableModel extends AbstractTableModel {

    private static final int COL_NAME = 0;
    private static final int COL_UNITS = 1;
    private static final int COL_SELECTED = 2;

    private static final String[] columnNames = {
        "Name", "Units", "Select"
    };

    private final List<SimulationOutputVariable> outputList;

    private final List<SimulationOutputVariable> displayList;
    private String _categoryFilter;
    private String _typeFilter;

    public OutputVariableTableModel(List<SimulationOutputVariable> list) {
        outputList = list;
        displayList = new ArrayList<>(outputList);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return displayList.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        SimulationOutputVariable variable = displayList.get(row);
        switch (column) {
            case COL_NAME:
                return variable.getDescription();
            case COL_UNITS:
                if (CVSim.useExternalUnits.get()) {
                    return variable.getExternalUnits();
                } else {
                    return variable.getInternalUnits();
                }
            case COL_SELECTED:
                return variable.isSelected();
            default:
                return "Invalid Column Lookup";
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        return col == COL_SELECTED;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        setOutputSelected(displayList.get(row), (Boolean) value);
        fireTableCellUpdated(row, col);
    }

    public void setCategoryFilter(String category) {
        _categoryFilter = category;
        updateDisplayList();
    }

    public void setTypeFilter(String type) {
        _typeFilter = type;
        updateDisplayList();
    }

    private void updateDisplayList() {
        System.out.println("updateDisplayList(): category: " + _categoryFilter
                + " -- type: " + _typeFilter);
        displayList.clear();

        for (SimulationOutputVariable v : outputList) {
            if ((_categoryFilter == null) || (_categoryFilter.equals(v.getCategory()))) {
                if ((_typeFilter == null) || (_typeFilter.equals(v.getType()))) {
                    displayList.add(v);
                }
            }
        }
        fireTableDataChanged();
    }

    public List<String> getCategoryNames() {
        ArrayList<String> names = new ArrayList<>();
        String cat;
        for (SimulationOutputVariable v : outputList) {
            cat = v.getCategory();
            if (!names.contains(cat)) {
                names.add(cat);
            }
        }
        return names;
    }

    public List<String> getOutputTypeNames() {
        return getFilteredOutputTypeNames(null);
    }

    public List<String> getFilteredOutputTypeNames() {
        return getFilteredOutputTypeNames(_categoryFilter);
    }

    public List<String> getFilteredOutputTypeNames(String filter) {
        ArrayList<String> names = new ArrayList<>();
        String type;
        for (SimulationOutputVariable v : outputList) {
            if ((filter == null) || (filter.equals(v.getCategory()))) {
                type = v.getType();
                if (!names.contains(type)) {
                    names.add(type);
                }
            }
        }
        return names;
    }

    public void setOutputSelected(SimulationOutputVariable simulationOutputVariable, Boolean sel) {
        simulationOutputVariable.select(sel);
        int _idx = displayList.indexOf(simulationOutputVariable);
        if (_idx >= 0) {
            fireTableCellUpdated(_idx, COL_SELECTED);
        }
    }

    public List<SimulationOutputVariable> getOutputList() {
        return outputList;
    }

    public List<SimulationOutputVariable> getFilteredOutputList() {
        return displayList;
    }

}
