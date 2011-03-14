package org.geworkbench.components.foldchange;

import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.FoldChangeResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;

/**
 *
 * @author zm2165
 * @version $Id$
 */

public class FoldChangeAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {	
	
	private static final long serialVersionUID = -8885144423769088617L;
	private static final int GROUP_A = 1;
	private static final int GROUP_B = 2;
	private static final int NEITHER_GROUP = 3;
	private static final double LOG2=Math.log(2);
	private static final double SMALLDOUBLE= 1E-99;
	
	private double[][] expMatrix;

	private int localAnalysisType;
	private static Log log = LogFactory.getLog(FoldChangeAnalysis.class);	

	private int numGenes,numExps;
	private double alpha;
	private boolean isLinear, isRatio;
	private int[] groupAssignments;	

	private int numberGroupA = 0;
	private int numberGroupB = 0;	
	
	public FoldChangeAnalysis() {
		localAnalysisType = AbstractAnalysis.FOLD_CHANGE_TYPE;
		setDefaultPanel(new FoldChangePanel());
	}
	
	

	public int getAnalysisType() {
		return localAnalysisType;
	}

	@Override
	public AlgorithmExecutionResults execute(Object input) {
		return calculate(input, false);
	}

	@SuppressWarnings("unchecked")
	AlgorithmExecutionResults calculate(Object input,
			boolean calledFromOtherComponent) {
		
		if (input == null || !(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}
		

		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;
		boolean allArrays = !data.useItemPanel();
		log.info("All arrays: " + allArrays);

		numGenes = data.markers().size();
		numExps = data.items().size();

		ProgressBar pbFCtest = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		pbFCtest.addObserver(this);
		pbFCtest.setTitle("Fold Change Analysis");
		pbFCtest.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));

		pbFCtest.setMessage("Calculating Fold Change, please wait...");
		pbFCtest.start();
		this.stopAlgorithm = false;

		groupAssignments = new int[numExps];

		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {
			pbFCtest.dispose();
			return null;
		}

		DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) set;
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		numberGroupA = 0;
		numberGroupB = 0;
		
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = data.items().get(i);
			String[] labels = context.getLabelsForItem(ma);
			if ((labels.length == 0) && allArrays) {
				groupAssignments[i] = GROUP_B;
				numberGroupB++;
			}
			for (String label : labels) {
				if (context.isLabelActive(label) || allArrays) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						groupAssignments[i] = GROUP_A;
						numberGroupA++;						
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = GROUP_B;
						numberGroupB++;						
					} else {
						groupAssignments[i] = NEITHER_GROUP;
					}
				}
			}
		}
		if (numberGroupA == 0 && numberGroupB == 0) {
			pbFCtest.dispose();
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".",
					null);
		}
		if (numberGroupA == 0) {
			pbFCtest.dispose();
			return new AlgorithmExecutionResults(false,
					"Please activate at least one set of arrays for \"case\".",
					null);
		}
		if (numberGroupB == 0) {
			pbFCtest.dispose();
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"control\".",
					null);
		}		

		try{
			alpha = Double.parseDouble(((FoldChangePanel) aspp).getAlpha());
			isLinear=((FoldChangePanel) aspp).isLinear();			
			isRatio=((FoldChangePanel) aspp).isRatio();			
		}
		catch(NumberFormatException e){
			pbFCtest.dispose();
			return new AlgorithmExecutionResults(false,
					"Parameters are not valid.", null);
		}
		
		expMatrix = new double[numGenes][numExps];
		
		for (int i = 0; i < numGenes; i++) {			
			if (this.stopAlgorithm) {
				pbFCtest.dispose();
				return null;
			}
			
			for (int j = 0; j < numExps; j++) {
				if((!isLinear)&&isRatio)
					expMatrix[i][j] = Math.pow(2,(double) data.getValue(i, j));
				else if(isLinear&&(!isRatio)){
					if (data.getValue(i, j)<SMALLDOUBLE) {
						pbFCtest.dispose();
						return new AlgorithmExecutionResults(false, "Input data is invalid for the parameter settings", null);
					}
					expMatrix[i][j]=Math.log( data.getValue(i, j))/LOG2;
				}
				else
					expMatrix[i][j]= data.getValue(i, j);
			}
			
		}

		
		String[][] labels = new String[2][];
		labels[0] = context.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
		labels[1] = context
				.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
		HashSet<String> caseSet = new HashSet<String>();
		HashSet<String> controlSet = new HashSet<String>();

		String groupAndChipsString = "";

		// case
		String[] classLabels = labels[0];
		groupAndChipsString += "\t case group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) || !data.useItemPanel()) {
				caseSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		// control
		classLabels = labels[1];
		groupAndChipsString += "\t control group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) || !data.useItemPanel()) {
				controlSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		int totalSelectedGroup = caseSet.size() + controlSet.size();
		String histHeader = null;
		String histMarkerString = GenerateMarkerString(data);

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;	

		Vector<Integer> clusterVector = sortGenesBySignificance(pbFCtest);			
		
		DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
				"Fold_change_"+alpha);
		
		for (Integer index : clusterVector) {
			DSGeneMarker item = data.markers().get(index);
			panelSignificant.add(item, new Float(index));			
		}
		
		publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
				DSGeneMarker.class, panelSignificant,
				SubpanelChangedEvent.NEW));			
	
		FoldChangeResult analysisResult = new FoldChangeResult(maSet,"Fold_change_"+alpha);
		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Fold Change Analysis", analysisResult);
		
		// add data set history.
		histHeader = GenerateHistoryHeader();
		String stemp=histHeader + groupAndChipsString + histMarkerString;
		ProjectPanel.addToHistory(analysisResult, stemp );
		
		pbFCtest.dispose();
		
		return results;
		
	} // end of method calculate

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

	private String GenerateHistoryHeader() {

		String histStr = "";
		// Header
		histStr += "Fold Change Analysis with the following parameters:\n";
		histStr += "----------------------------------------------------------\n";

		histStr += "Input Parameters:" + "\n";			
		histStr += "\t" + "Criterion: " + alpha + "\n";
		histStr += "\t" + "Input data format: ";
		if (isLinear)
			histStr +="Linear\n";
		else
			histStr +="Log2-transformed\n";
		histStr += "\t" + "Caculation method: ";
		if (isRatio)
			histStr +="Ratio" +"\n";
		else
			histStr +="Difference" +"\n";
		
		return histStr;
	}

	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		String histStr = null;

		histStr = "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n";

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}

	private String GenerateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer();

		histStr .append( view.markers().size() ).append( " markers analyzed:\n");
		for (DSGeneMarker marker : view.markers()) {
			histStr .append( "\t" + marker.getLabel() ).append( "\n");
		}

		return histStr.toString();

	}
	
	private Vector<Integer> sortGenesBySignificance(ProgressBar pbFCtest) {

		Vector<Integer> sigGenes = new Vector<Integer>();
		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbFCtest.dispose();
				return null;
			}

			if(isSignificant(i))
				sigGenes.add(new Integer(i));		
		}
		return sigGenes;
	
	}
	
	private boolean isSignificant(int gene) {
		boolean sig = false;
		double numbValidValuesA = 0;
		double numbValidValuesB = 0;
		
		for(int i=0;i<numExps;i++){
			int g=groupAssignments[i];
			if(g==GROUP_A)
				numbValidValuesA+=expMatrix[gene][i];
			else if (g==GROUP_B)
				numbValidValuesB+=expMatrix[gene][i];
		}
		
		numbValidValuesA/=numberGroupA;	//Average
		numbValidValuesB/=numberGroupB;
		
		
		double fcValue=0;
		if (isRatio&&(numbValidValuesB>SMALLDOUBLE)){
			fcValue=numbValidValuesA/numbValidValuesB;
			if((fcValue<1)&&(Math.abs(fcValue)>SMALLDOUBLE))
				fcValue=-1.0/fcValue;
		}
		else if(!isRatio)
			fcValue=numbValidValuesA-numbValidValuesB;
		
		if (fcValue>alpha) sig=true;
		
		return sig;		
	}	

}