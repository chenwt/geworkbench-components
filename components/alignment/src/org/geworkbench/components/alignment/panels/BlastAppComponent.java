package org.geworkbench.components.alignment.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.client.BlastAlgorithm;
import org.geworkbench.components.alignment.client.HMMDataSet;
import org.geworkbench.components.alignment.grid.CreateGridServiceDialog;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.events.MicroarraySetViewEvent;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
@AcceptTypes( {CSSequenceSet.class})public class BlastAppComponent extends
        CSSequenceSetViewEventBase {

    JCheckBox pfpFilterBox = new JCheckBox();
    JPanel jBasicPane = new JPanel();
    JLabel DatabaseLabel = new JLabel();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JTabbedPane jTabbedBlastPane = new JTabbedPane();
    JTabbedPane jTabbedHmmPane = new JTabbedPane();
    JTabbedPane jTabbedSmPane = new JTabbedPane();
    ServerInfoPanel jServerInfoPane = new ServerInfoPanel();
//     BlastGridServiceDataPanel sgePanel = new
//           BlastGridServiceDataPanel();
    CreateGridServicePanel sgePanel = new CreateGridServicePanel();
    JComboBox jMatrixBox = new JComboBox();
    JCheckBox lowComplexFilterBox = new JCheckBox();
    JPanel jAdvancedPane = new JPanel();
    JFileChooser jFileChooser1 = new JFileChooser();
    static final int BLAST = 0;
    static final int SW = 1;
    static final int HMM = 2;
    public static final String NCBILABEL = "NCBI BLAST Result";

    String[] databaseParameter = {
                                 "ncbi/nr                      Peptides of all non-redundant sequences.",
                                 "ncbi/pdbaa               Peptides Sequences derived from the PDB.",
                                 "ncbi/swissprot      SWISS-PROT protein sequence database.",
                                 "ncbi/yeast.aa            Yeast  genomic CDS translations.",
                                 "ncbi/nt                    All Non-redundant  DNA equences.",
                                 "ncbi/pdbnt                Nucleotide sequences derived from the PDB.",
                                 "ncbi/yeast.nt           Yeast genomic nucleotide sequences."};

    String[] programParameter = {
                                "blastp", "blastn", "blastx", "tblastn",
                                "tblastx"};
    String[] algorithmParameter = {
                                  "Smith-Waterman DNA",
                                  "Smith-Waterman Protein",
                                  "Frame (for DNA query to protein DB)",
                                  "Frame (for protein query to DNA DB)",
                                  //   "Double Frame (for DNA sequence to DNA DB)"
    };
    String[] hmmParameter = {
                            "Pfam global alignment only",
                            "Pfam local alignment only",
                            "Pfam global and local alignments"
    };

    //JList jDBList = new JList(databaseParameter);
    JPanel checkboxPanel = new JPanel();
    JList jDBList = new JList();
    JButton blastButton = new JButton();
    JScrollPane jScrollPane1 = new JScrollPane();
    JComboBox jProgramBox = new JComboBox();
    JPanel filterPanel = new JPanel();
    JCheckBox maskLookupOnlyBox = new JCheckBox();
    JLabel expectLabel = new JLabel();
    JComboBox jExpectBox = new JComboBox();
    JLabel matrixLabel = new JLabel();
    JPanel blastxSettingPanel = new JPanel();
    JComboBox jqueryGenericCodeBox = new JComboBox();
    JLabel jFrameShiftLabel = new JLabel();
    JComboBox jFrameShiftPaneltyBox = new JComboBox();
    ParameterSetter parameterSetter = new ParameterSetter();
    CSSequenceSet fastaFile;
    private BlastAppComponent blastAppComponent = null;
    JPanel subSeqPanel;
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JTextField jstartPointField = new JTextField();
    JTextField jendPointField = new JTextField();
    CardLayout cardLayout1 = new CardLayout();
    JProgressBar serviceProgressBar = new JProgressBar();
    JPanel progressBarPanel1 = new JPanel();
    JLabel jLabel5 = new JLabel();
    JPanel subSeqPanel1 = new JPanel();
    JPanel jPanel6 = new JPanel();
    JLabel jLabel6 = new JLabel();
    JComboBox jProgramBox1 = new JComboBox();
    JLabel jLabel7 = new JLabel();
    JLabel databaseLabel1 = new JLabel();
    JPanel jPanel7 = new JPanel();
    JLabel jLabel8 = new JLabel();
    GridBagLayout gridBagLayout7 = new GridBagLayout();
    JLabel jAlgorithmLabel = new JLabel();
    JTextField jendPointField1 = new JTextField();
    JPanel jPanel8 = new JPanel();
    JPanel jOtherAlgorithemPane = new JPanel();
    GridBagLayout gridBagLayout8 = new GridBagLayout();
    FlowLayout flowLayout2 = new FlowLayout();
    JTextField jstartPointField1 = new JTextField();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel jPanel9 = new JPanel();
    JProgressBar progressBar1 = new JProgressBar();
    GridBagLayout gridBagLayout9 = new GridBagLayout();
    JScrollPane jScrollPane2 = new JScrollPane();
    JList jList2 = new JList(algorithmParameter);
    GridBagLayout gridBagLayout13 = new GridBagLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    JCheckBox jDisplayInWebBox = new JCheckBox();
    BorderLayout borderLayout1 = new BorderLayout();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel jGenericCodeLabel = new JLabel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JButton jButton1 = new JButton();
    JButton blastStopButton = new JButton();
    JButton algorithmSearch = new JButton();
    JLabel jLabel13 = new JLabel();
    JProgressBar progressBar3 = new JProgressBar();
    JTextField jendPointField3 = new JTextField();
    JPanel progressBarPanel3 = new JPanel();
    FlowLayout flowLayout4 = new FlowLayout();
    GridBagLayout gridBagLayout14 = new GridBagLayout();
    BorderLayout borderLayout8 = new BorderLayout();
    JPanel jHMMPane = new JPanel();
    JPanel subSeqPanel3 = new JPanel();
    JPanel jPanel11 = new JPanel();
    JLabel jLabel14 = new JLabel();
    JPanel jPanel14 = new JPanel();
    JPanel jPanel15 = new JPanel();
    BorderLayout borderLayout9 = new BorderLayout();
    JLabel jLabel15 = new JLabel();
    JTextField jstartPointField3 = new JTextField();
    JList jList4 = new JList(hmmParameter);
    JPanel jPanel16 = new JPanel();
    JLabel jAlgorithmLabel2 = new JLabel();
    JLabel jLabel16 = new JLabel();
    FlowLayout flowLayout5 = new FlowLayout();
    JButton jButton2 = new JButton();
    JScrollPane jScrollPane3 = new JScrollPane();
    BorderLayout borderLayout6 = new BorderLayout();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    JCheckBox allArraysCheckBox;
    JToolBar jToolBar2 = new JToolBar();
    TitledBorder titledBorder1 = new TitledBorder("");
    Border border1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    Border border2 = new TitledBorder(border1,
                                      "Please specify subsequence, program and database");
    JToolBar jToolBar1 = new JToolBar();
    JLabel jLabel4;
    JLabel jLabel9 = new JLabel();
    XYLayout xYLayout1 = new XYLayout();
    ImageIcon startButtonIcon = new ImageIcon(this.getClass().getResource(
            "start.gif"));
    ImageIcon stopButtonIcon = new ImageIcon(this.getClass().getResource(
            "stop.gif"));
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    XYLayout xYLayout2 = new XYLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JScrollPane jScrollPane4 = new JScrollPane();
    public static final int MAIN = 0;
    public BlastAppComponent() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * todo Jbuilder set up error, need change permission to private
     * @throws Exception
     */
    public void jbInit() throws Exception {
        super.jbInit();
        pfpFilterBox = new JCheckBox();
        jBasicPane = new JPanel();
        DatabaseLabel = new JLabel();
        jTabbedPane1 = new JTabbedPane();
        jTabbedBlastPane = new JTabbedPane();
        jTabbedHmmPane = new JTabbedPane();
        jTabbedSmPane = new JTabbedPane();
        jServerInfoPane = new ServerInfoPanel();
        jScrollPane4 = new JScrollPane();
        CreateGridServicePanel sgePanel = new CreateGridServicePanel();
        //JComboBox jMatrixBox = new JComboBox();
        JCheckBox lowComplexFilterBox = new JCheckBox();
        JPanel jAdvancedPane = new JPanel();
        JFileChooser jFileChooser1 = new JFileChooser();

        checkboxPanel = new JPanel();
        jDBList = new JList();
        blastButton = new JButton();
        jScrollPane1 = new JScrollPane();
        jProgramBox = new JComboBox();
        filterPanel = new JPanel();
        maskLookupOnlyBox = new JCheckBox();
        expectLabel = new JLabel();
        jExpectBox = new JComboBox();
        matrixLabel = new JLabel();
        blastxSettingPanel = new JPanel();
        jqueryGenericCodeBox = new JComboBox();
        jFrameShiftLabel = new JLabel();
        jFrameShiftPaneltyBox = new JComboBox();
        parameterSetter = new ParameterSetter();

        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jstartPointField = new JTextField();
        jendPointField = new JTextField();
        cardLayout1 = new CardLayout();
        serviceProgressBar = new JProgressBar();
        progressBarPanel1 = new JPanel();
        jLabel5 = new JLabel();
        subSeqPanel1 = new JPanel();
        jPanel6 = new JPanel();
        jLabel6 = new JLabel();
        jProgramBox1 = new JComboBox();
        jLabel7 = new JLabel();
        databaseLabel1 = new JLabel();
        jPanel7 = new JPanel();
        jLabel8 = new JLabel();
        gridBagLayout7 = new GridBagLayout();
        jAlgorithmLabel = new JLabel();
        jendPointField1 = new JTextField();
        jPanel8 = new JPanel();
        jOtherAlgorithemPane = new JPanel();
        gridBagLayout8 = new GridBagLayout();
        flowLayout2 = new FlowLayout();
        jstartPointField1 = new JTextField();
        borderLayout5 = new BorderLayout();
        jPanel9 = new JPanel();
        progressBar1 = new JProgressBar();
        gridBagLayout9 = new GridBagLayout();
        jScrollPane2 = new JScrollPane();
        jList2 = new JList(algorithmParameter);
        gridBagLayout13 = new GridBagLayout();
        borderLayout4 = new BorderLayout();
        jDisplayInWebBox = new JCheckBox();
        borderLayout1 = new BorderLayout();
        gridBagLayout3 = new GridBagLayout();
        jGenericCodeLabel = new JLabel();
        gridBagLayout2 = new GridBagLayout();
        jButton1 = new JButton();
        blastStopButton = new JButton();
        algorithmSearch = new JButton();
        jLabel13 = new JLabel();
        progressBar3 = new JProgressBar();
        jendPointField3 = new JTextField();
        progressBarPanel3 = new JPanel();
        flowLayout4 = new FlowLayout();
        gridBagLayout14 = new GridBagLayout();
        borderLayout8 = new BorderLayout();
        jHMMPane = new JPanel();
        subSeqPanel3 = new JPanel();
        jPanel11 = new JPanel();
        jLabel14 = new JLabel();
        jPanel14 = new JPanel();
        jPanel15 = new JPanel();
        borderLayout9 = new BorderLayout();
        jLabel15 = new JLabel();
        jstartPointField3 = new JTextField();
        jList4 = new JList(hmmParameter);
        jPanel16 = new JPanel();
        jAlgorithmLabel2 = new JLabel();
        jLabel16 = new JLabel();
        flowLayout5 = new FlowLayout();
        jButton2 = new JButton();
        jScrollPane3 = new JScrollPane();
        borderLayout6 = new BorderLayout();
        gridBagLayout4 = new GridBagLayout();

        jToolBar2 = new JToolBar();
        titledBorder1 = new TitledBorder("");
        border1 = BorderFactory.createEtchedBorder(Color.white,
                new Color(165, 163, 151));
        border2 = new TitledBorder(border1,
                                   "Please specify subsequence, program and database");
        jToolBar1 = new JToolBar();

        jLabel9 = new JLabel();
        xYLayout1 = new XYLayout();
        startButtonIcon = new ImageIcon(this.getClass().getResource(
                "start.gif"));
        stopButtonIcon = new ImageIcon(this.getClass().getResource(
                "stop.gif"));
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        borderLayout2 = new BorderLayout();
        xYLayout2 = new XYLayout();
        borderLayout3 = new BorderLayout();

//above is part of code to get rid of npe.
        //sgePanel.setPv(this);
        allArraysCheckBox = new JCheckBox("Activated Sequences", true);
        subSeqPanel = new JPanel();
        subSeqPanel.setBorder(border2);
        jLabel4 = new JLabel();
        jLabel4.setText("jLabel4");
        jLabel9.setText("Program: ");
        jToolBar1.setBorder(null);
        serviceProgressBar.setMinimumSize(new Dimension(10, 26));
        serviceProgressBar.setPreferredSize(new Dimension(104, 26));

        checkboxPanel.setLayout(xYLayout2);
        jBasicPane.setPreferredSize(new Dimension(364, 250));
        jPanel3.setLayout(borderLayout3);
        //this.add(jLabel4, java.awt.BorderLayout.NORTH);
        pfpFilterBox.setToolTipText("Paracel Filtering Package");
        pfpFilterBox.setSelected(true);
        pfpFilterBox.setText("PFP Filter");
        //   jEntThreshBox.addActionListener(new
        //                                  ParameterPanel_jEntThreshBox_actionAdapter(this));
        jBasicPane.setLayout(borderLayout2);
        //jBasicPane.setPreferredSize(new Dimension(10, 100));
        jBasicPane.setMinimumSize(new Dimension(10, 100));
        //jDecreaseDensitySupportBox.setSelectedIndex(0);
        DatabaseLabel.setText("Database:");
        // jServerInfoPane.setPv(this);
        jServerInfoPane.setLayout(cardLayout1);
        //jMatrixBox.setSelectedIndex(0);
        lowComplexFilterBox.setMinimumSize(new Dimension(10, 23));
        lowComplexFilterBox.setMnemonic('0');
        lowComplexFilterBox.setSelected(false);
        lowComplexFilterBox.setText("Low Complexity");
        lowComplexFilterBox.addActionListener(new
                                              BlastAppComponent_lowComplexFilterBox_actionAdapter(this));
        jAdvancedPane.setLayout(gridBagLayout3);
        jDBList.setToolTipText("Select a database");
        jDBList.setVerifyInputWhenFocusTarget(true);
        jDBList.setVisibleRowCount(1);
        jProgramBox.addItem("Select a program");
        jProgramBox.addItem("blastn");
        jProgramBox.addItem("blastp");
        jProgramBox.addItem("blastx");
        jProgramBox.addItem("tblastn");
        jProgramBox.addItem("tblastx");
        blastButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastButton.setHorizontalAlignment(SwingConstants.LEFT);
        blastButton.setHorizontalTextPosition(SwingConstants.CENTER);
        //blastButton.setText("BLAST");
        blastButton.setIcon(startButtonIcon);
//         blastButton.setMaximumSize(new Dimension(26, 26));
//      blastButton.setMinimumSize(new Dimension(26, 26));
//      blastButton.setPreferredSize(new Dimension(26, 26));
        blastButton.setToolTipText("Start BLAST");

        blastButton.addActionListener(new
                                      BlastAppComponent_blastButton_actionAdapter(this));
        jTabbedPane1.setDebugGraphicsOptions(0);
        jTabbedPane1.setMinimumSize(new Dimension(5, 5));
        jProgramBox.addActionListener(new
                                      BlastAppComponent_jProgramBox_actionAdapter(this));
        jMatrixBox = new JComboBox();
        jMatrixBox.addActionListener(new
                                     BlastAppComponent_jMatrixBox_actionAdapter(this));
        // this.setLayout(borderLayout1);
        maskLookupOnlyBox.setText("Mask the lookup table.");
        maskLookupOnlyBox.setMinimumSize(new Dimension(5, 23));
        maskLookupOnlyBox.setMnemonic('0');
        maskLookupOnlyBox.setSelected(false);
        expectLabel.setText("Matrix:");
        jMatrixBox.addItem("dna.mat");
        jMatrixBox.setToolTipText("Select the Matrix.");
        jMatrixBox.setVerifyInputWhenFocusTarget(true);
        jMatrixBox.setSelectedIndex(0);

        jExpectBox.setSelectedIndex( -1);
        jExpectBox.setVerifyInputWhenFocusTarget(true);
        jExpectBox.setToolTipText("Select the expect value here.");
        // jExpectBox.addActionListener(new BlastAppComponent_jExpectBox_actionAdapter(this));
        matrixLabel.setText("Expect:");
        jqueryGenericCodeBox.setSelectedItem("10");

        jqueryGenericCodeBox.setSelectedIndex( -1);
        jqueryGenericCodeBox.setVerifyInputWhenFocusTarget(true);
        jqueryGenericCodeBox.setToolTipText("Select the Generic Code.");
        //jqueryGenericCodeBox.addActionListener(new BlastAppComponent_jqueryGenericCodeBox_actionAdapter(this));
        jFrameShiftLabel.setText("Frame shift penalty:");
        jFrameShiftPaneltyBox.setToolTipText(
                "Select the panelty of FrameShift.");
        jFrameShiftPaneltyBox.setVerifyInputWhenFocusTarget(true);
        jFrameShiftPaneltyBox.setSelectedIndex( -1);
        jFrameShiftPaneltyBox.setSelectedIndex( -1);
        jFrameShiftPaneltyBox.setSelectedItem(null);
        jFrameShiftPaneltyBox.addActionListener(new
                                                BlastAppComponent_jFrameShiftPaneltyBox_actionAdapter(this));
        blastxSettingPanel.setLayout(gridBagLayout2);
        jServerInfoPane.setMinimumSize(new Dimension(0, 0));
        //jServerInfoPane.setPreferredSize(new Dimension(0, 0));
        jServerInfoPane.setToolTipText("Blast server Info");
        jProgramBox.setAutoscrolls(false);
        jProgramBox.setMinimumSize(new Dimension(26, 21));
        //jProgramBox.setPreferredSize(new Dimension(26, 21));
        subSeqPanel.setLayout(xYLayout1);
        jLabel1.setText("to ");
        jLabel2.setText("Subsequence: From");
        jLabel3.setText("Subsequence: ");
        jstartPointField.setText("1");
        jendPointField.setText("");
        progressBarPanel1.setMinimumSize(new Dimension(10, 16));
        progressBarPanel1.setLayout(borderLayout5);
        jLabel5.setAlignmentY((float) 0.5);
        jLabel5.setMinimumSize(new Dimension(5, 15));
        jLabel5.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabel5.setText("Please specify subsequence, database and program .");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        subSeqPanel1.setLayout(gridBagLayout9);
        jPanel6.setLayout(flowLayout2);
        jLabel6.setText("to ");
//    jProgramBox1.addActionListener(new
        //                                BlastAppComponent_jProgramBox1_actionAdapter(this));
        jProgramBox1.setAutoscrolls(false);
        jProgramBox1.setMinimumSize(new Dimension(26, 21));
        //jProgramBox1.setPreferredSize(new Dimension(26, 21));
        jLabel7.setText("Subsequence: ");
        databaseLabel1.setToolTipText("");
        databaseLabel1.setText("Database:");
        jPanel7.setLayout(gridBagLayout7);
        jLabel8.setText("From");
        jAlgorithmLabel.setText("Algorithms:");
        jendPointField1.setText("end");
        jOtherAlgorithemPane.setLayout(gridBagLayout8);
//    jOtherAlgorithemPane.setPreferredSize(new Dimension(10, 100));
        jOtherAlgorithemPane.setMinimumSize(new Dimension(10, 100));
        jstartPointField1.setText("1");
        jPanel9.setLayout(gridBagLayout13);
        progressBar1.setOrientation(JProgressBar.HORIZONTAL);
        progressBar1.setBorder(BorderFactory.createEtchedBorder());
        progressBar1.setStringPainted(true);
        jPanel8.setLayout(borderLayout4);

//    jProgramBox2.setPreferredSize(new Dimension(26, 21));
//    jBasicPane1.setPreferredSize(new Dimension(10, 100));
        //   blastButton2.addActionListener(new
        //                                 BlastAppComponent_blastButton2_actionAdapter(this));
        jAdvancedPane.setMinimumSize(new Dimension(5, 25));
        filterPanel.setMinimumSize(new Dimension(5, 10));
        blastxSettingPanel.setMinimumSize(new Dimension(5, 115));
        subSeqPanel1.setMinimumSize(new Dimension(10, 30));
        jPanel8.setMinimumSize(new Dimension(5, 15));
        jPanel7.setPreferredSize(new Dimension(5, 46));
        jPanel9.setPreferredSize(new Dimension(5, 93));
        jList2.setMaximumSize(new Dimension(209, 68));
        jList2.setMinimumSize(new Dimension(100, 68));
        jDisplayInWebBox.setMinimumSize(new Dimension(10, 23));
        jDisplayInWebBox.setSelected(true);
        jDisplayInWebBox.setText("Display result in your web browser");
        jGenericCodeLabel.setToolTipText("");
        jGenericCodeLabel.setText("Query genetic code:");
        jButton1.setFont(new java.awt.Font("Arial Black", 0, 11));
        jButton1.setIcon(stopButtonIcon);
        //jButton1.setText("STOP");

        jButton1.addActionListener(new
                                   BlastAppComponent_jButton1_actionAdapter(this));
        blastStopButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastStopButton.setVerifyInputWhenFocusTarget(true);
        //blastStopButton.setText("STOP");
        blastStopButton.setIcon(stopButtonIcon);
        blastStopButton.setToolTipText("Stop the Query");

        blastStopButton.addActionListener(new
                                          BlastAppComponent_blastStopButton_actionAdapter(this));
        algorithmSearch.setFont(new java.awt.Font("Arial Black", 0, 11));
        //algorithmSearch.setText("SEARCH");
        algorithmSearch.setIcon(startButtonIcon);
        algorithmSearch.addActionListener(new
                                          BlastAppComponent_algorithmSearch_actionAdapter(this));
        jLabel13.setText("From");
        progressBar3.setStringPainted(true);
        progressBar3.setBorder(BorderFactory.createEtchedBorder());
        progressBar3.setOrientation(JProgressBar.HORIZONTAL);
        jendPointField3.setText("end");
        progressBarPanel3.setLayout(borderLayout9);
        progressBarPanel3.setMinimumSize(new Dimension(10, 16));
        jHMMPane.setLayout(gridBagLayout4);
        jHMMPane.setDebugGraphicsOptions(0);
        jHMMPane.setMinimumSize(new Dimension(10, 100));
        subSeqPanel3.setLayout(gridBagLayout14);
        subSeqPanel3.setMinimumSize(new Dimension(10, 30));
        jPanel11.setLayout(borderLayout8);
        jPanel11.setMinimumSize(new Dimension(5, 15));
        jLabel14.setText("Subsequence: ");
        jPanel14.setLayout(flowLayout5);
        jPanel14.setPreferredSize(new Dimension(5, 46));
        jPanel15.setLayout(flowLayout4);
        jLabel15.setText("to ");
        jstartPointField3.setText("1");
        jList4.setMaximumSize(new Dimension(209, 68));
        jList4.setMinimumSize(new Dimension(100, 68));
        jPanel16.setLayout(borderLayout6);
        jPanel16.setPreferredSize(new Dimension(5, 93));
        jAlgorithmLabel2.setText("Search Mode:");
        jLabel16.setAlignmentY((float) 0.5);
        jLabel16.setMinimumSize(new Dimension(5, 15));
        jLabel16.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabel16.setText("Please specify subsequence, search mode.");
        jLabel16.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.setFont(new java.awt.Font("Arial Black", 0, 11));
        //jButton2.setText("HMM SEARCH");
        jButton2.setIcon(startButtonIcon);
        jButton2.addActionListener(new
                                   BlastAppComponent_jButton2_actionAdapter(this));
        blastxSettingPanel.add(jExpectBox,
                               new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jFrameShiftPaneltyBox,
                               new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jqueryGenericCodeBox,
                               new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jFrameShiftLabel,
                               new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(matrixLabel,
                               new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jMatrixBox,
                               new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(expectLabel,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jGenericCodeLabel,
                               new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        jAdvancedPane.add(filterPanel,
                          new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), -150,
                                                 82));

//        jTabbedHmmPane.add(jHMMPane, "HMM");
//        jTabbedHmmPane.add(jAdvancedPane, "Advanced Options");
        jToolBar2.add(serviceProgressBar);
        serviceProgressBar.setOrientation(JProgressBar.HORIZONTAL);
        serviceProgressBar.setBorder(BorderFactory.createEtchedBorder());
        serviceProgressBar.setStringPainted(true);
        //jPanel6.add(algorithmSearch, null);
        //jPanel6.add(jButton1, null);
        jOtherAlgorithemPane.add(progressBarPanel1,
                                 new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(1, -3, 0, 0), 247, 2));
        progressBarPanel1.add(progressBar1, BorderLayout.SOUTH);
        jOtherAlgorithemPane.add(jPanel8,
                                 new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 390, 18));
        jPanel8.add(jLabel5, BorderLayout.CENTER);
        jOtherAlgorithemPane.add(jPanel7,
                                 new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 146, -13));
        jOtherAlgorithemPane.add(subSeqPanel1,
                                 new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 14), 80, -1));
        subSeqPanel1.add(jLabel8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 17, 0, 0), 0, 10));
        subSeqPanel1.add(jstartPointField1,
                         new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 18, 0, 0), 14, 4));
        subSeqPanel1.add(jLabel6, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        subSeqPanel1.add(jendPointField1,
                         new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 36, 0, 49), 6, 4));
        subSeqPanel1.add(jLabel7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        jOtherAlgorithemPane.add(jPanel6,
                                 new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(7, -3, 0, 0), 312, -5));
        jPanel7.add(databaseLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(13, 8, 13, 0), 23, 5));
        jPanel7.add(jProgramBox1, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(13, 0, 13, 0), 131, -1));
        jOtherAlgorithemPane.add(jPanel9,
                                 new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 329, 81));
        jPanel9.add(jAlgorithmLabel,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                           , GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 347, 0));
        jPanel9.add(jScrollPane2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 59), 78, -61));
        jTabbedPane1.add(jTabbedBlastPane, "BLAST");
        jTabbedPane1.add(jHMMPane, "HMM");
        jScrollPane2.getViewport().add(jList2, null);

        subSeqPanel3.add(jLabel13, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 17, 0, 0), 0, 10));
        subSeqPanel3.add(jstartPointField3,
                         new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 18, 0, 0), 14, 4));
        subSeqPanel3.add(jLabel15, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        subSeqPanel3.add(jendPointField3,
                         new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 36, 0, 49), 6, 4));
        subSeqPanel3.add(jLabel14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        jTabbedPane1.add(jOtherAlgorithemPane, "Other Algorithms");
        jHMMPane.add(progressBarPanel3,
                     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                            , GridBagConstraints.CENTER,
                                            GridBagConstraints.BOTH,
                                            new Insets(0, 0, 0, 0), 291, -2));
        progressBarPanel3.add(progressBar3, BorderLayout.SOUTH);
        jHMMPane.add(jPanel14, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 390, -18));
        jHMMPane.add(jPanel16, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 386, 1));
        jPanel16.add(jScrollPane3, BorderLayout.CENTER);
        jPanel16.add(jAlgorithmLabel2, BorderLayout.NORTH);
        jHMMPane.add(jPanel15, new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(7, 0, 3, 0), 192, 3));
        //HMM button removed.
        //jPanel15.add(jButton2, null);
        jHMMPane.add(jPanel11, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 183, 26));
        jPanel11.add(jLabel16, BorderLayout.CENTER);
        jHMMPane.add(subSeqPanel3, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 98, 7));
        jScrollPane3.getViewport().add(jList4, null);
        filterPanel.add(pfpFilterBox, null);
        filterPanel.add(lowComplexFilterBox, null);
        filterPanel.add(maskLookupOnlyBox, null);
        filterPanel.add(jDisplayInWebBox, null);
        jTabbedBlastPane.add(jBasicPane, "Main");
        jTabbedBlastPane.add(jAdvancedPane, "Advanced Options");
        jTabbedBlastPane.add(jServerInfoPane, "Service");
        //jTabbedBlastPane.add(sgePanel, "Grid Services");

        jAdvancedPane.add(blastxSettingPanel,
                          new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(10, 10, 0, 0), 0, 7));
        mainPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        // mainPanel.add(checkboxPanel, BorderLayout.SOUTH);
        jToolBar1.add(jLabel2);
        jToolBar1.add(jstartPointField);
        jToolBar1.add(jLabel1);
        jToolBar1.add(jendPointField);
        subSeqPanel.add(jScrollPane1, new XYConstraints(0, 89, 352, 97));
        subSeqPanel.add(jLabel9, new XYConstraints(0, 36, 60, 23));
        subSeqPanel.add(DatabaseLabel, new XYConstraints(0, 59, 61, 23));
        subSeqPanel.add(jProgramBox, new XYConstraints(84, 36, 267, 25)); //edit for new class.
        subSeqPanel.add(jToolBar1, new XYConstraints( -1, 0, 353, 27));
        displayToolBar.add(Box.createHorizontalStrut(10), null);
        displayToolBar.add(blastButton);
        displayToolBar.add(Box.createHorizontalStrut(5), null);
        displayToolBar.add(blastStopButton);
        jScrollPane4.getViewport().add(jPanel3);
        jPanel3.add(subSeqPanel, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jScrollPane4, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jToolBar2, java.awt.BorderLayout.NORTH);
        jExpectBox.addItem("10");
        jExpectBox.addItem("1");

        jExpectBox.addItem("0.1");
        jExpectBox.addItem("0.01");
        jExpectBox.addItem("100");

        jExpectBox.addItem("1000");
        jqueryGenericCodeBox.addItem("Standard");
        jqueryGenericCodeBox.addItem("Vertebrate Mitochondrial");
        jqueryGenericCodeBox.addItem("Yeast Mitochondrial ");
        jqueryGenericCodeBox.addItem("Invertebrate Mitochondrial ");
        jqueryGenericCodeBox.addItem("Echinoderm Mitochondrial ");
        jqueryGenericCodeBox.addItem("Euplotid Nuclear ");
        jFrameShiftPaneltyBox.addItem("NO OOF");
        jFrameShiftPaneltyBox.addItem("6");
        jFrameShiftPaneltyBox.addItem("7");
        jFrameShiftPaneltyBox.addItem("8");
        jFrameShiftPaneltyBox.addItem("9");
        jFrameShiftPaneltyBox.addItem("10");
        jFrameShiftPaneltyBox.addItem("11");
        jFrameShiftPaneltyBox.addItem("12");
        jFrameShiftPaneltyBox.addItem("13");
        jFrameShiftPaneltyBox.addItem("14");
        jFrameShiftPaneltyBox.addItem("15");

        jFrameShiftPaneltyBox.addItem("16");
        jFrameShiftPaneltyBox.addItem("17");
        jFrameShiftPaneltyBox.addItem("18");
        jFrameShiftPaneltyBox.addItem("19");
        jFrameShiftPaneltyBox.addItem("20");
        jFrameShiftPaneltyBox.addItem("25");
        jFrameShiftPaneltyBox.addItem("50");
        jFrameShiftPaneltyBox.addItem("100");

        jFrameShiftPaneltyBox.addItem("1000");
        jTabbedPane1.setSelectedComponent(jTabbedBlastPane);
        jTabbedBlastPane.setSelectedComponent(jBasicPane);
        /*
             jProgramBox1.addItem("Smith-Waterman DNA");
             jProgramBox1.addItem("Smith-Waterman Protein");
             jProgramBox1.addItem("Frame (for DNA sequece to protein DB)");
         jProgramBox1.addItem("Reverse Frame (for protein sequecne to protein DB)");
             jProgramBox1.addItem("Double Frame (for DNA sequence to DNA DB)");
         */

        jProgramBox1.addItem("ecoli.nt");
        jProgramBox1.addItem("pdb.nt");
        jProgramBox1.addItem("pdbaa");
        jProgramBox1.addItem("yeast.aa");
        jProgramBox1.addItem("nr");

    }

    void jProgramBox_actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();

        // Get the new item

        //System.out.println(newItem + "selected the program" + e.getActionCommand());
        String selectedProgramName = (String) cb.getSelectedItem();
        if (selectedProgramName != null) {
            jDBList = new JList(AlgorithmMatcher.translateToArray((String)
                    selectedProgramName));
            (jScrollPane1.getViewport()).add(jDBList, null);
            String[] model = AlgorithmMatcher.translateToMatrices(
                    selectedProgramName);
            jMatrixBox.setModel(new DefaultComboBoxModel(model));
        }
        //jScrollPanel = new JScrollPanel(jDBList);
        // repaint();
    }

    void jMatrixBox_actionPerformed(ActionEvent e) {

    }

    void jFrameShiftPaneltyBox_actionPerformed(ActionEvent e) {

    }

    void lowComplexFilterBox_actionPerformed(ActionEvent e) {

    }

    public CSSequenceSet getFastaFile() {
        return fastaFile;
    }

    public void setBlastAppComponent(BlastAppComponent appComponent) {
        blastAppComponent = appComponent;

    }

    public void setFastaFile(CSSequenceSet sd) {
        fastaFile = sd;
        int endPoint = 0;
        if (sd != null) {
//            for (Object o : sd) {
//                int newPoint = ((CSSequence) o).length();
//                if (endPoint < newPoint) {
//                    endPoint = newPoint;
//                }
//            }
            endPoint = sd.getMaxLength();
            jendPointField.setText(new Integer(endPoint).toString());
            jendPointField1.setText(new Integer(endPoint).toString());
            jendPointField3.setText(new Integer(endPoint).toString());

        }
    }

    public void reportError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    public ParameterSetter retriveNCBIParameters() {
        ParameterSetting parameterSetting = new ParameterSetting();
        String dbName = (String) jDBList.getSelectedValue();

        String programName = (String) jProgramBox.getSelectedItem();

        if (dbName == null) {
            reportError("Please select a DATABASE to search!",
                        "Parameter Error");
            return null;

        } else {
            //get the last part of db name
            String[] list = dbName.split("/");
            if (list.length > 1) {
                String[] dbNameWithSuffix = list[list.length - 1].split(" ");
                dbName = dbNameWithSuffix[0];
            } else {

            }
        }
        if (programName == null) {
            reportError("Please select a PROGRAM to search!", "Parameter Error");
            return null;
        }
        parameterSetting.setDbName(dbName);
        parameterSetting.setProgramName(programName);
        parameterSetting.setViewInBrowser(jDisplayInWebBox.isSelected());

        if (activeSequenceDB != null) {
            if (sequenceDB == null) {
                reportError("Please select a sequence file first!",
                            "Parameter Error");
                return null;
            } else { //to handle new sequenceDB.

                try {
                    String tempFolder = System.getProperties().getProperty(
                            "temporary.files.directory");
                    if (tempFolder == null) {
                        tempFolder = ".";

                    }

                    String outputFile = tempFolder + "Blast" +
                                        RandomNumberGenerator.getID() +
                                        ".html";
                    //progressBar = new JProgressBar(0, 100);

                    serviceProgressBar.setForeground(Color.BLUE);
                    serviceProgressBar.setBackground(Color.WHITE);

//                    serviceProgressBar.setIndeterminate(true);
//                    serviceProgressBar.setString(
//                            "Please wait for response from NCBI BLAST Server...");
                    updateProgressBar(10, "Wait...");
                    if (fastaFile == null && activeSequenceDB != null) {
                        fastaFile = (CSSequenceSet) activeSequenceDB;
                    }
                    SoapClient sc = new SoapClient(programName, dbName,
                            outputFile);

//                    //sc.setSequenceDB((CSSequenceSet) sequenceDB);
                    sc.setSequenceDB(activeSequenceDB);
                    BlastAlgorithm blastAlgo = new BlastAlgorithm();
                    blastAlgo.setUseNCBI(true);
                    blastAlgo.setParameterSetting(parameterSetting);

                    //       blastAlgo.setBlastAppComponent(blastAppComponent);
                    blastAlgo.setBlastAppComponent(this);
                    blastAlgo.setSoapClient(sc);
//                    blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
                    blastAlgo.start();
                    Thread.sleep(5);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return parameterSetter;

    }

    public ParameterSetter retriveParameters() {

        String dbName = (String) jDBList.getSelectedValue();

        String programName = (String) jProgramBox.getSelectedItem();

        if (dbName == null) {
            reportError("Please select a DATABASE to search!",
                        "Parameter Error");
            return null;

        } else {
            StringTokenizer st = new StringTokenizer(dbName);
            dbName = st.nextToken();

        }
        if (programName == null) {
            reportError("Please select a PROGRAM to search!", "Parameter Error");
            return null;
        }

        if (fastaFile == null) {
            if (sequenceDB == null) {
                reportError("Please select a sequence file first!",
                            "Parameter Error");
                return null;
            } else { //to handle new sequenceDB.
                jServerInfoPane.retriveServerInfo();
                try {
                    String tempFolder = System.getProperties().getProperty(
                            "temporary.files.directory");
                    if (tempFolder == null) {
                        tempFolder = ".";

                    }

                    String outputFile = tempFolder + "Blast" +
                                        RandomNumberGenerator.getID() +
                                        ".html";
                    //progressBar = new JProgressBar(0, 100);

                    serviceProgressBar.setForeground(Color.ORANGE);
                    serviceProgressBar.setBackground(Color.WHITE);

                    serviceProgressBar.setIndeterminate(true);
                    serviceProgressBar.setString("Blast is running.");
                    if (fastaFile == null) {
                        fastaFile = (CSSequenceSet) sequenceDB;
                    }
                    SoapClient sc = new SoapClient(programName, dbName,
                            outputFile);
                    //sc.setSequenceDB((CSSequenceSet) sequenceDB);
                    sc.setSequenceDB(activeSequenceDB);
                    BlastAlgorithm blastAlgo = new BlastAlgorithm();
                    // blastAlgo.setBlastAppComponent(blastAppComponent);
                    blastAlgo.setBlastAppComponent(this);
                    blastAlgo.setSoapClient(sc);
                    blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
                    blastAlgo.start();
                    Thread.sleep(5);
                    //System.out.println("WRONG at PVW: " + parameterSetter + "algo" + blastAlgo);
                    if (blastAlgo != null) {
                        parameterSetter.setAlgo(blastAlgo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            jServerInfoPane.retriveServerInfo();

            try {
                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";

                }

                String outputFile = tempFolder + "Blast" +
                                    RandomNumberGenerator.getID() +
                                    ".html";
                serviceProgressBar.setForeground(Color.ORANGE);
                serviceProgressBar.setBackground(Color.WHITE);
                serviceProgressBar.setIndeterminate(true);
                serviceProgressBar.setString("Blast is running.");
                if (fastaFile == null) {
                    fastaFile = activeSequenceDB;
                }
                SoapClient sc = new SoapClient(programName, dbName,
                                               outputFile);
                BlastAlgorithm blastAlgo = new BlastAlgorithm();
                sc.setSequenceDB(activeSequenceDB);
                //blastAlgo.setBlastAppComponent(blastAppComponent);
                blastAlgo.setBlastAppComponent(this);
                blastAlgo.setSoapClient(sc);
                blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
                blastAlgo.start();
                Thread.sleep(2);
                if (blastAlgo != null && parameterSetter != null) {
                    parameterSetter.setAlgo(blastAlgo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return parameterSetter;

    }

    public void retriveAlgoParameters() {

        if (jTabbedPane1.getSelectedIndex() == this.SW) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }
        if (jTabbedPane1.getSelectedIndex() == this.HMM) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }
        if (fastaFile == null && sequenceDB == null) {
            reportError("Please load a sequence file first!",
                        "No File Error");
            return;

        }
        String algoTitle = (String) jList2.getSelectedValue();
        if (algoTitle == null) {
            reportError("Please select a algorithm to search!",
                        "Parameter Error");
            return;

        }

        String algoName = AlgorithmMatcher.translate(algoTitle);

        String dbName = (String) jProgramBox1.getSelectedItem();

        if (dbName == null) {
            reportError("Please select a database name first!",
                        "No Database Error");
            return;

        }

        String matrix = (String) jMatrixBox.getSelectedItem();
//        if (matrix == null) {
//            reportError("Please select a matrix name first!",
//                        "No Matrix Error");
//            return;
//
//        }

        if (jTabbedPane1.getSelectedIndex() == this.SW) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }
        if (jTabbedPane1.getSelectedIndex() == this.HMM) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }

        //System.out.println("fasta file path: " + fastaFile);

        try {

            String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";

            }
            String outputFile = tempFolder + "Algo" +
                                RandomNumberGenerator.getID() +
                                ".html";
            // System.out.println(outputFile + " outputfile");
            //progressBar = new JProgressBar(0, 100);

            progressBar1.setForeground(Color.ORANGE);
            progressBar1.setBackground(Color.WHITE);

            progressBar1.setIndeterminate(true);
            progressBar1.setString(algoTitle + " is running.");

            SoapClient sc = new SoapClient(algoName, dbName, matrix,
                                           fastaFile.getFASTAFileName().
                                           trim(),
                                           outputFile);
            BlastAlgorithm blastAlgo = new BlastAlgorithm();
            blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
            blastAlgo.setBlastAppComponent(blastAppComponent);
            blastAlgo.setSoapClient(sc);
            blastAlgo.start();
            Thread.sleep(5);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * blastFinished
     * Take care of the state of finished blast.
     */
    public void blastFinished(String cmd) {
        Date finished_Date = new Date();

        if (cmd.startsWith("Interrupted")) {
            serviceProgressBar.setIndeterminate(false);

            serviceProgressBar.setForeground(Color.ORANGE);
            serviceProgressBar.setBackground(Color.ORANGE);
            serviceProgressBar.setString("Stopped on " + finished_Date);

        } else if (cmd.startsWith("OTHERS_Interrupted")) {
            progressBar1.setIndeterminate(false);

            progressBar1.setForeground(Color.ORANGE);
            progressBar1.setBackground(Color.ORANGE);
            progressBar1.setString("Stopped on " + finished_Date);

        } else {

            if (cmd.startsWith("pb")) {

                serviceProgressBar.setIndeterminate(false);

                serviceProgressBar.setForeground(Color.ORANGE);
                serviceProgressBar.setBackground(Color.ORANGE);
                serviceProgressBar.setString("Finished on " + finished_Date);
            } else if (cmd.startsWith("btk search")) {
                progressBar1.setIndeterminate(false);

                progressBar1.setForeground(Color.ORANGE);
                progressBar1.setBackground(Color.ORANGE);
                progressBar1.setString("Finished on " + finished_Date);

            } else if (cmd.startsWith("btk hmm")) {
                progressBar3.setIndeterminate(false);

                progressBar3.setForeground(Color.ORANGE);
                progressBar3.setBackground(Color.ORANGE);
                progressBar3.setString("Finished on " + finished_Date);

            }

        }
    }

    void blastButton_actionPerformed(ActionEvent e) {
//        System.out.println("thenumber=" + jTabbedPane1.getSelectedIndex());
        if (jTabbedPane1.getSelectedIndex() == this.BLAST) {
            jTabbedBlastPane.setSelectedIndex(this.MAIN);
            if (jServerInfoPane.getServerType() ==
                ServerInfoPanel.DEFAULTSERVERTYPE) {
                parameterSetter = retriveParameters();
            } else if (jServerInfoPane.getServerType() == ServerInfoPanel.NCBI) {
                parameterSetter = retriveNCBIParameters();

            }
        } else {
            retriveAlgoParameters();
        }
        //Session session = createSession(parameter);
        //session.start();

        /* try{
           BrowserLauncher.openURL("c:/data/status.html");
         }catch (IOException ex){ex.printStackTrace();}
         */
    }

    void blastButton1_actionPerformed(ActionEvent e) {
        //System.out.println("run");
        //retriveParameters();
        //retriveAlgoParameters();

    }

    void blastButton2_actionPerformed(ActionEvent e) {
        //System.out.println("stop");
        //stopBlastAction();

    }

    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
        setFastaFile(activeSequenceDB);
    }

    void stopBlastAction() {
        blastFinished("Interrupted");
        if (parameterSetter != null) {

            BWAbstractAlgorithm algo = parameterSetter.getAlgo();
            if (algo != null) {
                algo.stop();
            }
        }
    };

    void jButton1_actionPerformed(ActionEvent e) {
        //System.out.println("jbutton1");
        // retriveAlgoParameters();
        blastFinished("OTHERS_Interrupted");

    }

    void blastStopButton_actionPerformed(ActionEvent e) {
        stopBlastAction();
    }

    void algorithmSearch_actionPerformed(ActionEvent e) {
        try {
            retriveAlgoParameters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jButton7_actionPerformed(ActionEvent e) {
        try {
            BrowserLauncher.openURL("http://pfam.wustl.edu/browse.shtml");
        } catch (IOException ex) {
            reportError(ex.getMessage(), "Connection Error");

        }
    }

    public void updateProgressBar(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    serviceProgressBar.setString(text);
                    serviceProgressBar.setValue((int) (percent * 100));
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void updateProgressBar(final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    serviceProgressBar.setString(text);
                    serviceProgressBar.setIndeterminate(true);
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void updateProgressBar(final boolean boo, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    serviceProgressBar.setString(text);
                    serviceProgressBar.setIndeterminate(boo);
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }


    void jButton2_actionPerformed(ActionEvent e) {
        if (fastaFile == null || fastaFile.isDNA()) {
            reportError("Please select a PROTEIN sequence file first.",
                        "MisMatch Error");
            return;
        }
        String algoTitle = (String) jList4.getSelectedValue();
        if (algoTitle == null) {
            reportError("Please select a Pfam model first.", "Null Parameter.");
            return;
        }
        String query = AlgorithmMatcher.translate(algoTitle);

        try {

            String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = "./";

            }
            String outputFile = tempFolder + "Hmm" +
                                RandomNumberGenerator.getID() +
                                ".txt";
            //System.out.println(outputFile + " outputfile");
            //progressBar = new JProgressBar(0, 100);

            progressBar3.setForeground(Color.ORANGE);
            progressBar3.setBackground(Color.WHITE);

            progressBar3.setIndeterminate(true);
            progressBar3.setString(algoTitle + " is running.");

            SoapClient sc = new SoapClient(query, null, null,
                                           fastaFile.getFASTAFileName().trim(),
                                           outputFile);
            BlastAlgorithm blastAlgo = new BlastAlgorithm();
            blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
            blastAlgo.setBlastAppComponent(blastAppComponent);
            blastAlgo.setSoapClient(sc);
            blastAlgo.start();
            Thread.sleep(5);

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    /**
     * createGridDialog
     */
    public void createGridDialog() {

        CreateGridServiceDialog csd = new CreateGridServiceDialog(null,
                "grid service");

    }

    void jButton6_actionPerformed(ActionEvent e) {
        //loadFile();
        String textFile =
                "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Hmm89547134.txt";
        String inputfile =
                "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Hmm89547134.txt";
        HMMDataSet blastResult = new HMMDataSet(textFile,
                                                inputfile, null);
        try {

//add twice blastDataSet. change!@ ???
            ProjectNodeAddedEvent event = new ProjectNodeAddedEvent("message", null,
                    blastResult);
            blastAppComponent.publishProjectNodeAddedEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * loadFile
     */
    public void loadFile() {
        if (JFileChooser.APPROVE_OPTION ==
            jFileChooser1.showOpenDialog(mainPanel)) {
            // Call openFile to attempt to load the text from file into TextArea
            openFile(jFileChooser1.getSelectedFile().getPath());
        }
        mainPanel.repaint();

    }

    void openFile(String fileName) {
        try {
            // Open a file of the given name.
            File file = new File(fileName);

            // Get the size of the opened file.
            int size = (int) file.length();

            // Set to zero a counter for counting the number of
            // characters that have been read from the file.
            int chars_read = 0;

            // Create an input reader based on the file, so we can read its data.
            // FileReader handles international character encoding conversions.
            FileReader in = new FileReader(file);

            // Create a character array of the size of the file,
            // to use as a data buffer, into which we will read
            // the text data.
            char[] data = new char[size];

            // Read all available characters into the buffer.
            while (in.ready()) {
                // Increment the count for each character read,
                // and accumulate them in the data buffer.
                chars_read += in.read(data, chars_read, size - chars_read);
            }
            in.close();

            // jTextArea1.setText(new String(data, 0, chars_read));

//   jList4.add("your own model", null);
            // Display the name of the opened directory+file in the statusBar.
            //  statusBar.setText("Opened " + fileName);
            //  updateCaption();
        } catch (IOException e) {
            //statusBar.setText("Error opening " + fileName);
        }
    }

    public BlastAppComponent getBlastAppComponent() {
        return blastAppComponent;
    }

    /**
     * publishProjectNodeAddedEvent
     *
     * @param event ProjectNodeAddedEvent
     */
    @Publish public org.geworkbench.events.ProjectNodeAddedEvent
            publishProjectNodeAddedEvent(org.geworkbench.events.
                                         ProjectNodeAddedEvent event) {
        return event;
    }

}


class BlastAppComponent_jProgramBox_actionAdapter implements java.awt.
        event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jProgramBox_actionAdapter(BlastAppComponent
                                                adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jProgramBox_actionPerformed(e);
    }
}


class BlastAppComponent_jMatrixBox_actionAdapter implements java.awt.
        event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jMatrixBox_actionAdapter(BlastAppComponent
                                               adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMatrixBox_actionPerformed(e);
    }
}


class BlastAppComponent_jFrameShiftPaneltyBox_actionAdapter implements
        java.awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jFrameShiftPaneltyBox_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jFrameShiftPaneltyBox_actionPerformed(e);
    }
}


class BlastAppComponent_lowComplexFilterBox_actionAdapter implements
        java.awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_lowComplexFilterBox_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.lowComplexFilterBox_actionPerformed(e);
    }
}


class BlastAppComponent_blastButton_actionAdapter implements java.awt.
        event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_blastButton_actionAdapter(BlastAppComponent
                                                adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.blastButton_actionPerformed(e);
    }
}


/*
 class BlastAppComponent_blastButton1_actionAdapter implements java.awt.event.ActionListener {
  BlastAppComponent adaptee;

  BlastAppComponent_blastButton1_actionAdapter(BlastAppComponent adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.blastButton1_actionPerformed(e);
  }
 }
 */

class BlastAppComponent_jButton1_actionAdapter implements java.awt.event.
        ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jButton1_actionAdapter(BlastAppComponent
                                             adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton1_actionPerformed(e);
    }
}


class BlastAppComponent_blastStopButton_actionAdapter implements java.
        awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_blastStopButton_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.blastStopButton_actionPerformed(e);
    }
}


class BlastAppComponent_algorithmSearch_actionAdapter implements java.
        awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_algorithmSearch_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.algorithmSearch_actionPerformed(e);
    }
}


class BlastAppComponent_jButton2_actionAdapter implements java.awt.event.
        ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jButton2_actionAdapter(BlastAppComponent
                                             adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton2_actionPerformed(e);
    }
}
