package jcvsim.compartment21;

import edu.mit.lcp.CVSim;
import edu.mit.lcp.MainWindow;
import edu.mit.lcp.Parameter;
import javax.swing.JOptionPane;
import jcvsim.common.UnitsConversion;

public class Parameter21C extends Parameter {

    private final Parameters paramVec;
    ParameterName pvName;

    public Parameter21C(Parameters pvec, ParameterName parameterEnum,
            String category, String type,
            String name, String units, double min, double max) {
        super(category, type, name, units, min, max);
        paramVec = pvec;
        pvName = parameterEnum;
        setDefaultValue();
    }

    public Parameter21C(Parameters pvec, ParameterName parameterEnum,
            String category, String type, String name,
            UnitsConversion conversion, double minInExternalUnits, double maxInExternalUnits) {
        super(category, type, name, conversion, minInExternalUnits, maxInExternalUnits);
        paramVec = pvec;
        pvName = parameterEnum;
        setDefaultValue();
    }

    @Override
    public void setValue(Double value) {
        Double oldVal = getValue();
        double tzpfv = 0;

        // Relational Constraints
        // Total Zero-Pressure Filling Volume cannot be greater than Total Blood Volume
        if (getName().contains("Zero-Pressure Filling Volume")) {
            tzpfv = CVSim.sim.calculateTotalZeroPressureFillingVolume();
            if ((value - oldVal + tzpfv) > CVSim.sim.getParameterByName("Total Blood Volume").getValue()) {
                String msg = String.format("The Total Zero-Pressure Filling Volume cannot be greater than the Total Blood Volume.");
                JOptionPane.showMessageDialog(MainWindow.frame, msg);
                return;
            }
        }
        if (getName().equals("Total Blood Volume")) {
            tzpfv = CVSim.sim.calculateTotalZeroPressureFillingVolume();
            if (value < tzpfv) {
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

        // Parameter Updates
        CSimulation21C sim = (CSimulation21C) CVSim.sim;
        Simulation main = sim.simulation;
        // Compliances inside the thorax
        switch (getName()) {
            case "Brachiocephalic Arteries Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.BRACH_ART_COMPLIANCE, 1);
                break;
            case "Superior Vena Cava Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.SVC_COMPLIANCE, 4);
                break;
            case "Inferior Vena Cava Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.IVC_COMPLIANCE, 14);
                break;
            case "Thoracic Aorta Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.THORACIC_AORTA_COMPLIANCE, 5);
                break;
            case "Ascending Aorta Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.ASCENDING_AORTA_COMPLIANCE, 0);
                break;
            case "Pulmonary Arterial Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.PULM_ART_COMPLIANCE, 17);
                break;
            // Compliances outside the thorax
            case "Pulmonary Venous Compliance":
                main.updateComplianceInsideThorax(value, paramVec, ParameterName.PULM_VEN_COMPLIANCE, 18);
                break;
            case "Abdominal Aorta Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.ABDOM_AORTA_COMPLIANCE, 6);
                break;
            case "Abdominal Veins Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.ABDOM_VEN_COMPLIANCE, 13);
                break;
            case "Lower Body Arteries Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.LBODY_ART_COMPLIANCE, 11);
                break;
            case "Lower Body Veins Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.LBODY_VEN_COMPLIANCE, 12);
                break;
            case "Renal Arteries Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.RENAL_ART_COMPLIANCE, 7);
                break;
            case "Renal Veins Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.RENAL_VEN_COMPLIANCE, 8);
                break;
            case "Splanchnic Arteries Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.SPLAN_ART_COMPLIANCE, 9);
                break;
            case "Splanchnic Veins Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.SPLAN_VEN_COMPLIANCE, 10);
                break;
            case "Upper Body Arteries Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.UBODY_ART_COMPLIANCE, 2);
                break;
            // Total Blood Volume
            case "Upper Body Veins Compliance":
                main.updateComplianceOutsideThorax(value, paramVec, ParameterName.UBODY_VEN_COMPLIANCE, 3);
                break;
            // Intra-thoracic Pressure
            case "Total Blood Volume":
                main.updateTotalBloodVolume(value, paramVec);
                break;
            // Zero-Pressure Filling Volume
            case "Intra-thoracic Pressure":
                main.updateIntrathoracicPressure(value, paramVec);
                break;
            case "Abdominal Aorta Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.ABDOM_AORTA_ZPFV, ParameterName.ABDOM_AORTA_COMPLIANCE, 6);
                break;
            case "Abdominal Veins Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.ABDOM_VEN_ZPFV, ParameterName.ABDOM_VEN_COMPLIANCE, 13);
                break;
            case "Ascending Aorta Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.ASCENDING_AORTA_VOLUME, ParameterName.ASCENDING_AORTA_COMPLIANCE, 0);
                break;
            case "Brachiocephalic Arteries Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.BRACH_ART_ZPFV, ParameterName.BRACH_ART_COMPLIANCE, 1);
                break;
            case "Inferior Vena Cava Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.IVC_ZPFV, ParameterName.IVC_COMPLIANCE, 14);
                break;
            case "Lower Body Arteries Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.LBODY_ART_ZPFV, ParameterName.LBODY_ART_COMPLIANCE, 11);
                break;
            case "Lower Body Veins Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.LBODY_VEN_ZPFV, ParameterName.LBODY_VEN_COMPLIANCE, 12);
                break;
            case "Renal Arteries Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.RENAL_ART_ZPFV, ParameterName.RENAL_ART_COMPLIANCE, 7);
                break;
            case "Renal Veins Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.RENAL_VEN_ZPFV, ParameterName.RENAL_VEN_COMPLIANCE, 8);
                break;
            case "Splanchnic Arteries Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.SPLAN_ART_ZPFV, ParameterName.SPLAN_ART_COMPLIANCE, 9);
                break;
            case "Splanchnic Veins Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.SPLAN_VEN_ZPFV, ParameterName.SPLAN_VEN_COMPLIANCE, 10);
                break;
            case "Superior Vena Cava Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.SVC_ZPFV, ParameterName.SVC_COMPLIANCE, 4);
                break;
            case "Thoracic Aorta Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.THORACIC_AORTA_ZPFV, ParameterName.THORACIC_AORTA_COMPLIANCE, 5);
                break;
            case "Upper Body Arteries Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.UBODY_ART_ZPFV, ParameterName.UBODY_ART_COMPLIANCE, 2);
                break;
            case "Upper Body Veins Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.UBODY_VEN_ZPFV, ParameterName.UBODY_VEN_COMPLIANCE, 3);
                break;
            case "Pulmonary Arterial Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.PULM_ART_ZPFV, ParameterName.PULM_ART_COMPLIANCE, 17);
                break;
            // Everything else
            case "Pulmonary Venous Zero-Pressure Filling Volume":
                main.updateZeroPressureFillingVolume(value, paramVec, ParameterName.PULN_VEN_ZPFV, ParameterName.PULM_VEN_COMPLIANCE, 18);
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
