package org.geworkbench.components.sequenceretriever;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

/**
 * Widget to retrieve Promoter sequence from UCSC's DAS sequence server.
 *
 * @author Xuegong Wang
 * @author manjunath at genomecenter dot columbia dot edu
 * @author xiaoqing at genomecenter dot columbia dot edu
 * @version $Id: SequenceRetriever.java,v 1.61 2009-08-10 20:36:11 my2248 Exp $
 */

@SuppressWarnings("unchecked")
@AcceptTypes( { DSMicroarraySet.class })
public class SequenceRetriever implements VisualPlugin {
	private Log log = LogFactory.getLog(SequenceRetriever.class);

	private static final String LOCAL = "Local";
	private static final String UCSC = "UCSC";
	private static final String EBI = "EBI";


	private String currentSource = LOCAL;

	private final static String NORMAL = "normal";
	private final static String STOP = "stop";
	private final static String CLEAR = "clear";
	private final static String RUNNING = "running";
	private final static String PROTEINVIEW = "Protein";
	private final static String DNAVIEW = "DNA";

	private String currentView = DNAVIEW;
	private String status = NORMAL;

	private DSPanel<DSGeneMarker> markers = null;

	private DSPanel<DSGeneMarker> activeMarkers = null;

	private CSSequenceSet sequenceDB = new CSSequenceSet<DSSequence>();

	private CSSequenceSet dnaSequenceDB = new CSSequenceSet<DSSequence>();

	private CSSequenceSet proteinSequenceDB = new CSSequenceSet<DSSequence>();

	private DSItemList markerList;

	private static final String NOANNOTATION = "---";

	private DSMicroarraySet<DSMicroarray> refMASet = null;

	private TreeMap<String, ArrayList<String>> currentRetrievedSequences = new TreeMap<String, ArrayList<String>>();

	// Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> currentRetrievedMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> retrievedProteinSequences = new TreeMap<String, ArrayList<String>>();

	// Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> retrievedProteinMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> retrievedDNASequences = new TreeMap<String, ArrayList<String>>();

	// //Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> retrievedDNAMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> cachedRetrievedProteinSequences = new TreeMap<String, ArrayList<String>>();

	// Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> cachedRetrievedProteinMap = new HashMap<String, RetrievedSequenceView>();

	// Map the marker name with associated sequence name.
	private TreeMap<String, ArrayList<String>> cachedRetrievedDNASequences = new TreeMap<String, ArrayList<String>>();

	// //Map the sequence name with the sequence display
	private HashMap<String, RetrievedSequenceView> cachedRetrievedDNAMap = new HashMap<String, RetrievedSequenceView>();

	// Layouts,Panels and Panes
	private JPanel main = new JPanel();

	private JToolBar jToolbar2 = new JToolBar();

	private JScrollPane seqScrollPane = new JScrollPane();

	private RetrievedSequencesPanel seqDisPanel = new RetrievedSequencesPanel();

	private JPanel jPanel2 = new JPanel();

	private SpinnerNumberModel model = new SpinnerNumberModel(1999, 1, 1999, 1);

	private JSpinner beforeText = new JSpinner();

	private SpinnerNumberModel model1 = new SpinnerNumberModel(2000, 1, 2000, 1);

	private JSpinner afterText = new JSpinner();

	private JLabel jLabel1 = new JLabel();

	private JLabel jLabel2 = new JLabel();

	private JPopupMenu jpopMenu = new JPopupMenu();

	private JMenuItem jClearUnselectedItem = new JMenuItem();

	private JMenuItem jClearAllItem = new JMenuItem();

	private JSplitPane rightPanel = new JSplitPane();

	private JPanel jPanel3 = new JPanel();

	private GridLayout gridLayout1 = new GridLayout();

	private JButton jActivateBttn = new JButton();

	private JButton jButton2 = new JButton();

	private JButton stopButton = new JButton();

	private JButton clearButton = new JButton();

	private JPanel jPanel1 = new JPanel();

	private JScrollPane jScrollPane2 = new JScrollPane();

	private JPanel jPanel4 = new JPanel();

	private BorderLayout borderLayout5 = new BorderLayout();

	private JLabel jLabel4 = new JLabel();

	private DefaultListModel ls1 = new DefaultListModel();

	private DefaultListModel ls2 = new DefaultListModel();

	private JList jSelectedList = new JList();

	private JComboBox jComboCategory = new JComboBox();

	private JComboBox jSourceCategory = new JComboBox();

	private JPanel jPanel6 = new JPanel();

	private JProgressBar jProgressBar1 = new JProgressBar();

	private GridLayout gridLayout2 = new GridLayout();

	private JLabel jLabel6 = new JLabel();

	private JTabbedPane tabPane = new JTabbedPane();

	private JPanel markerPanel = new JPanel();

	private FlowLayout flowLayout1 = new FlowLayout();

