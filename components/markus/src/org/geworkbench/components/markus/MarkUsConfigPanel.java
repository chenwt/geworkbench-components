package org.geworkbench.components.markus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Parameters panel used by Mark-Us.
 * 
 * @author meng
 * @author zji
 * @author yc2480
 * @version $Id: MarkUsConfigPanel.java,v 1.5 2009-02-25 21:38:09 chiangy Exp $
 */
public class MarkUsConfigPanel extends AbstractSaveableParameterPanel {
	private Log log = LogFactory.getLog(this.getClass());
	private static final long serialVersionUID = 238110585216063808L;

	// structure analysis
	JCheckBox skan = new JCheckBox("", true);
	JCheckBox dali = new JCheckBox();
	JCheckBox screen = new JCheckBox("", true);
	JCheckBox delphi = new JCheckBox("", true);

	// sequence analysis
	JCheckBox psiblast = new JCheckBox("", true);
	JCheckBox ips = new JCheckBox("", true);
	JCheckBox consurf = new JCheckBox("", true);
	JCheckBox consurf3 = new JCheckBox("", false);
	JCheckBox consurf4 = new JCheckBox("", false);

	JComboBox cbxChain = new JComboBox(new String[] { "A" });

	// delphi parameters
	JFormattedTextField gridsize = new JFormattedTextField();
	JFormattedTextField boxfill = new JFormattedTextField();
	JFormattedTextField steps = new JFormattedTextField();
	JFormattedTextField sc = new JFormattedTextField();
	JFormattedTextField radius = new JFormattedTextField();
	JComboBox ibc = new JComboBox(new String[] { "Zero", "Debye-Huckel Dipole",
			"Debye-Huckel Total" });
	JFormattedTextField nli = new JFormattedTextField();
	JFormattedTextField li = new JFormattedTextField();
	JFormattedTextField idc = new JFormattedTextField();
	JFormattedTextField edc = new JFormattedTextField();

	// consurf parameters
	JFormattedTextField csftitle3 = new JFormattedTextField();
	JFormattedTextField csftitle4 = new JFormattedTextField();
	JFormattedTextField eval3 = new JFormattedTextField();
	JFormattedTextField eval4 = new JFormattedTextField();
	JFormattedTextField iter3 = new JFormattedTextField();
	JFormattedTextField iter4 = new JFormattedTextField();
	JFormattedTextField filter3 = new JFormattedTextField();
	JFormattedTextField filter4 = new JFormattedTextField();
	JComboBox msa3 = new JComboBox(new String[] { "Muscle", "ClustalW" });
	JComboBox msa4 = new JComboBox(new String[] { "Muscle", "ClustalW" });

	// markus parameters 1st part - totally 9
	public boolean getskanValue() {
		return skan.isSelected();
	}

	public boolean getdaliValue() {
		return dali.isSelected();
	}

	public boolean getscreenValue() {
		return screen.isSelected();
	}

	public boolean getdelphiValue() {
		return delphi.isSelected();
	}

	public boolean getpsiblastValue() {
		return psiblast.isSelected();
	}

	public boolean getipsValue() {
		return ips.isSelected();
	}

	public boolean getconsurfValue() {
		return consurf.isSelected();
	}

	public boolean getconsurf3Value() {
		return consurf3.isSelected();
	}

	public boolean getconsurf4Value() {
		return consurf4.isSelected();
	}

	public String getChain() {
		return (String) cbxChain.getSelectedItem();
	}

	// delphi part - totally 10
	public int getgridsizeValue() {
		return ((Integer) gridsize.getValue()).intValue();
	}

	public int getboxfillValue() {
		return ((Integer) boxfill.getValue()).intValue();
	}

	public int getstepsValue() {
		return ((Integer) steps.getValue()).intValue();
	}

	public double getscValue() {
		return ((Double) sc.getValue()).doubleValue();
	}

	public double getradiusValue() {
		return ((Double) radius.getValue()).doubleValue();
	}

	public int getibcValue() {
		String desc = (String) ibc.getSelectedItem();
		if (desc.equals("Zero")) {
			return 1;
		} else if (desc.equals("Debye-Huckel Dipole")) {
			return 2;
		} else if (desc.equals("Debye-Huckel Total")) {
			return 4;
		}
		return 0;
	}

	public int getnliValue() {
		return ((Integer) nli.getValue()).intValue();
	}

	public int getliValue() {
		return ((Integer) li.getValue()).intValue();
	}

	public int getidcValue() {
		return ((Integer) idc.getValue()).intValue();
	}

