package org.geworkbench.components.analysis.clustering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.apache.commons.math.MathException;

import java.util.Set;
import java.util.Arrays;

/**
 * @author John Watkinson
 */
public class MultiTTestAnalysis extends AbstractAnalysis implements ClusteringAnalysis {

    private static class Indexable implements Comparable {

        private double[] data;
        private int index;

        public Indexable(double[] data, int index) {
            this.data = data;
            this.index = index;
        }

        public int compareTo(Object o) {
            // Assumes that the other object is an indexable referencing the same data
            Indexable other = (Indexable) o;
            if (data[index] > data[other.index]) {
                return 1;
            } else if (data[index] < data[other.index]) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private MultiTTestAnalysisPanel panel;

    public MultiTTestAnalysis() {
        setLabel("Multi t Test Analysis");
        panel = new MultiTTestAnalysisPanel();
        setDefaultPanel(panel);
    }

    public int getAnalysisType() {
        return AbstractAnalysis.TTEST_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        assert (input instanceof DSMicroarraySetView);
        DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
        DSMicroarraySet maSet = view.getMicroarraySet();
        TTest tTest = new TTestImpl();
        // Get params
        Set<String> labelSet = panel.getLabels();
        double alpha = panel.getPValue();
        int m = labelSet.size();
        if (m < 2) {
            return new AlgorithmExecutionResults(false, "At least two panels must be selected for comparison.", null);
        }
        // todo - check that all selected panels have at least two elements
        int numTests = m * (m - 1) / 2;
        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);
        String[] labels = labelSet.toArray(new String[m]);
        int n = view.markers().size();
        double[][] pValues = new double[n][numTests];
        int testIndex = 0;
        // Create panels and significant result sets to store results
        DSPanel<DSGeneMarker>[] panels = new DSPanel[numTests];
        DSSignificanceResultSet<DSGeneMarker>[] sigSets = new DSSignificanceResultSet[numTests];
        // todo - use a F-test to filter genes prior to finding significant genes with Holm t Test
        // Run tests
        try {
            for (int i = 0; i < m; i++) {
                String labelA = labels[i];
                DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
                int aSize = panelA.size();
                for (int j = i + 1; j < m; j++) {
                    String labelB = labels[j];
                    DSPanel<DSMicroarray> panelB = context.getItemsWithLabel(labelB);
                    int bSize = panelB.size();
                    for (int k = 0; k < n; k++) {
                        double[] a = new double[aSize];
                        for (int aIndex = 0; aIndex < aSize; aIndex++) {
                            a[aIndex] = panelA.get(aIndex).getMarkerValue(k).getValue();
                        }
                        double[] b = new double[bSize];
                        for (int bIndex = 0; bIndex < bSize; bIndex++) {
                            b[bIndex] = panelB.get(bIndex).getMarkerValue(k).getValue();
                        }
                        pValues[k][testIndex] = tTest.tTest(a, b);
                    }
                    String label = labelA + " vs. " + labelB;
                    panels[testIndex] = new CSPanel<DSGeneMarker>(label);
                    sigSets[testIndex] = new CSSignificanceResultSet<DSGeneMarker>(
                            maSet,
                            label,
                            new String[] {labelA},
                            new String[] {labelB},
                            alpha);
                    testIndex++;
                }
            }
            // Sort each set of pValues and then use Holm method to compute significance
            for (int i = 0; i < n; i++) {
                Indexable[] indices = new Indexable[numTests];
                for (int j = 0; j < numTests; j++) {
                    indices[j] = new Indexable(pValues[i], j);
                }
                Arrays.sort(indices);
                for (int j = 0; j < numTests; j++) {
                    int index = indices[j].index;
                    double pValue = pValues[i][index];
                    pValue = pValue * (numTests - j);
                    // Is this a critical p-Value?
                    if (pValue < alpha) {
                        DSGeneMarker marker = view.markers().get(i);
                        panels[index].add(marker);
                        sigSets[index].setSignificance(marker, pValue);
                    } else {
                        // Consider no more tests after the first one fails
                        break;
                    }
                }
            }
            // Add panels and sigsets
            for (int i = 0; i < numTests; i++) {
                sigSets[i].sortMarkersBySignificance();                
                publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(panels[i], SubpanelChangedEvent.NEW));
                publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("Analysis Result", null, sigSets[i]));
            }
        } catch (MathException me) {
            me.printStackTrace();
        }
        // todo
        return null;
    }

    @Subscribe public void receive(ProjectEvent event, Object source) {
        DSDataSet dataSet = event.getDataSet();
        if ((dataSet != null) && (dataSet instanceof DSMicroarraySet)) {
            panel.setMaSet((DSMicroarraySet) dataSet);
            panel.rebuildForm();
        }
    }

    @Subscribe public void receive(PhenotypeSelectorEvent event, Object source) {
        panel.rebuildForm();
    }

    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent event) {
        return event;
    }

    @Publish public ProjectNodeAddedEvent publishProjectNodeAddedEvent(ProjectNodeAddedEvent event) {
        return event;
    }

}
