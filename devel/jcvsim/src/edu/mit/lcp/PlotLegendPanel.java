package edu.mit.lcp;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class PlotLegendPanel extends JPanel {

    private TraceListModel traceList;

    private JPanel tracePanel;
    private Trace dynamicTrace;
    private PlotPanelStripChart plotPanel;
    private PlotAddElement addPlotTraceElement;

    PlotLegendPanel(TraceListModel listModel, PlotPanelStripChart parent) {
        traceList = listModel;
        plotPanel = parent;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        tracePanel = new JPanel();
        tracePanel.setLayout(new BoxLayout(tracePanel, BoxLayout.Y_AXIS));

        addPlotTraceElement = new PlotAddElement(CVSim.sim.getOutputVariables());
        add(addPlotTraceElement);
        add(new JSeparator());
        add(tracePanel);
        add(new Box.Filler(new Dimension(1, 1),
                new Dimension(1, Integer.MAX_VALUE),
                new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)));

        traceList.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                rePopulateLegend();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                rePopulateLegend();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                rePopulateLegend();
            }
        });

        populateLegend();
    }

    private void populateLegend() {
        dynamicTrace = addPlotTraceElement.getTrace();
        for (Trace trace : traceList) {
            if (dynamicTrace != trace) {
                tracePanel.add(new PlotLegendElement(trace));
            }
        }
    }

    private void rePopulateLegend() {
        tracePanel.removeAll();
        populateLegend();
        tracePanel.revalidate();
    }

    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    private class PlotAddElement extends JComponent {

        private JComboBox xTraceBox;
        private JComboBox yTraceBox;

        private Trace tempTrace;

        PlotAddElement(List<SimulationOutputVariable> vars) {
            setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

            xTraceBox = new JComboBox(new SimulationOutputVariableListModel(vars));
            xTraceBox.addActionListener(xTraceBoxActionListener);

            yTraceBox = new JComboBox(new SimulationOutputVariableListModel(vars));
            yTraceBox.addActionListener(yTraceBoxActionListener);

            //updateTrace();
            JButton addButton = new JButton("+");
            addButton.setMargin(new Insets(1, 1, 1, 1));
            addButton.addActionListener(addButtonActionListener);
            add(new JLabel("X:"));
            add(xTraceBox);
            add(new JLabel("Y:"));
            add(yTraceBox);
            add(addButton);
        }

        public Trace getTrace() {
            return tempTrace;
        }

// 	private void updateTrace() {
// 	    // Need to remove tempTrace first; Otherwise, everytime you change the 
// 	    // combobox selection, the previous selection is added to the legend panel
// 	    traceList.remove(tempTrace);
//  	    tempTrace = new Trace((VariableRecorderInterface)xTraceBox.getSelectedItem(),
//  				  (VariableRecorderInterface)yTraceBox.getSelectedItem(),
//  				  Color.BLACK);
//  	    traceList.add(tempTrace);
// 	}
        private ActionListener xTraceBoxActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //updateTrace();
            }
        };

        private ActionListener yTraceBoxActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //updateTrace();
            }
        };

        private ActionListener addButtonActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plotPanel.createNewTrace((SimulationOutputVariable) yTraceBox.getSelectedItem());
            }
        };
    }

    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////
    private class PlotLegendElement extends JComponent {

        public Trace myTrace;

        private JMenuBar settingsMenuBar;
        private ColorChooserMenu colorMenu;

        PlotLegendElement(Trace trace) {
            myTrace = trace;

            setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

            settingsMenuBar = new JMenuBar();
            settingsMenuBar.setMargin(new Insets(0, 0, 0, 0));

            colorMenu = new ColorChooserMenu(myTrace.getColor());
            settingsMenuBar.add(colorMenu);

            myTrace.addPropertyChangeListener(Trace.PROP_COLOR, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    colorMenu.setSelectedColor((Color) pce.getNewValue());
                }
            });

            JLabel lbl = new JLabel(trace.toString());
            JCheckBox enableButton = new JCheckBox(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    myTrace.setEnabled(((JCheckBox) event.getSource()).isSelected());
                }
            });
            enableButton.setSelected(myTrace.isEnabled());

            JButton removeButton = new JButton(new AbstractAction("X") {
                @Override
                public void actionPerformed(ActionEvent event) {
                    CVSim.sim.removeVariableRecorder(myTrace.getXVar());
                    CVSim.sim.removeVariableRecorder(myTrace.getYVar());
                    traceList.remove(myTrace);
                }
            });
            removeButton.setMargin(new Insets(1, 1, 1, 1));
            removeButton.setContentAreaFilled(false);

            add(enableButton);
            add(removeButton);
            add(settingsMenuBar);
            add(lbl);
        }

        //////////////////////////////////////
        class ColorChooserMenu extends JMenu {

            ColorChooserMenu() {
                this(Color.BLACK);
            }

            ColorChooserMenu(Color color) {
                setMargin(new Insets(0, 0, 0, 0));
                setIcon(new ColoredSquareIcon(color));
                add(new TraceColorMenuAction(myTrace, Color.BLACK));
                add(new TraceColorMenuAction(myTrace, Color.BLUE));
                add(new TraceColorMenuAction(myTrace, Color.CYAN));
                add(new TraceColorMenuAction(myTrace, Color.GREEN));
                add(new TraceColorMenuAction(myTrace, Color.LIGHT_GRAY));
                add(new TraceColorMenuAction(myTrace, Color.MAGENTA));
                add(new TraceColorMenuAction(myTrace, Color.ORANGE));
                add(new TraceColorMenuAction(myTrace, Color.PINK));
                add(new TraceColorMenuAction(myTrace, Color.RED));
                add(new TraceColorMenuAction(myTrace, Color.YELLOW));
            }

            public void setSelectedColor(Color color) {
                setIcon(new ColoredSquareIcon(color));
            }

            public Color getSelectedColor() {
                return ((ColoredSquareIcon) getIcon()).getColor();
            }
        }

        /////////////////////////////////////
        class ColoredSquareIcon implements Icon {

            private Color color;
            private int _size;

            public ColoredSquareIcon(Color c) {
                this(c, 12);
            }

            public ColoredSquareIcon(Color c, int s) {
                color = c;
                _size = s;
            }

            public Color getColor() {
                return color;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Color oldColor = g.getColor();
                g.setColor(color);
                g.fill3DRect(x, y, getIconWidth(), getIconHeight(), true);
                g.setColor(oldColor);
            }

            @Override
            public int getIconWidth() {
                return _size;
            }

            @Override
            public int getIconHeight() {
                return _size;
            }
        }

        ///////////////////////////////////////////////////////////
        private class TraceColorMenuAction extends AbstractAction {

            private Trace trace;
            private Color color;

            public TraceColorMenuAction(Trace trace, Color color) {
                this.trace = trace;
                this.color = color;
                putValue(SMALL_ICON, new ColoredSquareIcon(color));
                putValue(SHORT_DESCRIPTION, color.toString());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                trace.setColor(color);
            }
        }

    }
}