	public int getedcValue() {
		return ((Integer) edc.getValue()).intValue();
	}

	// analysis 3 & 4
	public String getcsftitle3Value() {
		String val = (String) csftitle3.getValue();
		val = val.replaceAll(" ", "%20");
		return val;
	}

	public String getcsftitle4Value() {
		String val = (String) csftitle4.getValue();
		val = val.replaceAll(" ", "%20");
		return val;
	}

	public double geteval3Value() {
		try {
			return Double.parseDouble(eval3.getText());
		} catch (Exception e) {
			throw new NumberFormatException();
		}
	}

	public double geteval4Value() {
		try {
			return Double.parseDouble(eval4.getText());
		} catch (Exception e) {
			throw new NumberFormatException();
		}
	}

	public int getiter3Value() {
		try {
			return Integer.parseInt(iter3.getText());
		} catch (Exception e) {
			throw new NumberFormatException();
		}
	}

	public int getiter4Value() {
		try {
			return Integer.parseInt(iter4.getText());
		} catch (Exception e) {
			throw new NumberFormatException();
		}
	}

	public int getfilter3Value() {
		try {
			return Integer.parseInt(filter3.getText());
		} catch (Exception e) {
			throw new NumberFormatException();
		}
	}

	public int getfilter4Value() {
		try {
			return Integer.parseInt(filter4.getText());
		} catch (Exception e) {
			throw new NumberFormatException();
		}
	}

	public String getmsa3Value() {
		return (String) msa3.getSelectedItem();
	}

	public String getmsa4Value() {
		return (String) msa4.getSelectedItem();
	}

	public MarkUsConfigPanel() {
		JTabbedPane tp = new JTabbedPane(SwingConstants.TOP);
		tp.addTab("Markus Parameters", buildMainPanel());
		tp.addTab("DelPhi Parameters", buildDelphiPanel());
		tp.addTab("Add Customized ConSurf analysis 3", buildConsurf3Panel());
		tp.addTab("Add Customized ConSurf analysis 4", buildConsurf4Panel());

		ToolTipManager.sharedInstance().setDismissDelay(1000 * 120);

		setDefaultParameters();

		setLayout(new BorderLayout());
		add(tp, BorderLayout.PAGE_START);

		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		// structure analysis
		skan.addActionListener(parameterActionListener);
		dali.addActionListener(parameterActionListener);
		screen.addActionListener(parameterActionListener);
		delphi.addActionListener(parameterActionListener);

		// sequence analysis
		psiblast.addActionListener(parameterActionListener);
		ips.addActionListener(parameterActionListener);
		consurf.addActionListener(parameterActionListener);
		consurf3.addActionListener(parameterActionListener);
		consurf4.addActionListener(parameterActionListener);

		cbxChain.addActionListener(parameterActionListener);

		// delphi parameters
		gridsize.addPropertyChangeListener(parameterActionListener);
		boxfill.addPropertyChangeListener(parameterActionListener);
		steps.addPropertyChangeListener(parameterActionListener);
		sc.addPropertyChangeListener(parameterActionListener);
		radius.addPropertyChangeListener(parameterActionListener);
		ibc.addActionListener(parameterActionListener);
		nli.addPropertyChangeListener(parameterActionListener);
		li.addPropertyChangeListener(parameterActionListener);
		idc.addPropertyChangeListener(parameterActionListener);
		edc.addPropertyChangeListener(parameterActionListener);

		// consurf parameters
		csftitle3.addPropertyChangeListener(parameterActionListener);
		csftitle4.addPropertyChangeListener(parameterActionListener);
		eval3.addPropertyChangeListener(parameterActionListener);
		eval4.addPropertyChangeListener(parameterActionListener);
		iter3.addPropertyChangeListener(parameterActionListener);
		iter4.addPropertyChangeListener(parameterActionListener);
		filter3.addPropertyChangeListener(parameterActionListener);
		filter4.addPropertyChangeListener(parameterActionListener);
		msa3.addActionListener(parameterActionListener);
		msa4.addActionListener(parameterActionListener);

	}

