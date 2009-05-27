package org.geworkbench.components.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.analysis.HighlightCurrentParameterThread;
import org.geworkbench.analysis.ParameterKey;
import org.geworkbench.analysis.ReHighlightable;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.Analysis;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.bison.model.analysis.ProteinStructureAnalysis;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;
import org.geworkbench.components.analysis.clustering.MultiTTestAnalysisPanel;
import org.geworkbench.components.analysis.clustering.TtestAnalysisPanel;
import org.geworkbench.components.cagrid.gui.GridServicePanel;
import org.geworkbench.engine.config.PluginRegistry;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.AnalysisInvokedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.ProjectNodeCompletedEvent;
import org.geworkbench.events.ProjectNodePendingEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.EdgeListDataSet;
import org.ginkgo.labs.util.FileTools;
import org.ginkgo.labs.ws.GridEndpointReferenceType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.dispatcher.client.DispatcherClient;

/**
 * Application component offering users a selection of microarray data
 * clustering options.
 * 
 * @author First Genetic Trust Inc.
 * @author keshav
 * @author yc2480
 * @version $Id: AnalysisPanel.java,v 1.81 2009-05-27 21:35:39 jiz Exp $
 * 
 */
@AcceptTypes( { DSMicroarraySet.class, AdjacencyMatrixDataSet.class,
		EdgeListDataSet.class, CSProteinStructure.class, CSSequenceSet.class })
