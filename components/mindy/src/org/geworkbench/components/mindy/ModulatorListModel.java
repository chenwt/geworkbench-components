package org.geworkbench.components.mindy;

import javax.swing.AbstractListModel;

/**
 * Heat map data model.
 *
 * @author mhall
 * @author oshteynb
 * @version $Id: ModulatorListModel.java,v 1.2 2009-04-27 15:49:02 keshav Exp $
 */
class ModulatorListModel extends AbstractListModel {
	/**
	 *
	 */
	private final MindyPlugin mindyPlugin;
	private ModulatorModel modulatorModel;

	private boolean showProbeName = false;

	public ModulatorListModel(MindyPlugin mindyPlugin, boolean showProbeName, ModulatorModel modulatorModel) {
		this.mindyPlugin = mindyPlugin;
		this.modulatorModel = modulatorModel;

		this.showProbeName = showProbeName;
	}

	public void setModulatorModel(ModulatorModel modulatorModel) {
		this.modulatorModel = modulatorModel;
	}

	public boolean isShowProbeName() {
		return this.showProbeName;
	}

	public void setShowProbeName(boolean b) {
		this.showProbeName = b;
	}

	/**
	 * Get the number of enabled modulators.
	 *
	 * @return number of enabled modulators
	 */
	public int getSize() {
		// TODO
//		return this.mindyPlugin.modTargetModel.getEnabledModulators().size();
		return this.modulatorModel.getSelections().getSelectedModulators().size();
	}

	/**
	 * Get the modulator specified by the index.
	 *
	 * @param i -
	 *            index
	 * @return Modulator marker name of the enabled modulators in the heat
	 *         map as specified by index i.
	 */
	public Object getElementAt(int i) {
		// really bad idea...
/*		boolean orig = this.mindyPlugin.modTargetModel.isShowProbeName();
		this.mindyPlugin.modTargetModel.setShowProbeName(this.showProbeName);
		String displayName = MindyPlugin.getMarkerDisplayName(this.mindyPlugin.modTargetModel.isShowProbeName(),
				this.mindyPlugin.modTargetModel.getEnabledModulators().get(i));
		this.mindyPlugin.modTargetModel.setShowProbeName(orig);
*/
		String displayName = MindyPlugin.getMarkerDisplayName(isShowProbeName(),
				this.modulatorModel.getSelections().getSelectedModulators().get(i));

		return displayName;
	}

	/**
	 * Refreshes the data model.
	 */
	public void refresh() {
		fireContentsChanged(this, 0, getSize());
	}
}