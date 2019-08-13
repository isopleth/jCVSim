/*
 * Copyright (C) 2017 Jason Leake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.mit.lcp;

import javax.swing.JTextField;

/**
 *
 * @author Jason Leake
 */
class HeartRatePanel extends JTextField {

    private final SimulationOutputVariableBuffer heartRateBuffer;

    HeartRatePanel() {
        setEditable(false);
        setToolTipText("RR variation in ms");
        setText("---------");
        int bufferSize = (int) (10000) / CVSim.sim.getDataCompressionFactor();
        heartRateBuffer = new SimulationOutputVariableBuffer(bufferSize, CVSim.sim.getOutputVariable("HR"));
        CVSim.sim.addVariableRecorder(heartRateBuffer);
    }

    void update() {
        PointsList list = heartRateBuffer.getPointsList();

        // Convert bpm to ms rr interval variation
        this.setText(String.format("%.0f", 1000. * (60 / list.getMinY()) - (60 / list.getMaxY())));
        this.repaint();
    }
}
