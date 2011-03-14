package org.geworkbench.components.interactions.cellularnetwork;
import javax.swing.event.ListSelectionEvent;

/**
  * An event that characterizes a change in the current selection.
  * @author Min You
  */
public class TableSelectionEvent extends ListSelectionEvent {

  /**
	 * 
	 */
	private static final long serialVersionUID = -1066562486408377868L;
/**
    * The index of the column whose selection has changed.
    */
  protected int columnIndex;

  public TableSelectionEvent(Object source, int firstRowIndex, int lastRowIndex,
			      int columnIndex, boolean isAdjusting) {

	  super(source, firstRowIndex, lastRowIndex, isAdjusting);
    this.columnIndex = columnIndex;
	}

  /**
    * Returns the index of the column whose selection has changed.
    * @return The last column whose selection value has changed, where zero is the first column.
    */
  public int getColumnIndex() {
    return columnIndex;
  }
}