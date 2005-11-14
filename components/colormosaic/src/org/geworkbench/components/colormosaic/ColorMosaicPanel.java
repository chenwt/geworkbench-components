package org.geworkbench.components.colormosaic;

import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.associationdiscovery.cluster.CSMatchedMatrixPattern;
import org.geworkbench.util.associationdiscovery.cluster.CSMatrixPattern;
import org.geworkbench.events.AssociationPanelEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.pattern.CSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.util.DSPValue;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

@AcceptTypes({DSMicroarraySet.class}) public class ColorMosaicPanel implements Printable, VisualPlugin, MenuListener {
    private JPanel mainPanel = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JToolBar jToolBar1 = new JToolBar();
    private JButton printBtn = new JButton();
    private JButton copyBtn = new JButton();
    private JToggleButton jToggleButton1 = new JToggleButton("Abs");
    private JScrollPane jScrollPane = new JScrollPane();
    private ColorMosaicImage colorMosaicImage = new ColorMosaicImage();
    private JPanel jPanel1 = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JSpinner jGeneWidthSlider = new JSpinner();
    private JSpinner jGeneHeightSlider = new JSpinner();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JSlider jIntensitySlider = new JSlider();
    private JLabel jLabel5 = new JLabel();
    private JToggleButton jTogglePrintDescription = new JToggleButton("Label", true);
    private JToggleButton jTogglePrintRatio = new JToggleButton("Ratio", true);
    private JToggleButton jTogglePrintAccession = new JToggleButton("Accession", false);
    private JToggleButton jToggleButton2 = new JToggleButton("Pat");
    private JToggleButton jHideMaskedBtn = new JToggleButton("Display");
    private BorderLayout borderLayout2 = new BorderLayout();
    private CMHRuler colRuler = new CMHRuler(colorMosaicImage);
    private CMVRuler rowRuler = new CMVRuler(colorMosaicImage);
    private JCheckBox jAllMArrays = new JCheckBox();
    private JCheckBox jAllMarkers = new JCheckBox();
    private HashMap listeners = new HashMap();

    private static final int GENE_HEIGHT = 10;
    private static final int GENE_WIDTH = 20;

    public ColorMosaicPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void setChips(DSMicroarraySet chips) {
        if (chips != null) {
            colorMosaicImage.setChips(chips);
            mainPanel.repaint();
        }
    }

    private void jbInit() throws Exception {
        mainPanel.setBackground(Color.WHITE);
        colRuler.setPreferredWidth(1000);
        rowRuler.setPreferredSize(new Dimension(20, 1000));
        printBtn.setMaximumSize(new Dimension(26, 26));
        printBtn.setMinimumSize(new Dimension(26, 26));
        printBtn.setPreferredSize(new Dimension(26, 26));
        printBtn.setIcon(new ImageIcon(ColorMosaicPanel.class.getResource("print.gif")));
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printBtn_actionPerformed(e);
            }
        });
        mainPanel.setMinimumSize(new Dimension(200, 300));
        mainPanel.setPreferredSize(new Dimension(438, 300));
        mainPanel.setLayout(borderLayout2);
        copyBtn.setMaximumSize(new Dimension(26, 26));
        copyBtn.setMinimumSize(new Dimension(26, 26));
        copyBtn.setPreferredSize(new Dimension(26, 26));
        copyBtn.setIcon(new ImageIcon(ColorMosaicPanel.class.getResource("copy.gif")));


        jToggleButton1.setToolTipText("");
        jToggleButton1.setHorizontalTextPosition(SwingConstants.CENTER);
        jToggleButton1.setMargin(new Insets(2, 3, 2, 3));
