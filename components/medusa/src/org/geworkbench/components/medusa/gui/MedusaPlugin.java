package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.MedusaData;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This plugin sets the layout for the MEDUSA visualization.
 * 
 * @author keshav
 * @version $Id: MedusaPlugin.java,v 1.8 2007-06-15 17:10:46 keshav Exp $
 */
public class MedusaPlugin extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MedusaData medusaData = null;

	private String path = "temp/medusa/dataset/output/run1/";

	private String rulesPath = path + "rules/";

	private List<String> rulesFiles = null;

	public MedusaPlugin(MedusaData medusaData) {
		super();
		this.medusaData = medusaData;

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel motifPanel = new JPanel();

		int i = 0;
		List<DSGeneMarker> targets = medusaData.getTargets();

		List<DSGeneMarker> regulators = medusaData.getRegulators();

		double[][] targetMatrix = new double[targets.size()][];
		for (DSGeneMarker target : targets) {
			double[] data = medusaData.getArraySet().getRow(target);
			targetMatrix[i] = data;
			i++;
		}

		int j = 0;
		double[][] regulatorMatrix = new double[regulators.size()][];
		for (DSGeneMarker regulator : regulators) {
			double[] data = medusaData.getArraySet().getRow(regulator);
			regulatorMatrix[j] = data;
			j++;
		}

		List<String> targetNames = new ArrayList<String>();
		for (DSGeneMarker marker : targets) {
			targetNames.add(marker.getLabel());
		}

		List<String> regulatorNames = new ArrayList<String>();
		for (DSGeneMarker marker : regulators) {
			regulatorNames.add(marker.getLabel());
		}

		motifPanel.setLayout(new GridLayout(2, 3));

		/* dummy panel at position 0,0 of the grid */
		JPanel dummyPanel = new JPanel();
		motifPanel.add(dummyPanel);

		/* regulator heat map at postion 0,1 */
		DiscreteHeatMapPanel regulatorHeatMap = new DiscreteHeatMapPanel(
				regulatorMatrix, 1, 0, -1, regulatorNames, true);
		motifPanel.add(regulatorHeatMap);

		/* regulator labels at position 0,2 */
		FormLayout regulatorLabelLayout = new FormLayout("pref,60dlu", // columns
				"5dlu"); // add rows dynamically
		DefaultFormBuilder regulatorLabelBuilder = new DefaultFormBuilder(
				regulatorLabelLayout);
		regulatorLabelBuilder.nextRow();

		for (String name : regulatorNames) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText(name);
			checkBox.setSelected(true);
			regulatorLabelBuilder.append(checkBox);
			regulatorLabelBuilder.appendRow("10dlu");
		}
		motifPanel.add(regulatorLabelBuilder.getPanel());

		/* discrete hit or miss heat map */
		this.rulesFiles = new ArrayList<String>();

		for (int k = 0; k < medusaData.getMedusaCommand().getIter(); k++) {
			rulesFiles.add("rule_" + k + ".xml");
		}
		DiscreteHitOrMissHeatMapPanel hitOrMissPanel = new DiscreteHitOrMissHeatMapPanel(
				rulesPath, rulesFiles, targetNames, path);
		motifPanel.add(hitOrMissPanel);

		/* target heat map at postion 1,1 */
		DiscreteHeatMapPanel targetHeatMap = new DiscreteHeatMapPanel(
				targetMatrix, 1, 0, -1, targetNames, true, 120);
		motifPanel.add(targetHeatMap);

		/* target labels at position 1,2 */
		FormLayout targetLabelLayout = new FormLayout("pref,60dlu", // columns
				"75dlu"); // add rows dynamically
		DefaultFormBuilder targetLabelBuilder = new DefaultFormBuilder(
				targetLabelLayout);
		targetLabelBuilder.nextRow();

		for (String name : targetNames) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText(name);
			checkBox.setSelected(true);
			targetLabelBuilder.append(checkBox);
			targetLabelBuilder.appendRow("10dlu");
		}
		motifPanel.add(targetLabelBuilder.getPanel());

		// TODO add back in
		// JScrollPane scrollPane = new JScrollPane(motifPanel,
		// ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
		// ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// tabbedPane.add("Motif", scrollPane);
		tabbedPane.add("Motif", motifPanel);

		JPanel pssmPanel = new JPanel();
		tabbedPane.add("PSSM", pssmPanel);

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		this.revalidate();

	}
}
