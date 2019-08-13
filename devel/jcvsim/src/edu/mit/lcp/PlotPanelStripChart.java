package edu.mit.lcp;

import static edu.mit.lcp.PlotType.STRIPCHART;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
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
public class PlotPanelStripChart extends PlotPanel {

    ///////////////////////
    // private variables
    //////////////////////
    private static final int NUM_TICKS = 5;

    private int traceBufferSize;
    private final TraceListModel traceList;
    private final JSlider speedSlider;
    private SimulationOutputVariableBuffer timeAxisBuffer;
    private double secondsPerUnit;
    private HeartRatePanel heartRatePanel;

    private PlotComponent plot;

    public PlotPanelStripChart(TraceListModel model) {
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

        plot = new PlotComponent(traceList, PlotComponent.SCALE_X_DPI, STRIPCHART);
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

        yScale = new YscaleComponent(traceList, NUM_TICKS, this);

        // Setup Add widget and speed slider
        List<SimulationOutputVariable> tmpVarList = new ArrayList<>(CVSim.sim.getOutputVariables());
        tmpVarList.remove(CVSim.sim.getOutputVariable("TIME"));

        final JComboBox yTraceBox = new JComboBox(new SimulationOutputVariableListModel(tmpVarList));
        JButton addButton = new JButton("Add Trace");
        addButton.setMargin(new Insets(1, 2, 2, 2));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTrace((SimulationOutputVariable) yTraceBox.getSelectedItem());
            }
        });

        speedSlider = new JSlider(1, 100, 5);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    System.out.println("New Paper Speed: " + (double) source.getValue());
                    setPaperSpeed((double) source.getValue() / 10.0);
                }
            }
        });

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Plot Speed");
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        speedSlider.setBorder(titledBorder);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("Y: "));
        controlPanel.add(yTraceBox);
        controlPanel.add(addButton);
        controlPanel.add(speedSlider);
        controlPanel.add(multipleYScalesCheckBox);

        if (CVSim.getSimulationModel() == SimulationModelType.TWENTY_ONE_COMPARTMENT_MODEL) {
            heartRatePanel = new HeartRatePanel();
            controlPanel.add(heartRatePanel);
        } else {
            heartRatePanel = null;
        }
        controlPanel.setPreferredSize(new Dimension(100, 75));

        //add(xScale);
        this.setLayout(new BorderLayout());
        add(BorderLayout.PAGE_START, controlPanel);
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
                    if (heartRatePanel != null) {
                        heartRatePanel.update();
                    }
                    lastUpdateTime = currentTime;
                }
            }
        };

        // Set default paper speed, and generate a default trace
        secondsPerUnit = 1;
        calculateTraceBufferSize();

    }

    @Override
    public void setMultipleYScales(boolean newValue) {
        boolean oldValue = getMultipleYScales();
        multipleYScales = newValue;
        yScale._changes.firePropertyChange(YscaleComponent.PROP_MULTISCALES, oldValue, newValue);
    }

    @Override
    public PlotComponent getPlot() {
        return plot;
    }

    private void createTimeAxisBuffer() {
        timeAxisBuffer = new SimulationOutputVariableBuffer(traceBufferSize, CVSim.sim.getOutputVariable("TIME"));
        CVSim.sim.addVariableRecorder(timeAxisBuffer);
    }

    public void createNewTrace(SimulationOutputVariable var) {
        //System.out.println("createNewTrace(" + var + ")");
        if (timeAxisBuffer == null) {
            createTimeAxisBuffer();
        }
        SimulationOutputVariableBuffer y = new SimulationOutputVariableBuffer(traceBufferSize, var);

        Trace newTrace = new Trace(timeAxisBuffer, y, traceList.getNextColor());
        addTrace(newTrace);
    }

    @Override
    public void addTrace(Trace t) {
        //System.out.println("addTrace(" + t + ")");
        traceList.add(t);
        CVSim.sim.addVariableRecorder(t.getYVar());
    }

    @Override
    public void removeTrace(Trace t) {
        //System.out.println("removeTrace(" + t + ")");
        traceList.remove(t);
        CVSim.sim.removeVariableRecorder(t.getYVar());
        if (traceList.isEmpty() && (timeAxisBuffer != null)) {
            CVSim.sim.removeVariableRecorder(timeAxisBuffer);
            timeAxisBuffer = null;
        }
    }

    @Override
    public void removeAllTraces() {
        for (Trace t : traceList) {
            removeTrace(t);
        }
    }

    private int calculateTraceBufferSize() {
        int stepSize = CVSim.sim.getDataCompressionFactor();
        double plotTimeRange = secondsPerUnit * plot.getScaledPlotBounds().getWidth();
        traceBufferSize = (int) (1000 * plotTimeRange) / (stepSize);
        return traceBufferSize;
    }

    private void updateTraceBufferSizes() {
        boolean wasRunning = false;

        int oldTraceBufferSize = traceBufferSize;
        calculateTraceBufferSize();
        if (oldTraceBufferSize != traceBufferSize) {
            if (CVSim.simThreadControl.isRunning()) {
                wasRunning = true;
                CVSim.simThreadControl.pause();
            }

            for (Trace trace : traceList) {
                trace.resizeBuffers(traceBufferSize);
            }

            if (wasRunning) {
                CVSim.simThreadControl.start();
            }
        }
    }

    private void updateTraceTransforms() {
        AffineTransform plotTransform = plot.getPlotTransform();

        for (Trace trace : traceList) {
            AffineTransform at = trace.getTransformCopy();
            if ((trace.isEnabled()) && (at != null)) {
                // start by setting the trace's transform to the
                //  value of the plot transform
                at.setTransform(plotTransform);

                // prepare scaling and translation values
                Range xrange;
                PointsList pointsList = trace.getList();
                if (pointsList.isEmpty()) {
                    xrange = trace.getXRange();
                } else {
                    xrange = pointsList.getXDataRange();
                }
                Range yrange = trace.getYRange();
                Double ydist = yrange.upper - yrange.lower;

                // apply additional scaling for the data
                at.scale(-1 / secondsPerUnit, 1 / ydist);
                at.translate(-xrange.upper, -yrange.lower);
                trace.setTransform(at);
            }
        }
    }

    public void setPaperSpeed(double inchesPerSecond) {
        secondsPerUnit = 1 / inchesPerSecond;
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
                    if (getMultipleYScales()) {
                        Trace trace0 = traceList.get(0);
                        transPt = getScaledPoint(trace0.getTransformCopy(), screenPt);
                        String str = String.format("%.2f, %.2f", transPt.getX(), transPt.getY());
                        point.add(str, trace0.getColor());
                        for (Trace trace : traceList) {
                            transPt = getScaledPoint(trace.getTransformCopy(), screenPt);
                            str = String.format("%.2f, %.2f", transPt.getX(), transPt.getY());
                            // don't want duplicate points
                            if (!(point.getString(0).equals(str))) {
                                point.add(str, trace.getColor());
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