//        jToggleButton1.setFont(new java.awt.Font("Serif", 0, 10));
//        jToggleButton1.setMaximumSize(new Dimension(26, 26));
//        jToggleButton1.setPreferredSize(new Dimension(26, 26));
//        jToggleButton1.setMinimumSize(new Dimension(26, 26));

        jToggleButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButton1_actionPerformed(e);
            }
        });

        jPanel1.setLayout(gridBagLayout1);
        jLabel1.setText("Gene Height");
        jLabel2.setText("Gene Width");
        jGeneHeightSlider.setValue(new Integer(10));
        colorMosaicImage.setGeneHeight(10);
        colorMosaicImage.setParent(this);
        jGeneHeightSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneHeightSlider_stateChanged(e);
            }
        });
        jGeneWidthSlider.setValue(new Integer(20));
        colorMosaicImage.setGeneWidth(20);
        jGeneWidthSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jGeneWidthSlider_stateChanged(e);
            }
        });
        jIntensitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jIntensitySlider_stateChanged(e);
            }
        });
        jIntensitySlider.setPaintTicks(true);
        jIntensitySlider.setValue(100);
        jIntensitySlider.setMinorTickSpacing(2);
        jIntensitySlider.setMinimum(1);
        jIntensitySlider.setMaximum(200);
        jIntensitySlider.setMajorTickSpacing(50);
        jLabel5.setText("Intensity");
//        jTogglePrintDescription.setMaximumSize(new Dimension(50, 25));
//        jTogglePrintDescription.setMinimumSize(new Dimension(50, 25));
//        jTogglePrintDescription.setPreferredSize(new Dimension(50, 25));
        jTogglePrintDescription.setMargin(new Insets(2, 3, 2, 3));

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jTogglePrintDescription_actionPerformed(e);
            }
        };
        listeners.put("File.Print", listener);
        jTogglePrintDescription.addActionListener(listener);

        jTogglePrintRatio.setMaximumSize(new Dimension(50, 25));
        jTogglePrintRatio.setMinimumSize(new Dimension(50, 25));
        jTogglePrintRatio.setPreferredSize(new Dimension(50, 25));
        jTogglePrintRatio.setMargin(new Insets(2, 3, 2, 3));
        jTogglePrintRatio.setSelected(false);

        jTogglePrintRatio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jTogglePrintRatio_actionPerformed(e);
            }
        });

//        jTogglePrintAccession.setMaximumSize(new Dimension(50, 25));
//        jTogglePrintAccession.setMinimumSize(new Dimension(50, 25));
//        jTogglePrintAccession.setPreferredSize(new Dimension(50, 25));
        jTogglePrintAccession.setMargin(new Insets(2, 3, 2, 3));

        jTogglePrintAccession.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jTogglePrintAccession_actionPerformed(e);
            }
        });

        jToggleButton2.setMargin(new Insets(2, 3, 2, 3));
        jToggleButton2.setHorizontalTextPosition(SwingConstants.CENTER);
        jToggleButton2.setToolTipText("");

//        jToggleButton2.setMinimumSize(new Dimension(26, 26));
//        jToggleButton2.setPreferredSize(new Dimension(26, 26));
//        jToggleButton2.setMaximumSize(new Dimension(26, 26));
//        jToggleButton2.setFont(new java.awt.Font("Serif", 0, 10));

        jToggleButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButton2_actionPerformed(e);
            }
        });

        jHideMaskedBtn.setMargin(new Insets(2, 3, 2, 3));
        jHideMaskedBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        jHideMaskedBtn.setToolTipText("");

