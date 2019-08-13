package edu.mit.lcp;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class PlotPoint {

    private boolean enabled = false;
    private final List<String> stringList = new ArrayList<>();
    private final List<Color> colorList = new ArrayList<>();
    public final int x;
    public final int y;

    public PlotPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void add(String str, Color color) {
        stringList.add(str);
        colorList.add(color);
    }

    public Color getColor(int i) {
        return colorList.get(i);
    }

    public String getString(int i) {
        return stringList.get(i);
    }

    public int getSize() {
        return colorList.size();
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
