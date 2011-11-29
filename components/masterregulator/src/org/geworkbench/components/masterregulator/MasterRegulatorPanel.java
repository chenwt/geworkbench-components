package org.geworkbench.components.masterregulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.parsers.AdjacencyMatrixFileFormat;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.Util;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Parameter Panel used for Master Regulator Analysis
 * 
 * @author yc2480
 * @version $Id$
 */
public final class MasterRegulatorPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -6160058089960168299L;

	private static final float PValueThresholdDefault = 0.01f;
	// private static final String TFGeneListDefault =
	// ("AFFX-HUMGAPDH/M33197_3_at, AFFX-HUMGAPDH/M33197_5_at,
	// AFFX-HUMGAPDH/M33197_M_at, AFFX-HUMRGE/M10098_3_at,
	// AFFX-HUMRGE/M10098_M_at");
	private static final String TFGeneListDefault = "";
	private static final String[] DEFAULT_SET = { " " };

	private Log log = LogFactory.getLog(this.getClass());
	private ArrayListModel<String> adjModel; 

	private JTextField pValueTextField = null;
	private JTextField TFGeneListTextField = null; // Marker 1, Marker 2...
	private JTextField networkTextField = null;
	private JTextField sigGeneListTextField = null;
	private HashMap<String, AdjacencyMatrixDataSet> adjMatrix = new HashMap<String, AdjacencyMatrixDataSet>();
	private DSMicroarraySet maSet = null;

	private JComboBox networkMatrix = createNetworkMatrixComboBox();
	private JComboBox tfGroups = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));
	private JComboBox sigGroups = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));;
	private JButton loadNetworkButton = new JButton("Load");
	private JButton loadTFButton = new JButton("Load");
	private JButton loadSigButton = new JButton("Load");
	private JComboBox networkFrom = null;
	private JComboBox tfFrom = null;
	private JComboBox sigFrom = null;
	private JTextField mintg = new JTextField("20");  //minimum number of targets to run GSEA
	private JTextField minsp = new JTextField("6");  //minimum number of samples for label shuffling
	private JTextField nperm = new JTextField("1000"); //number of permutations
	private JTextField tail = new JTextField("2");   //tail: If the Spearman's correlation value is not known, use tail = 1. Otherwise tail = 2. 2 for GSEA2, 1 for GSEA
	private JTextField pvshadow = new JTextField("0.01"); //Significance threshold for shadow analysis
	private JTextField pvsynergy = new JTextField("0.01"); //Significance threshold for synergy analysis
	private JTextField resultid = new JTextField("mra0001"); //mra result id for retrieving prior result
	private JCheckBox priorBox = new JCheckBox("Retrieve prior result with ID: ");
	private static final String lastDirConf = FilePathnameUtils.getUserSettingDirectoryPath()
					+ "masterregulator" + FilePathnameUtils.FILE_SEPARATOR + "lastDir.conf";
	boolean allpos = true;
	private int correlationCol = 3;

	public MasterRegulatorPanel() {
		networkTextField = new JTextField();
		networkTextField.setEditable(false);
		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Network");
		builder.append("Load Network");
		networkFrom = createNetworkFromComboBox();
		networkFrom.setSelectedIndex(1); // preselect "From File"
		// JComboBox networkMatrix = createNetworkMatrixComboBox();
		builder.append(networkFrom);
		networkMatrix.setEnabled(false);
		builder.append(networkMatrix);

		builder.append(networkTextField);

		// JButton loadNetworkButton=new JButton("Load");
		loadNetworkButton.addActionListener(new LoadNetworkButtonListener(
				adjMatrix));
		builder.append(loadNetworkButton);
		builder.nextLine();

		builder.appendSeparator("Enrichment Threshold");
		builder.append("FET/GSEA p-value ");
		if (pValueTextField == null)
			pValueTextField = new JTextField();
		pValueTextField.setText(Float.toString(PValueThresholdDefault));
		builder.append(pValueTextField);
		builder.nextLine();

		JTabbedPane jTabbedPane1 = new JTabbedPane();
		jTabbedPane1.add(builder.getPanel(), "Main");

		layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
		builder = new DefaultFormBuilder(layout);
		builder.append("Master Regulators");
		tfFrom = createTFFromComboBox();
		tfFrom.setSelectedIndex(0); // preselect "From File"
		tfFrom.setEnabled(false);
		// JComboBox tfGroups = createGroupsComboBox();
		builder.append(tfFrom);
		// tfGroups.setEnabled(false);
		builder.append(tfGroups);

		if (TFGeneListTextField == null)
			TFGeneListTextField = new JTextField();
		TFGeneListTextField.setText(TFGeneListDefault);
		builder.append(TFGeneListTextField);
		loadTFButton.addActionListener(new LoadMarkerFileListener());
		builder.append(loadTFButton);
		builder.nextLine();

		builder.append("Signature Markers");
		sigFrom = createSigFromComboBox();
		sigFrom.setSelectedIndex(0);
		sigFrom.setEnabled(false);
		// preselect "From File"
		builder.append(sigFrom);
		// sigGroups.setEnabled(false);
		builder.append(sigGroups);

		if (sigGeneListTextField == null)
			sigGeneListTextField = new JTextField();
		// sifGeneListTextField.setText(TFGeneListDefault);
		builder.append(sigGeneListTextField);
		loadSigButton.addActionListener(new LoadMarkerFileListener());
		builder.append(loadSigButton);
		builder.nextLine();

		jTabbedPane1.add(builder.getPanel(), "FET");

		builder = new DefaultFormBuilder(new FormLayout(
				"left:max(60dlu;pref), 10dlu, 100dlu, 80dlu, "
				+ "60dlu, 10dlu, 100dlu", ""));
		builder.append("Minimum number of Targets", mintg);
		builder.append("GSEA Tail", tail);
		builder.append("Minimum number of Samples", minsp);
		builder.append("Shadow P-value", pvshadow);
		builder.append("Number of GSEA Permutations", nperm);
		builder.append("Synergy P-value", pvsynergy);
		builder.nextLine();

		builder.append(priorBox, resultid);
		resultid.setEnabled(false);
		priorBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (priorBox.isSelected()) {
					mintg.setEnabled(false);
					minsp.setEnabled(false);
					nperm.setEnabled(false);
					tail.setEnabled(false);
					pvshadow.setEnabled(false);
					pvsynergy.setEnabled(false);
					pValueTextField.setEnabled(false);
					networkFrom.setEnabled(false);
					networkMatrix.setEnabled(false);
					networkTextField.setEnabled(false);
					loadNetworkButton.setEnabled(false);
					resultid.setEnabled(true);
				} else {
					mintg.setEnabled(true);
					minsp.setEnabled(true);
					nperm.setEnabled(true);
					tail.setEnabled(true);
					pvshadow.setEnabled(true);
					pvsynergy.setEnabled(true);
					pValueTextField.setEnabled(true);
					networkFrom.setEnabled(true);
					if (networkFrom.getSelectedIndex()==0)
						networkMatrix.setEnabled(true);
					else{
						networkTextField.setEnabled(true);
						loadNetworkButton.setEnabled(true);
					}
					resultid.setEnabled(false);
				}
				parameterActionListener.actionPerformed(null);
			}
		});
		jTabbedPane1.add(builder.getPanel(), "MARINa");
		this.add(jTabbedPane1, BorderLayout.CENTER);

		tfGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) tfGroups.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel,
							TFGeneListTextField)) {
						tfGroups.setSelectedIndex(0);
						TFGeneListTextField.setText(TFGeneListDefault);
					}

			}
		});

		sigGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) sigGroups.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel,
							sigGeneListTextField)) {
						sigGroups.setSelectedIndex(0);
						sigGeneListTextField.setText("");
					}
			}
		});

		parameterActionListener = new ParameterActionListener(
				this);
		TFGeneListTextField.addActionListener(parameterActionListener);
		sigGeneListTextField.addActionListener(parameterActionListener);
		networkTextField.addActionListener(parameterActionListener);
		networkFrom.addActionListener(parameterActionListener);
		networkMatrix.addActionListener(parameterActionListener);
		tfFrom.addActionListener(parameterActionListener);
		sigFrom.addActionListener(parameterActionListener);
		tfGroups.addActionListener(parameterActionListener);
		sigGroups.addActionListener(parameterActionListener);
		pValueTextField.addActionListener(parameterActionListener);
		mintg.addActionListener(parameterActionListener);
		minsp.addActionListener(parameterActionListener);
		nperm.addActionListener(parameterActionListener);
		tail.addActionListener(parameterActionListener);
		pvshadow.addActionListener(parameterActionListener);
		pvsynergy.addActionListener(parameterActionListener);
		resultid.addActionListener(parameterActionListener);
		priorBox.addActionListener(parameterActionListener);

		TFGeneListTextField.addFocusListener(parameterActionListener);
		sigGeneListTextField.addFocusListener(parameterActionListener);
		networkTextField.addFocusListener(parameterActionListener);
		networkFrom.addFocusListener(parameterActionListener);
		networkMatrix.addFocusListener(parameterActionListener);
		loadNetworkButton.addFocusListener(parameterActionListener);
		pValueTextField.addFocusListener(parameterActionListener);
		mintg.addFocusListener(parameterActionListener);
		minsp.addFocusListener(parameterActionListener);
		nperm.addFocusListener(parameterActionListener);
		tail.addFocusListener(parameterActionListener);
		pvshadow.addFocusListener(parameterActionListener);
		pvsynergy.addFocusListener(parameterActionListener);
		resultid.addFocusListener(parameterActionListener);
	}
	private ParameterActionListener parameterActionListener;

	public class LoadNetworkButtonListener implements
			java.awt.event.ActionListener {
		private HashMap<String, AdjacencyMatrixDataSet> adjMatrixHolder;

		public LoadNetworkButtonListener(
				HashMap<String, AdjacencyMatrixDataSet> adjMatrixHolder) {
			this.adjMatrixHolder = adjMatrixHolder;
		}

		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getActionCommand().equals("Load")) {
				if (maSet != null) {
					String adjMatrixFileStr = "C:\\Documents and Settings\\yc2480\\eclipse_geworkbench_workspace\\geworkbench-core\\data\\testaracne4.adjmat";
					File adjMatrixFile = new File(adjMatrixFileStr);
					JFileChooser chooser = new JFileChooser(adjMatrixFile
							.getParent());
					String lastDir = null;
					if ((lastDir = getLastDir()) != null) {
						chooser.setCurrentDirectory(new File(lastDir));
					}
					chooser.setFileFilter(new AdjacencyMatrixFileFormat().getFileFilter());
					chooser.showOpenDialog(MasterRegulatorPanel.this);
					if (chooser.getSelectedFile() != null) {
						File selectedFile = chooser.getSelectedFile();
						adjMatrixFileStr = selectedFile.getPath();
						networkTextField.setText(adjMatrixFileStr);
						networkFilename = selectedFile.getName();
						saveLastDir(selectedFile.getParent());

						if (!openDialog()) return;

						//no need to generate adjmatrix for 5col network file
						//because 5col network format is used only by grid mra as a file
						if (!selectedFormat.equals(marina5colformat)){
							try {
								AdjacencyMatrix matrix = AdjacencyMatrixDataSet
								.parseAdjacencyMatrix(adjMatrixFileStr, maSet,
										interactionTypeMap, selectedFormat,
										selectedRepresentedBy, isRestrict);

								AdjacencyMatrixDataSet adjMatrix = new AdjacencyMatrixDataSet(matrix, 
										0, adjMatrixFileStr, adjMatrixFileStr, maSet);
								this.adjMatrixHolder.remove("adjMatrix");
								this.adjMatrixHolder.put("adjMatrix", adjMatrix);
							} catch (InputFileFormatException e1) {
								log.error(e1.getMessage());
								e1.printStackTrace();
							}
						} else  this.adjMatrixHolder.remove("adjMatrix");
					} else {
						// user canceled
					}
				}
			}
		}
	}

	private class LoadMarkerFileListener implements
			java.awt.event.ActionListener {

		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getActionCommand().equals("Load")) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					String hubMarkersFile = "data/test.txt";
					File hubFile = new File(hubMarkersFile);
					JFileChooser chooser = new JFileChooser(hubFile.getParent());
					chooser.showOpenDialog(MasterRegulatorPanel.this);
					if (chooser.getSelectedFile() != null) {
						hubMarkersFile = chooser.getSelectedFile().getPath();
						BufferedReader reader = new BufferedReader(
								new FileReader(hubMarkersFile));
						String hub = reader.readLine();
						while (hub != null && !"".equals(hub)) {
							geneListBuilder.append(hub + ", ");
							hub = reader.readLine();
						}
						String geneString = geneListBuilder.toString();
						geneString = geneString.substring(0, geneString
								.length() - 2);
						if (e.getSource().equals(loadTFButton))
							TFGeneListTextField.setText(geneString);
						else if (e.getSource().equals(loadSigButton))
							sigGeneListTextField.setText(geneString);
					} else {
						// user canceled
					}
				} catch (IOException ioe) {
					log.error(ioe);
				}

			}
		}
	}

	private JComboBox createNetworkFromComboBox() {
		ArrayListModel<String> networkFromModel = new ArrayListModel<String>();
		networkFromModel.add("From Project");
		networkFromModel.add("From File");
		NetworkFromListener networkFromListener = new NetworkFromListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) networkFromModel);
		selectionInList.addPropertyChangeListener(networkFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private class NetworkFromListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				if (evt.getNewValue() == "From Project") {
					networkMatrix.setEnabled(true);
					loadNetworkButton.setEnabled(false);
					networkTextField.setEnabled(false);
					// clear combo box
					// load adj matrix into the list
					/*
					 * for (Iterator iterator =
					 * adjacencymatrixDataSets.iterator(); iterator .hasNext();) {
					 * AdjacencyMatrixDataSet element = (AdjacencyMatrixDataSet)
					 * iterator.next();
					 * System.out.println("add"+element.getDataSetName()+"to
					 * combo box."); }
					 */
				} else if (evt.getNewValue() == "From File") {
					networkMatrix.setEnabled(false);
					loadNetworkButton.setEnabled(true);
					networkTextField.setEnabled(true);
					// active load button
					// show file name loaded
				}
		}
	}

	private class TFFromListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				if (evt.getNewValue() == "From Sets") {
					tfGroups.setEnabled(true);
					loadTFButton.setEnabled(false);
					getGroups();
					// hide fileNameField
					// clear combo box
					// load adj matrix into the list
				} else if (evt.getNewValue() == "From File") {
					tfGroups.setEnabled(false);
					loadTFButton.setEnabled(true);
					// active load button
					// show file name loaded
				}
		}
	}

	private class SigFromListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				if (evt.getNewValue() == "From Sets") {
					sigGroups.setEnabled(true);
					loadSigButton.setEnabled(false);
					getGroups();
					// hide fileNameField
					// clear combo box
					// load adj matrix into the list
				} else if (evt.getNewValue() == "From File") {
					sigGroups.setEnabled(false);
					loadSigButton.setEnabled(true);
					// active load button
					// show file name loaded
				}
		}
	}

	private JComboBox createNetworkMatrixComboBox() {
		adjModel = new ArrayListModel<String>();
		// we'll generate network list in addAdjMatrixToCombobox()
		AdjListener adjListener = new AdjListener(this.adjMatrix);
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) adjModel);
		selectionInList.addPropertyChangeListener(adjListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private JComboBox createTFFromComboBox() {
		ArrayListModel<String> tfFromModel = new ArrayListModel<String>();
		tfFromModel.add("From Sets");
		tfFromModel.add("From File");
		TFFromListener tfFromListener = new TFFromListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) tfFromModel);
		selectionInList.addPropertyChangeListener(tfFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private JComboBox createSigFromComboBox() {
		ArrayListModel<String> sigFromModel = new ArrayListModel<String>();
		sigFromModel.add("From Sets");
		sigFromModel.add("From File");
		SigFromListener sigFromListener = new SigFromListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) sigFromModel);
		selectionInList.addPropertyChangeListener(sigFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private class AdjListener implements PropertyChangeListener {
		HashMap<String, AdjacencyMatrixDataSet> adjMatrix;

		public AdjListener(HashMap<String, AdjacencyMatrixDataSet> adjMatrix) {
			this.adjMatrix = adjMatrix;
		};

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				log.info("User select adj matrix: " + evt.getNewValue());
			for (Iterator<AdjacencyMatrixDataSet> iterator = adjacencymatrixDataSets
					.iterator(); iterator.hasNext();) {
				AdjacencyMatrixDataSet adjMatrixDataSet = (AdjacencyMatrixDataSet) iterator
						.next();
				if (adjMatrixDataSet.getDataSetName().equals(evt.getNewValue())) {
					this.adjMatrix.remove("adjMatrix");
					this.adjMatrix.put("adjMatrix", adjMatrixDataSet);
				}
			}
		}
	}

	// after user selected adjMatrix in the panel, you can use this method to
	// get the adjMatrix user selected.
	public AdjacencyMatrixDataSet getAdjMatrixDataSet() {
		return this.adjMatrix.get("adjMatrix");
	}

	public double getPValue() {
		try {
			return Double.valueOf(pValueTextField.getText());
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	public void setPValue(double d) {
		pValueTextField.setText(Double.toString(d));
	}

	public String getTranscriptionFactor() {
		return TFGeneListTextField.getText();
	}

	public void setTranscriptionFactor(String TFString) {
		TFGeneListTextField.setText(TFString);
	}

	public String getSigMarkers() {
		return sigGeneListTextField.getText();
	}

	public void setSigMarkers(String sigString) {
		sigGeneListTextField.setText(sigString);
	}

	ArrayList<AdjacencyMatrixDataSet> adjacencymatrixDataSets = new ArrayList<AdjacencyMatrixDataSet>();

	public String getSelectedAdjMatrix()
	{		 
		   return (String)networkMatrix.getSelectedItem();
	}
	
	public void setSelectedAdjMatrix(String datasetName)
	{		 
		networkMatrix.getModel().setSelectedItem(datasetName);
	}
	
	
	public void addAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		adjacencymatrixDataSets.add(adjDataSet);
		adjModel.add(adjDataSet.getDataSetName());		 
		
	}

	public void clearAdjMatrixCombobox() {
		adjacencymatrixDataSets.clear();
		adjModel.clear();
	}

	public void removeAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		try {
			adjacencymatrixDataSets.remove(adjDataSet);
			// adjModel.remove(adjDataSet.getDataSetName());
			adjModel.remove(adjModel.indexOf(adjDataSet.getDataSetName()));
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

	}

	public void renameAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet,
			String oldName, String newName) {
		for (AdjacencyMatrixDataSet adjSet : adjacencymatrixDataSets) {
			if (adjSet == adjDataSet)
				adjSet.setLabel(newName);
		}
		adjModel.remove(oldName);
		adjModel.add(newName);
	}

	public void setMicroarraySet(DSMicroarraySet maSet) {
		this.maSet = maSet;
	}

	public void getGroups() {
	}

	public void addGroupsToComboBox() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 *      Set inputed parameters to GUI.
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		if (parameters == null)
			return; // FIXME: this is a quick patch for 0001691, should fix it
					// correctly.
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);

		if (parameters.get("networkFrom") != null
				&& !parameters.get("networkFrom").toString().trim().equals(""))
			networkFrom.setSelectedIndex((Integer) parameters
					.get("networkFrom"));

     	if (parameters.get("networkMatrix") != null )
	        networkMatrix.setSelectedItem(parameters.get("networkMatrix"));
		
		String networkText = parameters.get("networkField")==null?null:parameters.get("networkField").toString();		 
		if (maSet != null && networkTextField.isEnabled()
				&& networkText != null && !networkText.trim().equals("")) {
			networkTextField.setText(networkText);
			networkFilename = new File(networkText).getName();
			if (!is5colnetwork(networkText, 10)){
				try {
					AdjacencyMatrixDataSet adjMatrix2 = new AdjacencyMatrixDataSet(
							0, networkText, networkText, maSet, networkText);
					this.adjMatrix.remove("adjMatrix");
					this.adjMatrix.put("adjMatrix", adjMatrix2);
				} catch (InputFileFormatException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		if (parameters.get("tfFrom") != null
				&& !parameters.get("tfFrom").toString().trim().equals(""))
			tfFrom.setSelectedIndex((Integer) parameters.get("tfFrom"));

		if (parameters.get("sigFrom") != null
				&& !parameters.get("sigFrom").toString().trim().equals(""))
			sigFrom.setSelectedIndex((Integer) parameters.get("sigFrom"));	
		
		if (parameters.get("tfGroups") != null)
	    	   tfGroups.setSelectedItem(parameters.get("tfGroups"));
	  	
	    if (parameters.get("sigGroups") != null  )
	    	   sigGroups.setSelectedItem(parameters.get("sigGroups"));

		
		if ((!tfGroups.isEnabled()) && parameters.get("TF") != null) {
			String TF = (String) parameters.get("TF");
			setTranscriptionFactor(TF);
		}

		if ((!sigGroups.isEnabled()) && parameters.get("sigMarkers") != null) {
			String sigMarkers = (String) parameters.get("sigMarkers");
			setSigMarkers(sigMarkers);
		}

		if (parameters.get("Fisher's Exact P Value") != null) {
			double d = (Double) parameters.get("Fisher's Exact P Value");
			if (d >= 0 && d <= 1)
			   setPValue(d);
			else
			   setPValue(0.01);
		}

		if (parameters.get("mintg") != null)
			setMintg((Integer)parameters.get("mintg"));
		if (parameters.get("minsp") != null)
			setMinsp((Integer)parameters.get("minsp"));
		if (parameters.get("nperm") != null)
			setNperm((Integer)parameters.get("nperm"));
		if (parameters.get("tail") != null)
			setTail((Integer)parameters.get("tail"));
		if (parameters.get("pvshadow") != null)
			setPVshadow((Double)parameters.get("pvshadow"));
		if (parameters.get("pvsynergy") != null)
			setPVsynergy((Double)parameters.get("pvsynergy"));
		if (parameters.get("resultid") != null)
			setResultid((String)parameters.get("resultid"));
		if (parameters.get("priorid") != null)
			setPriorid((Boolean)parameters.get("priorid"));

		stopNotifyAnalysisPanelTemporary(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> answer = new HashMap<Serializable, Serializable>();
		answer.put("TF", getTranscriptionFactor());
		answer.put("sigMarkers", getSigMarkers());	 
		if (networkFrom.isEnabled())
			answer.put("networkFrom", networkFrom.getSelectedIndex());	
		if (networkMatrix.isEnabled() && networkMatrix.getSelectedItem() != null)
		   answer.put("networkMatrix", (String)networkMatrix.getSelectedItem());
		if (networkTextField.isEnabled())
			answer.put("networkField", networkTextField.getText());
		answer.put("tfFrom", tfFrom.getSelectedIndex());	 
		answer.put("sigFrom", sigFrom.getSelectedIndex());
		if (tfGroups.getSelectedItem() != null)
		   answer.put("tfGroups", (String)tfGroups.getSelectedItem());   
    	if (sigGroups.getSelectedItem() != null)
		answer.put("sigGroups", (String)sigGroups.getSelectedItem());
    	
		if (getPValue() > 1 || getPValue() < 0)
			answer.put("Fisher's Exact P Value", 0.01);
		else
			answer.put("Fisher's Exact P Value", getPValue());

		if (mintg.isEnabled())     answer.put("mintg", getMintg());
		if (minsp.isEnabled())     answer.put("minsp", getMinsp());
		if (nperm.isEnabled())     answer.put("nperm", getNperm());
		if (tail.isEnabled())      answer.put("tail", getTail());
		if (pvshadow.isEnabled())  answer.put("pvshadow", getPVshadow());
		if (pvsynergy.isEnabled()) answer.put("pvsynergy", getPVsynergy());
		if (resultid.isEnabled())  answer.put("resultid", getResultid());
		answer.put("priorid", getPriorid());

		return answer;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	void setSelectorPanel(MasterRegulatorPanel aspp, DSPanel<DSGeneMarker> ap) {
		aspp.selectorPanel = ap;
		String currentTargetSet = (String) aspp.tfGroups.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) aspp.tfGroups
				.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetComboModel.addElement(label);
			if (StringUtils.equals(label, currentTargetSet.trim())) {
				targetComboModel.setSelectedItem(label);
			}
		}

		String currentSigSet = (String) aspp.sigGroups.getSelectedItem();
		DefaultComboBoxModel sigComboModel = (DefaultComboBoxModel) aspp.sigGroups
				.getModel();
		sigComboModel.removeAllElements();
		sigComboModel.addElement(" ");
		sigGeneListTextField.setText("");
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			sigComboModel.addElement(label);
			if (StringUtils.equals(label, currentSigSet.trim())) {
				sigComboModel.setSelectedItem(label);
			}
		}

	}

	private String getLastDir(){
		String dir = null;
		try {
			File file = new File(lastDirConf);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				dir = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return dir;
	}
	private void saveLastDir(String dir){
		//save as last used dir
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(lastDirConf));
			br.write(dir);
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Test if the network is in 5-column format, and if all correlation cols are positive.
	 * @param fname    network file name
	 * @param numrows  test format in the first numrows; if numrows <= 0, test whole file.
	 * @return if the network is in 5-column format
	 */
	private boolean is5colnetwork(String fname, int numrows){
		if (!new File(fname).exists())
			return false;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(fname));
			allpos = true;
			String line = null; int i = 0;
			while( (line = br.readLine()) != null && 
					(numrows <= 0 || i++ < numrows)) {
				String[] toks = line.split("\t");
				if (toks.length != 5 || !isDouble(toks[2]) 
						|| !isDouble(toks[3]) || !isDouble(toks[4]))
					return false;
				if (allpos && Double.valueOf(toks[correlationCol]) < 0)
					allpos = false;
			}
			log.info("This is a 5-column network");
			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}finally{
			try{ 
				if (br!=null) br.close(); 
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	boolean use5colnetwork(){
		return !networkMatrix.isEnabled() && selectedFormat.equals(marina5colformat);
	}

	private boolean isDouble(String s){
		try{
			Double.parseDouble(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public int getMintg() {
		try {
			return Integer.valueOf(mintg.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public void setMintg(int p){
		mintg.setText(Integer.toString(p));
	}
	public int getMinsp() {
		try {
			return Integer.valueOf(minsp.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public void setMinsp(int p){
		minsp.setText(Integer.toString(p));
	}
	public int getNperm() {
		try {
			return Integer.valueOf(nperm.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public void setNperm(int p){
		nperm.setText(Integer.toString(p));
	}
	public int getTail() {
		try {
			return Integer.valueOf(tail.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public void setTail(int p){
		tail.setText(Integer.toString(p));
	}
	public double getPVshadow() {
		try {
			return Double.valueOf(pvshadow.getText());
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}
	public void setPVshadow(double d) {
		pvshadow.setText(Double.toString(d));
	}
	public double getPVsynergy() {
		try {
			return Double.valueOf(pvsynergy.getText());
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}
	public void setPVsynergy(double d) {
		pvsynergy.setText(Double.toString(d));
	}
	public String getResultid(){
		if (!priorBox.isSelected()) return null;
		return resultid.getText().toLowerCase();
	}
	public void setResultid(String id){
		resultid.setText(id);
	}
	public boolean getPriorid(){
		return priorBox.isSelected();
	}
	public void setPriorid(boolean i){
		priorBox.setSelected(i);
	}

	private String networkFilename = "";
	public String getNetworkFilename(){
		if (!networkTextField.isEnabled()) return "adjMatrix5col.txt";
		return networkFilename;
	}

	/*get zipped network file in byte[]*/
	public byte[] getNetwork(){
		if (!networkFrom.isEnabled()) return null;
		if (use5colnetwork())
			return getNetworkFromFile();
		else return getNetworkFromAdjMatrix();
	}
	
	private byte[] getNetworkFromFile(){
		String fname = networkTextField.getText();
		if (!is5colnetwork(fname, 0))
			return null;

		int blocksize = 4096;
		FileInputStream in = null;
		GZIPOutputStream zipout = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			zipout = new GZIPOutputStream(bo);
			byte[] buffer = new byte[blocksize];

			in = new FileInputStream(fname);
			int length;
			while ((length = in.read(buffer, 0, blocksize)) != -1)
				zipout.write(buffer, 0, length);
			zipout.close();
			return bo.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (in!=null)     in.close();
				if (zipout!=null) zipout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private byte[] getNetworkFromAdjMatrix(){
		AdjacencyMatrixDataSet amSet = getAdjMatrixDataSet();
		if (amSet==null) return null;
		AdjacencyMatrix matrix  = amSet.getMatrix();
		if (matrix==null) return null;
		boolean goodNetwork = false;
		allpos = true;
		GZIPOutputStream zipout = null;
		try{
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			zipout = new GZIPOutputStream(bo);

			for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
				DSGeneMarker marker1 = getMarkerInNode(node1, matrix);
				if (marker1 != null && marker1.getLabel() != null) {
					StringBuilder builder = new StringBuilder();
					for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
						DSGeneMarker marker2 = getMarkerInNode(edge.node2, matrix);
						if (marker2 != null && marker2.getLabel() != null) {
							double rho = 1, pvalue = 0;
							double[] v1 = maSet.getRow(marker1);
							double[] v2 = maSet.getRow(marker2);
							if (v1 != null && v1.length > 0 && v2 != null && v2.length > 0){
								double[][] arrayData = new double[][]{v1, v2};
								RealMatrix rm = new SpearmansCorrelation().computeCorrelationMatrix(transpose(arrayData));
								if (rm.getColumnDimension() > 1)  rho = rm.getEntry(0, 1);
								if (allpos && rho < 0)  allpos = false;
								try{
									pvalue = new PearsonsCorrelation(rm, v1.length).getCorrelationPValues().getEntry(0, 1);
								}catch(Exception e){
									e.printStackTrace();
								}
							}
							builder.append(marker1.getLabel() + "\t");
							builder.append(marker2.getLabel() + "\t"
									+ edge.info.value +"\t"  // Mutual information
									+ rho+ "\t"   // Spearman's correlation = 1
									+ pvalue +"\n"); // P-value for Spearman's correlation = 0
						}
					}
					if (!goodNetwork && builder.length() > 0) goodNetwork = true;
					zipout.write(builder.toString().getBytes());
				}
			}
			zipout.close();
			if (!goodNetwork) return null;
			return bo.toByteArray();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			if (zipout!=null) {
				try{
					zipout.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	private double[][] transpose(double[][] in){
		if (in==null || in.length==0 || in[0].length==0)
			return null;
		int row = in.length;
		int col = in[0].length;
		double[][] out = new double[col][row];
		for(int i=0; i<row; i++)
			for (int j=0; j<col; j++)
				out[j][i] = in[i][j];
		return out;
	}

	private DSGeneMarker getMarkerInNode(AdjacencyMatrix.Node node, AdjacencyMatrix matrix){
		if (node == null || matrix == null) return null;
		DSGeneMarker marker = null;
		if (node.type == NodeType.MARKER) 
			marker = node.getMarker();
		else 
			marker = matrix.getMicroarraySet().getMarkers().get(node.stringId);
		return marker;
	}

	HashSet<String> getIxClass(String contextClass){
		DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager.getCurrentContext(maSet);
		String[] groups = context.getLabelsForClass(contextClass);
		HashSet<String> hash = new HashSet<String>();
		for (String group : groups){
			if (context.isLabelActive(group)){
				DSPanel<DSMicroarray> panel = context.getItemsWithLabel(group);
				int size = panel.size();
				for (int i = 0; i < size; i++)
					hash.add(panel.get(i).getLabel());
			}
		}
		return hash;
	}

	private String[] representedByList;
	private String selectedRepresentedBy = AdjacencyMatrixDataSet.PROBESET_ID;
	private HashMap<String, String> interactionTypeMap = null;
	private boolean isRestrict = true;
	private boolean isCancel = false;
	String selectedFormat = AdjacencyMatrixDataSet.ADJ_FORMART;
	String marina5colformat = "marina 5-column format";

	private class LoadInteractionNetworkPanel extends JPanel {

		static final long serialVersionUID = -1855255412334333328L;

		final JDialog parent;

		private JComboBox formatJcb;
		private JComboBox presentJcb;

		public LoadInteractionNetworkPanel(JDialog parent) {

			setLayout(new BorderLayout());
			this.parent = parent;
			init();

		}

		private void init() {

			JPanel panel1 = new JPanel(new GridLayout(3, 2));
			JPanel panel3 = new JPanel(new GridLayout(0, 3));
			JLabel label1 = new JLabel("File Format:    ");

			formatJcb = new JComboBox();
			formatJcb.addItem(AdjacencyMatrixDataSet.ADJ_FORMART);
			formatJcb.addItem(AdjacencyMatrixDataSet.SIF_FORMART);
			formatJcb.addItem(marina5colformat);
			JLabel label2 = new JLabel("Node Represented By:   ");

			representedByList = new String[4];
			representedByList[0] = AdjacencyMatrixDataSet.PROBESET_ID;
			representedByList[1] = AdjacencyMatrixDataSet.GENE_NAME;
			representedByList[2] = AdjacencyMatrixDataSet.ENTREZ_ID;
			representedByList[3] = AdjacencyMatrixDataSet.OTHER;
			presentJcb = new JComboBox(representedByList);

			JButton continueButton = new JButton("Continue");
			JButton cancelButton = new JButton("Cancel");
			formatJcb.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (formatJcb.getSelectedItem().toString().equals(
							AdjacencyMatrixDataSet.ADJ_FORMART)) {
						representedByList = new String[4];
						representedByList[0] = AdjacencyMatrixDataSet.PROBESET_ID;
						representedByList[1] = AdjacencyMatrixDataSet.GENE_NAME;
						representedByList[2] = AdjacencyMatrixDataSet.ENTREZ_ID;
						representedByList[3] = AdjacencyMatrixDataSet.OTHER;
						presentJcb.setModel(new DefaultComboBoxModel(
								representedByList));
					} else if (formatJcb.getSelectedItem().toString().equals(
							marina5colformat)) {
						representedByList = new String[1];
						representedByList[0] = AdjacencyMatrixDataSet.PROBESET_ID;
						presentJcb.setModel(new DefaultComboBoxModel(
								representedByList));
					} else {
						representedByList = new String[3];
						representedByList[0] = AdjacencyMatrixDataSet.GENE_NAME;
						representedByList[1] = AdjacencyMatrixDataSet.ENTREZ_ID;
						representedByList[2] = AdjacencyMatrixDataSet.OTHER;
						presentJcb.setModel(new DefaultComboBoxModel(
								representedByList));
					}
				}
			});

			if (networkFilename.toLowerCase().endsWith(".sif"))
				formatJcb.setSelectedItem(AdjacencyMatrixDataSet.SIF_FORMART);
			else if (networkFilename.toLowerCase().contains("5col"))
				formatJcb.setSelectedItem(marina5colformat);
			else
				formatJcb.setSelectedItem(AdjacencyMatrixDataSet.ADJ_FORMART);
			continueButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					continueButtonActionPerformed();
					parent.dispose();
					isCancel = false;
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					parent.dispose();
					isCancel = true;
				}
			});

			panel1.add(label1);
			panel1.add(formatJcb);

			panel1.add(label2);
			panel1.add(presentJcb);

			panel3.add(cancelButton);
			panel3.add(new JLabel("  "));
			panel3.add(continueButton);
			
			this.add(panel1, BorderLayout.CENTER);
			this.add(panel3, BorderLayout.SOUTH);
			parent.getRootPane().setDefaultButton(continueButton);
		}

		private void continueButtonActionPerformed() {
			selectedFormat = formatJcb.getSelectedItem().toString();
			selectedRepresentedBy = presentJcb.getSelectedItem().toString();
		}

	}
	private boolean openDialog(){
		JDialog loadDialog = new JDialog();

		loadDialog.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				isCancel = true;
			}
		});

		isCancel = false;
		loadDialog.setTitle("Load Interaction Network");
		LoadInteractionNetworkPanel loadPanel = new LoadInteractionNetworkPanel(
				loadDialog);

		loadDialog.add(loadPanel);
		loadDialog.setModal(true);
		loadDialog.pack();
		Util.centerWindow(loadDialog);
		loadDialog.setVisible(true);

		if (isCancel)
			return false;

		if ((selectedFormat
				.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART) && !networkFilename
				.toLowerCase().endsWith(".sif"))
				|| (networkFilename.toLowerCase().endsWith(".sif") && !selectedFormat
						.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART))) {
			String theMessage = "The network format selected may not match that of the file.  \nClick \"Cancel\" to terminate this process.";
			Object[] optionChoices = { "Continue", "Cancel"};
			int result = JOptionPane.showOptionDialog(
				(Component) null, theMessage, "Warning",
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				null, optionChoices, optionChoices[1]);
			if (result == JOptionPane.NO_OPTION)
				return false;

		} else if (selectedFormat.equals(marina5colformat) && !is5colnetwork(networkTextField.getText(), 10)){
			JOptionPane.showMessageDialog(null,  "The network format selected does not match that of the file.",
					"Format Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (selectedFormat
				.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)) {
			interactionTypeMap = new org.geworkbench.parsers.AdjacencyMatrixFileFormat().getInteractionTypeMap();
		}
		return true;
	}
	
	String validateNetwork(){
		 if (use5colnetwork()) {
			 if (!is5colnetwork(networkTextField.getText(), 10)) return "Network file format error";
			 BufferedReader br = null;
				try{
					br = new BufferedReader(new FileReader(networkTextField.getText()));
					String line = null;
					while( (line = br.readLine()) != null) {
						String[] toks = line.split("\t");
						if (maSet.getMarkers().get(toks[0])!=null &&
								maSet.getMarkers().get(toks[1])!=null)
							return "Valid";
					}
					return "No matching markers";
				}catch(IOException e){
					e.printStackTrace();
					return "Network file IO exception";
				}finally{
					try{ 
						if (br!=null) br.close(); 
					}catch(IOException e){
						e.printStackTrace();
					}
				} 
		 } else {
			 AdjacencyMatrixDataSet amSet = getAdjMatrixDataSet();
			 if (amSet==null) return "Network (Adjacency Matrix) has not been loaded yet.";
			 AdjacencyMatrix matrix  = amSet.getMatrix();
			 if (matrix==null) return "Network (Adjacency Matrix) has not been loaded yet.";

			 for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
				 DSGeneMarker marker1 = getMarkerInNode(node1, matrix);
				 if (marker1 != null && marker1.getLabel() != null) {
					 for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
						 DSGeneMarker marker2 = getMarkerInNode(edge.node2, matrix);
						 if (marker2 != null && marker2.getLabel() != null) return "Valid";
					 }
				 }
			 }
		 }
		 return "No matching markers";
	}

}