//        jHideMaskedBtn.setMinimumSize(new Dimension(26, 26));
//        jHideMaskedBtn.setPreferredSize(new Dimension(26, 26));
//        jHideMaskedBtn.setMaximumSize(new Dimension(26, 26));
//        jHideMaskedBtn.setFont(new java.awt.Font("Serif", 0, 10));

        jHideMaskedBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jHideMaskedBtn_actionPerformed(e);
            }
        });

        jScrollPane.getViewport().setBackground(Color.white);
        jAllMArrays.setSelected(true);
        jAllMArrays.setText("All arrays");
        jAllMArrays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAllMArrays_actionPerformed(e);
            }
        });
        jAllMarkers.setSelected(true);
        jAllMarkers.setText("All Markers");
        jAllMarkers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAllMarkers_actionPerformed(e);
            }
        });
        mainPanel.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(printBtn, null);
        jToolBar1.add(copyBtn, null);
        jToolBar1.add(jToggleButton2, null);
        jToolBar1.add(jToggleButton1, null);
        jToolBar1.add(jHideMaskedBtn, null);
        jToolBar1.add(jTogglePrintRatio, null);
        jToolBar1.add(jTogglePrintAccession, null);
        jToolBar1.add(jTogglePrintDescription, null);
        jToolBar1.add(jAllMArrays, null);
        jToolBar1.add(jAllMarkers, null);
        mainPanel.add(jScrollPane, BorderLayout.CENTER);
        mainPanel.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jGeneWidthSlider, new GridBagConstraints(2, 1, 1, 1, 0.34, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(jGeneHeightSlider, new GridBagConstraints(1, 1, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(jLabel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(jLabel2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(jIntensitySlider, new GridBagConstraints(0, 1, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(jLabel5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        jScrollPane.getViewport().add(colorMosaicImage, null);
        jScrollPane.setColumnHeaderView(colRuler);
        jScrollPane.setRowHeaderView(rowRuler);
        jScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JComponent() {
            public void paint(Graphics g) {
                setBackground(Color.WHITE);
                Rectangle clip = g.getClipBounds();
                g.clearRect(clip.x, clip.y, clip.width, clip.height);
            }
        });
        colorMosaicImage.setPrintRatio(jTogglePrintRatio.isSelected());
        colorMosaicImage.setPrintAccession(jTogglePrintAccession.isSelected());
        colorMosaicImage.setPrintDescription(jTogglePrintDescription.isSelected());
//        colorMosaicImage = makeBlankColorMosaicImage(GENE_HEIGHT, GENE_WIDTH, jTogglePrintRatio.isSelected(),
//                jTogglePrintAccession.isSelected(), jTogglePrintDescription.isSelected());
    }

    public void revalidate() {
        mainPanel.revalidate();
        if (rowRuler != null) {
            rowRuler.revalidate();
            rowRuler.repaint();
        }
        if (colRuler != null) {
            colRuler.revalidate();
            colRuler.repaint();
        }
    }

    private void resetColorMosaicImage(int geneHeight, int geneWidth,
                                                       boolean printRatio, boolean printAccession, boolean printDesc) {
        colorMosaicImage.setGeneHeight(geneHeight);
        colorMosaicImage.setGeneWidth(geneWidth);
        colorMosaicImage.setPrintRatio(printRatio);
        colorMosaicImage.setPrintAccession(printAccession);
        colorMosaicImage.setPrintRatio(printRatio);
        colorMosaicImage.setMarkerPanel(null);
        colorMosaicImage.setPanel(null);
        colorMosaicImage.setChips(null);
    }

    public void notifyPatternSelection(CSMatchedMatrixPattern[] selectedPatterns) {
        colorMosaicImage.clearPatterns();
        for (int i = 0; i < selectedPatterns.length; i++) {
            colorMosaicImage.addPattern(selectedPatterns[i]);
            //System.out.println("Adding Pattern:");
        }
        revalidate();
    }

    void addPatterns(Collection patterns) throws ArrayIndexOutOfBoundsException {
        Iterator it = patterns.iterator();
        while (it.hasNext()) {
            CSMatchedMatrixPattern pattern = (CSMatchedMatrixPattern) it.next();
            colorMosaicImage.addPattern(pattern);
        }
        revalidate();
    }

    void clearPatterns() {
        colorMosaicImage.clearPatterns();
        revalidate();
    }

    void jGeneHeightSlider_stateChanged(ChangeEvent e) {
        int h = ((Integer) jGeneHeightSlider.getValue()).intValue();
        colorMosaicImage.setGeneHeight(h);
        revalidate();
    }

    void jGeneWidthSlider_stateChanged(ChangeEvent e) {
        int w = ((Integer) jGeneWidthSlider.getValue()).intValue();
        colorMosaicImage.setGeneWidth(w);
        revalidate();
    }

    void jIntensitySlider_stateChanged(ChangeEvent e) {
        double v = (double) jIntensitySlider.getValue() / 100.0;
        if (v > 1) {
            colorMosaicImage.setIntensity(1 + Math.exp(v) - Math.exp(1.0));
        } else {
            colorMosaicImage.setIntensity(v);
        }
    }

    void jToggleButton1_actionPerformed(ActionEvent e) {
        colorMosaicImage.setAbsDisplay(jToggleButton1.isSelected());
        mainPanel.repaint();
    }

    void jTogglePrintRatio_actionPerformed(ActionEvent e) {
        colorMosaicImage.setPrintRatio(jTogglePrintRatio.isSelected());
    }

    void jTogglePrintAccession_actionPerformed(ActionEvent e) {
        colorMosaicImage.setPrintAccession(jTogglePrintAccession.isSelected());
    }

    void jTogglePrintDescription_actionPerformed(ActionEvent e) {
        colorMosaicImage.setPrintDescription(jTogglePrintDescription.isSelected());
    }

    void jToggleButton2_actionPerformed(ActionEvent e) {
        colorMosaicImage.toggleShowPattern(jToggleButton2.isSelected());
    }

    void jHideMaskedBtn_actionPerformed(ActionEvent e) {
        if (jHideMaskedBtn.isSelected()) {
            colorMosaicImage.clearPatterns();
            DSMicroarraySet<DSMicroarray> mArraySet = colorMosaicImage.getGeneChips();
            if (mArraySet != null) {
                int markerNo = mArraySet.getMarkers().size();
                CSMatrixPattern thePattern = new CSMatrixPattern();
                thePattern.init(markerNo);
                CSMatchedMatrixPattern matchedPattern = new CSMatchedMatrixPattern(thePattern);
                for (int i = 0; i < markerNo; i++) {
                    matchedPattern.getPattern().markers()[i] = mArraySet.getMarkers().get(i);
                }
                for (int i = 0; i < mArraySet.size(); i++) {
                    DSPatternMatch<DSMicroarray, DSPValue> match = new CSPatternMatch<DSMicroarray, DSPValue>(mArraySet.get(i));
                }
                colorMosaicImage.addPattern(matchedPattern);
            }
        } else {
            colorMosaicImage.clearPatterns();
        }
        revalidate();
    }

    void printBtn_actionPerformed(ActionEvent e) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception PrinterExeption) {
                System.out.println("Exception: " + PrinterExeption);
            }
        }

    }

    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        int[] res = {600, 600, 3};
        Graphics2D g2 = (Graphics2D) g;
        double scaleX = (double) res[0] / 72.0;
        double scaleY = (double) res[0] / 72.0;
        g2.scale(1.0 / scaleX, 1.0 / scaleY);
        g2.setStroke(new BasicStroke(1.0F));
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        double inches = 0.0;
        rowRuler.computeWidth(scaleY);

        int rowSpace = (int) rowRuler.getScaledWidth();
        int colSpace = (int) ((double) colRuler.getHeight() * scaleX);
        int r = 72;
        double d = pf.getImageableWidth();
        inches = d / (double) r - (double) rowSpace / (double) res[0];
        //GraphicsConfiguration gc = g2.getDeviceConfiguration();
        g2.translate(pf.getImageableX() * scaleX, pf.getImageableY() * scaleY);
        colorMosaicImage.setAutoWidth(inches, res[0]);
        System.out.print("Painting ... ");
        g2.translate(rowSpace, 0);
        colRuler.paint(g2, res[0]);
        g2.translate(-rowSpace, colSpace);
        rowRuler.paint(g2, res[0]);
        g2.translate(rowSpace, 0);
        colorMosaicImage.paint(g2, res[0], false);
        System.out.println("Done");
        return Printable.PAGE_EXISTS;
    }

    public String getComponentName() {
        return "Color Mosaic";
    }

    public void notifyMAChange(DSMicroarraySet microarraySet) {
        setChips(microarraySet);
    }

    public void notifyComponent(Object subscriber, Class anInterface) {
        colorMosaicImage.notifyComponent(subscriber, anInterface);
    }

    private void setMarkerPanel(DSPanel<DSGeneMarker> panel) {
        if (panel != null) {
            colorMosaicImage.setMarkerPanel(panel);
            revalidate();
            colorMosaicImage.repaint();
        }
    }

    private void setMicroarrayPanel(DSPanel<DSMicroarray> panel) {
        if (panel != null) {
            colorMosaicImage.setPanel(panel);
            revalidate();
            colorMosaicImage.repaint();
        }
    }

    void jAllMArrays_actionPerformed(ActionEvent e) {
        colorMosaicImage.showAllMArrays(jAllMArrays.isSelected());
        colRuler.revalidate();
        mainPanel.repaint();
    }

    void jAllMarkers_actionPerformed(ActionEvent e) {
        colorMosaicImage.showAllMarkers(jAllMarkers.isSelected());
        rowRuler.revalidate();
        mainPanel.repaint();
    }

    public Component getComponent() {
        return mainPanel;
    }

    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DSDataSet dataFile = selection.getDataSet();

        if (dataFile != null) {
            if (dataFile instanceof DSMicroarraySet) {
                DSMicroarraySet set = (DSMicroarraySet) dataFile;
                if (set != null) {
                    if (colorMosaicImage.getChips() != set) {
                        setChips(set);
                    }
                }
            }
        } else {
            jToggleButton2.setSelected(false);
            jHideMaskedBtn.setSelected(false);
            resetColorMosaicImage(GENE_HEIGHT, GENE_WIDTH, jTogglePrintRatio.isSelected(),
                    jTogglePrintAccession.isSelected(), jTogglePrintDescription.isSelected());
            colorMosaicImage.repaint();
            mainPanel.repaint();
        }
    }

    public ActionListener getActionListener(String key) {
        return (ActionListener) listeners.get(key);
    }

    @Subscribe public void receive(AssociationPanelEvent e, Object source) {
        CSMatchedMatrixPattern[] patterns  = e.getPatterns();
        if (e.message.equalsIgnoreCase("selection")) {
            notifyPatternSelection(patterns);
        } else if (e.message.equalsIgnoreCase("clear")) {
            colorMosaicImage.clearPatterns();
        }
    }

    @Subscribe public void receive(PhenotypeSelectorEvent e, Object source) {
        DSPanel<DSBioObject> pl = e.getTaggedItemSetTree();
        setMicroarrayPanel((DSPanel) pl);
    }

    @Subscribe public void receive(org.geworkbench.events.GeneSelectorEvent e, Object source) {
        DSPanel<DSGeneMarker> panel = e.getPanel();
        if (panel != null) {
            for (int i = 0; i < panel.panels().size(); i++) {
                //                if (panel.panels().get(i) instanceof IPValuePanel) {
                colorMosaicImage.pValueWidth = 18;
                colorMosaicImage.isPrintPValue = true;
                setMarkerPanel(panel);
                jHideMaskedBtn_actionPerformed(null);
                return;
                //                }
                //                else {
                //                    colorMosaicImage.pValueWidth = 0;
                //                    colorMosaicImage.isPrintPValue = false;
                //                    jHideMaskedBtn_actionPerformed(null);
                //                }
            }
            setMarkerPanel(panel);
        }
    }

    @Publish public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    public Image getColorMosaicAsImage() {
        Dimension mainDim = colorMosaicImage.getPreferredSize();
        Dimension topDim = colRuler.getPreferredSize();
        Dimension leftDim = rowRuler.getPreferredSize();
        Dimension dim = new Dimension(mainDim.width + leftDim.width, mainDim.height + topDim.height);
        BufferedImage image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, dim.width, dim.height);
        AffineTransform start = g.getTransform();
        // Draw the main image
        g.translate(leftDim.width, topDim.height);
        colorMosaicImage.paint(g, ColorMosaicImage.DEFAULTRES, false);
        // Draw the column ruler
        g.setTransform(start);
        g.translate(leftDim.width, 0);
        // g.setClip(leftDim.width, 0, mainDim.width, topDim.height);
        colRuler.paint(g);
        // Draw the row ruler
        g.setTransform(start);
        g.translate(0, topDim.height);
        // g.setClip(0, topDim.height, leftDim.width, mainDim.height);
        rowRuler.paint(g);
        //        // temp: write image
        //        Iterator writers = ImageIO.getImageWritersByFormatName("PNG");
        //        ImageWriter writer = (ImageWriter) writers.next();
        //        File f = new File("colorMosaic.png");
        //        try {
        //            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        //            writer.setOutput(ios);
        //            writer.write(image);
        //            ios.close();
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        //        // end temp: write image
        return image;
    }

    public void createImageSnapshot() {
        Image currentImage = getColorMosaicAsImage();
        ImageIcon newIcon = new ImageIcon(currentImage, "Color Mosaic View");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("Color Mosaic View", newIcon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        publishImageSnapshotEvent(event);

    }

    @Publish public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent(org.geworkbench.events.ImageSnapshotEvent event) {
        return event;
    }

}