public class AnalysisPanel extends MicroarrayViewEventBase implements
		VisualPlugin, ReHighlightable {

	private Log log = LogFactory.getLog(this.getClass());

	/* static variables */
	private static final String DEFAULT_PARAMETER_SETTING_NAME = "New Parameter Setting Name";
	private static final String SERVICE = "Service";
	private static final String PARAMETERS = "Parameters";
	private static final String USER_INFO = "userinfo";
	private static final int ANALYSIS_TAB_COUNT = 1;
	private static final String USER_INFO_DELIMIETER = "==";

	/* dispatcher */

	/* from application.properties */
	final static String DISPATCHER_URL = "dispatcher.url";

	/* from PropertiesManager (user preference) */
	private static final String GRID_HOST_KEY = "dispatcherURL";

	private String dispatcherUrl = System.getProperty(DISPATCHER_URL);

	private String userInfo = null;

	/* user interface */
	private JPanel analysisPanel = null;

	private JScrollPane analysisScrollPane = null;

	private JPanel innerAnalysisPanel = null;

	private BorderLayout analysisPanelBorderLayout = null;

	private BorderLayout borderLayout3 = null;

	private ParameterPanel emptyParameterPanel = null;

	private JSplitPane analysisParameterSplitPane = null;

	private JPanel selectedAnalysisParameterPanel = null;

	private ParameterPanel currentParameterPanel = null;

	private BorderLayout borderLayout4 = null;

	private BorderLayout borderLayout5 = null;

	private JPanel parameterPanel = null;

	private JButton analyze = null;

	private GridLayout gridLayout3 = null;

	private JScrollPane jScrollPane1 = null;

	private JPanel jPanel1 = null;

	private JScrollPane jScrollPane3 = null;

	private JTabbedPane jAnalysisTabbedPane = null;

	private GridServicePanel jGridServicePanel = null;

	/* buttons */
	private JButton save = null;
	private JButton delete = null;

	/* other */

	private List<Thread> threadList = new ArrayList<Thread>();

	/*
	 * Contains the pluggable clustering analysis available to the user to
	 * choose from. These analyses will have been defined in the application
	 * configuration file as <code>plugin</code> components and they are
	 * expected to have been associated with the extension point <code>clustering</code>.
	 */
	protected AbstractAnalysis[] availableAnalyses = null;
	protected AbstractAnalysis selectedAnalysis = null;

	private JList analysesJList = null;
	private JList paramsJList = null;

	/*
	 * Results obtained from execution of an analysis. This is an instance
	 * variable as the analysis is carried out on a worker thread.
	 */
	private AlgorithmExecutionResults results = null;

	/**
	 * Default Constructor
	 */
	public AnalysisPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		reset();
	}

	/**
	 * 
	 * @return Return the Analysis Panel
	 */
	public AnalysisPanel getAnalysisPanel() {
		return this;
	}

	/**
	 * Utility method to construct the GUI
	 * 
	 * @throws Exception
	 *             exception thrown during GUI construction
	 */
	public void jbInit() throws Exception {
		super.jbInit();
		analysisPanel = new JPanel();
		analysisScrollPane = new JScrollPane();
		innerAnalysisPanel = new JPanel();
		analysisPanelBorderLayout = new BorderLayout();
		borderLayout3 = new BorderLayout();
		emptyParameterPanel = new ParameterPanel();
		analysisParameterSplitPane = new JSplitPane();
		save = new JButton("Save Settings");
		delete = new JButton("Delete Settings");
		selectedAnalysisParameterPanel = new JPanel();
		currentParameterPanel = emptyParameterPanel;
		borderLayout4 = new BorderLayout();
		borderLayout5 = new BorderLayout();
		parameterPanel = new JPanel();

		analyze = new JButton("Analyze");
		/* Double it's width */
		Dimension d = analyze.getPreferredSize();
		d.setSize(d.getWidth() * 2, d.getHeight());
		analyze.setPreferredSize(d);

		gridLayout3 = new GridLayout();

		paramsJList = new JList();

		jScrollPane1 = new JScrollPane();
		jPanel1 = new JPanel();
		analysesJList = new JList();
		jScrollPane3 = new JScrollPane();

		analysisPanel.setLayout(analysisPanelBorderLayout);
		innerAnalysisPanel.setLayout(borderLayout3);

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_actionPerformed(e);
			}

		});
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delete_actionPerformed(e);
			}

		});

		selectedAnalysisParameterPanel.setLayout(borderLayout4);
		currentParameterPanel.setLayout(borderLayout5);

		parameterPanel.setLayout(new BorderLayout());
		analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analyze_actionPerformed(e);
			}

		});
		analysisParameterSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		analysisParameterSplitPane.setDividerSize(3);
		/* Make sure that only one parameter set can be selected at a time; */
		paramsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramsJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				namedParameterSelection_action(e);
			}
		});
		paramsJList.setAutoscrolls(true);
		paramsJList.setBorder(BorderFactory.createLineBorder(Color.black));
		jScrollPane1.setPreferredSize(new Dimension(248, 68));
		jPanel1.setLayout(gridLayout3);
		jPanel1.setMinimumSize(new Dimension(0, 0));
		jPanel1.setPreferredSize(new Dimension(50, 50));
		jPanel1.setToolTipText("");
		/* Make sure that only one analysis can be selected at a time; */
		analysesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		analysesJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				analysisSelected_action(e);
			}
		});
		analysesJList.setBorder(BorderFactory.createLineBorder(Color.black));
		gridLayout3.setColumns(2);
		analysisPanel.add(analysisScrollPane, BorderLayout.CENTER);
		analysisScrollPane.getViewport().add(innerAnalysisPanel, null);
		innerAnalysisPanel.add(analysisParameterSplitPane, BorderLayout.CENTER);
		analysisParameterSplitPane.add(parameterPanel, JSplitPane.BOTTOM);

		// FIXME - this doesn't seem to do anything
		selectedAnalysisParameterPanel.add(currentParameterPanel,
				BorderLayout.CENTER);

		parameterPanel.add(selectedAnalysisParameterPanel, BorderLayout.CENTER);

		/* buttons */
		save.setPreferredSize(analyze.getPreferredSize());
		delete.setPreferredSize(analyze.getPreferredSize());
		delete.setEnabled(false);

		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder buttonsBuilder = new DefaultFormBuilder(layout);
		buttonsBuilder.setDefaultDialogBorder();
		buttonsBuilder.appendSeparator("Analysis Actions");
		buttonsBuilder.append(analyze);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(save);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(delete);

		parameterPanel.add(buttonsBuilder.getPanel(), BorderLayout.LINE_END);

		analysisParameterSplitPane.add(jPanel1, JSplitPane.TOP);
		jPanel1.add(jScrollPane1, null);
		jPanel1.add(jScrollPane3, null);
		jScrollPane3.getViewport().add(paramsJList);
		jScrollPane1.getViewport().add(analysesJList);

		jAnalysisTabbedPane = new JTabbedPane();
		parameterPanel.setName(PARAMETERS);
		jAnalysisTabbedPane.add(parameterPanel);
		analysisParameterSplitPane.add(jAnalysisTabbedPane);

		mainPanel.add(analysisPanel, BorderLayout.CENTER);
	}

	/**
	 * Resets the list of analysis.
	 */
	private void reset() {
		getAvailableAnalyses();
		displayAnalyses();
	}

	/**
	 * Queries the {@link PluginRegistry} for available
	 * {@link ClusteringAnalysis} type plugins.
	 * 
	 * This method gets invoked every time that the analysis pane is placed into
	 * focus in order to get the most recent list of analysis. Given the dynamic
	 * loading of components this approach guarantees that any new plugins
	 * loaded between uses of the analysis panel, will be correctly picked up.
	 */
	public void getAvailableAnalyses() {
		/* To check if the last used analysis is still available. */
		boolean selectionChanged = true;
		ClusteringAnalysis[] analyses = ComponentRegistry.getRegistry()
				.getModules(ClusteringAnalysis.class);
		availableAnalyses = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableAnalyses[i] = (AbstractAnalysis) analyses[i];
			if (selectedAnalysis == availableAnalyses[i]) {
				selectionChanged = false;
			}
		}
		if (selectionChanged) {
			if (availableAnalyses.length > 0) {
				selectedAnalysis = availableAnalyses[0];
			} else {
				selectedAnalysis = null;
			}
		}
		String[] names = new String[availableAnalyses.length];
		for (int i = 0; i < availableAnalyses.length; i++) {
			names[i] = ComponentRegistry.getRegistry().getDescriptorForPlugin(
					availableAnalyses[i]).getLabel();
			availableAnalyses[i].setLabel(names[i]);
		}
	}

	@Publish
	@SuppressWarnings("unchecked")
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	/**
	 * Post analysis steps to check if analysis terminated properly and then to
	 * fire the appropriate application event
	 */
	@SuppressWarnings("unchecked")
	private void analysisDone() {
		/*
		 * If everything was OK construct and fire the proper application-level
		 * event, thus notify interested application components of the results
		 * of the analysis operation. If there were problems encountered, let
		 * the user know.
		 */
		if (results != null) {
			if (!results.isExecutionSuccessful()) {
				JOptionPane.showMessageDialog(null, results.getMessage(),
						"Analysis Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object resultObject = results.getResults();
			if (resultObject instanceof DSAncillaryDataSet) {
				DSAncillaryDataSet dataSet = (DSAncillaryDataSet) resultObject;
				final ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
						"Analysis Result", null, dataSet);
				publishProjectNodeAddedEvent(event);
				return;
			}
			if (resultObject instanceof Hashtable) {
				DSPanel<DSGeneMarker> panel = (DSPanel) ((Hashtable) resultObject)
						.get("Significant Genes");
				if (panel != null) {
					publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent(
							DSGeneMarker.class, panel, SubpanelChangedEvent.NEW));
				}
			}
		}
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public AnalysisInvokedEvent publishAnalysisInvokedEvent(
			AnalysisInvokedEvent event) {
		return event;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public ProjectNodePendingEvent publishProjectNodePendingEvent(
			ProjectNodePendingEvent event) {
		return event;
	}

	/**
	 * 
	 * @param pendingEvent
	 * @return
	 */
	@Publish
	public ProjectNodeCompletedEvent publishProjectNodeCompletedEvent(
			ProjectNodeCompletedEvent event) {
		log.info("publishing project node completed event");
		return event;
	}

	/**
	 * 
	 * @param ppne
	 * @param source
	 */
	@Subscribe
	public void receive(
			org.geworkbench.events.PendingNodeLoadedFromWorkspaceEvent ppne,
			Object source) {
		DispatcherClient dispatcherClient = null;
		try {
			PropertiesManager pm = PropertiesManager.getInstance();
			String savedHost = null;
			try {
				savedHost = pm.getProperty(this.getClass(), GRID_HOST_KEY,
						dispatcherUrl);
				if (!StringUtils.isEmpty(savedHost)) {
					dispatcherUrl = savedHost;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			dispatcherClient = new DispatcherClient(dispatcherUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collection<GridEndpointReferenceType> gridEprs = ppne.getGridEprs();

		for (GridEndpointReferenceType gridEpr : gridEprs) {

			PollingThread pollingThread = new PollingThread(getAnalysisPanel(),
					gridEpr, dispatcherClient);
			threadList.add(pollingThread);
			pollingThread.start();

		}

	}

	/**
	 * 
	 * @param maSetView
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String generateHistoryString(DSMicroarraySetView maSetView) {
		String ans = "";

		// TODO: this probably should get from method like
		// DSMicroarraySetView.toString() or
		// DSMicroarraySetView.generateHistoryString()

		/* Generate text for microarrays/groups */
		ans += "=The MicroarraySetView used for analysis contains following data="
				+ FileTools.NEWLINE;
		try {
			log.debug("We got a " + maSetView.items().getClass().toString());
			if (maSetView.items().getClass() == CSPanel.class) {
				log.debug("situation 1: microarraySets selected");
				DSItemList paneltest = ((DSPanel) maSetView.items()).panels();
				Iterator groups2 = paneltest.iterator(); /* groups */
				ans += "==Microarray Sets [" + paneltest.size() + "]=="
						+ FileTools.NEWLINE;
				while (groups2.hasNext()) {
					DSPanel temp = (DSPanel) groups2.next();
					ans += FileTools.TAB + temp.toString() + FileTools.NEWLINE;
					Iterator groups3 = temp.iterator(); /*
														 * microarrays in the
														 * group
														 */
					while (groups3.hasNext()) {
						Object temp2 = groups3.next();
						ans += FileTools.TAB + FileTools.TAB + temp2.toString()
								+ FileTools.NEWLINE;
					}
				}
			} else if (maSetView.items().getClass() == CSExprMicroarraySet.class) {
				log.debug("situation 2: microarraySets not selected");
				CSExprMicroarraySet exprSet = (CSExprMicroarraySet) maSetView
						.items();
				ans += "==Used Microarrays [" + exprSet.size() + "]=="
						+ FileTools.NEWLINE;
				for (Iterator<DSMicroarray> iterator = exprSet.iterator(); iterator
						.hasNext();) {
					DSMicroarray array = iterator.next();
					ans += FileTools.TAB + array.getLabel() + FileTools.NEWLINE;
				}
			}
			ans += "==End of Microarray Sets==" + FileTools.NEWLINE;
			/* Generate text for markers */
			DSItemList paneltest = maSetView.getMarkerPanel();
			if ((paneltest != null) && (paneltest.size() > 0)) {
				log.debug("situation 3: markers selected");
				Iterator groups2 = paneltest.iterator(); /* groups */
				ans += "==Used Markers [" + paneltest.size() + "]=="
						+ FileTools.NEWLINE;
				while (groups2.hasNext()) {
					CSExpressionMarker temp = (CSExpressionMarker) groups2
							.next();
					ans += FileTools.TAB + temp.getLabel() + FileTools.NEWLINE;
				}
			} else {
				log.debug("situation 4: no markers selected.");
				DSItemList<DSGeneMarker> markers = maSetView.markers();
				ans += "==Used Markers [" + markers.size() + "]=="
						+ FileTools.NEWLINE;
				for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
					DSGeneMarker marker = (DSGeneMarker) iterator.next();
					ans += FileTools.TAB + marker.getLabel()
							+ FileTools.NEWLINE;
				}
			}
			ans += "==End of Used Markers==" + FileTools.NEWLINE;
		} catch (ClassCastException cce) {
			/* it's not a DSPanel, we generate nothing for panel part */
			log.error(cce);
		}
		ans += "=End of MicroarraySetView data=";
		return ans;
	}

	/**
	 * 
	 * @param dataset
	 * @return
	 */
	// TODO: probably need to be more specific....
	@SuppressWarnings("unchecked")
	public String generateHistoryString(DSDataSet dataset) {
		if (dataset == null) {
			return "No information on the data set." + FileTools.NEWLINE;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("The data set used for analysis is [ ");
			sb.append(dataset.getDataSetName());
			sb.append(" ] from file [ ");
			sb.append(dataset.getPath());
			sb.append(" ]." + FileTools.NEWLINE);
			return sb.toString();
		}
	}

	@Subscribe
	public void receive(org.geworkbench.events.PendingNodeCancelledEvent e,
			Object source) {
		for (Iterator<Thread> iterator = threadList.iterator(); iterator
				.hasNext();) {
			PollingThread element = (PollingThread) iterator.next();
			if (element.getGridEPR() == e.getGridEpr()) {
				element.cancel();
			}
		}
	}

	/**
	 * 
	 * @return boolean
	 */
	private boolean isGridAnalysis() {
		// TODO: use this to check if it's a grid analysis or not is wrong. it
		// cause bug 0001174 in Mantis. Quick fix made user input service
		// information every time. Should have a better implementation.
		if (jGridServicePanel != null) {
			ButtonGroup gridSelectionButtonGroup = jGridServicePanel
					.getButtonGroup();

			ButtonModel bm = gridSelectionButtonGroup.getSelection();
			if (StringUtils.equals(bm.getActionCommand(), "Grid")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return String
	 */
	private String getServiceUrl() {
		ButtonGroup bg = jGridServicePanel.getServicesButtonGroup();

		ButtonModel bm = bg.getSelection();

		if (bm == null) {
			return null;
		}

		String url = bm.getActionCommand();
		return url;
	}

	/*
	 * 
	 */
	private void getUserInfo() {
		final JDialog userpasswdDialog = new JDialog();
		log.debug("getting user info...");

		DefaultFormBuilder usernamePasswdPanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:35dlu"));

		final JTextField usernameField = new JTextField(15);
		final JPasswordField passwordField = new JPasswordField(15);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = usernameField.getText();
				String passwd = new String(passwordField.getPassword());
				if (username.trim().equals("") || passwd.trim().equals("")) {
					userInfo = null;
				} else {
					userInfo = username + USER_INFO_DELIMIETER + passwd;
					PropertiesManager properties = PropertiesManager
							.getInstance();
					try {
						properties.setProperty(this.getClass(), USER_INFO,
								String.valueOf(userInfo));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				userpasswdDialog.dispose();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userInfo = "";
				userpasswdDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		usernamePasswdPanelBuilder.appendColumn("5dlu");
		usernamePasswdPanelBuilder.appendColumn("45dlu");

		usernamePasswdPanelBuilder.append("username", usernameField);
		usernamePasswdPanelBuilder.append("password", passwordField);

		PropertiesManager pm = PropertiesManager.getInstance();
		String savedUserInfo = null;
		try {
			savedUserInfo = pm.getProperty(this.getClass(), USER_INFO, "");
			if (!StringUtils.isEmpty(savedUserInfo)) {
				String s[] = savedUserInfo.split(USER_INFO_DELIMIETER);
				if (s.length >= 2) {
					usernameField.setText(s[0]);
					passwordField.setText(s[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		JPanel indexServicePanel = new JPanel(new BorderLayout());
		indexServicePanel.add(usernamePasswdPanelBuilder.getPanel());
		indexServicePanel.add(buttonPanel, BorderLayout.SOUTH);
		userpasswdDialog.add(indexServicePanel);
		userpasswdDialog.setModal(true);
		userpasswdDialog.pack();
		Util.centerWindow(userpasswdDialog);
		userpasswdDialog.setVisible(true);
		log.debug("got user info: " + userInfo);
	}

	/**
	 * Displays the list of available analyses.
	 */
	private void displayAnalyses() {
		/* Clean the list */
		analysesJList.removeAll();

		/* Get the display names of the available analyses. */
		String[] names = new String[availableAnalyses.length];
		for (int i = 0; i < availableAnalyses.length; i++) {
			names[i] = ComponentRegistry.getRegistry().getDescriptorForPlugin(
					availableAnalyses[i]).getLabel();
			if (log.isDebugEnabled())
				if (availableAnalyses[i] instanceof AbstractGridAnalysis) {
					log.info("Analysis: " + availableAnalyses[i]
							+ ", Is grid enabled? " + true);
				} else {
					log.info("Analysis: " + availableAnalyses[i]
							+ ", Is grid enabled? " + false);
				}
		}

		/* Show graphical components */
		analysesJList.setListData(names);
		if (selectedAnalysis != null) {
			analysesJList.setSelectedValue(selectedAnalysis.getLabel(), true);
		} else {
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
		}
		analysisPanel.revalidate();
	}

	/**
	 * Set the parameters panel used in the analysis pane.
	 * 
	 * @param parameterPanel
	 *            parameter panel stored on the file system
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		selectedAnalysisParameterPanel.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		selectedAnalysisParameterPanel.add(currentParameterPanel,
				BorderLayout.CENTER);
		analysisPanel.revalidate();
		analysisPanel.repaint();
		if (currentParameterPanel instanceof AbstractSaveableParameterPanel)
			((AbstractSaveableParameterPanel) currentParameterPanel)
					.setParameterHighlightCallback(new HighlightCurrentParameterThread(
							this));
	}

	/**
	 * Update the list that shows the known preset parameter settings for the
	 * selected filter.
	 * 
	 * @param storedParameters
	 */
	private void setNamedParameters(String[] storedParameters) {
		paramsJList.removeAll();
		paramsJList.setListData(storedParameters);

		/* make sure that only one parameter set can be selected at a time */
		paramsJList.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		analysisPanel.revalidate();
		highlightCurrentParameterGroup();
	}

	/**
	 * scan the saved list, see if the parameters in it are same as current one,
	 * if yes, highlight it.
	 */
	private void highlightCurrentParameterGroup() {
		ParameterPanel currentParameterPanel = selectedAnalysis
				.getParameterPanel();
		String[] parametersNameList = selectedAnalysis
				.getNamesOfStoredParameterSets();
		paramsJList.clearSelection();
		for (int i = 0; i < parametersNameList.length; i++) {
			Map<Serializable, Serializable> parameter1 = ((AbstractSaveableParameterPanel) currentParameterPanel)
					.getParameters();
			Map<Serializable, Serializable> parameter2 = new HashMap<Serializable, Serializable>();
			parameter2.putAll(selectedAnalysis
					.getNamedParameterSet(parametersNameList[i]));
			parameter2.remove(ParameterKey.class.getSimpleName());
			if (parameter1.equals(parameter2)) {
				/*
				 * Move matched one to the top of the list, so user can always
				 * see them.
				 */
				String[] savedParameterSetNames = selectedAnalysis
						.getNamesOfStoredParameterSets();
				/* savedParameterSetNames[i] will need to be moved to top */
				/*
				 * sets before it needs to move back, and it needs to be moved
				 * to the first one.
				 */
				String matchedOne = savedParameterSetNames[i];
				if (i != 0) {
					for (int j = i - 1; j >= 0; j--) {
						savedParameterSetNames[j + 1] = savedParameterSetNames[j];
					}
					savedParameterSetNames[0] = matchedOne;
				}
				/* set the JList to display the re-organized list */
				paramsJList.removeAll();
				paramsJList.setListData(savedParameterSetNames);
				/*
				 * make sure that only one parameter set can be selected at a
				 * time
				 */
				paramsJList.getSelectionModel().setSelectionMode(
						ListSelectionModel.SINGLE_SELECTION);
				analysisPanel.revalidate();
				/* select the first one (which matches current settings) */
				paramsJList.setSelectedIndex(0);
				/*
				 * Since we don't allow duplicate parameter sets in the list, so
				 * if we detect one, we can skip the rest.
				 */
				break;
			}
		}
	}

	/**
	 * 
	 */
	public void refreshHighLight() {
		highlightCurrentParameterGroup();
	}

	/**
	 * Delete the selected saved parameter.
	 * 
	 * @param name -
	 *            name of the saved parameter
	 */
	private void removeNamedParameter(String name) {
		selectedAnalysis.removeNamedParameter(name);
		this.setNamedParameters(selectedAnalysis
				.getNamesOfStoredParameterSets());
	}

	/* action listeners */
	/**
	 * Listener invoked when the "Save Settings" button is pressed.
	 * 
	 * @param e
	 */
	private void save_actionPerformed(ActionEvent e) {

		/*
		 * If the parameterSet already exist, we popup a message window to
		 * inform user
		 */
		if (selectedAnalysis
				.parameterSetExist(selectedAnalysis.getParameters())) {
			JOptionPane.showMessageDialog(null, "ParameterSet already exist.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			/*
			 * A pop-up window for the user to enter the parameter name. If the
			 * currently displayed parameter already has a name associated with
			 * it, use that name in the pop-up, otherwise the default.
			 */
			int index = paramsJList.getSelectedIndex();
			String namedParameter = null;
			if (index != -1) {
				namedParameter = (String) paramsJList.getModel().getElementAt(
						index);
				if (currentParameterPanel.isDirty()) {
					namedParameter = DEFAULT_PARAMETER_SETTING_NAME;
				}
			} else {
				namedParameter = DEFAULT_PARAMETER_SETTING_NAME;
			}
			String paramName = JOptionPane.showInputDialog(analysisPanel,
					namedParameter, namedParameter);
			File checkFile = new File(selectedAnalysis.scrubFilename(paramName));
			if (checkFile.exists()) {
				int answer = JOptionPane
						.showConfirmDialog(
								null,
								"Parameter set name already used by other training panels in the same directory. Click OK to override it, or click Cancel to choose another name.",
								"Warning", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			if (selectedAnalysis != null && paramName != null) {

				selectedAnalysis.saveParameters(paramName);

				String[] savedParameterSetNames = selectedAnalysis
						.getNamesOfStoredParameterSets();

				/* set the JList to display the saved parameter groups */
				setNamedParameters(savedParameterSetNames);

			}
		}
	}

	/**
	 * Listener invoked when the "Delete Settings" button is pressed
	 * 
	 * @param e
	 */
	private void delete_actionPerformed(ActionEvent e) {
		int choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to delete saved parameters?",
				"Deleting Saved Parameters", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if ((selectedAnalysis != null) && (choice == 0)
				&& (this.paramsJList.getSelectedIndex() >= 0)) {
			log.info("Deleting saved parameters: "
					+ (String) this.paramsJList.getSelectedValue());
			this.removeNamedParameter((String) this.paramsJList
					.getSelectedValue());
			if (this.paramsJList.getModel().getSize() < 1)
				this.delete.setEnabled(false);
		}
	}

	int previousSelectedIndex = -1;
	/**
	 * Listener invoked when an analysis is selected from the {@link JList} of
	 * analyses. The parameters for this analysis are shown.
	 * 
	 * @param lse
	 */
	private void analysisSelected_action(ListSelectionEvent lse) {
		if (analysesJList.getSelectedIndex() == -1) {
			return;
		}
		delete.setEnabled(false);

		int index = analysesJList.getSelectedIndex();
		selectedAnalysis = availableAnalyses[index];

		/* Set the parameters panel for the selected analysis. */
		ParameterPanel paramPanel = selectedAnalysis.getParameterPanel();
		if (paramPanel != null) {
			setParametersPanel(paramPanel);

			// TODO add some variant back in
			String[] storedParameterSetNames = availableAnalyses[index]
					.getNamesOfStoredParameterSets();
			setNamedParameters(storedParameterSetNames);

			if (paramPanel instanceof MultiTTestAnalysisPanel
					|| paramPanel instanceof TtestAnalysisPanel)
				super.chkAllArrays.setVisible(false);
			else
				super.chkAllArrays.setVisible(true);

			/*
			 * If it's first time (means just after load from file) for this
			 * analysis, assign last saved parameters to current parameter panel
			 * and highlight last saved group.
			 */
			if (paramPanel instanceof AbstractSaveableParameterPanel) {
				if (((AbstractSaveableParameterPanel) paramPanel).isFirstTime()) {
					selectLastSavedParameterSet();
					((AbstractSaveableParameterPanel) paramPanel)
							.setFirstTime(false);
				}
			}

			save.setEnabled(true);
		} else {
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
			/*
			 * Since the analysis admits no parameters, there are no named
			 * parametersettings to show.
			 */
			setNamedParameters(new String[0]);
		}

		if (selectedAnalysis instanceof AbstractGridAnalysis) {
			if (analysesJList.getSelectedIndex() != previousSelectedIndex){
				jGridServicePanel = new GridServicePanel(SERVICE);
				jGridServicePanel.setAnalysisType(selectedAnalysis);
				if (jAnalysisTabbedPane.getTabCount() > ANALYSIS_TAB_COUNT)
					jAnalysisTabbedPane.remove(ANALYSIS_TAB_COUNT);
	
				jAnalysisTabbedPane.addTab("Services", jGridServicePanel);
			}
		} else {
			jAnalysisTabbedPane.remove(jGridServicePanel);
			jGridServicePanel = null; // TODO this is just a quick fix for bug
			// 0001174, Quick fix made user input
			// service information every time.
			// Should have a better implementation.
		}
		previousSelectedIndex = analysesJList.getSelectedIndex();		
	}

	/**
	 * 
	 */
	private void selectLastSavedParameterSet() {
		int lastIndex = paramsJList.getModel().getSize() - 1;
		if (lastIndex >= 0) {
			String paramName = selectedAnalysis.getLastSavedParameterSetName();
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedAnalysis
					.getNamedParameterSet(paramName);
			if (parameters != null) // fix share directory issue in gpmodule
				selectedAnalysis.setParameters(parameters);
		} else {
			/* nothing saved, so select nothing */
		}
	}

	/**
	 * Listener invoked when a named parameter is selected.
	 * 
	 * @param e
	 */
	private void namedParameterSelection_action(ListSelectionEvent e) {
		if (selectedAnalysis == null) {
			delete.setEnabled(false);
			return;
		}
		int index = paramsJList.getSelectedIndex();
		if (index != -1) {
			delete.setEnabled(true);

			String paramName = (String) paramsJList.getModel().getElementAt(
					index);
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedAnalysis
					.getNamedParameterSet(paramName);
			selectedAnalysis.setParameters(parameters);
		}
	}

	/**
	 * Listener invoked when the "Analyze" button is pressed.
	 * 
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	private void analyze_actionPerformed(ActionEvent e) {
		maSetView = getDataSetView();

		if (currentParameterPanel instanceof MultiTTestAnalysisPanel
				|| currentParameterPanel instanceof TtestAnalysisPanel)
			onlyActivatedArrays = true;
		else
			onlyActivatedArrays = !chkAllArrays.isSelected();

		if (selectedAnalysis == null
				|| ((refMASet == null) && (refOtherSet == null))) {
			return;
		}

		if (refOtherSet != null) { /*
									 * added for analysis that do not take in
									 * microarray data set
									 */

			AnalysisInvokedEvent event = new AnalysisInvokedEvent(
					selectedAnalysis, "");
			publishAnalysisInvokedEvent(event);
		} else if ((maSetView != null) && (refMASet != null)) {
			AnalysisInvokedEvent event = new AnalysisInvokedEvent(
					selectedAnalysis, maSetView.getDataSet()
							.getLabel());
			publishAnalysisInvokedEvent(event);
		}

		ParamValidationResults pvr = selectedAnalysis.validateParameters();
		if (!pvr.isValid()) {
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
		} else {
			analyze.setEnabled(false);
			maSetView.useMarkerPanel(onlyActivatedMarkers);
			maSetView.useItemPanel(onlyActivatedArrays);
			Thread t = new Thread(new Runnable() {
				public void run() {
					ProgressBar pBar = null;

					try {
						/* check if we are dealing with a grid analysis */
						if (isGridAnalysis()) {
							ParamValidationResults validResult = ((AbstractGridAnalysis) selectedAnalysis)
									.validInputData(maSetView, refMASet);
							if (!validResult.isValid()) {
								JOptionPane.showMessageDialog(null, validResult
										.getMessage(), "Invalid Input Data",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
							/* ask for username and password */
							getUserInfo();
							if (userInfo == null) {
								JOptionPane
										.showMessageDialog(
												null,
												"Please make sure you entered valid username and password",
												"Invalid User Account",
												JOptionPane.ERROR_MESSAGE);
								return;
							}
							if (StringUtils.isEmpty(userInfo)) {
								userInfo = null;
								return;
							}
							pBar = Util.createProgressBar("Grid Services",
									"Submitting service request");
							pBar.start();
							pBar.reset();
							String url = getServiceUrl();
							if (!StringUtils.isEmpty(url)) {

								AbstractGridAnalysis selectedGridAnalysis = (AbstractGridAnalysis) selectedAnalysis;

								List<Serializable> serviceParameterList = ((AbstractGridAnalysis) selectedGridAnalysis)
										.handleBisonInputs(maSetView,
												refOtherSet);

								/* adding user info */
								serviceParameterList.add(userInfo);

								dispatcherUrl = jGridServicePanel.dispatcherLabelListener
										.getHost();
								DispatcherClient dispatcherClient = new DispatcherClient(
										dispatcherUrl);

								GridEndpointReferenceType gridEpr = dispatcherClient
										.submit(
												serviceParameterList,
												url,
												((AbstractGridAnalysis) selectedGridAnalysis)
														.getBisonReturnType());

								ProjectNodePendingEvent pendingEvent = new ProjectNodePendingEvent(
										"Analysis Pending", gridEpr);
								pendingEvent
										.setDescription(selectedGridAnalysis
												.getLabel()
												+ " (pending)");
								/* generate history for grid analysis */
								String history = "";
								history += "Grid service information:"
										+ FileTools.NEWLINE;
								history += FileTools.TAB + "Index server url: "
										+ jGridServicePanel.getIndexServerUrl();
								history += FileTools.TAB + "Dispatcher url: "
										+ dispatcherUrl + FileTools.NEWLINE;
								history += FileTools.TAB + "Service url: "
										+ url + FileTools.NEWLINE
										+ FileTools.NEWLINE;
								history += selectedAnalysis.createHistory();
								if (refOtherSet != null)
									history += generateHistoryString(refOtherSet);
								else if ((maSetView != null)
										&& (refMASet != null))
									history += generateHistoryString(maSetView);

								pendingEvent.setHistory(history);

								log.info("event is " + pendingEvent);

								publishProjectNodePendingEvent(pendingEvent);

								PollingThread pollingThread = new PollingThread(
										getAnalysisPanel(), gridEpr,
										dispatcherClient);
								threadList.add(pollingThread);
								pollingThread.start();

							} else {
								log.error("Cannot execute with url:  " + url);
								JOptionPane
										.showMessageDialog(
												null,
												"Cannot execute grid analysis: Invalid URL specified.",
												"Invalid grid URL Error",
												JOptionPane.ERROR_MESSAGE);
							}
						} else {
							if (refOtherSet != null) {
								/*
								 * added for analysis that do not take in
								 * microarray data set
								 */
								results = selectedAnalysis.execute(refOtherSet);
							} else if ((maSetView != null)
									&& (refMASet != null)) {
								// TODO: this validation procedure should move
								// to AbstractAnalysis
								if (selectedAnalysis instanceof AbstractGridAnalysis) {
									ParamValidationResults validResult = ((AbstractGridAnalysis) selectedAnalysis)
											.validInputData(maSetView, refMASet);
									if (!validResult.isValid()) {
										JOptionPane.showMessageDialog(null,
												validResult.getMessage(),
												"Invalid Input Data",
												JOptionPane.ERROR_MESSAGE);
										results = null;
										analysisDone();
										analyze.setEnabled(true);
										return;
									}
								}
								results = selectedAnalysis.execute(maSetView);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						analyze.setEnabled(true);
					}
					// FIXME don't use this check - remove projectNodeEvent from
					// executeGridAnalysis first
					if (!isGridAnalysis()) {
						analysisDone();
					} else {
						pBar.stop();
					}
				}

			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}
	
	/**
	 *  Refresh the list of available analyses. 
	 *  */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent even, Object source) {
		super.receive(even, source);
		if(even.getDataSet()!=null) {
			if (even.getDataSet().getClass().equals(CSProteinStructure.class)) {
				getAvailableProteinStructureAnalyses();
			} else if (even.getDataSet().getClass().equals(CSSequenceSet.class)) {
				getAvailableProteinSequenceAnalyses();
			} else {
				getAvailableAnalyses();
			}
			displayAnalyses();
		}
	}

	/**
	 * Get ProtainAnalysis - the analyses for PDB data files, similar to getAvailableAnalyses() for all ClusteringAnalysise.
	 */
	private void getAvailableProteinStructureAnalyses() {
		boolean selectionChanged = true;
		Analysis[] analyses = ComponentRegistry.getRegistry().getModules(
				ProteinStructureAnalysis.class);
		availableAnalyses = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableAnalyses[i] = (AbstractAnalysis) analyses[i];
			if (selectedAnalysis == availableAnalyses[i]) {
				selectionChanged = false;
			}
		}
		if (selectionChanged) {
			if (availableAnalyses.length > 0) {
				selectedAnalysis = availableAnalyses[0];
			} else {
				selectedAnalysis = null;
			}
		}
	}
	
	/**
	 * Get the protein sequence analyses.
	 */
	private void getAvailableProteinSequenceAnalyses() {
		boolean selectionChanged = true;
		Analysis[] analyses = ComponentRegistry.getRegistry().getModules(
				ProteinSequenceAnalysis.class);
		availableAnalyses = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableAnalyses[i] = (AbstractAnalysis) analyses[i];
			if (selectedAnalysis == availableAnalyses[i]) {
				selectionChanged = false;
			}
		}
		if (selectionChanged) {
			if (availableAnalyses.length > 0) {
				selectedAnalysis = availableAnalyses[0];
			} else {
				selectedAnalysis = null;
			}
		}
	}
}