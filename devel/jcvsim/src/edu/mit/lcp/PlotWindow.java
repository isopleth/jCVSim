package edu.mit.lcp;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PlotWindow extends JPanel {

    private final PlotPanel plot;
    private final TraceLegendTableComponent legend;
    private final TraceListModel traceList;

    public PlotWindow(TraceListModel listModel, PlotType plotType) {
        traceList = listModel;
        setLayout(new BorderLayout());

        // Setup PlotPanel
        switch (plotType) {
            case PARAMETRIC:
                plot = new PlotPanelXYChart(traceList);
                break;
            case STRIPCHART:
                plot = new PlotPanelStripChart(traceList);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        plot.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Setup Legend Table
        legend = new TraceLegendTableComponent(this, traceList);

        // Add components to window
        add(plot, BorderLayout.CENTER);
        add(legend, BorderLayout.PAGE_END);

        setVisible(true);
    }

    public PlotPanel getPlot() {
        return plot;
    }

}
