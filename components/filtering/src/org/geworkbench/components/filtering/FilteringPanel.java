package org.geworkbench.components.filtering;

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
import org.geworkbench.analysis.HighlightCurrentParameterThread;
import org.geworkbench.analysis.ParameterKey;
import org.geworkbench.analysis.ReHighlightable;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.ComponentRegistry;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.FilteringEvent;
import org.ginkgo.labs.util.FileTools;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust, yc2480
 * @version $ID$
 */

/**
 * Application component offering users a selection of microarray filtering
 * options.
 */
@AcceptTypes( { DSMicroarraySet.class })
public class FilteringPanel implements VisualPlugin, ReHighlightable {
	private Log log = LogFactory.getLog(FilteringPanel.class);

	/**
	 * The underlying GUI panel for the filtering component
	 */
	protected JPanel filteringPanel = new JPanel();
	private JSplitPane jSplitPane1 = new JSplitPane();

	/**
	 * Contains the pluggable filters available to the user to choose from.
	 * These filters will have been defined in the application configuration
	 * file as <code>plugin</code> components and they are expected to have
	 * been associated with the extension point <code>filters</code>.
	 */
	protected AbstractAnalysis[] availableFilters;

	/**
	 * The currently selected microarray set.
	 */
	protected DSMicroarraySet<?> maSet = null;

	/**
	 * The most recently used filter.
	 */
	protected AbstractAnalysis selectedFilter = null;

	/**
	 * JList used to display the normalizers.
	 */
	protected JList pluginFilters = new JList();

