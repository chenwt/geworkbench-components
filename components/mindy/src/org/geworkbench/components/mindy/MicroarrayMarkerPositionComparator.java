package org.geworkbench.components.mindy;

import java.util.*;
import org.geworkbench.bison.datastructure.bioobjects.microarray.*;

/**
 * Compare marker positions in microarrays (for sorting microarrays)
 * @author ch2514
 * @version $Id: MicroarrayMarkerPositionComparator.java,v 1.4 2007-08-14 19:53:23 hungc Exp $
 */
public class MicroarrayMarkerPositionComparator implements Comparator<DSMicroarray> {
	public static final int EXPRESSION_VALUE = 1;
	
	private int markerPosition;
	private int mode;
	private boolean ascending;

	/**
	 * @param markerPosition - the marker position with which to compare the microarrays.
	 * @param ascending - (for sorting).  If true, the microarrys is sorted in
	 * ascending order based on the marker position.
	 */
	public MicroarrayMarkerPositionComparator(int markerPosition, int mode, boolean ascending){
		this.markerPosition = markerPosition;
		this.mode = mode;
		this.ascending = ascending;
	}

	/**
	 * Compares two microarrays based on marker position.  
	 * This method is for Collections sorting to call.
	 * 
	 * @param ma1 - the first microarray to be compared
	 * @param ma2 - the second microarray to be compared
	 * 
	 * @return A negative integer if the first microarray precedes the second.  
	 * Zero if the two microarray have the same data at the specified marker position.
	 * A positive integer if the second microarray precedes the first.
	 * 	
	 */
	public int compare(DSMicroarray ma1, DSMicroarray ma2) {
		switch(mode){
        case EXPRESSION_VALUE:
			if(ascending){
				return Float.compare(ma1.getRawMarkerData()[markerPosition], ma2.getRawMarkerData()[markerPosition]);
			} else {
				return Float.compare(ma2.getRawMarkerData()[markerPosition], ma1.getRawMarkerData()[markerPosition]);
			}		
		}
		return 0;
	}
}