	public SequenceRetriever() {
		try {
			jbInit();
			SequenceFetcher.getSequenceFetcher();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class SequenceListRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1764773552579977347L;

		public Component getListCellRendererComponent(JList list, Object value, // value
				// to
				// display
				int index, // cell index
				boolean isSelected, // is the cell selected
				boolean cellHasFocus) // the list and the cell have the focus
		{
			String s = value.toString();

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				setText("<html><font color=RED>" + s + "</font></html>");
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				if (currentRetrievedSequences.containsKey(s)) {
					setText("<html><font color=blue>" + s + "</font></html>");
				} else {
					setText(s);
				}
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			return this;
		}
	}

	@SuppressWarnings("serial")
	void jbInit() throws Exception {
		// jProgressBar1.setForeground(Color.green);
		jProgressBar1.setMinimumSize(new Dimension(10, 16));
		jProgressBar1.setBorderPainted(true);
		jPanel6.setLayout(gridLayout2);
		jLabel6.setHorizontalAlignment(SwingConstants.LEFT);
		jLabel6.setText("Type");
		rightPanel = new JSplitPane();
		rightPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jPanel1.setLayout(flowLayout1);
		// jComboCategory.setPreferredSize(new Dimension(46, 21));
		jpopMenu.add(jClearUnselectedItem);
		jpopMenu.add(jClearAllItem);

		seqDisPanel.setBorder(null);
		seqDisPanel.setMinimumSize(new Dimension(10, 10));
		jPanel3.setBorder(null);

		main.setLayout(new BorderLayout());
		jToolbar2.setBorder(BorderFactory.createEtchedBorder());
		jToolbar2.setMinimumSize(new Dimension(20, 25));
		jToolbar2.setPreferredSize(new Dimension(20, 25));

		seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
		seqScrollPane.setMaximumSize(new Dimension(32767, 32767));
		seqScrollPane.setMinimumSize(new Dimension(24, 24));
		seqScrollPane.setPreferredSize(new Dimension(250, 250));

		jPanel2.setLayout(new BorderLayout());
		beforeText.setModel(model);
//		beforeText.setSize(new Dimension(15, 10));
//		beforeText.setPreferredSize(new Dimension(15, 10));

		afterText.setModel(model1);
//		afterText.setSize(new Dimension(15, 10));
//		afterText.setPreferredSize(new Dimension(15, 10));

		jLabel1.setToolTipText("Downstream");
		jLabel1.setText("+");
		jLabel2.setToolTipText("Upstream");
		jLabel2.setText("-");
		seqDisPanel.setMaximumSize(new Dimension(32767, 32767));
		seqDisPanel.setPreferredSize(new Dimension(216, 40));

		jPanel3.setLayout(gridLayout1);
		jPanel3.setPreferredSize(new Dimension(160, 240));
		gridLayout1.setColumns(1);

		jActivateBttn.setMaximumSize(new Dimension(100, 27));
		jActivateBttn.setMinimumSize(new Dimension(100, 27));
		jActivateBttn.setPreferredSize(new Dimension(100, 27));
		jActivateBttn.setToolTipText("Add a data node to current project");
		jActivateBttn.setText("Add To Project");
		jActivateBttn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jActivateBttn_actionPerformed(e);
			}
		});
		jButton2.setToolTipText("Get sequence of selected markers");
		jButton2.setHorizontalTextPosition(SwingConstants.TRAILING);
		jButton2.setText("Get Sequence");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
			}
		});
		stopButton.setText(STOP);
		stopButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopButton_actionPerformed(e);
			}
		});
		clearButton.setText(CLEAR);
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearButton_actionPerformed(e);
			}
		});
		jPanel4.setLayout(borderLayout5);
		jLabel4.setText("Selected Microarray Markers");
		jSelectedList = new JList(ls2) {
			public String getToolTipText(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (-1 < index) {
					String item = getModel().getElementAt(index).toString();
					ArrayList arraylist = currentRetrievedSequences.get(item);
					if (arraylist != null && arraylist.size() > 0) {
						if (arraylist.size() == 1) {
							return arraylist.size() + " sequence found.";
						} else {
							return arraylist.size() + " sequences found.";
						}
					} else {
						return "No sequence.";
					}
				}
				return null;
			}
		};
		jSelectedList.setCellRenderer(new SequenceListRenderer());
		jSelectedList
				.addListSelectionListener(new SequenceListSelectionListener());
		jComboCategory.setSelectedItem(DNAVIEW);
		jComboCategory.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changeSequenceCategory();
			}
		});
		jSourceCategory.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (jSourceCategory != null) {
					String cmd = (String) jSourceCategory.getSelectedItem();
					if (currentSource != null
							&& !currentSource.equalsIgnoreCase(cmd)) {
						if (cmd != null && cmd.equalsIgnoreCase(LOCAL)) {
							model = new SpinnerNumberModel(1999, 1, 1999, 1);
							model1 = new SpinnerNumberModel(2000, 1, 2000, 1);
						} else if (cmd != null && cmd.equalsIgnoreCase(UCSC)) {
							model = new SpinnerNumberModel(10000, 1, 98000, 1);
							model1 = new SpinnerNumberModel(10000, 1, 10000, 1);
						}
						currentSource = cmd;
					}
					beforeText.setModel(model);
					afterText.setModel(model1);
					beforeText.revalidate();
					beforeText.repaint();
					main.repaint();
				}
			}
		});

		main.add(jToolbar2, BorderLayout.SOUTH);
		jToolbar2.add(jLabel2, null);
		jToolbar2.add(beforeText, null);
		jToolbar2.add(jLabel1, null);
		jToolbar2.add(afterText, null);
		jToolbar2.add(stopButton, null);
		jToolbar2.add(clearButton, null);
		jToolbar2.add(jButton2, null);
		jToolbar2.add(jActivateBttn, null);
		jPanel2.add(rightPanel, BorderLayout.CENTER);
		tabPane = new JTabbedPane();
		markerPanel = new TFListPanel();
		String markTab = "Marker";
		tabPane.add(markTab, jPanel3);
		String findTab = "Find a Marker";
		tabPane.add(findTab, markerPanel);
		rightPanel.add(seqScrollPane, JSplitPane.RIGHT);
		rightPanel.add(tabPane, JSplitPane.LEFT);
		jPanel3.add(jPanel4, null);
		jPanel4.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jSelectedList, null);
		jPanel2.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jLabel6, null);
		jPanel1.add(jComboCategory, null);
		jPanel1.add(jSourceCategory, null);
		main.add(jPanel6, BorderLayout.NORTH);
		jPanel6.add(jProgressBar1, null);
		seqScrollPane.getViewport().add(seqDisPanel, null);
		main.add(jPanel2, BorderLayout.CENTER);
		jComboCategory.addItem(DNAVIEW);
		jComboCategory.addItem(PROTEINVIEW);
	}

	public void setSequenceDB(CSSequenceSet db2) {
		sequenceDB = db2;

	}

	public DSSequenceSet getSequenceDB() {
		return sequenceDB;
	}

	private void changeSequenceCategory() {

		String sequenceType = (String) jComboCategory.getSelectedItem();
		currentView = sequenceType;
		if (sequenceType.equalsIgnoreCase(PROTEINVIEW)) {
			beforeText.setEnabled(false);
			afterText.setEnabled(false);
			jSourceCategory.removeAllItems();
			jSourceCategory.addItem(EBI);
			sequenceDB = proteinSequenceDB;
			currentRetrievedMap = retrievedProteinMap;
			currentRetrievedSequences = retrievedProteinSequences;
			updateDisplay(proteinSequenceDB, retrievedProteinMap);
		} else {
			beforeText.setEnabled(true);
			afterText.setEnabled(true);
			jSourceCategory.removeAllItems();
			jSourceCategory.addItem(UCSC);
			jSourceCategory.addItem(LOCAL);

			currentSource = UCSC;
			sequenceDB = dnaSequenceDB;
			currentRetrievedMap = retrievedDNAMap;
			currentRetrievedSequences = retrievedDNASequences;
			updateDisplay(dnaSequenceDB, retrievedDNAMap);
		}
		sequenceDB.parseMarkers();
		jSelectedList.updateUI();

	}

	/**
	 * Handle the selections at the MarkerList table.
	 *
	 * @param indices
	 */
	private void updateSelectedSequenceDB(int[] indices) {
		seqDisPanel.initialize();
		
		CSSequenceSet displaySequenceDB = new CSSequenceSet();
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];

			if (ls2 != null && ls2.size() > index
					&& index > -1) {

				DSGeneMarker marker = (DSGeneMarker) ls2.get(index);

				ArrayList<String> values = currentRetrievedSequences.get(marker.toString());
				if (values == null) {
					continue;
				} else {

					for (String o : values) {
						RetrievedSequenceView retrievedSequenceView = currentRetrievedMap.get(o);
						if (retrievedSequenceView != null
							&& retrievedSequenceView.getSequence() != null) {
							displaySequenceDB.addASequence(retrievedSequenceView.getSequence());
						}
					}
					displaySequenceDB.parseMarkers();
					seqDisPanel.setDisplaySequenceDB(displaySequenceDB);
				}
			}
		}
	}

	private void updateProgressBar(final double percent, final String text) {
		Runnable r = new Runnable() {
			public void run() {
				try {
					jProgressBar1.setForeground(Color.GREEN);
					jProgressBar1.setString(text);
					jProgressBar1.setValue((int) (percent * 100));
					if (text.startsWith("Stop")) {
						jProgressBar1.setForeground(Color.RED);
					}
				} catch (Exception e) {
				}
			}
		};
		SwingUtilities.invokeLater(r);
	}

	void stopButton_actionPerformed(ActionEvent e) {
		status = STOP;
		stopButton.setEnabled(false);
	}

	void clearButton_actionPerformed(ActionEvent e) {
		status = CLEAR;
		cleanUp();

	}

	void cleanUp() {
		sequenceDB = new CSSequenceSet();
		currentRetrievedMap = new HashMap<String, RetrievedSequenceView>();
		currentRetrievedSequences = new TreeMap<String, ArrayList<String>>();
		cleanUp(DNAVIEW);
		cleanUp(PROTEINVIEW);

	}

	void cleanUp(String theView) {

		if (theView == DNAVIEW) {
			dnaSequenceDB = new CSSequenceSet();
			retrievedDNAMap = new HashMap<String, RetrievedSequenceView>();
			retrievedDNASequences = new TreeMap<String, ArrayList<String>>();
		} else {
			proteinSequenceDB = new CSSequenceSet();
			retrievedProteinMap = new HashMap<String, RetrievedSequenceView>();
			retrievedProteinSequences = new TreeMap<String, ArrayList<String>>();
		}
		seqDisPanel
				.setRetrievedMap(new HashMap<String, RetrievedSequenceView>());
		seqDisPanel.initialize();
		jSelectedList.clearSelection();
		jSelectedList.repaint();
	}

	void jButton2_actionPerformed(ActionEvent e) {
		currentView = (String) jComboCategory.getSelectedItem();
		currentSource = (String) jSourceCategory.getSelectedItem();
		cleanUp(currentView);
		status = RUNNING;
		stopButton.setEnabled(true);
		if (ls2.getSize() > 0) {
			seqDisPanel.initialize();
			jProgressBar1.setIndeterminate(false);
			jProgressBar1.setMinimum(0);
			jProgressBar1.setMaximum(100);
			jProgressBar1.setStringPainted(true);
			updateProgressBar(0, "");
			if (sequenceDB != null) {
				sequenceDB = new CSSequenceSet();
			}
			
			String annotationFileName = ((CSMicroarraySet)refMASet).getAnnotationFileName(); 
			if(annotationFileName==null) {
				JOptionPane.showMessageDialog(null, "No annotation file was loaded for this dataset, cannot retrieve sequences.");
				return;
			}

			SwingWorker<Void, Void> t = new SwingWorker<Void, Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					int size = ls2.getSize();
					Vector list = new Vector();
					for (int x = 0; x < size; ++x) {
						Object o = ls2.get(x);
						list.addElement(o);
					}

					if (!getSequences(list)){
						log.warn("getSequences did not succeed.");
					}
					return null;
				}
				
				@Override
				public void done() {
					if (status.equalsIgnoreCase(STOP)) {
						sequenceDB = new CSSequenceSet();
						updateProgressBar(100, "Stopped on " + new Date());
					} else {
						updateProgressBar(100, "Finished on " + new Date());
						jSelectedList.updateUI();
						seqDisPanel.setRetrievedMap(currentRetrievedMap);

					}
					stopButton.setEnabled(false);
					jComboCategory.setSelectedItem(currentView);
				}

			};
			t.execute();
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select gene(s) or marker(s).");
		}

	}

	void jActivateBttn_actionPerformed(ActionEvent e) {
		if ((sequenceDB != null) && (sequenceDB.getSequenceNo() >= 1)) {

			CSSequenceSet selectedSequenceDB = new CSSequenceSet();
			for (Object sequence : sequenceDB) {
				if (sequence != null) {
					RetrievedSequenceView retrievedSequenceView = currentRetrievedMap
							.get(sequence.toString());
					if (retrievedSequenceView.isIncluded()) {
						selectedSequenceDB.addASequence((DSSequence) sequence);
					}
				}
			}
			if (selectedSequenceDB.size() > 0) {
				String fileName = this.getRandomFileName();
				selectedSequenceDB.writeToFile(fileName);
				String label = JOptionPane
						.showInputDialog("Please enter a name for the dataset");
				if (label != null) {
					selectedSequenceDB.setLabel(label);
					selectedSequenceDB.parseMarkers();
					ProjectPanel.addToHistory(selectedSequenceDB, generateHistStr());
					ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
							"message", selectedSequenceDB, null);
					publishProjectNodeAddedEvent(event);


				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Please select at least one sequence.");
			}
		}
	}

	private void getCachedDNASequences(DSGeneMarker marker) {

		CSSequence seqs = SequenceFetcher.getCachedPromoterSequence(marker,
				((Integer) model.getNumber()).intValue(), ((Integer) model1
						.getNumber()).intValue());

		if (seqs != null) {
			dnaSequenceDB.addASequence(seqs);
			RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(
					seqs);
			retrievedDNAMap.put(seqs.toString(), retrievedSequenceView);
			dnaSequenceDB.parseMarkers();
			if (retrievedDNASequences.containsKey(marker.toString())) {
				ArrayList<String> values = retrievedDNASequences.get(marker
						.toString());
				if(!values.contains(seqs.toString()))
					values.add(seqs.toString());
			} else {
				ArrayList<String> values = new ArrayList<String>();
				values.add(seqs.toString());
				retrievedDNASequences.put(marker.toString(), values);
			}
		}
	}

	private boolean getSequences(Vector selectedList) {

		if (selectedList != null) {

			// sequenceDB = new CSSequenceSet();

			if (((String) jComboCategory.getSelectedItem())
					.equalsIgnoreCase(DNAVIEW)) {
				RetrievedSequenceView.setUpstreamTotal(((Integer) model
						.getNumber()).intValue());
				RetrievedSequenceView.setDownstreamTotal(((Integer) model1
						.getNumber()).intValue());
				if (jSourceCategory.getSelectedItem().equals(LOCAL)) {
					for (int i = 0; i < selectedList.size(); i++) {
						DSGeneMarker marker = (DSGeneMarker) selectedList
								.get(i);
						double progress = (double) i
								/ (double) (selectedList.size());
						updateProgressBar(progress, "Retrieving "
								+ marker.getLabel());
						if (status.equalsIgnoreCase(STOP)) {
							return false;
						}
						getCachedDNASequences(marker);
					}

				} else if (jSourceCategory.getSelectedItem().equals(UCSC)) {
					int startPoint = ((Integer) model.getNumber()).intValue();
					int endPoint = ((Integer) model1.getNumber()).intValue();

					String currentChipType = AnnotationParser
							.getCurrentChipType();
					String database = "";
					String annotationFileName = ((CSMicroarraySet)refMASet).getAnnotationFileName(); 
					database = SequenceFetcher.matchChipType(currentChipType, annotationFileName);

					/* No selection was made */
					if (database == null) {
						return false;
					}

					boolean serverWorking = true;
					for (int i = 0; i < selectedList.size(); i++) {
						if (!serverWorking)
							break;
						DSGeneMarker marker = (DSGeneMarker) selectedList
								.get(i);
						double progress = (double) (i + 1)
								/ (double) (selectedList.size());
						if (status.equalsIgnoreCase(STOP)) {
							return false;
						}
						updateProgressBar(progress, "Retrieving "
								+ marker.getLabel());
						String[] knownGeneName = AnnotationParser.getInfo(
								marker.getLabel(), AnnotationParser.REFSEQ);
						if (knownGeneName != null && knownGeneName.length > 0) {

							for (String geneName : knownGeneName) {
								if (!serverWorking)
									break;
								if (geneName == null
										|| geneName.equals(NOANNOTATION)) {
									continue;
								}
								Vector geneChromosomeMatchers = null;
								try {
									geneChromosomeMatchers = SequenceFetcher
											.getGeneChromosomeMatchers(
													geneName, database);
								} catch (SQLException sqle) {
									JOptionPane.showMessageDialog(null,
											"Remote server may be unavailable",
											"Error during sequence query",
											JOptionPane.ERROR_MESSAGE);
									log.error(sqle, sqle);
									serverWorking = false;
								}
								if (geneChromosomeMatchers != null) {
									for (int j = 0; j < geneChromosomeMatchers
											.size(); j++) {
										GeneChromosomeMatcher o = (GeneChromosomeMatcher) geneChromosomeMatchers
												.get(j);
										CSSequence seqs = SequenceFetcher
												.getSequenceFetcher()
												.getSequences(o, startPoint,
														endPoint);
										if (seqs != null) {
											// set up label now only
											// marker.label + Real start point.
											if (o.isPositiveStrandDirection()) {
												seqs.setLabel(marker.getLabel()
														+ "_" + o.getChr()
														+ "_"
														+ o.getStartPoint());
											} else {
												seqs
														.setLabel(marker
																.getLabel()
																+ "_"
																+ o.getChr()
																+ "_"
																+ o
																		.getEndPoint());
											}
											dnaSequenceDB.addASequence(seqs);
											RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(
													seqs);
											retrievedSequenceView
													.setGeneChromosomeMatcher(o);
											retrievedDNAMap.put(
													seqs.toString(),
													retrievedSequenceView);
											if (retrievedDNASequences
													.containsKey(marker
															.toString())) {
												ArrayList<String> values = retrievedDNASequences
														.get(marker.toString());
												if(!values.contains(seqs.toString()))
													values.add(seqs.toString());
											} else {
												ArrayList<String> values = new ArrayList<String>();
												values.add(seqs.toString());
												retrievedDNASequences.put(
														marker.toString(),
														values);
											}
											// sequenceDB.parseMarkers();
										}

									}
								}
							}
						}
					}

				}

				postProcessSequences();

			} else {
				proteinSequenceDB = new CSSequenceSet();

				if (selectedList != null) {
					try {

					for (int count = 0; count < selectedList.size(); count++) {
						DSGeneMarker geneMarker = (DSGeneMarker) selectedList
								.get(count);
						String affyid = geneMarker.getLabel();
						if (status.equalsIgnoreCase(STOP)) {
							return false;
						}
						double progress = (double) count
								/ (double) (selectedList.size());
						if (affyid.endsWith("_at")) { // if this is affyid
							updateProgressBar(progress, "Retrieving " + affyid);
							CSSequenceSet sequenceSet = SequenceFetcher
									.getAffyProteinSequences(affyid);

							String[] uniprotids = AnnotationParser.getInfo(
									affyid, AnnotationParser.SWISSPROT);
							if (sequenceSet.size() > 0) {
								ArrayList<String> values = new ArrayList<String>();
								int i = 0;
								for (Object o : sequenceSet) {
									retrievedProteinSequences.put(geneMarker
											.toString(), values);
									proteinSequenceDB
											.addASequence((CSSequence) o);
									RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(
											(CSSequence) o);
									retrievedSequenceView
											.setUrl(uniprotids[i++]);
									retrievedProteinMap.put(o.toString(),
											retrievedSequenceView);
									proteinSequenceDB.parseMarkers();
									values.add(o.toString());
									retrievedProteinSequences.put(geneMarker
											.toString(), values);

								}
							}

						}
					} // end of for loop
					} catch (RemoteException e) {
						showNoSequencesDialog(e.toString());
						return false;
					}


				}

				// Need to remove all previous result.
				// sequenceDB = new CSSequenceSet();
				// sequenceDB.readFASTAFile(new File(fileName));
				/**
				 * todo Don't know why we need save it in temp file. Maybe for
				 * the editor to read it?
				 */
				postProcessSequences();
			}

		}
		return true;
	}

	private void postProcessSequences() {
		if (currentView.equalsIgnoreCase(DNAVIEW)) {
			sequenceDB = dnaSequenceDB;
			currentRetrievedMap = retrievedDNAMap;
			currentRetrievedSequences = retrievedDNASequences;
			if (cachedRetrievedDNAMap == null) {
				cachedRetrievedDNAMap = currentRetrievedMap;
				cachedRetrievedDNASequences = retrievedDNASequences;
			} else {
				cachedRetrievedDNAMap.putAll(currentRetrievedMap);
				cachedRetrievedDNASequences.putAll(currentRetrievedSequences);
			}
		} else {
			sequenceDB = proteinSequenceDB;
			currentRetrievedMap = retrievedProteinMap;
			currentRetrievedSequences = retrievedProteinSequences;
			if (cachedRetrievedProteinMap == null) {
				cachedRetrievedProteinMap = currentRetrievedMap;
				cachedRetrievedProteinSequences = retrievedDNASequences;
			} else {
				cachedRetrievedProteinMap.putAll(currentRetrievedMap);
				cachedRetrievedProteinSequences
						.putAll(currentRetrievedSequences);
			}
		}
		sequenceDB.parseMarkers();
		String fileName = this.getRandomFileName();
		if (sequenceDB.getSequenceNo() == 0) {
			showNoSequencesDialog("No sequences retrieved for selected markers");
		}
		if (sequenceDB.getSequenceNo() != 0) {
			sequenceDB.writeToFile(fileName);
			sequenceDB = new CSSequenceSet();
			sequenceDB.readFASTAFile(new File(fileName));
			
			updateDisplay(sequenceDB, currentRetrievedMap);
		}
	}

	// this method is meant to be called from non-EDT thread
	private void showNoSequencesDialog(final String message) {
	    Runnable showModalDialog = new Runnable() {
	        public void run() {
				JOptionPane.showMessageDialog(getComponent(),
				message);
	        }
	    };
	    try {
			SwingUtilities.invokeAndWait(showModalDialog);
		} catch (Exception e) {
			log.error(e);
		} 
	}
	
	private void updateDisplay(CSSequenceSet selectedSet,
			HashMap<String, RetrievedSequenceView> newMap) {
		if (selectedSet == null || newMap == null || selectedSet.size()==0 || newMap.size()==0 ) {
			cleanUp(currentView);
			return;
		}
		seqDisPanel.setRetrievedMap(newMap);
		seqDisPanel.initialize(selectedSet);
		main.revalidate();
		main.repaint();
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
	}

	/**
	 * receiveProjectSelection
	 *
	 * @param e
	 *            ProjectEvent
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		log.debug("Source object " + source);

		if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
			refMASet = null;
			cleanUp();

		} else {
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSMicroarraySet) {
				if (refMASet != dataSet) {
					this.refMASet = (DSMicroarraySet) dataSet;
					cleanUp();
					sequenceDB = new CSSequenceSet();
					markerList = refMASet.getMarkers();
				}
			}

		}
	}

	/**
	 * geneSelectorAction
	 *
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe
	public void receive(GeneSelectorEvent e, Object publisher) {
		log.debug("received GeneSelectorEvent::source="
				+ publisher.getClass().getName());
		markers = e.getPanel();
		final Runnable processEventThread = new Runnable() {
			public void run() {
				 processEvent();
			}
		};

		Thread t = new Thread(processEventThread);
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		log.debug("end GeneSelectorEvent::source="
				+ publisher.getClass().getName());

	}

	/**
	 * geneSelectorAction
	 *
	 * @param e
	 *            GeneSelectorEvent
	 */

	public void processEvent() {
		// log.debug("process GeneSelectorEvent::source="
		// + publisher.getClass().getName());

		activeMarkers = new CSPanel();
		if (markers != null) {
			log.debug(markers.size() + " " + markers.isActive());
			TreeSet oldList = new TreeSet();
			if (ls2.size() > 0) {
				for (Object o : ls2.toArray()) {
					oldList.add(o);
				}
			}
			ls2.clear();
			boolean atLeastOneActive = false;
			if (markers.size() > 0) {
				CSSequenceSet tempSequenceDB = new CSSequenceSet();
				HashMap tempMap = new HashMap<String, RetrievedSequenceView>();
				TreeMap tempSequencesList = new TreeMap<String, ArrayList<String>>();
				ListOrderedSet<DSGeneMarker> orderedSet = new ListOrderedSet<DSGeneMarker>();
				for (int j = 0; j < markers.panels().size(); j++) {
					DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
					if (mrk.isActive()) {
						atLeastOneActive = true;
						for (int i = 0; i < mrk.size(); i++) {
							orderedSet.add(mrk.get(i));
							activeMarkers.add(mrk.get(i));
						}
					}
				}
				oldList.removeAll(orderedSet);
				for (Iterator<DSGeneMarker> markerIter = orderedSet.iterator(); markerIter
						.hasNext();) {
					DSGeneMarker mrk = markerIter.next();
					ls2.addElement(mrk);
					ArrayList<String> values = null;
					if (currentView.equalsIgnoreCase(DNAVIEW)) {
						values = cachedRetrievedDNASequences
								.get(mrk.toString());
					} else {
						values = cachedRetrievedProteinSequences.get(mrk
								.toString());
					}

					if (values != null) {
						for (String key : values) {
							RetrievedSequenceView retrievedSequenceView = currentRetrievedMap
									.get(key);

							if (currentView.equalsIgnoreCase(DNAVIEW)) {
								retrievedSequenceView = cachedRetrievedDNAMap
										.get(key);
							} else {
								retrievedSequenceView = cachedRetrievedProteinMap
										.get(key);
							}
							if (retrievedSequenceView != null) {
								DSSequence sequence = retrievedSequenceView
										.getSequence();
								if (sequence != null) {
									tempMap.put(key, retrievedSequenceView);
									tempSequenceDB.addASequence(sequence);
								}
							}

						}
						tempSequencesList.put(mrk.toString(), values);
					}
				}

				currentRetrievedMap = tempMap;
				currentRetrievedSequences = tempSequencesList;
				sequenceDB = tempSequenceDB;
				updateDisplay(sequenceDB, currentRetrievedMap);
				markers = activeMarkers;
				//
				if (!atLeastOneActive) {
					updateDisplay(null, null);
				}
				log.debug("Active markers / markers: " + activeMarkers.size()
						+ " / " + markers.size());
			} else {
				updateDisplay(null, null);
			}
		}
	}

	/**
	 * getComponent
	 *
	 * @return Component
	 */
	public Component getComponent() {
		return main;
	}

	void jResultList_mouseClicked(MouseEvent e) {
	}

	/**
	 * todo sdfsd
	 *
	 * @param e
	 *            ActionEvent
	 */

	void jClearAllItem_actionPerformed(ActionEvent e) {
		ls1.removeAllElements();
	}

	private String getRandomFileName() {
		String tempString = "temp" + RandomNumberGenerator.getID() + ".fasta";
		String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();
		String fileName = tempFolder + tempString;
		return fileName;
	}

	private class TFListPanel extends JPanel {
		private static final long serialVersionUID = 8681983607024585608L;

		public static final String NEXT_BUTTON_TEXT = "Find";

		public static final String SEARCH_LABEL_TEXT = "Search:";

		private JList list;

		private JButton nextButton;

		private JTextField searchField;

		private DefaultListModel model;

		private JScrollPane scrollPane;

		private boolean lastSearchFailed = false;

		private boolean prefixMode = false;

		public TFListPanel() {
			super();
			model = new DefaultListModel();
			setLayout(new BorderLayout());
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			JLabel searchLabel = new JLabel(SEARCH_LABEL_TEXT);
			nextButton = new JButton(NEXT_BUTTON_TEXT);
			searchField = new JTextField();
			list = new JList(model);
			scrollPane = new JScrollPane();
			// Compose components
			topPanel.add(searchLabel);
			topPanel.add(searchField);
			topPanel.add(nextButton);
			add(topPanel, BorderLayout.NORTH);
			scrollPane.getViewport().setView(list);
			add(scrollPane, BorderLayout.CENTER);
			// Add appropriate listeners
			nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					findNext(true);
				}
			});
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					handleMouseEvent(e);
				}
			});
			searchField.getDocument().addDocumentListener(
					new DocumentListener() {
						public void insertUpdate(DocumentEvent e) {
							searchFieldChanged();
						}

						public void removeUpdate(DocumentEvent e) {
							searchFieldChanged();
						}

						public void changedUpdate(DocumentEvent e) {
							searchFieldChanged();
						}
					});

		}

		private void handleMouseEvent(MouseEvent event) {
			int index = list.locationToIndex(event.getPoint());
			if (index != -1) {
				if (event.isMetaDown()) {
					elementRightClicked(index, event);
				} else if (event.getButton() == MouseEvent.BUTTON1) {
					if (event.getClickCount() > 1) {
						elementDoubleClicked(index, event);
					} else {
						elementClicked(index, event);
					}
				}
			}
		}

		private void handlePostSearch() {
			if (lastSearchFailed) {
				searchField.setForeground(Color.red);
			}
		}

		private void handlePreSearch() {
			searchField.setForeground(Color.black);
		}

		/**
		 * Override to customize the result of the 'next' button being clicked
		 * (or ENTER being pressed in text field).
		 */
		protected boolean findNext(boolean ascending) {
			handlePreSearch();

			String text = searchField.getText().toLowerCase();
			if (findNext(text)) {
				JOptionPane.showMessageDialog(this, "No marker can be found.");
			} else {
				boolean confirmed = false;
				int confirm = JOptionPane.showConfirmDialog(this,
						"Use the markers to retrieve sequences?");
				if (confirm == JOptionPane.YES_OPTION) {
					confirmed = true;
				}
				if (confirmed) {
					cleanUp(currentView);
					if (model.getSize() > 0) {
						seqDisPanel.initialize();
						jProgressBar1.setIndeterminate(false);
						jProgressBar1.setMinimum(0);
						jProgressBar1.setMaximum(100);
						jProgressBar1.setStringPainted(true);
						updateProgressBar(0, "");
						if (sequenceDB != null) {
							sequenceDB = new CSSequenceSet();
						}
						Thread t = new Thread() {

							public void run() {
								int size = model.getSize();
								Vector list = new Vector();
								for (int x = 0; x < size; ++x) {
									Object o = model.get(x);
									list.addElement(o);
								}
								getSequences(list);
								if (status.equalsIgnoreCase(STOP)) {
									sequenceDB = new CSSequenceSet();
									updateProgressBar(100, "Stopped on "
											+ new Date());
								} else {
									updateProgressBar(100, "Finished on "
											+ new Date());
									jSelectedList.updateUI();
									seqDisPanel
											.setRetrievedMap(currentRetrievedMap);
								}
								stopButton.setEnabled(false);
							}
						};
						t.setPriority(Thread.MIN_PRIORITY);
						t.start();
					}

				}
			}
			handlePostSearch();
			return !lastSearchFailed;
		}

		/**
		 * Search the markerList to get the matched markers.
		 */
		protected boolean findNext(String query) {
			model.removeAllElements();
			if (markerList != null) {
				Object theOne = markerList.get(query);
				if (theOne != null) {
					model.addElement(theOne);

				}
				for (Object o : markerList) {
					String element = o.toString().toLowerCase();
					if (element.contains(query)) {
						model.addElement(o);
					}
				}
			}

			return false;
		}

		protected void searchFieldChanged() {
			handlePreSearch();

			handlePostSearch();
		}

		/**
		 * Does nothing by default. Override to handle a list element being
		 * clicked.
		 *
		 * @param index
		 *            the list element that was clicked.
		 */
		protected void elementClicked(int index, MouseEvent e) {
		}

		/**
		 * Does nothing by default. Override to handle a list element being
		 * double-clicked.
		 *
		 * @param index
		 *            the list element that was clicked.
		 */
		protected void elementDoubleClicked(int index, MouseEvent e) {
		}

		/**
		 * Does nothing by default. Override to handle a list element being
		 * right-clicked.
		 *
		 * @param index
		 *            the list element that was clicked.
		 */
		protected void elementRightClicked(int index, MouseEvent e) {
		}

		public JList getList() {
			return list;
		}

		public ListModel getModel() {
			return model;
		}

		public int getHighlightedIndex() {
			return list.getSelectedIndex();
		}

		/**
		 * Set the highlightedIndex automatically.
		 *
		 * @param theIndex
		 *            int
		 * @return boolean
		 */
		public boolean setHighlightedIndex(int theIndex) {
			if (model != null && model.getSize() > theIndex) {
				list.setSelectedIndex(theIndex);
				list
						.scrollRectToVisible(list.getCellBounds(theIndex,
								theIndex));

				return true;
			}
			return false;
		}

		public boolean isPrefixMode() {
			return prefixMode;
		}

		public void setPrefixMode(boolean prefixMode) {
			this.prefixMode = prefixMode;
		}

	}

	public void matrixDetailButton_actionPerformed(ActionEvent e) {

	}

	private class SequenceListSelectionListener implements
			ListSelectionListener {
		int[] selectedRows;

		public void valueChanged(ListSelectionEvent e) {
			// Ignore extra messages.
			if (e.getValueIsAdjusting()) {
				return;
			}
			JList lsm = (JList) e.getSource();

			selectedRows = lsm.getSelectedIndices();
			updateSelectedSequenceDB(selectedRows);

		}

	}

	String generateHistStr()
	{
		String histStr = "Sequence Retriever\n";
		histStr += "get sequence parameters:\n";
		histStr += "Type Category: " +  jComboCategory.getSelectedItem().toString() + "\n";;
		histStr += "Source Category: " +  jSourceCategory.getSelectedItem().toString() + "\n";
		if (((String) jComboCategory.getSelectedItem())
				.equalsIgnoreCase(DNAVIEW)) {
			histStr += "Start Point:" + ((Integer) model.getNumber()).intValue() + "\n";
			histStr += "End Point:" + ((Integer) model1.getNumber()).intValue() + "\n";
			if (jSourceCategory.getSelectedItem().equals(UCSC))
				histStr += "Genome Assembly: " +  SequenceFetcher.getGenomeAssembly() + "\n";
		}

		histStr += "------------------------------------\n";
		return histStr;
	}





}
