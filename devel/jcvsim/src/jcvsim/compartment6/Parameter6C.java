package jcvsim.compartment6;

import edu.mit.lcp.CVSim;
import edu.mit.lcp.MainWindow;
import edu.mit.lcp.Parameter;
import javax.swing.JOptionPane;

public class Parameter6C extends Parameter {

    private final Parameters paramVec;
    public final ParameterName pvName;

    public Parameter6C(Parameters pvec, ParameterName parameterEnum,
            String category, String type,
            String name, String units) {
        super(category, type, name, units);
        paramVec = pvec;
        pvName = parameterEnum;
        setDefaultValue();
    }

    public Parameter6C(Parameters pvec, ParameterName parameterEnum, String category, String type,
            String name, String units, double min, double max) {
        super(category, type, name, units, min, max);
        paramVec = pvec;
        pvName = parameterEnum;
        setDefaultValue();
    }

    @Override
    public void setValue(Double value) {
        Double oldVal = getValue();

        // Relational Constraints
        // Total Zero-Pressure Filling Volume cannot be greater than Total Blood Volume
        if (getName().equals("Total Zero-Pressure Filling Volume")) {
            if (value > CVSim.sim.getParameterByName("Total Blood Volume").getValue()) {
                String msg = String.format("The Total Zero-Pressure Filling Volume cannot be greater than the Total Blood Volume.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }
        if (getName().equals("Total Blood Volume")) {
            if (value < CVSim.sim.getParameterByName("Total Zero-Pressure Filling Volume").getValue()) {
                String msg = String.format("The Total Blood Volume cannot be less than the Total Zero-Pressure Filling Volume.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }

        // LV Systolic Compliance cannot be greater than LV Diastolic Compliance
        if (getName().equals("Left Ventricle Systolic Compliance")) {
            if (value > CVSim.sim.getParameterByName("Left Ventricle Diastolic Compliance").getValue()) {
                String msg = String.format("The Left Ventricle Systolic Compliance cannot be greater than the Left Ventricle Diastolic Compliance.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }
        if (getName().equals("Left Ventricle Diastolic Compliance")) {
            if (value < CVSim.sim.getParameterByName("Left Ventricle Systolic Compliance").getValue()) {
                String msg = String.format("The Left Ventricle Diastolic Compliance cannot be less than the Left Ventricle Systolic Compliance.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }

        // RV Systolic Compliance cannot be greater than RV Diastolic Compliance
        if (getName().equals("Right Ventricle Systolic Compliance")) {
            if (value > CVSim.sim.getParameterByName("Right Ventricle Diastolic Compliance").getValue()) {
                String msg = String.format("The Right Ventricle Systolic Compliance cannot be greater than the Right Ventricle Diastolic Compliance.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }
        if (getName().equals("Right Ventricle Diastolic Compliance")) {
            if (value < CVSim.sim.getParameterByName("Right Ventricle Systolic Compliance").getValue()) {
                String msg = String.format("The Right Ventricle Diastolic Compliance cannot be less than the Right Ventricle Systolic Compliance.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }

        // Check that value entered by user is not outside the valid parameter range
        if (!CVSim.gui.parameterPanel.patientDisplayModeEnabled()) {
            if (value < getMin()) {
                String msg = String.format("The value you entered for " + getName()
                        + " is outside \nthe allowed range. Please enter a value between %.3f and %.3f.",
                        getMin(), getMax());
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                value = getMin();
            } else if (value > getMax()) {
                String msg = String.format("The value you entered for " + getName()
                        + " is outside \nthe allowed range. Please enter a value between %.3f and %.3f.",
                        getMin(), getMax());
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                value = getMax();
            }
        }
        CSimulation6C sim = (CSimulation6C) CVSim.sim;
        Simulation main = sim.simulation;
        switch (getName()) {
            case "Pulmonary Arterial Compliance":
                main.updatePulmonaryArterialCompliance(value, paramVec);
                break;
            case "Pulmonary Venous Compliance":
                main.updatePulmonaryVenousCompliance(value, paramVec);
                break;
            case "Arterial Compliance":
                main.updateArterialCompliance(value, paramVec);
                break;
            case "Venous Compliance":
                main.updateVenousCompliance(value, paramVec);
                break;
            case "Total Blood Volume":
                main.updateTotalBloodVolume(value, paramVec);
                break;
            case "Intra-thoracic Pressure":
                main.updateIntrathoracicPressure(value, paramVec);
                break;
            case "Total Zero-Pressure Filling Volume":
                main.updateTotalZeroPressureFillingVolume(value, paramVec);
                break;
            default:
                main.updateParameter(value, paramVec, pvName);
                break;
        }

        firePropertyChange("VALUE", oldVal, value);

        String str = String.format(getName() + " changed from %.3f to %.3f.",
                oldVal, getValue());
        System.out.println(str);

    }

    @Override
    public Double getValue() {
        return paramVec.get(pvName);
    }

}
