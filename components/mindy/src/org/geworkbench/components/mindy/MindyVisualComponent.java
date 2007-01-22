package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author mhall
 */
@AcceptTypes(MindyDataSet.class)
public class MindyVisualComponent implements VisualPlugin {

    static Log log = LogFactory.getLog(MindyVisualComponent.class);

    private MindyDataSet dataSet;
    private JPanel plugin;
    private MindyPlugin mindyPlugin;
    private ArrayList<DSGeneMarker> selectedMarkers;

    public MindyVisualComponent() {
        // Just a place holder
        plugin = new JPanel(new BorderLayout());
    }

    public Component getComponent() {
        return plugin;
    }

    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        log.debug("MINDY received project event.");
        DSDataSet data = projectEvent.getDataSet();
        if ((data != null) && (data instanceof MindyDataSet)) {
            if (dataSet != data) {
                dataSet = ((MindyDataSet) data);
                plugin.removeAll();
                mindyPlugin = new MindyPlugin(dataSet.getData());
                mindyPlugin.limitMarkers(selectedMarkers);
                plugin.add(mindyPlugin, BorderLayout.CENTER);
                plugin.revalidate();
                plugin.repaint();
            }
        }
    }

    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (dataSet != null && e.getPanel() != null) {
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet.getData().getArraySet());
            maView.setMarkerPanel(e.getPanel());
            maView.useMarkerPanel(true);
            if (maView.getMarkerPanel().activeSubset().size() == 0) {
                selectedMarkers = null;
            } else {
                DSItemList<DSGeneMarker> uniqueMarkers = maView.getUniqueMarkers();
                if (uniqueMarkers.size() > 0) {
                    selectedMarkers = new ArrayList<DSGeneMarker>();
                    for (Iterator<DSGeneMarker> iterator = uniqueMarkers.iterator(); iterator.hasNext();) {
                        DSGeneMarker marker = iterator.next();
                        log.debug("Selected " + marker.getShortName());
                        selectedMarkers.add(marker);
                    }
                }
            }
            mindyPlugin.limitMarkers(selectedMarkers);
        } else {
            log.error("Dataset in this component is null, or selection sent was null");
        }
    }


}
