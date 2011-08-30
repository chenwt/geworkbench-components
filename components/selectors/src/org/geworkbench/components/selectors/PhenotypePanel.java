package org.geworkbench.components.selectors;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreePath;

import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.selectors.GenePanel.MarkerPanelSetFileFilter;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.SingleMicroarrayEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.Util;

import com.Ostermiller.util.ExcelCSVParser;

/**
 * @author John Watkinson
 * @version $Id$
 */
public class PhenotypePanel extends SelectorPanel<DSMicroarray> {

    public static final String[] CLASSES =
            {"Case", "Control", "Test", "Ignore"};

    public static final Color[] CLASS_COLORS =
            {Color.RED, Color.WHITE, Color.GREEN, Color.LIGHT_GRAY};

    public static final ImageIcon[] CLASS_ICONS =
            {
                    new ImageIcon(PhenotypePanel.class.getResource("redpin.gif")),
                    new ImageIcon(PhenotypePanel.class.getResource("whitepin.gif")),
                    new ImageIcon(PhenotypePanel.class.getResource("greenpin.gif")),
                    new ImageIcon(PhenotypePanel.class.getResource("graypin.gif"))
            };

    public static final String DEFAULT_CLASS = "Control";

    private class ClassificationListener implements ActionListener {
        private int classIndex;

        public ClassificationListener(int classIndex) {
            this.classIndex = classIndex;
        }

        public void actionPerformed(ActionEvent e) {
            String[] labels = getSelectedTreesFromTree();
            if (labels.length > 0) {
                for (int i = 0; i < labels.length; i++) {
                    context.assignClassToLabel(labels[i], CLASSES[classIndex]);
                    // Notify model
                    treeModel.fireLabelChanged(labels[i]);
                }
                throwLabelEvent();
            }
        }
    }

    private static class PhenotypeCellRenderer extends SelectorTreeRenderer {

		private static final long serialVersionUID = 5317111542943009611L;

		public PhenotypeCellRenderer(final PhenotypePanel panel) {
            super(panel);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (value instanceof String) {
                String label = (String) value;
                String clazz = selectorPanel.getContext().getClassForLabel(label);
                int index = getIndexForClass(clazz);
                cellLabel.setIcon(CLASS_ICONS[index]);
            } else if (value instanceof DSMicroarray) {
                // TODO - somehow show class of individual items
//                String clazz = selectorPanel.getContext().getClassForItem((DSMicroarray) value);
//                int index = getIndexForClass(clazz);
//                comp.setBackground(CLASS_COLORS[index]);
            }
            return comp;
        }
    }

    private DSMicroarraySet<DSMicroarray> set;
    private JRadioButtonMenuItem[] rightClickClassButtons;
    private JRadioButtonMenuItem[] leftClickClassButtons;
    private JPopupMenu classPopup;

    public PhenotypePanel() {
        super(DSMicroarray.class, "Array/Phenotype");
        // Add "Classification" item and sub-items
        JMenu classificationMenu = new JMenu("Classification");
        classPopup = new JPopupMenu();
        ButtonGroup classGroup = new ButtonGroup();
        rightClickClassButtons = new JRadioButtonMenuItem[4];
        leftClickClassButtons = new JRadioButtonMenuItem[4];
        for (int i = 0; i < CLASSES.length; i++) {
            rightClickClassButtons[i] = new JRadioButtonMenuItem(CLASSES[i]);
            leftClickClassButtons[i] = new JRadioButtonMenuItem(CLASSES[i]);
            classificationMenu.add(rightClickClassButtons[i]);
            classPopup.add(leftClickClassButtons[i]);
            rightClickClassButtons[i].addActionListener(new ClassificationListener(i));
            leftClickClassButtons[i].addActionListener(new ClassificationListener(i));
            classGroup.add(rightClickClassButtons[i]);
            classGroup.add(leftClickClassButtons[i]);
        }
        treePopup.add(classificationMenu);
        setTreeRenderer(new PhenotypeCellRenderer(this));
        // Add classification legend
        JPanel legend = new JPanel();
        legend.setBackground(Color.WHITE);
        legend.setLayout(new BoxLayout(legend, BoxLayout.X_AXIS));
        for (int i = 0; i < CLASSES.length; i++) {
            JLabel classLabel = new JLabel(CLASSES[i], CLASS_ICONS[i], SwingConstants.TRAILING);
            classLabel.setIconTextGap(0);
            classLabel.addMouseListener(new MouseAdapter(){
            	public void mousePressed(MouseEvent e){
            		String classname = null;
            		JLabel label = (JLabel)e.getComponent();
            		if (label!=null) classname = label.getText();
            		loadButtonPressed(classname);
            	}
            });
            legend.add(classLabel);
            legend.add(Box.createHorizontalGlue());
        }
        lowerPanel.add(legend);
    }

