package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.engine.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.engine.model.analysis.FilteringAnalysis;

import java.util.Vector;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Modifies a microarray set by discard all markers whose value is missing in
 * more than a user-specified number of microarrays.
 */
public class MissingValuesFilter extends AbstractAnalysis implements FilteringAnalysis {
    int maxMissingMicroarrays;

    public MissingValuesFilter() {
        setLabel("Missing values filter");
        setDefaultPanel(new MissingValuesFilterPanel());
    }

    public int getAnalysisType() {
        return MISSING_VALUES_FILTER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) input;
        // Collect the parameters needed for the execution of the filter
        maxMissingMicroarrays = ((MissingValuesFilterPanel) aspp).getMaxMissingArrays();
        int arrayCount = maSet.size();
        int markerCount = maSet.size();
        // Identify the markers that do not meet the cutoff value.
        Vector markersToPrune = new Vector();
        for (int i = 0; i < markerCount; i++)
            if (isMissing(maSet, i, maxMissingMicroarrays))
                markersToPrune.add(maSet.get(i));
        // Remove all identified markers.
        for (int i = 0; i < markersToPrune.size(); i++)
            maSet.getMarkers().remove((DSGeneMarker) markersToPrune.get(i));
        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * Check if the <code>index</code>-th marker in the microarrays set
     * <code>maSet</code> is missing in more than <code>maxAllowed</code>
     * microarrays.
     *
     * @param maSet      The reference microarray set.
     * @param index      The index of a marker.
     * @param maxAllowed The cutoff number of microarrays.
     * @return <code>true</code> if the designated marker is missing in more than
     *         <code>maxAllowed</code> microarrays. <code>false</code> otherwise.
     */
    private boolean isMissing(DSMicroarraySet<DSMicroarray> maSet, int index, int maxAllowed) {
        int arrayCount = maSet.size();
        DSMarkerValue markerValue = null;
        int numMissing = 0;
        for (int i = 0; i < arrayCount; i++) {
            markerValue = maSet.get(i).getMarkerValue(index);
            if (markerValue.isMissing())
                ++numMissing;
        }

        if (numMissing > maxAllowed)
            return true;
        return false;
    }

    public String getType() {
        return "Missing Values Filter";
    }

}

