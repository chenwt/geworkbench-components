package org.geworkbench.components.cagrid.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: GridServicePanel.java,v 1.22 2007-04-03 05:26:57 keshav Exp $
 */
public class GridServicePanel extends JPanel {
	private Log log = LogFactory.getLog(this.getClass());

	JPanel innerPanel = null;

	JPanel outerPanel = null;

	JScrollPane serviceDetailsScrollPane = null;

	ButtonGroup buttonGroup = null;

	Collection<String> analysisSet = new HashSet<String>();

	GridServicesButtonListener gridServicesButtonListener;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GridServicePanel(String name) {
		super();
		super.setName(name);
		super.setLayout(new BorderLayout());

		analysisSet.add("Hierarchical");
		analysisSet.add("Som");
		analysisSet.add("Aracne");

		/* part A */
		DefaultFormBuilder indexServiceBuilder = new DefaultFormBuilder(
				new FormLayout(""));
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");
		indexServiceBuilder.appendColumn("right:pref");
		indexServiceBuilder.appendColumn("10dlu");

		String localButtonString = "Local";
		JRadioButton localButton = new JRadioButton(localButtonString);
		localButton.setSelected(true);
		localButton.setActionCommand(localButtonString);

		String gridButtonString = "Grid";
		JRadioButton gridButton = new JRadioButton(gridButtonString);
		gridButton.setSelected(false);
		gridButton.setActionCommand(gridButtonString);
		/* add to the button group */
		buttonGroup = new ButtonGroup();
		buttonGroup.add(localButton);
		buttonGroup.add(gridButton);

		indexServiceBuilder.append(localButton);
		indexServiceBuilder.append(gridButton);

		// index service label
		JLabel indexServiceLabel = new JLabel("Change Index Service");
		indexServiceLabel.setForeground(Color.BLUE);

		// index service label listener
		final IndexServiceLabelListener indexServiceLabelListener = new IndexServiceLabelListener(
				indexServiceLabel);
		indexServiceLabel.addMouseListener(indexServiceLabelListener);
		indexServiceBuilder.append(indexServiceLabel);

		// grid services button
		JButton getServicesButton = indexServiceLabelListener
				.getIndexServiceButton();
		indexServiceBuilder.append(getServicesButton);

		/* part B */
		final DefaultFormBuilder urlServiceBuilder = createUrlServiceBuilder();

		JScrollPane urlServiceBuilderScrollPane = new JScrollPane(
				urlServiceBuilder.getPanel(),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		/* part C */
		final IndexServiceSelectionButtonListener indexServiceSelectionButtonListener = new IndexServiceSelectionButtonListener();

		gridServicesButtonListener = new GridServicesButtonListener(
				indexServiceSelectionButtonListener, indexServiceLabelListener,
				urlServiceBuilder);
		getServicesButton.addActionListener(gridServicesButtonListener);

		/* add A, B, and C to the main (this) */
		this.add(indexServiceBuilder.getPanel(), BorderLayout.NORTH);

		this.add(urlServiceBuilderScrollPane);

		this.add(indexServiceSelectionButtonListener
				.getServiceDetailsBuilderScrollPane(), BorderLayout.SOUTH);
		this.revalidate();
	}

	/**
	 * 
	 * @return
	 */
	public static DefaultFormBuilder createUrlServiceBuilder() {
		final DefaultFormBuilder urlServiceBuilder = new DefaultFormBuilder(
				new FormLayout(""));
		urlServiceBuilder.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");
		urlServiceBuilder.appendColumn("center:pref");
		urlServiceBuilder.appendColumn("10dlu");

		urlServiceBuilder.append("");
		urlServiceBuilder.append("Grid Service URL");
		urlServiceBuilder.append("Research Center Name");
		urlServiceBuilder.append("Description");
		return urlServiceBuilder;
	}

	/**
	 * 
	 * @param analysisType
	 */
	public void setAnalysisType(AbstractAnalysis analysisType) {

		for (String type : analysisSet) {
			if (StringUtils.lowerCase(analysisType.getLabel()).contains(
					StringUtils.lowerCase(type))) {
				log.info("Analysis is " + type);
				gridServicesButtonListener.setSelectedAnalysisType(type);
				break;
			}
		}

	}

	/**
	 * 
	 * @return
	 */
	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	/**
	 * 
	 * @return
	 */
	public ButtonGroup getServicesButtonGroup() {
		return gridServicesButtonListener.getServicesButtonGroup();
	}
}
