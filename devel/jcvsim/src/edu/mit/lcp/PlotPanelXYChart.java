package edu.mit.lcp;

import static edu.mit.lcp.PlotType.PARAMETRIC;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

// Custom plotter, extension of JPanel
public class PlotPanelXYChart extends PlotPanel {

    private static final int NUM_TICKS = 5;

    private int traceBufferSize;
    private final TraceListModel traceList;
    private final XscaleComponent xScale;
    private final JSlider timeSlider;

    private double trailLength;

    private boolean multipleXScales = false;

    private PlotComponent plot;

    public PlotPanelXYChart(TraceListModel model) {
        // store local reference to data model holding the traces
        traceList = model;

        // register handler for changes in traceList
        traceList.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
            }
        });

        // register handler for changes in simulation compression (step size change)
        CVSim.sim.addPropertyChangeListener(CSimulation.COMPRESSION, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                updateTraceBufferSizes();
            }
        });

        plot = new PlotComponent(traceList, PARAMETRIC);
        plot.setBorder(new CompoundBorder(new MatteBorder(14, 0, 14, 0, getBackground()),
                new LineBorder(Color.BLACK, 1)));

        // register handler for resize events
        plot.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTraceBufferSizes();
            }
        });

        plot.addMouseListener(new PlotMouseListener());

        // Add scales
        yScale = new YscaleComponent(traceList, NUM_TICKS, this);
        xScale = new XscaleComponent(traceList, NUM_TICKS, this);

        List<SimulationOutputVariable> tmpVarList
                = new ArrayList<>(CVSim.sim.getOutputVariables());
        tmpVarList.remove(CVSim.sim.getOutputVariable("TIME"));
        final JComboBox xTraceBox = new JComboBox(new SimulationOutputVariableListModel(tmpVarList));
        final JComboBox yTraceBox = new JComboBox(new SimulationOutputVariableListModel(tmpVarList));
        xTraceBox.setSelectedIndex(2);

        JButton addButton = new JButton("Add Trace");
        addButton.setMargin(new Insets(1, 2, 2, 2));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTrace((SimulationOutputVariable) xTraceBox.getSelectedItem(),
                        (SimulationOutputVariable) yTraceBox.getSelectedItem());
            }
        });

        timeSlider = new JSlider(1, 60, 30);
        timeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    System.out.println("New traillength: " + (double) source.getValue());
                    setTrailLength((double) source.getValue());
                }
            }
        });

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Time History");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        timeSlider.setBorder(titledBorder);

        JCheckBox multipleXScalesCheckBox = new JCheckBox("Multiple X Scales");
        multipleXScalesCheckBox.addItemListener(MultipleXScalesListener);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("X:"));
        controlPanel.add(xTraceBox);
        controlPanel.add(new JLabel("Y:"));
        controlPanel.add(yTraceBox);
        controlPanel.add(addButton);
        controlPanel.add(timeSlider);
        controlPanel.add(multipleXScalesCheckBox);
        controlPanel.add(multipleYScalesCheckBox);
        controlPanel.setPreferredSize(new Dimension(100, 85));

        // layout
        this.setLayout(new BorderLayout());
        add(BorderLayout.PAGE_START, controlPanel);
        add(BorderLayout.PAGE_END, xScale);
        add(BorderLayout.LINE_START, yScale);
        add(BorderLayout.CENTER, plot);

        sourceDataChanged = new ChangeListener() {
            long lastUpdateTime = System.currentTimeMillis();

            @Override
            public void stateChanged(ChangeEvent event) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > PlotPanel.UPDATE_INTERVAL_MS) {
                    updateTraceTransforms();
                    plot.repaint();
                    lastUpdateTime = currentTime;
                }
            }
        };

        // Set default trail length, and generate a default trace
        setTrailLength((double) 30.0);
        calculateTraceBufferSize();
    } // end constructor 

    public boolean getMultipleXScales() {
        return multipleXScales;
    }

    public void setMultipleXScales(boolean newValue) {
        boolean oldValue = multipleXScales;
        multipleXScales = newValue;
        xScale._changes.firePropertyChange(XscaleComponent.PROP_MULTISCALES, oldValue, newValue);
    }

    private final ItemListener MultipleXScalesListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (((JCheckBox) e.getSource()).isSelected()) {
                setMultipleXScales(true);
            } else {
                setMultipleXScales(false);
            }
        }
    };

    @Override
    public void setMultipleYScales(boolean newValue) {
        boolean oldValue = getMultipleYScales();
        multipleYScales = newValue;
        yScale._changes.firePropertyChange(YscaleComponent.PROP_MULTISCALES, oldValue, newValue);
        xScale.repaint();
    }

    public YscaleComponent getYScale() {
        return yScale;
    }

    @Override
    public PlotComponent getPlot() {
        return plot;
    }

    public void createNewTrace(SimulationOutputVariable xvar, SimulationOutputVariable yvar) {
        //System.out.println("createNewTrace(" + xvar + "," + yvar +")");
        SimulationOutputVariableBuffer x = new SimulationOutputVariableBuffer(traceBufferSize, xvar);
        SimulationOutputVariableBuffer y = new SimulationOutputVariableBuffer(traceBufferSize, yvar);

        Trace newTrace = new Trace(x, y, traceList.getNextColor());
        addTrace(newTrace);
    }

    @Override
    public void addTrace(Trace t) {
        //System.out.println("addTrace(" + t + ")");
        traceList.add(t);
        CVSim.sim.addVariableRecorder(t.getXVar());
        CVSim.sim.addVariableRecorder(t.getYVar());
    }

    @Override
    public void removeTrace(Trace t) {
        //System.out.println("removeTrace(" + t + ")");
        traceList.remove(t);
        CVSim.sim.removeVariableRecorder(t.getXVar());
        CVSim.sim.removeVariableRecorder(t.getYVar());
    }

    @Override
    public void removeAllTraces() {
        //System.out.println("removeAllTraces()");
        for (Trace t : traceList) {
            removeTrace(t);
        }
    }

    private int calculateTraceBufferSize() {
        int stepSize = CVSim.sim.getDataCompressionFactor();
        traceBufferSize = (int) (1000 * trailLength) / (stepSize);
        return traceBufferSize;
    }

    private void updateTraceBufferSizes() {
        boolean wasRunning = false;

        int oldTraceBufferSize = traceBufferSize;
        calculateTraceBufferSize();
        if (oldTraceBufferSize != traceBufferSize) {
            //System.out.println("updateTraceBufferSizes() with " + traceBufferSize);

            if (CVSim.simThreadControl.isRunning()) {
                wasRunning = true;
                CVSim.simThreadControl.pause();
            }

            for (Trace tr : traceList) {
                tr.resizeBuffers(traceBufferSize);
            }

            if (wasRunning) {
                CVSim.simThreadControl.start();
            }
        }
    }

    public ListModel getListModel() {
        return traceList;
    }

    private void updateTraceTransforms() {
        AffineTransform plotTransform = plot.getPlotTransform();

        Range xrange, yrange;
        Double xdist, ydist;

        for (Trace trace : traceList) {
            AffineTransform at = trace.getTransformCopy();
            if ((trace.isEnabled()) && (at != null)) {
                // start by setting the trace's transform to the
                //  value of the plot transform
                at.setTransform(plotTransform);

                // prepare scaling and translation values
                xrange = trace.getXRange();
                yrange = trace.getYRange();
                xdist = xrange.upper - xrange.lower;
                ydist = yrange.upper - yrange.lower;

                // apply additional scaling for the data
                at.scale(1 / xdist, 1 / ydist);
                at.translate(-xrange.lower,-yrange.lower);
                trace.setTransform(at);

            }
        }
    }

    public final void setTrailLength(double seconds) {
        trailLength = seconds;
        updateTraceBufferSizes();
    }

    class PlotMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // can only take measurement if sim is running
            if (!CVSim.simThreadControl.isRunning()) {
                PlotPoint point = new PlotPoint(e.getX(), e.getY());
                Point2D screenPt, transPt;
                screenPt = e.getPoint();
                try {
                    if (getMultipleXScales() || getMultipleYScales()) {
                        Trace t0 = traceList.get(0);
                        transPt = getScaledPoint(t0.getTransformCopy(), screenPt);
                        String str = String.format("%.2f, %.2f", transPt.getX(), transPt.getY());
                        point.add(str, t0.getColor());
                        for (Trace t : traceList) {
                            transPt = getScaledPoint(t.getTransformCopy(), screenPt);
                            str = String.format("%.2f, %.2f", transPt.getX(), transPt.getY());
                            // don't want duplicate measurements
                            if (!(point.getString(0).equals(str))) {
                                point.add(str, t.getColor());
                            }
                        }
                    } else {
                        transPt = getScaledPoint(traceList.get(0).getTransformCopy(), screenPt);
                        String str = String.format("%.2f, %.2f", transPt.getX(), transPt.getY());
                        point.add(str, Color.BLACK);
                    }

                    point.setEnabled(true);
                    plot.pointList.add(point);
                    plot.repaint();
                } catch (NoninvertibleTransformException ex) {
                    System.out.println("PlotPanel.java: NoninvertibleTransformException");
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent event) {
        }

        @Override
        public void mouseReleased(MouseEvent event) {
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
        }
    }

}

