package org.geworkbench.components.hierarchicalclustering;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.events.HierClusterModelEvent;
import org.geworkbench.events.HierClusterModelEventListener;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * The widget should throw Java events (to be handled by the wrapping
 * <code>HierClusterViewAppComponent</code> component), when the following
 * user-initiated actions occur:
 * <ul>
 * <li>
 * A branch is clicked. The event should communicate to the wrapping
 * component what cluster was selected.
 * </li>
 * <li>
 * A marker (or a microarray) is double clicked. The event thrown
 * should communicate to the wrapping component of the marker (or microarray)
 * selected.
 * </li>
 * </ul>
 * Utility for visualization of cluster analysis for genome-wide expression data
 * from DNA microarray hybridization
 * as described in:
 * <p/>
 * <p><h3>Cluster analysis and display of genome-wide expression patterns</h3></p>
 * <p>Michael B. Eisen, Paul T. Spellman, Patrick O. Brown AND David Botstein</p>
 * <p>Proc. Natl. Acad. Sci. USA</p>
 * <p>Vol. 95, pp. 14863�14868, December 1998</p>
 * <p>Genetics</p>
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public class HierClusterViewWidget extends JPanel implements HierClusterModelEventListener {

    static Log log = LogFactory.getLog(HierClusterViewWidget.class);

    /**
     * Property used for conveying the origin of a <code>PropertyChange</code>
     * event for distinguishing messages from other components which throw
     * <code>PropertyChange</code> events
     */
    public static final String SAVEIMAGE_PROPERTY = "saveClusterImage";

    /**
     * Property to signify that a Single Marker Selection originated from
     * the <code>HierClusterViewWidget</code>
     */
    public static String SINGLE_MARKER_SELECTED_PROPERTY = "HierarchicalClusterSingleMarkerSelected";

    /**
     * Property to signify that a Single Marker Selection originated from
     * the <code>HierClusterViewWidget</code>
     */
    public static String MULTIPLE_MARKER_SELECTED_PROPERTY = "HierarchicalClusterMultipleMarkerSelected";

    /**
     * Use for drawing the marker values
     */
    private ColorContext markerColors = null;

    /**
     * The underlying micorarray set used in the hierarchical clustering
     * analysis.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mASet = null;

    /**
     * The subcluster of markerCluster currently being displayed. This
     * variable is set when the user clicks on a marker branch of the displayed
     * image.
     */
    private MarkerHierCluster originalMarkerCluster = null;

    /**
     * Currently displayed marker cluster
     */
    private MarkerHierCluster selectedMarkerCluster = null;

    /**
     * Currently displayed microarray cluster
     */
    private MicroarrayHierCluster selectedArrayCluster = null;

    /**
     * The subcluster of arrayCluster currently being displayed. This
     * variable is set when the user clicks on a branch of the displayed
     * image.
     */
    private MicroarrayHierCluster originalArrayCluster;

    /**
     * The canvas on which the actual dendrogram is drawn.
     */
    private JPanel jPanel2 = new JPanel();

    /**
     * The <code>JPanel</code> on which the marker dendrogram is painted
     */
    HierClusterTree markerDendrogram = null;

    /**
     * The <code>JPanel</code> on which the array dendrogram is painted
     */
    HierClusterTree arrayDendrogram = null;

    /**
     * The <code>JPanel</code> on which the array names is painted
     */
    HierClusterLabels arrayNames = null;
    JPanel arrayContainer = new JPanel();

    /**
     * Slider for controlling color intensity of markers in the dendrogram
     */
    private JSlider slider = new JSlider();

    /**
     * Bit that controls if zooming the dendrograms is enabled
     */
    private boolean zoomEnabled = false;

    /**
     * Saves the state of the "Tootip" button
     */
    private boolean showSignal = false;

    /**
     * <code>MouseListener</code> that captures clicks on the dendrograms
     */
    private MouseListener dendrogramListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            dendrogram_mouseClicked(e);
        }
    };

    /**
     * <code>MouseMotionListener</code> that captures mouse motion over the
     * dendrogram and the cluster leaf display panel
     */
    private MouseMotionListener motionListener = new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            this_mouseMoved(e);
        }
    };

    /**
     * <code>MouseListener</code> that triggers a <code>JPopupMenu</code>
     */
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseReleased(MouseEvent e) {
            this_mouseReleased(e);
        }

        public void mouseClicked(MouseEvent e) {
            this_mouseClicked(e);
        }

        public void mouseExited(MouseEvent e) {
            if (e.getSource() == markerDendrogram) {
                log.debug("Setting highlight on marker dendrogram to null");
                markerDendrogram.setCurrentHighlight(null);
            } else if (e.getSource() == arrayDendrogram) {
                log.debug("Setting highlight on array dendrogram to null");
                arrayDendrogram.setCurrentHighlight(null);
            }
        }
    };

    /**
     * <code>AdjustmentListener</code> that is added to <code>JScrollBar</code>
     * components to track if the <code>JScrollBar</code> has stopped moving
     */
    private AdjustmentListener scrollBarListener = new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent e) {
            this_adjustmentValueChanged(e);
        }
    };

    /**
     * Application menu listeners returned by this component
     */
    private HashMap listeners = new HashMap();

    /**
     * Visual widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();

    /**
     * Visual widget
     */
    private JToolBar jToolBar1 = new JToolBar();

    /**
     * Visual widget
     */
    private JScrollPane jScrollPane1 = new JScrollPane();

    /**
     * Visual widget
     */
    private BorderLayout borderLayout2 = new BorderLayout();

    /**
     * Visual widget
     */
    private HierClusterDisplay display = new HierClusterDisplay();

    /**
     * Visual widget
     */
    private Component jSpacer3;

    /**
     * Visual widget
     */
    private JToggleButton jToolTipToggleButton = new JToggleButton();

    /**
     * Visual widget
     */
    private Component jSpacer4;

    /**
     * Visual widget
     */
    private Component jSpacer1;

    /**
     * Visual widget
     */
    private JSpinner jGeneHeight = new JSpinner();
    private JLabel heightLabel = new JLabel("Gene Height");

    /**
     * Visual widget
     */
    private JSpinner jGeneWidth = new JSpinner();
    private JLabel widthLabel = new JLabel("Gene Width");

    /**
     * Visual widget
     */
    private JCheckBox jCheckBox1 = new JCheckBox();

    /**
     * Visual widget
     */
    private JPopupMenu contextMenu = new JPopupMenu();

    /**
     * Visual widget
     */
    private JMenuItem imageSnapShot = new JMenuItem();

    /**
     * Visual Widget
     */
    private JMenuItem addToPanel = new JMenuItem();

    /**
     * Default Constructor
     */
    public HierClusterViewWidget() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the <code>ColorContext</code> for marker rendering in the dendrogram
     *
     * @param cc <code>ColorContext</code> to be used
     */
    public void setColorContext(ColorContext cc) {
        markerColors = cc;
    }

    /**
     * Returns application menu listeners that this component handles
     *
     * @return listeners
     */
    public HashMap getListeners() {
        return listeners;
    }

    /**
     * <code>HierClusterModelEventListener</code> interface method that notifies
     * this component of a change in the Clusering data model
     *
     * @param hcme the new model wrapping the clustering data
     */
    public void hierClusterModelChange(HierClusterModelEvent hcme) {
        mASet = hcme.getMicroarraySet();
        originalMarkerCluster = selectedMarkerCluster = hcme.getMarkerCluster();
        originalArrayCluster = selectedArrayCluster = hcme.getMicroarrayCluster();
        display.setChips(mASet);
        markerDendrogram.setChips(mASet);
        arrayDendrogram.setChips(mASet);
        arrayNames.setChips(mASet);
        init(originalMarkerCluster, originalArrayCluster);
    }

    /**
     * Resets clusters and sizes
     *
     * @param markerCluster <code>HierCluster</code> representing the
     *                      Marker Dendrogram
     * @param arrayCluster  <code>HierCluster</code> representing the
     *                      Microarray Dendrogram
     */
    private void init(HierCluster markerCluster, HierCluster arrayCluster) {
        if (mASet == null) {
            return;
        }
        jGeneHeight.setValue(HierClusterDisplay.geneHeight);
        jGeneWidth.setValue(HierClusterDisplay.geneWidth);

        display.setMarkerHierCluster((MarkerHierCluster) markerCluster);
        display.setMicroarrayHierCluster((MicroarrayHierCluster) arrayCluster);
        arrayNames.setMicroarrayHierCluster((MicroarrayHierCluster) arrayCluster);
        markerDendrogram.setTreeData(markerCluster);

        if ((mASet != null) && (mASet.items().size() == 1)) {
            arrayDendrogram.setTreeData(null);
            arrayNames.setTreeData(null);
        } else {
            arrayDendrogram.setTreeData(arrayCluster);
            arrayNames.setTreeData(arrayCluster);
        }
        setSizes();
        revalidate();
        repaint();
    }

    /**
     * Utility method to set sizes of the trees and the cluster mosaic
     */
    private void setSizes() {
        int mdw = markerDendrogram.getWidth();
        int ht = markerDendrogram.getMaxHeight();
        int adw = arrayDendrogram.getMaxHeight();
        int adh = arrayDendrogram.getHeight();
        int ndh = arrayNames.getHeight();
        arrayDendrogram.leftOffset = mdw;
        arrayNames.leftOffset = mdw;
        jPanel2.setPreferredSize(new Dimension((int) ((adw + mdw) * 1.5), (int) (ndh + adh + ht + (ht / 5))));
        jPanel2.setSize(new Dimension((int) ((adw + mdw) * 1.5), (int) (ndh + adh + ht + (ht / 5))));
        //setPreferredSize(new Dimension( (int) ( (adw + mdw) * 1.5),
        //                             (int) (ht + (ht / 5))));
        //setSize(new Dimension( (int) ( (adw + mdw) * 1.5),
        //                    (int) (ht + (ht / 5))));
        display.setPreferredSize(new Dimension((int) mdw / 2, ht));
        display.setSize(new Dimension((int) mdw / 2, ht));
    }

    /**
     * Highlights (or de-highlights) the designate MarkerInfo, if the marker
     * info belongs to some cluster in currentMarkerCluster (i.e., it is a
     * MarkerInfo currently displayed). This will be useful when a marker is
     * selected from the marker list.
     *
     * @param mInfo  MarkerInfo to be highlighted
     * @param status whether to be highlighted or not
     */
    public void highlight(DSGeneMarker mInfo, boolean status) {
    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        jSpacer3 = Box.createHorizontalStrut(8);
        jSpacer4 = Box.createHorizontalStrut(8);
        jSpacer1 = Box.createHorizontalStrut(8);
        this.setLayout(borderLayout1);
        jPanel2.setLayout(borderLayout2);
        jToolTipToggleButton.setToolTipText("Toggle signal");
        jToolTipToggleButton.setActionCommand("TOOL_TIP_TOGGLE");
        jToolTipToggleButton.setSelected(false);
        jToolTipToggleButton.setIcon(new ImageIcon(this.getClass().getResource("bulb_icon_grey.gif")));
        jToolTipToggleButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("bulb_icon_gold.gif")));
        jToolTipToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToolTipToggleButton_actionPerformed(e);
            }
        });
        slider.setPaintTicks(true);
        slider.setValue(100);
        slider.setMinorTickSpacing(2);
        slider.setMinimum(1);
        slider.setMaximum(200);
        slider.setMajorTickSpacing(50);
        slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slider.setBorder(new LineBorder(Color.black));
        slider.setToolTipText("Intensity");
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                slider_stateChanged(e);
            }
        });
        jCheckBox1.setText("Enable Selection");
        jCheckBox1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomCheckBox_actionPerformed(e);
            }
        });

        SpinnerNumberModel snm2 = new SpinnerNumberModel(HierClusterDisplay.geneHeight, 1, 100, 1);
        jGeneHeight = new JSpinner(snm2);
        jGeneHeight.setToolTipText("Gene Height");
        jGeneHeight.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneHeight_stateChanged(e);
            }
        });
        jGeneWidth.setToolTipText("Gene Width");

        SpinnerNumberModel snm1 = new SpinnerNumberModel(new Integer(HierClusterDisplay.geneWidth), new Integer(1), new Integer(100), new Integer(1));
        jGeneWidth.setModel(snm1);
        jGeneWidth.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneWidth_stateChanged(e);
            }
        });
        ButtonBarBuilder bbuilder = new ButtonBarBuilder();
        bbuilder.addFixed(jCheckBox1);
        bbuilder.addGlue();
        bbuilder.addFixed(heightLabel);
        bbuilder.addRelatedGap();
        bbuilder.addFixed(jGeneHeight);
        bbuilder.addUnrelatedGap();
        bbuilder.addFixed(widthLabel);
        bbuilder.addRelatedGap();
        bbuilder.addFixed(jGeneWidth);
        bbuilder.addGlue();
        bbuilder.addFixed(new JLabel("Intensity"));
        bbuilder.addRelatedGap();
        bbuilder.addGriddedGrowing(slider);
        bbuilder.addGlue();
        bbuilder.addFixed(jToolTipToggleButton);

        this.add(bbuilder.getPanel(), BorderLayout.SOUTH);

        this.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jPanel2, null);
        jScrollPane1.getVerticalScrollBar().addAdjustmentListener(scrollBarListener);
        jScrollPane1.getHorizontalScrollBar().addAdjustmentListener(scrollBarListener);
        jPanel2.add(display, BorderLayout.CENTER);
        display.addMouseMotionListener(motionListener);
        display.addMouseListener(mouseListener);
        markerDendrogram = new HierClusterTree(this, null, HierClusterTree.HORIZONTAL);
        markerDendrogram.addMouseListener(dendrogramListener);
        markerDendrogram.addMouseMotionListener(motionListener);
        markerDendrogram.addMouseListener(mouseListener);
        jPanel2.add(markerDendrogram, BorderLayout.WEST);
        arrayDendrogram = new HierClusterTree(this, null, HierClusterTree.VERTICAL);
        arrayDendrogram.addMouseListener(dendrogramListener);
        arrayDendrogram.addMouseMotionListener(motionListener);
        arrayDendrogram.addMouseListener(mouseListener);
        arrayNames = new HierClusterLabels(this);
        arrayContainer.setLayout(new BorderLayout());
        arrayContainer.add(arrayNames, BorderLayout.NORTH);
        arrayContainer.add(arrayDendrogram, BorderLayout.CENTER);
        jPanel2.add(arrayContainer, BorderLayout.NORTH);
        imageSnapShot.setText("Image Snapshot");
        addToPanel.setText("Add to Set");
        contextMenu.add(imageSnapShot);
        contextMenu.add(addToPanel);

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage_actionPerformed(e);
            }
        };

        imageSnapShot.addActionListener(listener);
        listeners.put("File.Image snapshot", listener);
        addToPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToPanel_actionPerformed(e);
            }
        });
    }

    /**
     * Handles selections/deselections of the 'Zoom' checkbox
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void zoomCheckBox_actionPerformed(ActionEvent e) {
        zoomEnabled = jCheckBox1.isSelected();

        if (!zoomEnabled) {
            resetOriginal();
        }
    }

    /**
     * Handles changes in the intensity slider
     *
     * @param e <code>ChangeEvent</code> forwarded by the slider listener
     */
    private void slider_stateChanged(ChangeEvent e) {
        double v = (double) slider.getValue() / 100.0;

        if (v > 1) {
            display.setIntensity((1 + Math.exp(v)) - Math.exp(1.0));
        } else {
            display.setIntensity(v);
        }
    }

    /**
     * Handles {@link java.awt.event.MouseMotionListener#mouseReleased}
     * <p/>
     * invocations on this widget
     *
     * @param e <code>MouseEvent</code> forwarded by the mouse listener.
     */
    private void this_mouseMoved(MouseEvent e) {
        if (zoomEnabled) {
            if (e.getSource() == markerDendrogram) {
                markerDendrogram.setCurrentHighlightForMouseLocation(e.getY(), e.getX());
                if (markerDendrogram.isPointClickable(e.getX(), e.getY(), false)) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            } else if (e.getSource() == arrayDendrogram) {
                arrayDendrogram.setCurrentHighlightForMouseLocation(e.getX(), e.getY());
                if (arrayDendrogram.isPointClickable(e.getX(), e.getY(), true)) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

        if (e.getSource() instanceof HierClusterDisplay) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            display.drawCell(e.getX(), e.getY(), showSignal);
        }
    }

    /**
     * Handles {@link java.awt.event.MouseListener#mouseClicked} invocations on
     * this one of the Dendrograms
     *
     * @param e <code>MouseEvent</code> forwarded by the Dendrogram.
     */
    private void dendrogram_mouseClicked(MouseEvent e) {
        if (zoomEnabled) {
            if (e.getSource() == markerDendrogram) {
                selectedMarkerCluster = (MarkerHierCluster) markerDendrogram.getNodeClicked(e.getX(), e.getY());

                if (selectedMarkerCluster != null) {
                    init(selectedMarkerCluster, selectedArrayCluster);
                }
            } else if (e.getSource() == arrayDendrogram) {
                selectedArrayCluster = (MicroarrayHierCluster) arrayDendrogram.getNodeClicked(e.getX(), e.getY());

                if (selectedArrayCluster != null) {
                    init(selectedMarkerCluster, selectedArrayCluster);
                }
            }

            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Handles {@link java.awt.event.MouseListener#mouseReleased} invocations on
     * this widget
     *
     * @param e <code>MouseEvent</code> forwarded by the mouse listener.
     */
    private void this_mouseReleased(MouseEvent e) {
        if (e.isMetaDown()) {
            if (e.getSource() == display) {
                e.translatePoint(markerDendrogram.getWidth(), arrayNames.getHeight() + arrayDendrogram.getHeight());
            }

            if (e.getSource() == markerDendrogram) {
                e.translatePoint(0, arrayNames.getHeight() + arrayDendrogram.getHeight());
            }

            if (e.getSource() == arrayDendrogram) {
                e.translatePoint(0, arrayNames.getHeight());
            }

            if (e.getSource() == arrayNames) {
                e.translatePoint(0, 0);
            }

            contextMenu.show(jPanel2, e.getX(), e.getY());
        }
    }

    /**
     * Handles {@link java.awt.event.MouseListener#mouseClicked} invocations on
     * this widget
     *
     * @param e <code>MouseEvent</code> forwarded by the mouse listener.
     */
    private void this_mouseClicked(MouseEvent e) {
        if (e.getSource() == display) {
            DSGeneMarker mInfo = display.getMarkerInfoClicked(e.getX(), e.getY());

            if (mInfo != null) {
                firePropertyChange(SINGLE_MARKER_SELECTED_PROPERTY, null, mInfo);
            }
        }
    }

    /**
     * Handles changes to the Gene Height widget
     *
     * @param e <code>ChangeEvent</code> forwarded by the listener
     */
    private void jGeneHeight_stateChanged(ChangeEvent e) {
        HierClusterDisplay.geneHeight = ((Integer) ((JSpinner) e.getSource()).getValue()).intValue();
        markerDendrogram.resizingMarker = true;
        arrayDendrogram.resizingMarker = true;
        arrayNames.resizingMarker = true;
        setSizes();
        revalidate();
        repaint();
    }

    /**
     * Handles changes to the Gene Width widget
     *
     * @param e <code>ChangeEvent</code> forwarded by the listener
     */
    private void jGeneWidth_stateChanged(ChangeEvent e) {
        HierClusterDisplay.geneWidth = ((Integer) ((JSpinner) e.getSource()).getValue()).intValue();
        markerDendrogram.resizingMarker = true;
        arrayDendrogram.resizingMarker = true;
        arrayNames.resizingMarker = true;
        setSizes();
        revalidate();
        repaint();
    }

    /**
     * Handles Image Snapshot menu item selection
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void saveImage_actionPerformed(ActionEvent e) {
        if ((markerDendrogram != null) && (arrayDendrogram != null) && (display != null)) {
            display.imageSnapshot = markerDendrogram.imageSnapshot = arrayDendrogram.imageSnapshot = arrayNames.imageSnapshot = true;
            this.paintImmediately(0, 0, this.getWidth(), this.getHeight());

            int w = display.image.getWidth() + markerDendrogram.getWidth();
            int h = arrayDendrogram.getHeight() + display.image.getHeight() + arrayNames.image.getHeight();
            BufferedImage tempImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D ig = tempImage.createGraphics();
            ig.setColor(Color.white);
            jPanel2.paint(ig);
//            arrayNames.paintComponent(ig);
//            markerDendrogram.paintComponent(ig);
//            arrayDendrogram.paintComponent(ig);
//            ig.fillRect(0, 0, markerDendrogram.getWidth(), arrayDendrogram.getHeight() + arrayNames.image.getHeight());
//            ig.drawImage(markerDendrogram.image, null, 0, arrayDendrogram.image.getHeight() + arrayNames.image.getHeight());
//            ig.drawImage(arrayNames.image, null, 0, 0);
//            ig.drawImage(arrayDendrogram.image, null, 0, arrayNames.image.getHeight());
            ig.drawImage(display.image, null, markerDendrogram.getWidth(), arrayDendrogram.getHeight() + arrayNames.image.getHeight());

            ImageIcon newIcon = new ImageIcon(tempImage, "Hierarchical Clustering Image : " + mASet.getDataSet().getLabel());
            display.imageSnapshot = markerDendrogram.imageSnapshot = arrayDendrogram.imageSnapshot = arrayNames.imageSnapshot = false;
            firePropertyChange(SAVEIMAGE_PROPERTY, null, newIcon);
        }
    }

    /**
     * Handles Add to Panel menu item selection
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void addToPanel_actionPerformed(ActionEvent e) {
        if (selectedMarkerCluster != null) {
            java.util.List<Cluster> leaves = selectedMarkerCluster.getLeafChildren();

//            Cluster[] leaves = selectedMarkerCluster.getLeafChildren();
            DSGeneMarker[] mInfos = new DSGeneMarker[leaves.size()];

            for (int i = 0; i < leaves.size(); i++)
                mInfos[i] = ((MarkerHierCluster) leaves.get(i)).getMarkerInfo();

            if (mInfos != null) {
                firePropertyChange(MULTIPLE_MARKER_SELECTED_PROPERTY, null, mInfos);
            }
        }
    }

    /**
     * Resets original clusters
     */
    private void resetOriginal() {
        selectedMarkerCluster = originalMarkerCluster;
        selectedArrayCluster = originalArrayCluster;
        init(originalMarkerCluster, originalArrayCluster);
    }

    /**
     * Handles selection/deselections of the ToolTip toggle button
     *
     * @param e <code>ActionEvent</code> forwarded by the listener
     */
    private void jToolTipToggleButton_actionPerformed(ActionEvent e) {
        showSignal = jToolTipToggleButton.isSelected();
    }

    /**
     * Method to handle <code>AdjustmentListener</code> that is added to
     * <code>JScrollBar</code> components to track if the <code>JScrollBar</code>
     * has stopped moving
     *
     * @param e <code>AdjustmentEvent</code> that is fired by a
     *          <code>JScrollBar</code>
     */
    private void this_adjustmentValueChanged(AdjustmentEvent e) {
        if (!e.getValueIsAdjusting()) {
            this.revalidate();
            this.repaint();
        }
    }
}