	private JComponent buildMainPanel() {
		JPanel jp = new JPanel(new GridLayout(2, 2));
		jp.add(buildLeftPanel());
		jp.add(buildRightPanel());

		FormLayout layout = new FormLayout(
				"left:max(25dlu;pref), 10dlu, left:max(35dlu;pref), 2dlu, max(25dlu;pref), 5dlu, "
						+ "left:max(25dlu;pref), 2dlu, max(25dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.append("Chain", cbxChain);
		jp.add(builder.getPanel());
		return jp;
	}

	private JComponent buildLeftPanel() {
		FormLayout layout = new FormLayout(
				"left:max(25dlu;pref), 10dlu, left:max(35dlu;pref), 2dlu, max(25dlu;pref), 5dlu, "
						+ "left:max(25dlu;pref), 2dlu, max(25dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		// JComponent sp = builder.appendSeparator("Structure Homologs");
		JLabel sh = new JLabel("Structure Homologs");
		sh.setForeground(Color.blue);
		sh
				.setToolTipText("<html>Structure relationships are identified by the structure alignment method Skan. The reference database combines SCOP<br>domains and PDB entries filtered by 60% sequence identity. In addition the Dali structure alignment method can be used.</html>");
		builder.append(sh);

		skan.setEnabled(false);
		builder.append("Skan", skan);
		builder.append("Dali", dali);

		sh = new JLabel("Cavity Analysis");
		sh.setForeground(Color.blue);
		sh
				.setToolTipText("<html>SCREEN is used to identify protein cavities that are capable of binding chemical compounds. SCREEN will provide an<br>assessment of the druggability of each surface cavity based on its properties.</html>");
		builder.append(sh);

		screen.setEnabled(false);
		builder.append("SCREEN", screen);
		builder.nextLine();

		sh = new JLabel("Electrostatic Potential");
		sh.setForeground(Color.blue);
		sh
				.setToolTipText("<html>The electrostatic potential plays an important role in inferring protein properties like DNA binding regions or the enzymatic<br>activities. To calculate the electrostatic potential DelPhi is used. The default parameters are tuned to suit protein domains in<br>general, though adjustment by the user might be necessary. Read the DelPhi manual for a detailed parameter description.</html>");
		builder.append(sh);
		builder.append("DelPhi", delphi);

		JPanel p = new JPanel();
		TitledBorder tb = BorderFactory
				.createTitledBorder("Structure Analysis");
		p.setBorder(tb);
		p.add(builder.getPanel());
		return p;
	}

	private JComponent buildRightPanel() {
		FormLayout layout = new FormLayout(
				"left:max(25dlu;pref), 10dlu, left:max(35dlu;pref), 2dlu, max(25dlu;pref), 5dlu, "
						+ "left:max(25dlu;pref), 2dlu, max(25dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		JLabel sh = new JLabel("Sequence Homologs");
		sh.setForeground(Color.blue);
		sh
				.setToolTipText("<html>Proteins sharing sequence similarity with the target protein are identified by running three PSI BLAST iterations (E-value<br>0.001) against the UniProt reference database. Additionally sequence domain and motif databases are searched using the<br>InterProScan service at the EBI.</html>");
		builder.append(sh);

		psiblast.setEnabled(false);
		builder.append("PSI-BLAST", psiblast);
		ips.setEnabled(false);
		builder.append("InterProScan", ips);

		sh = new JLabel("<html>Amino Acid<br>Conservation Profile</html>");
		sh.setForeground(Color.blue);
		sh
				.setToolTipText("<html>Highly conserved amino acids can indicate functionally relevant regions. To identify these amino acids sequences identified<br>by BLAST sharing less than 80% identity are aligned using Muscle. For the resulting multiple sequence alignment ConSurf is<br>used to estimate the conservation scores. If seeds and full Pfam alignments are available, these are used additionally for<br>the conservation analysis.<br>Two ConSurf analyses can be defined by the User specifying the number of PSI-BLAST iterations, the E-value threshold,<br>and the sequence identity cutoff.</html>");
		builder.append(sh);
		consurf.setEnabled(false);
		builder.append("ConSurf", consurf);
		builder.nextLine();

		sh = new JLabel("Add Customized ConSurf");
		sh.setForeground(Color.blue);
		builder.append(sh);
		builder.append("analysis3", consurf3);
		builder.append("analysis4", consurf4);

		JPanel p = new JPanel();
		TitledBorder tb = BorderFactory.createTitledBorder("Sequence Analysis");
		p.setBorder(tb);
		p.add(builder.getPanel());
		return p;
	}

	private JComponent buildDelphiPanel() {
		FormLayout layout = new FormLayout(
				"left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "
						+ "left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "
						+ "left:max(40dlu;pref), 4dlu, max(60dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.append("Grid size", gridsize);
		builder.append("Percentage box fill", boxfill);
		builder.append("Focusing steps", steps);
		builder.nextLine();
		builder.append("Salt concentration", sc);
		builder.append("Probe radius", radius);
		builder.append("Initial boundary condition", ibc);
		builder.nextLine();
		builder.append("Non linear iterations", nli);
		builder.append("Linear iterations", li);
		builder.nextLine();
		builder.append("Internal dielectric constant", idc);
		builder.append("External dielectric constant", edc);

		return builder.getPanel();
	}

	private JComponent buildConsurf3Panel() {
		FormLayout layout = new FormLayout(
				"left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "
						+ "left:max(40dlu;pref), 4dlu, max(60dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.append("Title", csftitle3);
		builder.nextLine();
		builder.append("PSI-Blast E Value", eval3);
		builder.append("Iterations", iter3);
		builder.nextLine();
		builder.append("Identity Filter Percentage", filter3);
		builder.append("Multiple Sequence Alignment", msa3);

		return builder.getPanel();
	}

	private JComponent buildConsurf4Panel() {
		FormLayout layout = new FormLayout(
				"left:max(40dlu;pref), 4dlu, max(60dlu;pref), 5dlu, "
						+ "left:max(40dlu;pref), 4dlu, max(60dlu;pref)", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.append("Title", csftitle4);
		builder.nextLine();
		builder.append("PSI-Blast E Value", eval4);
		builder.append("Iterations", iter4);
		builder.nextLine();
		builder.append("Identity Filter Percentage", filter4);
		builder.append("Multiple Sequence Alignment", msa4);

		return builder.getPanel();
	}

	private void setDefaultParameters() {
		// structure analysis
		dali.setSelected(false);
		delphi.setSelected(true);

		// delphi
		int gridsizeValue = 145;
		gridsize.setValue(gridsizeValue);
		gridsize.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int boxfillValue = 85;
		boxfill.setValue(boxfillValue);
		boxfill.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int stepsValue = 3;
		steps.setValue(stepsValue);
		steps.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		double scValue = 0.145;
		sc.setValue(scValue);
		sc.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		double radiusValue = 1.4;
		radius.setValue(radiusValue);
		radius.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int ibcValue = 2;
		ibc.setSelectedIndex(ibcValue);
		int nliValue = 1000;
		nli.setValue(nliValue);
		nli.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int liValue = 1000;
		li.setValue(liValue);
		li.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int idcValue = 2;
		idc.setValue(idcValue);
		idc.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int edcValue = 80;
		edc.setValue(edcValue);
		edc.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

		// consurf
		String csftitle3Value = "analysis 3";
		csftitle3.setValue(csftitle3Value);
		csftitle3.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int msa3Value = 0;
		msa3.setSelectedIndex(msa3Value);

		String csftitle4Value = "analysis 4";
		csftitle4.setValue(csftitle4Value);
		csftitle4.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		int msa4Value = 0;
		msa4.setSelectedIndex(msa4Value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("skan", (Boolean) getskanValue());
		parameters.put("dali", (Boolean) getdaliValue());
		parameters.put("screen", (Boolean) getscreenValue());
		parameters.put("delphi", (Boolean) getdelphiValue());
		parameters.put("psiblast", (Boolean) getpsiblastValue());
		parameters.put("ips", (Boolean) getipsValue());
		parameters.put("consurf", (Boolean) getconsurfValue());
		parameters.put("consurf3", (Boolean) getconsurf3Value());
		parameters.put("consurf4", (Boolean) getconsurf4Value());
		parameters.put("Chain", (String) getChain());
		parameters.put("gridsize", (Integer) getgridsizeValue());
		parameters.put("boxfill", (Integer) getboxfillValue());
		parameters.put("steps", (Integer) getstepsValue());
		parameters.put("sc", (Double) getscValue());
		parameters.put("radius", (Double) getradiusValue());
		parameters.put("ibc", (Integer) getibcValue());
		parameters.put("nli", (Integer) getnliValue());
		parameters.put("li", (Integer) getliValue());
		parameters.put("idc", (Integer) getidcValue());
		parameters.put("edc", (Integer) getedcValue());
		parameters.put("csftitle3", (String) getcsftitle3Value());
		try {
			parameters.put("eval3", (Double) geteval3Value());
		} catch (NumberFormatException nfe) {
			parameters.put("eval3", "");
			log.error(nfe, nfe);
		}
		try {
			parameters.put("iter3", (Integer) getiter3Value());
		} catch (NumberFormatException nfe) {
			parameters.put("iter3", "");
			log.error(nfe, nfe);
		}
		try {
			parameters.put("filter3", (Integer) getfilter3Value());
		} catch (NumberFormatException nfe) {
			parameters.put("filter3", "");
			log.error(nfe, nfe);
		}
		parameters.put("msa3", (String) getmsa3Value());
		parameters.put("csftitle4", (String) getcsftitle4Value());
		try {
			parameters.put("eval4", (Double) geteval4Value());
		} catch (NumberFormatException nfe) {
			parameters.put("eval4", "");
			log.error(nfe, nfe);
		}
		try {
			parameters.put("iter4", (Integer) getiter4Value());
		} catch (NumberFormatException nfe) {
			parameters.put("iter4", "");
			log.error(nfe, nfe);
		}
		try {
			parameters.put("filter4", (Integer) getfilter4Value());
		} catch (NumberFormatException nfe) {
			parameters.put("filter4", "");
			log.error(nfe, nfe);
		}
		parameters.put("msa4", (String) getmsa4Value());
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("skan")) {
				skan.setSelected((Boolean) value);
			}
			if (key.equals("dali")) {
				dali.setSelected((Boolean) value);
			}
			if (key.equals("screen")) {
				screen.setSelected((Boolean) value);
			}
			if (key.equals("delphi")) {
				delphi.setSelected((Boolean) value);
			}
			if (key.equals("psiblast")) {
				psiblast.setSelected((Boolean) value);
			}
			if (key.equals("ips")) {
				ips.setSelected((Boolean) value);
			}
			if (key.equals("consurf")) {
				consurf.setSelected((Boolean) value);
			}
			if (key.equals("consurf3")) {
				consurf3.setSelected((Boolean) value);
			}
			if (key.equals("consurf4")) {
				consurf4.setSelected((Boolean) value);
			}
			if (key.equals("cbxChain")) {
				cbxChain.setSelectedItem((String) value);
			}
			if (key.equals("gridsize")) {
				gridsize.setValue((Integer) value);
			}
			if (key.equals("boxfill")) {
				boxfill.setValue((Integer) value);
			}
			if (key.equals("steps")) {
				steps.setValue((Integer) value);
			}
			if (key.equals("sc")) {
				sc.setValue((Double) value);
			}
			if (key.equals("radius")) {
				radius.setValue((Double) value);
			}
			if (key.equals("icb")) {
				Integer i = (Integer) value;
				if (i.compareTo(1) == 0) {
					ibc.setSelectedItem("Zero");
				} else if (i.compareTo(2) == 0) {
					ibc.setSelectedItem("Debye-Huckel Dipole");
				} else if (i.compareTo(4) == 0) {
					ibc.setSelectedItem("Debye-Huckel Total");
				}
			}
			if (key.equals("nli")) {
				nli.setValue((Integer) value);
			}
			if (key.equals("li")) {
				li.setValue((Integer) value);
			}
			if (key.equals("idc")) {
				idc.setValue((Integer) value);
			}
			if (key.equals("edc")) {
				edc.setValue((Integer) value);
			}
			if (key.equals("csftitle3")) {
				String s = (String) value;
				s = s.replaceAll("%20", " ");
				csftitle3.setValue(s);
			}
			if (key.equals("csftitle4")) {
				String s = (String) value;
				s = s.replaceAll("%20", " ");
				csftitle4.setValue(s);
			}
			if (key.equals("eval3")) {
				try {
					eval3.setValue((Double) value);
				} catch (Exception e) {
					eval3.setText("");
					log.error("problem parsing eval3");
				}
			}
			if (key.equals("eval4")) {
				try {
					eval4.setValue((Double) value);
				} catch (Exception e) {
					eval4.setText("");
					log.error("problem parsing eval4");
				}
			}
			if (key.equals("iter3")) {
				try {
					iter3.setValue((Integer) value);
				} catch (Exception e) {
					iter3.setText("");
					log.error("problem parsing iter3");
				}
			}
			if (key.equals("iter4")) {
				try {
					iter4.setValue((Integer) value);
				} catch (Exception e) {
					iter4.setText("");
					log.error("problem parsing iter4");
				}
			}
			if (key.equals("filter3")) {
				try {
					filter3.setValue((Integer) value);
				} catch (Exception e) {
					filter3.setText("");
					log.error("problem parsing filter3");
				}
			}
			if (key.equals("filter4")) {
				try {
					filter4.setValue((Integer) value);
				} catch (Exception e) {
					filter4.setText("");
					log.error("problem parsing filter4");
				}
			}
			if (key.equals("msa3")) {
				msa3.setSelectedItem((String) value);
			}
			if (key.equals("msa4")) {
				msa4.setSelectedItem((String) value);
			}
		}
		stopNotifyAnalysisPanelTemporary(false);
	}

}
