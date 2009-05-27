package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.analysis.ParameterKey;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.analysis.HighlightCurrentParameterThread;
import org.geworkbench.analysis.ReHighlightable;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.NormalizationEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: First Genetic Trust Inc.
 * </p>
 * 
 * Application component offering users a selection of microarray normalization
 * options.
 * 
 * @author First Genetic Trust, keshav, yc2480
 * @version $ID$
 */
@AcceptTypes( { DSMicroarraySet.class })
public class NormalizationPanel implements VisualPlugin, ReHighlightable {
	private Log log = LogFactory.getLog(this.getClass());
	/**
	 * The underlying panel for the normalization component
	 */
	protected JPanel normalizationPanel = new JPanel();
	private JSplitPane jSplitPane1 = new JSplitPane();

	/**
	 * Contains the pluggable normalizers available to the user to choose from.
	 * These normalizers will have been defined in the application configuration
	 * file as <code>plugin</code> components and they are expected to have
	 * been associated with the extension point <code>normalizers</code>.
	 * E.g., availableNormalizers[i].getLabel(), should give the display name
	 * for an analysis.
	 */
	protected AbstractAnalysis[] availableNormalizers;
	/**
	 * The currently selected microarray set.
	 */
	protected DSMicroarraySet<?> maSet = null;
	/**
	 * The most recently used normalizer.
	 */
	protected AbstractAnalysis selectedNormalizer = null;
	/**
	 * JList used to display the normalizers.
	 */
	protected JList pluginNormalizers = new JList();
	/**
	 * JList used to display named parameter settings for a selected normalizer.
	 */
	protected JList namedParameters = new JList();
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane2 = new JScrollPane();
	JPanel jPanel3 = new JPanel();
	JPanel jPanelControl = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	BorderLayout borderLayout3 = new BorderLayout();
	GridLayout gridLayout1 = new GridLayout();
	GridLayout gridLayout2 = new GridLayout();
	JButton analyze = new JButton("Normalize");
	JButton save = new JButton("Save Settings");
	JButton delete = new JButton("Delete Settings");
	JPanel jPanel4 = new JPanel();
	FlowLayout flowLayout1 = new FlowLayout();
	ParameterPanel emptyParameterPanel = new ParameterPanel();
	ParameterPanel currentParameterPanel = emptyParameterPanel;
	BorderLayout borderLayout4 = new BorderLayout();
	BorderLayout borderLayout5 = new BorderLayout();
	BorderLayout borderLayout6 = new BorderLayout();
	JPanel jPanel1 = new JPanel();
	GridLayout gridLayout3 = new GridLayout();
	JScrollPane jScrollPane1 = new JScrollPane();
	JScrollPane jScrollPane3 = new JScrollPane();