    private void loadButtonPressed(String classname){
    	helper = getSelectorHelper();
		JFileChooser fc = new JFileChooser(".");
		javax.swing.filechooser.FileFilter filter = new MarkerPanelSetFileFilter();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filter);
		fc.setDialogTitle("Load "+classname+" Arrayset");
		if (!lastDir.equals("")) {
			fc.setCurrentDirectory(new File(lastDir));
		}
		int choice = fc.showOpenDialog(mainPanel.getParent());

		if (choice == JFileChooser.APPROVE_OPTION) {
			lastDir = fc.getSelectedFile().getPath();
			try {
				helper.setLastDataDirectory(fc.getCurrentDirectory()
						.getCanonicalPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			DSPanel<DSMicroarray> panel = getPanelFromSet(fc.getSelectedFile());
			addPanel(panel);
			if (!classname.equals(context.getDefaultClass()))
				context.assignClassToLabel(panel.getLabel(), classname);
			throwLabelEvent();
		}
    }

	private DSPanel<DSMicroarray> getPanelFromSet(final File file) {
		FileInputStream inputStream = null;
		String filename = file.getName();
		if (filename.toLowerCase().endsWith(".csv")) {
			filename = filename.substring(0, filename.length() - 4);
		}
		// Ensure loaded file has unique name
		Set<String> nameSet = new HashSet<String>();
		int n = context.getNumberOfLabels();
		for (int i = 0; i < n; i++) {
			nameSet.add(context.getLabel(i));
		}
		filename = Util.getUniqueName(filename, nameSet);
		DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>(filename);
		
		List<String> selectedNames = new ArrayList<String>();
		try {
			inputStream = new FileInputStream(file);
			ExcelCSVParser parser = new ExcelCSVParser(inputStream);
			String[][] data = parser.getAllValues();
			for (int i = 0; i < data.length; i++) {
				String[] line = data[i];
				if (line.length > 0) {
					selectedNames.add(line[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Lost cause
				}
			}
		}
		for(DSMicroarray array: itemList) {
			if(selectedNames.contains(array.getLabel()))
				panel.add(array);
		}
		return panel;
	}

    protected void setSelectorLastDirConf() {
		selectorLastDirConf = FilePathnameUtils.getUserSettingDirectoryPath()
				+ "selectors" + FilePathnameUtils.FILE_SEPARATOR
				+ "selectorPhenoLastDir.conf";
	}

    protected SelectorHelper<DSMicroarray> getSelectorHelper() {
		helper = new SelectorHelper<DSMicroarray>(this);
		return helper;
	}

    private static int getIndexForClass(String clazz) {
        for (int i = 0; i < CLASSES.length; i++) {
            if (CLASSES[i].equals(clazz)) {
                return i;
            }
        }
        return -1;
    }

    protected void labelClicked(MouseEvent e, TreePath path, String label) {
        if (e.getX() < panelTree.getPathBounds(path).x + treeRenderer.getCheckBoxWidth() + CLASS_ICONS[0].getIconWidth()) {
            String clazz = context.getClassForLabel(label);
            int i = getIndexForClass(clazz);
            leftClickClassButtons[i].setSelected(true);
            classPopup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    protected void showTreePopup(MouseEvent e) {
        String label = getLabelForPath(rightClickedPath);
        String clazz = context.getClassForLabel(label);
        int i = getIndexForClass(clazz);
        rightClickClassButtons[i].setSelected(true);
        super.showTreePopup(e);
    }

    @SuppressWarnings("rawtypes")
	protected void initializeContext(DSAnnotationContext context) {
        super.initializeContext(context);
        for (int i = 0; i < CLASSES.length; i++) {
            context.addClass(CLASSES[i]);
        }
        context.setDefaultClass(DEFAULT_CLASS);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected boolean dataSetChanged(DSDataSet dataSet) {
        if (dataSet instanceof DSMicroarraySet) {
            set = (DSMicroarraySet<DSMicroarray>) dataSet;
            setItemList(set);
            return true;
        } else {
            dataSetCleared();
            return false;
        }
    }

    protected void throwLabelEvent() {
        PhenotypeSelectorEvent<DSMicroarray> event = new PhenotypeSelectorEvent<DSMicroarray>(context.getLabelTree(), set);
        publishPhenotypeSelectorEvent(event);
    }

    protected void publishSingleSelectionEvent(DSMicroarray item) {
        SingleMicroarrayEvent event = new SingleMicroarrayEvent(item, "Selected");
        publishSingleMicroarrayEvent(event);
    }

    @Publish
    public PhenotypeSelectorEvent<DSMicroarray> publishPhenotypeSelectorEvent(PhenotypeSelectorEvent<DSMicroarray> event) {
        return event;
    }
    
    /**
     * Called when a single marker is selected by a component.
     */
	@Subscribe
	public void receive(PhenotypeSelectedEvent event, Object source) {
		JList list = itemAutoList.getList();
		if (list != null) {
			int index = itemList.indexOf(event.getObject());
			list.setSelectedIndex(index);
			if (index != -1)
				list.scrollRectToVisible(list.getCellBounds(index, index));

		}
	}

    @Publish
    public SingleMicroarrayEvent publishSingleMicroarrayEvent(SingleMicroarrayEvent event) {
        return event;
    }

}
