package org.geworkbench.components.hierarchicalclustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;


/**
 * <p>Title: Bioworks</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 3.0
 */

public class HierClusterLabels extends JPanel {

    static Log log = LogFactory.getLog(HierClusterLabels.class);

    /**
     * Value for width of marker in pixels
     */
    protected static int geneWidth = 20;

    /**
     * The underlying micorarray set used in the hierarchical clustering
     * analysis.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = null;

    /**
     * The current array cluster being rendered in the marker Dendrogram
     */
    private MicroarrayHierCluster currentArrayCluster = null;

    /**
     * The leaf microarrays clusters in <code>currentArrayCluster</code>.
     */
    private Cluster[] leafArrays = null;

    /**
     * <code>Image</code> painted on synchronously with this panel
     */
    BufferedImage image = null;

    /**
     * Bit to paint the offline image
     */
    boolean imageSnapshot = false;

    /**
     * Font used with text labels
     */
    private Font labelFont = null;

    /**
     * Placeholder for font size
     */
    private int fontSize = 5;

    /**
     * The maximum font height in pixels
     */
    private final int maxFontSize = 10;

    /**
     * Default resolution for text display
     */
    private final int defaultResolution = 120;

    /**
     * The current resolution
     */
    private int resolution = defaultResolution;

    /**
     * Space from eisenplot where the accession is printed
     */
    private int labelGutter = 5;

    /**
     * To be set for labels
     */
    int leftOffset = 0;

    /**
     * Maximum height in pixels of the tree given the number of leaves,
     * computed based on the visual height of each leaf. This would be attained
     * only if the Heirarchical tree is a strict binary tree
     */
    private int maxHeight;

    /**
     * Maximum depth of the tree in terms of the nodes between the root and
     * a leaf, both inclusive
     */
    private int maxDepth;

    /**
     * Offset from top left corner of the canvas in canvas for drawing the tree
     */
    private int offSet = 5;

    /**
     * Height in pixels used for drawing a leaf
     */
    private int leafHeight = HierClusterDisplay.geneHeight;

    /**
     * Width in pixels used drawing a leaf
     */
    private int leafWidth = HierClusterDisplay.geneWidth;

    /**
     * Width of the dendrogram
     */
    private int width = 50;

    /**
     * Container holding this dendrogram tree
     */
    private JPanel parent = null;

    /**
     * Check to call setSizes in paint only if the canvas has been resized
     */
    protected boolean resizingMarker = false;

    /**
     * Result of Hierarchical Clustering
     */
    private HierCluster clusterRoot = null;

    public HierClusterLabels(JPanel parent) {
        super();
        this.parent = parent;
    }

    /**
     * Receives the reference microarray set on which the hierarchical
     * clustering analysis is based
     *
     * @param chips the reference microarray set
     */
    public void setChips(DSMicroarraySetView<DSGeneMarker, DSMicroarray> chips) {
        microarraySet = chips;
    }

    /**
     * Receives the <code>MicroarrayHierCluster</code> from the
     * <code>HierClusterViewWidget</code>
     *
     * @param mhc the marker cluster
     */
    public void setMicroarrayHierCluster(MicroarrayHierCluster mhc) {
        currentArrayCluster = mhc;

        if (currentArrayCluster != null) {
            java.util.List<Cluster> leaves = currentArrayCluster.getLeafChildren();
            leafArrays = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
            leaves.toArray(leafArrays);

//            leafArrays = currentArrayCluster.getLeafChildren();
        }
    }

    /**
     * <code>JComponent</code> method used to render this component
     *
     * @param g Graphics used for painting
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (resizingMarker) {
            setSizes(clusterRoot);
        }

        try {
            if (microarraySet != null) {
                Graphics2D ig = null;

                if (imageSnapshot) {
                    image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                    ig = image.createGraphics();
                    ig.setColor(Color.white);
                    ig.fillRect(0, 0, this.getWidth(), this.getHeight());
                }

                g.setColor(Color.white);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.black);

                int chipNo = 0;

                if (currentArrayCluster == null) {
                    chipNo = microarraySet.items().size();
                } else {
                    chipNo = leafArrays.length;
                }

                setFont();
                g.setFont(labelFont);

                AffineTransform at = new AffineTransform();
                at.rotate(-Math.PI / 2);

                AffineTransform saveAt = ((Graphics2D) g).getTransform();
                ((Graphics2D) g).transform(at);

                for (int j = 0; j < chipNo; j++) {
                    DSMicroarray mArray = null;

                    if (leafArrays != null) {
                        mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                    } else {
                        mArray = microarraySet.get(j);
                    }

                    int yRatio = (int) ((j + 0.3) * geneWidth);
                    String name = mArray.getLabel();

                    if (name == null) {
                        name = "Undefined";
                    }

                    g.drawString(name, -width, leftOffset + yRatio);
                }

                ((Graphics2D) g).setTransform(saveAt);

                if (imageSnapshot) {
                    ig.setColor(Color.black);
                    setFont();
                    ig.setFont(labelFont);

                    AffineTransform saveAt1 = ((Graphics2D) ig).getTransform();
                    ((Graphics2D) ig).transform(at);

                    for (int j = 0; j < chipNo; j++) {
                        DSMicroarray mArray = null;

                        if (leafArrays != null) {
                            mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                        } else {
                            mArray = microarraySet.get(j);
                        }

                        int yRatio = (int) ((j + 0.3) * geneWidth);
                        String name = mArray.getLabel();

                        if (name == null) {
                            name = "Undefined";
                        }

                        ig.drawString(name, -width, leftOffset + yRatio);
                    }

                    ((Graphics2D) ig).setTransform(saveAt1);
                }
            }
        } catch (NullPointerException npe) {
            log.error(npe);
        }
    }

    /**
     * Sets the <code>Font</code> used for drawing text
     */
    private void setFont() {
        int fontSize = Math.min(getFontSize(), (int) ((double) maxFontSize / (double) defaultResolution * (double) resolution));

        if ((fontSize != this.fontSize) || (labelFont == null)) {
            this.fontSize = fontSize;
            labelFont = new Font("Times New Roman", Font.PLAIN, this.fontSize);
        }
    }

    /**
     * Gets the <code>Font</code> size
     *
     * @return <code>Font</code> size
     */
    private int getFontSize() {
        return Math.max(HierClusterDisplay.geneWidth, 5);
    }

    /**
     * The <code>HierCluster<code> representing the entire
     * Hierarchical clustering tree obtained from the Analysis
     *
     * @param treeData <code>HierCluster<code> representing the entire
     *                 Hierarchical clustering tree
     */
    void setTreeData(HierCluster treeData) {
        clusterRoot = treeData;
        setSizes(clusterRoot);
        resizingMarker = true;
    }

    /**
     * Sets the display sizes based on the root node obtained from the clustering
     * analysis
     *
     * @param hc root node used for setting sizes
     */
    private void setSizes(HierCluster hc) {
        if (hc != null) {
            maxDepth = hc.getDepth();
        } else {
            maxDepth = 1;
        }

        if (microarraySet != null) {
            if (hc != null) {
                maxHeight = hc.getLeafChildrenCount() * HierClusterDisplay.geneWidth;
            } else {
                maxHeight = microarraySet.items().size() * HierClusterDisplay.geneWidth;
            }

            width = 100;
            geneWidth = HierClusterDisplay.geneWidth;
            setPreferredSize(new Dimension(this.getParent().getWidth(), width));
            setSize(new Dimension(this.getParent().getWidth(), width));
        }
    }
}