	/**
	 * Default Constructor
	 */
	public NormalizationPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Get (and display) the available normalizers from the
		 * ComponentRegistry
		 */
		reset();
	}

	/**
	 * Implementation of method from interface <code>VisualPlugin</code>.
	 * 
	 * @return
	 */
	public Component getComponent() {
		return normalizationPanel;
	}

	/*
	 * 
	 */
	private void jbInit() throws Exception {
		jSplitPane1 = new JSplitPane();
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setDividerSize(3);

		jPanel4.setLayout(borderLayout5);
		normalizationPanel.setLayout(borderLayout2);
		jPanel3.setLayout(borderLayout3);
		jPanelControl.setLayout(borderLayout4);
		gridLayout1.setColumns(2);
		gridLayout1.setRows(3);
		gridLayout1.setVgap(0);
		gridLayout2.setColumns(4);
		gridLayout2.setRows(3);
		analyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				normalization_actionPerformed(e);
			}

		});
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
		jPanel1.setLayout(gridLayout3);
		jPanel1.setMinimumSize(new Dimension(0, 0));
		jPanel1.setPreferredSize(new Dimension(50, 50));
		jPanel1.setMaximumSize(new Dimension(50, 100));
		jScrollPane1.setPreferredSize(new Dimension(248, 100));
		/* Make sure that only one normalizer can be selected at a time; */
		pluginNormalizers.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		pluginNormalizers.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				normalizerSelected_action(e);
			}

		});
		pluginNormalizers
				.setBorder(BorderFactory.createLineBorder(Color.black));
		/* Make sure that only one parameter set can be selected at a time; */
		namedParameters.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		namedParameters.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				namedParameterSelection_action(e);
			}

		});
		namedParameters.setAutoscrolls(true);
		namedParameters.setBorder(BorderFactory.createLineBorder(Color.black));
		normalizationPanel.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jPanel3, null);
		jPanel3.add(jSplitPane1, BorderLayout.CENTER);
		currentParameterPanel.setLayout(borderLayout6);
		jSplitPane1.add(jPanelControl, JSplitPane.BOTTOM);
		jPanelControl.add(jPanel4, BorderLayout.WEST);
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);

		/* Add buttons */
		analyze.setPreferredSize(delete.getPreferredSize());
		save.setPreferredSize(delete.getPreferredSize());
		delete.setEnabled(false);

		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Normalization Actions");
		builder.append(analyze);
		builder.nextLine();
		builder.append(save);
		builder.nextLine();
		builder.append(delete);

		jPanelControl.add(builder.getPanel(), BorderLayout.EAST);

		jSplitPane1.add(jPanel1, JSplitPane.TOP);
		jPanel1.add(jScrollPane1, null);
		jPanel1.add(jScrollPane3, null);
		jScrollPane3.getViewport().add(namedParameters, null);
		jScrollPane1.getViewport().add(pluginNormalizers, null);
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
		if ((selectedNormalizer != null) && (choice == 0)
				&& (namedParameters.getSelectedIndex() >= 0)) {
			log.info("Deleting saved parameters: "
					+ (String) namedParameters.getSelectedValue());
			this.removeNamedParameter((String) namedParameters
					.getSelectedValue());
			if (namedParameters.getModel().getSize() < 1)
				delete.setEnabled(false);
		}
	}
	
	/**
	 * Delete the selected saved parameter.
	 * 
	 * @param name -
	 *            name of the saved parameter
	 */
	private void removeNamedParameter(String name) {
		selectedNormalizer.removeNamedParameter(name);
		this.setNamedParameters(selectedNormalizer
				.getNamesOfStoredParameterSets());
	}


	/**
	 * Implementation of method from interface <code>ProjectListener</code>.
	 * Handles notifications about change of the currently selected microarray
	 * set.
	 * 
	 * @param pe
	 *            Project event containing the newly selected microarray set.
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent pe, Object source) {
		DSDataSet<?> dataSet = pe.getDataSet();
		if (dataSet instanceof DSMicroarraySet<?>) {
			maSet = (DSMicroarraySet<?>) dataSet;
			reset();
		}
	}

	/**
	 * Queries the extension point <code>normalizers</code> within the
	 * <code>PluginRegistry </code> for available normalizer-type plugins.
	 * 
	 * This method gets invoked every time that the normalization panel gets the
	 * focus, in order to get the most recent list of normalizers: given dynamic
	 * loading of components this approach guarantees that any new plugins
	 * loaded between uses of the normalization panel, will be correctly picked
	 * up.
	 */
	public void getAvailableAnalyses() {
		/* To check if the last used normalizer is still available. */
		boolean selectionChanged = true;
		/*
		 * Populate 'availableNormalizers[]' from ComponentRegistry.
		 */
		NormalizingAnalysis[] analyses = ComponentRegistry.getRegistry()
				.getModules(NormalizingAnalysis.class);
		availableNormalizers = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableNormalizers[i] = (AbstractAnalysis) analyses[i];
			if (selectedNormalizer == availableNormalizers[i])
				selectionChanged = false;
		}

		/*
		 * If the selectedNormalizer has been removed from the list of available
		 * normalizers, reset.
		 */
		if (selectionChanged)
			if (analyses.length > 0)
				selectedNormalizer = availableNormalizers[0];
			else
				selectedNormalizer = null;
	}

	/**
	 * Obtains from the <code>ComponentRegistry</code> and displays the set of
	 * available normalizers.
	 */
	public void reset() {
		/* Get the most recent available normalizers. Redisplay */
		getAvailableAnalyses();
		displayNormalizers();
	}

	/**
	 * Displays the list of available normalizers.
	 */
	private void displayNormalizers() {
		/* Clean the list */
		pluginNormalizers.removeAll();

		/* Get the display names of the available normalizers. */
		String[] names = new String[availableNormalizers.length];
		for (int i = 0; i < availableNormalizers.length; i++) {
			names[i] = availableNormalizers[i].getLabel();
		}
		/* Show graphical components */
		pluginNormalizers.setListData(names);

		/* Highlight selected Normalizer */
		if (selectedNormalizer != null)
			pluginNormalizers.setSelectedValue(selectedNormalizer.getLabel(),
					true);
		else {
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
		}
	}

	/**
	 * Set the parameters panel used in the normalization pane.
	 * 
	 * @param parameterPanel
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		jPanel4.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
		normalizationPanel.revalidate();
		normalizationPanel.repaint();
		/* Set the call back function for list highlighting. */
		if (currentParameterPanel instanceof AbstractSaveableParameterPanel)
			((AbstractSaveableParameterPanel) currentParameterPanel)
					.setParameterHighlightCallback(new HighlightCurrentParameterThread(
							this));
	}

	/**
	 * Update the list that shows the known preset parameter settings for the
	 * selected normalizer.
	 * 
	 * @param storedParameters
	 *            Parameter names you want to shown in the parameter set list UI
	 */
	private void setNamedParameters(String[] storedParameters) {
		namedParameters.removeAll();
		namedParameters.setListData(storedParameters);
		/* Make sure that only one parameter set can be selected at a time; */
		namedParameters.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		normalizationPanel.revalidate();
		highlightCurrentParameterGroup();
	}

	/**
	 * we'll need a flag to stop cycle events. eg: select set will change GUI,
	 * change GUI will refresh highlight.
	 */
	static boolean calledFromProgram = false;

	/**
	 * scan the saved list, check each parameter set, see if the parameters in
	 * it are the same as in current parameter panel, if yes, highlight it.
	 */
	private void highlightCurrentParameterGroup() {
		if (!calledFromProgram) {
			calledFromProgram = true;
			ParameterPanel currentParameterPanel = selectedNormalizer
					.getParameterPanel();
			String[] parametersNameList = selectedNormalizer
					.getNamesOfStoredParameterSets();
			namedParameters.clearSelection();
			for (int i = 0; i < parametersNameList.length; i++) {
				Map<Serializable, Serializable> parameter1 = ((AbstractSaveableParameterPanel) currentParameterPanel)
						.getParameters();
				Map<Serializable, Serializable> parameter2 = new HashMap<Serializable, Serializable>();
				parameter2.putAll(selectedNormalizer
						.getNamedParameterSet(parametersNameList[i]));
				parameter2.remove(ParameterKey.class.getSimpleName());
				if (parameter1.equals(parameter2)) {
					namedParameters.setSelectedIndex(i);
				}
			}
			calledFromProgram = false;
		}
	}

	/**
	 * Implement <code>ReHighlightable</code>, this method will be called by
	 * call back function.
	 * 
	 * When this method been called, the normalization panel will highlight the
	 * parameter group which contains same parameters as in current parameter
	 * panel.
	 */
	public void refreshHighLight() {
		highlightCurrentParameterGroup();
	}

	/**
	 * Listener invoked when a new normalizer is selected from the displayed
	 * list of normalizers.
	 * 
	 * @param lse
	 *            The <code>ListSelectionEvent</code> received from the list
	 *            selection.
	 */
	private void normalizerSelected_action(ListSelectionEvent lse) {
		if (pluginNormalizers.getSelectedIndex() == -1)
			return;
		delete.setEnabled(false);

		selectedNormalizer = availableNormalizers[pluginNormalizers
				.getSelectedIndex()];
		/* Set the parameters panel for the selected normalizer. */
		ParameterPanel paramPanel = selectedNormalizer.getParameterPanel();
		/*
		 * Set the list of available named parameters for the selected
		 * normalizer.
		 */
		if ((paramPanel != null)
				&& ((paramPanel instanceof AbstractSaveableParameterPanel) && (paramPanel
						.getClass() != AbstractSaveableParameterPanel.class))) {
			setParametersPanel(paramPanel);
			String[] storedParameterSetNames = selectedNormalizer
					.getNamesOfStoredParameterSets();
			setNamedParameters(storedParameterSetNames);
			/*
			 * If it's first time (means just after load from file) for this
			 * normalizer, assign last saved parameters to current normalization
			 * panel and highlight last saved group.
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
			/*
			 * Since the normalizer admits no parameters, there are no named
			 * parameter settings to show.
			 */
			setParametersPanel(this.emptyParameterPanel);
			setNamedParameters(new String[0]);

			save.setEnabled(false);
		}

	}

	/**
	 * This method is used for select and highlight the last saved parameter
	 * set.
	 * 
	 * This method will only be called once, when a normalizer been selected.
	 */
	private void selectLastSavedParameterSet() {
		int lastIndex = namedParameters.getModel().getSize() - 1;
		if (lastIndex >= 0) {
			String paramName = selectedNormalizer
					.getLastSavedParameterSetName();
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedNormalizer
					.getNamedParameterSet(paramName);
			selectedNormalizer.setParameters(parameters);
			if (parameters != null) // fix share directory issue in gpmodule
				selectedNormalizer.setParameters(parameters);
		} else {
			/* nothing saved, so select nothing */
		}
	}

	/**
	 * Listener invoked when a named parameter is selected from the relevant
	 * JList.
	 * 
	 * @param lse
	 *            the <code>ListSelectionEvent</code> received from the
	 *            <code>ListSelectionListener</code> listening to the
	 *            namedParameters JList
	 */
	private void namedParameterSelection_action(ListSelectionEvent e) {
		if (selectedNormalizer == null) {
			delete.setEnabled(false);
			return;
		}
		int index = namedParameters.getSelectedIndex();
		if (index != -1) {
			delete.setEnabled(true);

			String paramName = (String) namedParameters.getModel()
					.getElementAt(index);
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedNormalizer
					.getNamedParameterSet(paramName);
			selectedNormalizer.setParameters(parameters);
		}
	}

	/**
	 * Listener invoked when the "Normalization" button is pressed.
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by the "analyze" button
	 */
	private void normalization_actionPerformed(ActionEvent e) {
		if (selectedNormalizer == null || maSet == null)
			return;
		ParamValidationResults pvr = selectedNormalizer.validateParameters();
		if (!pvr.isValid()) {
			/* Bring up an error message */
			JOptionPane.showMessageDialog(null, pvr.getMessage(),
					"Parameter Validation Error", JOptionPane.ERROR_MESSAGE);
		} else {
			final int PROCEED_OPTION = 0;
			Object[] options = { "Proceed", "Cancel" };
			int n = JOptionPane
					.showOptionDialog(
							null,
							"You're making changes to the data. \nDo you want to save the current workspace before the change takes place?\n"
									+ "If you want to save the workspace, please click cancel and then save it from the application menu.",
							"Proceed to change?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, /*
																 * do not use a
																 * custom Icon
																 */
							options, /* the titles of buttons */
							options[0]); /* default button title */
			if (n != PROCEED_OPTION)
				return;

			/* Invoke the selected normalizer */
			AlgorithmExecutionResults results = selectedNormalizer
					.execute(maSet);
			/* If there were problems encountered, let the user know. */
			if (!results.isExecutionSuccessful()) {
				JOptionPane
						.showMessageDialog(null, results.getMessage(),
								"Normalizer Execution Error",
								JOptionPane.ERROR_MESSAGE);
				return;
			}

			/*
			 * If everything was OK, notify interested application components
			 * with the results of the normalization operation.
			 */
			if (results.getResults() instanceof DSMicroarraySet<?>) {
				DSMicroarraySet<?> normalizedData = (DSMicroarraySet<?>) results
						.getResults();
				publishNormalizationEvent(new NormalizationEvent(maSet,
						normalizedData, selectedNormalizer.getLabel()));
			} else {
				log
						.error("This shouldn't happen. results.getResults() should return a DSMicroarraySet<?>");
			}
		}
	}

	/**
	 * publish NormalizationEvent after we successfully execute the
	 * normalization.
	 */
	@Publish
	public org.geworkbench.events.NormalizationEvent publishNormalizationEvent(
			org.geworkbench.events.NormalizationEvent event) {
		return event;
	}

	/**
	 * Listener invoked when the "Save Parameters" button is pressed
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by "save" button
	 */
	private void save_actionPerformed(ActionEvent e) {
		if (selectedNormalizer.parameterSetExist(selectedNormalizer
				.getParameters())) {
			JOptionPane.showMessageDialog(null, "ParameterSet already exist.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
			/*
			 * Bring up a pop-up window for the user to enter the named to use.
			 * If the currently displayed parameter already has a name
			 * associated with it, use that name in the pop-up, otherwise show
			 * something like "New Parameter Setting Name".
			 */
			int index = namedParameters.getSelectedIndex();
			String namedParameter = null;
			if (index != -1) {
				namedParameter = (String) namedParameters.getModel()
						.getElementAt(index);
				if (currentParameterPanel.isDirty())
					namedParameter = "New Parameter Setting Name";
			} else {
				namedParameter = "New Parameter Setting Name";
			}

			String paramName = JOptionPane.showInputDialog(normalizationPanel,
					namedParameter, namedParameter);
			File checkFile = new File(selectedNormalizer
					.scrubFilename(paramName));
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
			if (selectedNormalizer != null && paramName != null) {
				selectedNormalizer.saveParameters(paramName);
				setNamedParameters(selectedNormalizer
						.getNamesOfStoredParameterSets());
			}
		}
	}
}