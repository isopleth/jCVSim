package edu.mit.lcp;

import static edu.mit.lcp.CVSim.getSimulationModel;
import static edu.mit.lcp.CVSim.sim;
import java.io.*;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OutputFile {

    private final File dataFile;
    private final List<SimulationOutputVariable> outputList;
    private final List<Parameter> parameterList;
    private final List<SimulationOutputVariableBuffer> recList;
    private PrintStream dataPrintStream;
    private final ChangeListener sourceDataChanged;
    private long lastUpdateTimeMs;
    private final AtomicLong recordsWrittenCount;
    private final boolean useExternalUnits;

    /**
     *
     * @param parametersList
     * @param simulationOutputVariablesList
     * @param minimumTimeBetweenUpdatesMs
     * @param recordsWrittenCount
     * @param file
     */
    public OutputFile(List<Parameter> parametersList,
            List<SimulationOutputVariable> simulationOutputVariablesList,
            long minimumTimeBetweenUpdatesMs,
            AtomicLong recordsWrittenCount,
            File file) {
        parameterList = new ArrayList<>(parametersList);
        outputList = new ArrayList<>(simulationOutputVariablesList);
        this.recordsWrittenCount = recordsWrittenCount;
        // Use the setting of this flag in CVSim as it is now, don't change
        // the setting here if it is changed there
        useExternalUnits = CVSim.useExternalUnits.get();
        lastUpdateTimeMs = 0;
        dataFile = file;

        recList = new ArrayList<>(outputList.size());

        sourceDataChanged = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                boolean update;
                if (minimumTimeBetweenUpdatesMs != 0) {
                    long updateTime = System.currentTimeMillis();
                    update = (updateTime - lastUpdateTimeMs) >= minimumTimeBetweenUpdatesMs;
                } else {
                    update = true;
                }
                if (update) {
                    writeData();
                    
                    lastUpdateTimeMs=System.currentTimeMillis();
                }
            }
        };
    }

    /**
     * Start logging
     *
     * @throws FileNotFoundException
     */
    public void startLogging() throws FileNotFoundException {

        // create the recorders to attach to simulation
        for (SimulationOutputVariable simulationOutputVariable : outputList) {
            recList.add(new SimulationOutputVariableBuffer(1, simulationOutputVariable));
        }

        // attach the recorders to the simulation
        for (SimulationOutputVariableBuffer r : recList) {
            sim.addVariableRecorder(r);
        }

        // prepare the output file
        dataPrintStream = new PrintStream(dataFile);
        writeHeader(dataPrintStream, "# ");

        // Clear down the records counter
        recordsWrittenCount.set(0);
        // begin receiving changes
        sim.addChangeListener(sourceDataChanged);
    }

    /**
     *
     */
    public void stopLogging() {
        // stop receiving changes
        sim.removeChangeListener(sourceDataChanged);

        // remove the recorders from the simulation
        for (SimulationOutputVariableBuffer r : recList) {
            sim.removeVariableRecorder(r);
        }

        // close the output file
        closeFile();
    }

    private void closeFile() {
        try {
            dataPrintStream.close();
        } catch (Exception e) {
            System.err.println("Error closing file " + dataFile);
            System.err.println(e);
        }
    }

    private void writeData() {
        // write outputs first then parameters
        boolean first = true;
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        // Write simulation outputs
        for (SimulationOutputVariableBuffer variable : recList) {
            if (first) {
                first = false;
            } else {
                dataPrintStream.print(", ");
            }
            double value;
            if (useExternalUnits) {
                value = variable.getLastDatumInExternalRepresentation();
            } else {
                value = variable.getLastDatum();
            }
            dataPrintStream.print(df.format(value));
        }

        // Write parameters
        for (Parameter parameter : parameterList) {
            if (first) {
                first = false;
            } else {
                dataPrintStream.print(", ");
            }
            double value;
            if (useExternalUnits) {
                value = parameter.getExternalRepresentationOfValue();
            } else {
                value = parameter.getValue();
            }
            dataPrintStream.print(df.format(value));
        }

        dataPrintStream.println();
        recordsWrittenCount.incrementAndGet();
    }

    /**
     * Write the preamble to the file describing its contents
     *
     * @param headerPrintStream
     * @param commentString
     */
    public void writeHeader(PrintStream headerPrintStream, String commentString) {
        if (commentString == null) {
            commentString = "";
        }
        headerPrintStream.println(commentString + "This is " + dataFile.getName());
        headerPrintStream.println(commentString);
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        headerPrintStream.println(commentString + "Created " + dateFormat.format(new Date()));
        headerPrintStream.println(commentString + new ProgramVersion().toString());
        headerPrintStream.println(commentString + getSimulationModel() + " model");
        int columnNumber = 1;
        for (SimulationOutputVariableBuffer variable : recList) {
            String units;
            if (useExternalUnits) {
                units = variable.getInternalUnits();
            } else {
                units = variable.getInternalUnits();
            }
            headerPrintStream.printf(commentString + "Column %d:\t"
                    + variable.getDescription() + " ("
                    + units + ")\n", columnNumber);
            columnNumber++;
        }
        for (Parameter parameter : parameterList) {
            String units;

            if (useExternalUnits) {
                units = parameter.getInternalUnits();
            } else {
                units = parameter.getInternalUnits();
            }
            headerPrintStream.printf(commentString + "Column %d:\t"
                    + parameter.getName() + " ("
                    + units + ")\n", columnNumber);
            columnNumber++;
        }
        headerPrintStream.println(commentString);

        // Now a short summary
        headerPrintStream.print(commentString);
        boolean first = true;
        for (SimulationOutputVariableBuffer recorder : recList) {
            if (first) {
                first = false;
            } else {
                dataPrintStream.print(", ");
            }
            headerPrintStream.print(recorder.getName());
            columnNumber++;
        }
        for (Parameter parameter : parameterList) {
            if (first) {
                first = false;
            } else {
                dataPrintStream.print(", ");
            }
            headerPrintStream.print(parameter.getName());
            columnNumber++;
        }
        headerPrintStream.println();
    }
}
