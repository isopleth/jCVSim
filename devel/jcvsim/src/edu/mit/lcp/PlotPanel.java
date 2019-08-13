package edu.mit.lcp;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

public abstract class PlotPanel extends JPanel {

    // How often the data is plotted
    static final long UPDATE_INTERVAL_MS = 50;

    public boolean multipleYScales = false;
    public JCheckBox multipleYScalesCheckBox;
    public YscaleComponent yScale;
    ChangeListener sourceDataChanged;

    PlotPanel() {
        this.setLayout(null);
        multipleYScalesCheckBox = new JCheckBox("Mult Y Scales");
        multipleYScalesCheckBox.addItemListener(MultipleYScalesListener);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(yScale.getPreferredSize().width, yScale.getPreferredSize().width * 8);
    }

    public boolean getMultipleYScales() {
        return multipleYScales;
    }

    public abstract void setMultipleYScales(boolean newValue);

    private ItemListener MultipleYScalesListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (((JCheckBox) e.getSource()).isSelected()) {
                setMultipleYScales(true);
            } else {
                setMultipleYScales(false);
            }
        }
    };

    public abstract PlotComponent getPlot();

    public Point2D getScaledPoint(AffineTransform at, Point2D pt)
            throws NoninvertibleTransformException {
        AffineTransform it = at.createInverse();
        Point2D scaledPt = it.transform(pt, null);
        return scaledPt;
    }

    public abstract void removeTrace(Trace t);

    public abstract void removeAllTraces();

    public abstract void addTrace(Trace t);

}