	/**
	 * JList used to display named parameter settings for a selected filter.
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
	JButton filter = new JButton("Filter");
	JButton save = new JButton("Save Settings");
	JButton deleteSetting = new JButton("Delete Settings");
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

	public FilteringPanel() {
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

	public Component getComponent() {
		return filteringPanel;
	}

	/*
	 * 
	 */
	private void jbInit() throws Exception {
		jSplitPane1 = new JSplitPane();
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setDividerSize(3);

		jPanel4.setLayout(borderLayout5);
		filteringPanel.setLayout(borderLayout2);
		jPanel3.setLayout(borderLayout3);
		jPanelControl.setLayout(borderLayout4);

		gridLayout1.setColumns(2);
		gridLayout1.setRows(3);
		gridLayout1.setVgap(0);
		gridLayout2.setColumns(4);
		gridLayout2.setRows(3);
		filter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filtering_actionPerformed(e);
			}

		});
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_actionPerformed(e);
			}

		});
		deleteSetting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delete_actionPerformed(e);
			}
		});

		jScrollPane1.setPreferredSize(new Dimension(248, 80));
		jPanel1.setLayout(gridLayout3);
		jPanel1.setMinimumSize(new Dimension(0, 0));
		jPanel1.setPreferredSize(new Dimension(50, 50));
		jPanel1.setMaximumSize(new Dimension(50, 80));
		/* Make sure that only one filter can be selected at a time; */
		pluginFilters.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		pluginFilters.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				filterSelected_action(e);
			}

		});
		pluginFilters.setBorder(BorderFactory.createLineBorder(Color.black));
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
		filteringPanel.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jPanel3, null);
		jPanel3.add(jSplitPane1, BorderLayout.CENTER);
		currentParameterPanel.setLayout(borderLayout6);
		jSplitPane1.add(jPanelControl, JSplitPane.BOTTOM);
		jPanelControl.add(jPanel4, BorderLayout.WEST);
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);

		jSplitPane1.add(jPanel1, JSplitPane.TOP);
		jPanel1.add(jScrollPane1, null);
		jPanel1.add(jScrollPane3, null);
		jScrollPane3.getViewport().add(namedParameters, null);
		jScrollPane1.getViewport().add(pluginFilters, null);

		save.setPreferredSize(deleteSetting.getPreferredSize());
		filter.setPreferredSize(deleteSetting.getPreferredSize());
		deleteSetting.setEnabled(false);

		FormLayout layout = new FormLayout("right:100dlu,10dlu", "");
		DefaultFormBuilder buttonsBuilder = new DefaultFormBuilder(layout);
		buttonsBuilder.setDefaultDialogBorder();
		buttonsBuilder.append(filter);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(save);
		buttonsBuilder.nextLine();
		buttonsBuilder.append(deleteSetting);

		jPanelControl.add(buttonsBuilder.getPanel(), BorderLayout.EAST);
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
		if ((selectedFilter != null) && (choice == 0)
				&& (namedParameters.getSelectedIndex() >= 0)) {
			log.info("Deleting saved parameters: "
					+ (String) namedParameters.getSelectedValue());
			this.removeNamedParameter((String) namedParameters
					.getSelectedValue());
			if (namedParameters.getModel().getSize() < 1)
				deleteSetting.setEnabled(false);
		}
	}
	
	/**
	 * Delete the selected saved parameter.
	 * 
	 * @param name -
	 *            name of the saved parameter
	 */
	private void removeNamedParameter(String name) {
		selectedFilter.removeNamedParameter(name);
		this.setNamedParameters(selectedFilter
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
		if (dataSet != null && dataSet instanceof DSMicroarraySet) {
			maSet = (DSMicroarraySet<?>) dataSet;
			reset();
		}
	}

	/**
	 * Queries the extension point <code>filters</code> within the
	 * <code>ComponentRegistry </code> for available filter-type plugins.
	 * 
	 * This method gets invoked every time that the analysis pane gets the
	 * focus, in order to get the most recent list of filters: given dynamic
	 * loading of components this approach guarantees that any new plugins
	 * loaded between uses of the filtering panel, will be correctly picked up.
	 * 
	 * @return <code>true</code> if the most recently used filter is found in
	 *         the <code>ComponentRegistry </code>. <code>fales</code>,
	 *         otherwise.
	 */
	public void getAvailableFilters() {
		/* To check if the last used normalizer is still available. */
		boolean selectionChanged = true;
		/* Populate 'availableFilters[]' from ComponentRegistry. */
		FilteringAnalysis[] analyses = ComponentRegistry.getRegistry()
				.getModules(FilteringAnalysis.class);
		availableFilters = new AbstractAnalysis[analyses.length];
		for (int i = 0; i < analyses.length; i++) {
			availableFilters[i] = (AbstractAnalysis) analyses[i];
			if (selectedFilter == availableFilters[i])
				selectionChanged = false;
		}

		/*
		 * If the selectedFilter has been removed from the list of available
		 * normalizers, reset.
		 */
		if (selectionChanged)
			if (analyses.length > 0)
				selectedFilter = availableFilters[0];
			else
				selectedFilter = null;
	}

	/**
	 * Obtains from the <code>ComponentRegistry</code> and displays the set of
	 * available filters.
	 */
	public void reset() {
		/* Get the most recent available normalizers. Redisplay */
		getAvailableFilters();
		displayFilters();
	}

	/**
	 * Displays the list of available filters.
	 */
	private void displayFilters() {
		/* Clear the list */
		pluginFilters.removeAll();
		/* Stores the display names of the available filters. */
		String[] names = new String[availableFilters.length];
		for (int i = 0; i < availableFilters.length; i++) {
			names[i] = availableFilters[i].getLabel();
		}

		pluginFilters.setListData(names);
		if (selectedFilter != null)
			pluginFilters.setSelectedValue(selectedFilter.getLabel(), true);
		else {
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
		}

	}

	/**
	 * Set the parameters panel used in the filtering pane.
	 * 
	 * @param parameterPanel
	 */
	private void setParametersPanel(ParameterPanel parameterPanel) {
		jPanel4.remove(currentParameterPanel);
		currentParameterPanel = parameterPanel;
		jPanel4.add(currentParameterPanel, BorderLayout.CENTER);
		filteringPanel.revalidate();
		filteringPanel.repaint();
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
		namedParameters.removeAll();
		namedParameters.setListData(storedParameters);
		/* Make sure that only one parameter set can be selected at a time; */
		namedParameters.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		filteringPanel.revalidate();
		highlightCurrentParameterGroup();
	}

	/**
	 * Scan the saved list, see if the parameters in it are same as current one,
	 * if yes, highlight it.
	 */
	private void highlightCurrentParameterGroup() {
		ParameterPanel currentParameterPanel = selectedFilter
				.getParameterPanel();
		String[] parametersNameList = selectedFilter
				.getNamesOfStoredParameterSets();
		namedParameters.clearSelection();
		for (int i = 0; i < parametersNameList.length; i++) {
			Map<Serializable, Serializable> parameter1 = ((AbstractSaveableParameterPanel) currentParameterPanel)
					.getParameters();
			Map<Serializable, Serializable> parameter2 = new HashMap<Serializable, Serializable>();
			parameter2.putAll(selectedFilter
					.getNamedParameterSet(parametersNameList[i]));
			parameter2.remove(ParameterKey.class.getSimpleName());
			if (parameter1.equals(parameter2)) {
				namedParameters.setSelectedIndex(i);
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
	 * Listener invoked when a new filter is selected from the displayed list of
	 * filters.
	 * 
	 * @param lse
	 *            The <code>ListSelectionEvent</code> received from the list
	 *            selection.
	 */
	private void filterSelected_action(ListSelectionEvent lse) {
		if (pluginFilters.getSelectedIndex() == -1)
			return;
		deleteSetting.setEnabled(false);

		selectedFilter = availableFilters[pluginFilters.getSelectedIndex()];
		/* Get the parameters panel for the selected filter. */
		ParameterPanel paramPanel = selectedFilter.getParameterPanel();
		/* Set the list of available named parameters for the selected filter. */
		if (paramPanel != null) {
			setParametersPanel(paramPanel);
			setNamedParameters(availableFilters[pluginFilters
					.getSelectedIndex()].getNamesOfStoredParameterSets());

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
			setParametersPanel(this.emptyParameterPanel);
			save.setEnabled(false);
			/*
			 * Since the filter admits no parameters, there are no named
			 * parameter settings to show.
			 */
			setNamedParameters(new String[0]);
		}

	}

	/**
	 * 
	 */
	private void selectLastSavedParameterSet() {
		int lastIndex = namedParameters.getModel().getSize() - 1;
		if (lastIndex >= 0) {
			String paramName = selectedFilter.getLastSavedParameterSetName();
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedFilter
					.getNamedParameterSet(paramName);
			if (parameters != null) // fix share directory issue in gpmodule
				selectedFilter.setParameters(parameters);
		} else {
			/* nothing saved, so select nothing */
		}
	}

	/**
	 * Listener invoked when a named parameter is selected from the relevant
	 * JList.
	 * 
	 * @param lse
	 *            the <code>MouseEvent</code> received from the
	 *            <code>MouseListener</code> listening to the namedParameters
	 *            JList
	 */
	private void namedParameterSelection_action(ListSelectionEvent e) {
		if (selectedFilter == null) {
			deleteSetting.setEnabled(false);
			return;
		}
		int index = namedParameters.getSelectedIndex();
		if (index != -1) {
			deleteSetting.setEnabled(true);

			String paramName = (String) namedParameters.getModel()
					.getElementAt(index);
			/* load from memory */
			Map<Serializable, Serializable> parameters = selectedFilter
					.getNamedParameterSet(paramName);
			selectedFilter.setParameters(parameters);
		}
	}

	/**
	 * Listener invoked when the "Filter" button is pressed.
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by the "analyze" button
	 */
	private void filtering_actionPerformed(ActionEvent e) {
		if (selectedFilter == null || maSet == null)
			return;
		ParamValidationResults pvr = selectedFilter.validateParameters();
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

			/* Invoke the selected filter */
			AlgorithmExecutionResults results = selectedFilter.execute(maSet);
			/* If there were problems encountered, let the user know. */
			if (!results.isExecutionSuccessful()) {
				JOptionPane.showMessageDialog(null, results.getMessage(),
						"Filter Execution Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			/*
			 * If everything was OK, notify interested application components
			 * with the results of the normalization operation.
			 */
			DSMicroarraySet<?> filteredData = (DSMicroarraySet<?>) results
					.getResults();
			String historyString = "";
			if (selectedFilter.getLabel() != null)
				historyString += selectedFilter.getLabel() + FileTools.NEWLINE;
			if (selectedFilter.createHistory() != null) /*
														 * to avoid printing
														 * null for panels
														 * didn't implement this
														 * method.
														 */
				historyString += selectedFilter.createHistory()
						+ FileTools.NEWLINE;
			historyString += FileTools.NEWLINE; /*
												 * to separate with next section
												 * (if any)
												 */
			publishFilteringEvent(new FilteringEvent(maSet, filteredData,
					historyString));
		}

	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public FilteringEvent publishFilteringEvent(FilteringEvent event) {
		return event;
	}

	/**
	 * Listener invoked when the "Save Parameters" button is pressed
	 * 
	 * @param e
	 *            <code>ActionEvent</code> generated by "save" button
	 */
	private void save_actionPerformed(ActionEvent e) {
		/*
		 * If the parameterSet already exist, we popup a message window to
		 * inform user
		 */
		if (selectedFilter.parameterSetExist(selectedFilter.getParameters())) {
			JOptionPane.showMessageDialog(null, "ParameterSet already exist.",
					"Canceled", JOptionPane.OK_OPTION);
		} else {
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

			String paramName = JOptionPane.showInputDialog(filteringPanel,
					namedParameter, namedParameter);
			File checkFile = new File(selectedFilter.scrubFilename(paramName));
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
			if (selectedFilter != null && paramName != null) {
				selectedFilter.saveParameters(paramName);
				setNamedParameters(selectedFilter
						.getNamesOfStoredParameterSets());
			}
		}
	}

}
