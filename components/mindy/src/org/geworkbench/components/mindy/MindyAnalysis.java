package org.geworkbench.components.mindy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.threading.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.text.Collator;

import edu.columbia.c2b2.mindy.Mindy;
import edu.columbia.c2b2.mindy.MindyResults;
import wb.data.MicroarraySet;
import wb.data.Microarray;
import wb.data.MarkerSet;
import wb.data.Marker;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;

/**
 * @author Matt Hall
 * @author ch2514
 * @version $ID$
 */
@SuppressWarnings("serial")
public class MindyAnalysis extends AbstractAnalysis implements ClusteringAnalysis {
    Log log = LogFactory.getLog(this.getClass());

    private MindyParamPanel paramPanel;
    private JDialog dialog;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private Task task;
    private MindyDataSet mindyDataSet;

    /**
     * Constructor.
     * Creates MINDY parameter panel.
     */
    public MindyAnalysis() {
        setLabel("MINDY");
        paramPanel = new MindyParamPanel();
        setDefaultPanel(paramPanel);
    }

    
    // not used - required to implement from interface ClusteringAnalysis
    public int getAnalysisType() {
        return AbstractAnalysis.ZERO_TYPE;
    }
    

    /**
     * The execute method the framework calls to analyze parameters and create MINDY results.
     * 
     * @param input - microarray set data coming from the framework
     * @return analysis algorithm results
     */
    @SuppressWarnings("unchecked")
    public AlgorithmExecutionResults execute(Object input) {
        log.debug("input: " + input);
        DSMicroarraySetView inputSetView = (DSMicroarraySetView) input;
        DSPanel arraySet = null;
        DSPanel markerSet = null;
        if(inputSetView.useItemPanel())
        	arraySet = inputSetView.getItemPanel();
        if(inputSetView.useMarkerPanel())
        	markerSet = inputSetView.getMarkerPanel();    
        
        // Mindy parameter validation always returns true 
        // (the method is not overrode from AbstractAnalysis)
        // so we can enter the execute() method and capture 
        // both parameter and input errors.
        // The eventual error message dialog (if there are errors)
        // would look the same as the one created by the analysis panel      
        
        
        // Use this to get params
        MindyParamPanel params = (MindyParamPanel) aspp;
        DSMicroarraySet<DSMicroarray> mSet = inputSetView.getMicroarraySet();
        StringBuilder paramDescB = new StringBuilder("Generated by MINDY run with paramters: \n");
        StringBuilder errMsgB = new StringBuilder();
        
        int numMAs = mSet.size();
        paramDescB.append("Number of microarrays: ");
        paramDescB.append(numMAs);
        paramDescB.append("\n");
        if(numMAs < 4){
        	errMsgB.append("Not enough microarrays in the set.  MINDY requires at least 4 microarrays.\n");
        }        
        
        int numMarkers = mSet.getMarkers().size();
        paramDescB.append("Number of markers: ");
        paramDescB.append(numMarkers);
        paramDescB.append("\n");
        if(numMarkers < 2){
        	errMsgB.append("Not enough markers in the microarrays. (Need at least 2)\n");            
        }
        
        paramDescB.append("Modulator list: ");
        ArrayList<Marker> modulators = new ArrayList<Marker>();
        ArrayList<String> modulatorGeneList = params.getModulatorGeneList();
        if((modulatorGeneList != null) && (modulatorGeneList.size() > 0)){
	        for (String modGene : modulatorGeneList) {
	            DSGeneMarker marker = mSet.getMarkers().get(modGene);
	            if (marker == null) {
	            	errMsgB.append("Couldn't find marker ");
	            	errMsgB.append(modGene);
	            	errMsgB.append(" from modulator file in microarray set.\n");
	            } else {
	            	paramDescB.append(modGene);
	            	paramDescB.append(" ");
	                modulators.add(new Marker(modGene));
	            }
	        }
	        paramDescB.append("\n");
        } else {
        	errMsgB.append("No modulator specified.\n");
        }

        paramDescB.append("DPI Annotated Genes: ");
        ArrayList<Marker> dpiAnnots = new ArrayList<Marker>();
        ArrayList<String> dpiAnnotList = params.getDPIAnnotatedGeneList();
        for (String modGene : dpiAnnotList) {
            DSGeneMarker marker = mSet.getMarkers().get(modGene);
            if (marker == null) {
            	errMsgB.append("Couldn't find marker ");
            	errMsgB.append(modGene);
            	errMsgB.append(" from DPI annotation file in microarray set.\n");
            } else {
            	paramDescB.append(modGene);
                paramDescB.append(" ");
                dpiAnnots.add(new Marker(modGene));
            }
        }
        paramDescB.append("\n");

        
        String transcriptionFactor = params.getTranscriptionFactor();
        DSGeneMarker transFac = mSet.getMarkers().get(transcriptionFactor);
        if (!transcriptionFactor.trim().equals("")){
	        if (transFac == null) {
	        	errMsgB.append("Specified hub marker (");
	        	errMsgB.append(transcriptionFactor);
	        	errMsgB.append(") not found in loadad microarray set.\n");
	        } else {
		        paramDescB.append("Hub Marker: ");
		        paramDescB.append(transcriptionFactor);
		        paramDescB.append("\n");
	        }
        } else {
        	errMsgB.append("No hub marker specified.\n");
        }
        
        boolean fullSetMI = false;
        if(params.getUnconditional().trim().equals(params.MI)){
        	fullSetMI = true;
        }
        float fullSetThreshold = params.getUnconditionalValue();
        
        boolean subsetMI = false;
        if(params.getConditional().trim().equals(params.MI)){
        	subsetMI = true;
        }
        float subsetThreshold = params.getConditionalValue();
        
        float setFraction = params.getSetFraction() / 100f;
        paramDescB.append("Set Fraction: ");
        paramDescB.append(setFraction);
        paramDescB.append("\n");
        if(Math.round(setFraction * 2 * numMarkers) < 2){
        	errMsgB.append("Not enough markers in the specified % sample.  MINDY requires at least 2 markers in the sample.\n");
        }
        
        // If parameters or inputs have errors, alert the user and return from execute()
        errMsgB.trimToSize();
        String s = errMsgB.toString();
        if(!s.equals("")){
        	log.info(errMsgB.toString());
	        JOptionPane.showMessageDialog(null, s, "Parameter and Input Validation Error", JOptionPane.ERROR_MESSAGE);
	        return null;
        }        
                
        
        // otherwise
        // run Mindy algorithm in the background
        // and display an indeterminate progress bar in the foreground
        createProgressBarDialog();
        Mindy mindy = new Mindy();
        task = new Task(mindy
    			, params
    			, mSet
    			, arraySet
    			, markerSet
    			, modulators
    			, dpiAnnots
    			, fullSetMI
    			, fullSetThreshold
    			, subsetMI
    			, subsetThreshold
    			, setFraction
    			, transFac);        
        task.execute();     
        dialog.setVisible(true);        
        
        s = paramDescB.toString();
        if(this.mindyDataSet != null){
        	log.info(s);
	        ProjectPanel.addToHistory(this.mindyDataSet, s);
	        return new AlgorithmExecutionResults(true, "MINDY Results Loaded.", this.mindyDataSet);
        } else {
        	JOptionPane.showMessageDialog(paramPanel.getParent(), "Cannot analyze data.", "MINDY Analyze Error", JOptionPane.WARNING_MESSAGE);
        	return null;
        }
    }
    
    private void createProgressBarDialog(){
    	// lay the groundwork for the progress bar dialog
        dialog = new JDialog();
        progressBar = new JProgressBar();
        cancelButton = new JButton("Cancel");
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);
        dialog.setTitle("MINDY Process Running");
        dialog.setSize(300, 50);
        dialog.setLocation((int) (dialog.getToolkit().getScreenSize().getWidth() - dialog.getWidth()) / 2, (int) (dialog.getToolkit().getScreenSize().getHeight() - dialog.getHeight()) / 2);
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);
        dialog.add(cancelButton, BorderLayout.EAST);
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	if((task != null) && (!task.isCancelled()) && (!task.isDone())) {
            		task.cancel(true);
            		log.info("Cancelling Mindy Analysis");
            	}
            	dialog.setVisible(false);
            	dialog.dispose();            	
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
            	if((task != null) && (!task.isCancelled()) && (!task.isDone())){
            		task.cancel(true);
            		log.info("Cancelling Mindy Analysis");
            	}
            }
        });
    }

    

    /**
     * Receives GeneSelectorEvents from the framework (i.e. the Selector Panel)
     * @param e
     * @param source
     */
    @Subscribe public void receive(GeneSelectorEvent e, Object source) {
        DSGeneMarker marker = e.getGenericMarker(); // GeneselectorEvent can be a panel event therefore won't work here,
        if (marker != null) { //so added this check point--xuegong
            paramPanel.setTranscriptionFactor(marker.getLabel());
        }
    }

	/**
	 * Publish MINDY data to the framework.
	 * @param data
	 * @return
	 */
    @Publish
    public MindyDataSet publishMatrixReduceSet(MindyDataSet data) {
        return data;
    }
    
    /**
     * The swing worker class that runs Mindy analysis in the background.
     * @author ch2514
     * @version $Id: MindyAnalysis.java,v 1.21 2007-09-17 19:11:05 hungc Exp $
     */
    class Task extends SwingWorker<MindyDataSet, Void> {
    	private Mindy mindy;
    	private MindyParamPanel params;
    	private DSMicroarraySet<DSMicroarray> mSet;
    	private DSPanel arraySet;
    	private DSPanel markerSet;
    	private ArrayList<Marker> modulators;
    	private ArrayList<Marker> dpiAnnots;
    	private boolean fullSetMI;
    	private float fullSetThreshold;
    	private boolean subsetMI;
    	private float subsetThreshold;
    	private float setFraction;
    	private DSGeneMarker transFac;
    	
    	/**
    	 * Constructor.
    	 * Takes in all the arguments required to run the mindy algorithm.
    	 * 
    	 * @param mindy
    	 * @param params
    	 * @param mSet
    	 * @param modulators
    	 * @param dpiAnnots
    	 * @param fullSetMI
    	 * @param fullSetThreshold
    	 * @param subsetMI
    	 * @param subsetThreshold
    	 * @param setFraction
    	 * @param transFac
    	 */
    	public Task(Mindy mindy
    			, MindyParamPanel params
    			, DSMicroarraySet<DSMicroarray> mSet
    			, DSPanel arraySet
    			, DSPanel markerSet
    			, ArrayList<Marker> modulators
    			, ArrayList<Marker> dpiAnnots
    			, boolean fullSetMI
    			, float fullSetThreshold
    			, boolean subsetMI
    			, float subsetThreshold
    			, float setFraction
    			, DSGeneMarker transFac
    			){
    		this.mindy = mindy;
    		this.params = params;
    		this.mSet = mSet;
    		this.arraySet = arraySet;
    		this.markerSet = markerSet;
    		this.modulators = modulators;
    		this.dpiAnnots = dpiAnnots;
    		this.fullSetMI = fullSetMI;
    		this.fullSetThreshold = fullSetThreshold;
    		this.subsetMI = subsetMI;
    		this.subsetThreshold = subsetThreshold;
    		this.setFraction = setFraction;
    		this.transFac = transFac;
    	}
    	
    	/**
    	 * Runs Mindy analysis.
    	 * @return a mindy data set.  If the analysis fails, returns null.
    	 */
    	public MindyDataSet doInBackground(){
    		log.info("Running MINDY analysis.");
    		
            MindyResults results=null;
            try{
            	log.info("Running MINDY algorithm. " + System.currentTimeMillis());
            	results = mindy.runMindy(convert(mSet, arraySet, markerSet), new Marker(params.getTranscriptionFactor()), modulators,
                    dpiAnnots, fullSetMI, fullSetThreshold, subsetMI, subsetThreshold,
                    setFraction, params.getDPITolerance());
            	log.info("Finished running MINDY algorithm. " + System.currentTimeMillis());
            } catch (Exception e){
            	log.error("Cannot analyze data.", e);            	
            	return null;
            }            
            log.info("MINDY analysis complete.  Converting Mindy results. " + System.currentTimeMillis());
            List<MindyData.MindyResultRow> dataRows = new ArrayList<MindyData.MindyResultRow>();
            Collator myCollator = Collator.getInstance();            
            for (MindyResults.MindyResultForTarget result : results) {
                DSItemList<DSGeneMarker> markers = mSet.getMarkers();
                DSGeneMarker target = markers.get(result.getTarget().getName());
                for (MindyResults.MindyResultForTarget.ModulatorSpecificResult specificResult : result) {
                    DSGeneMarker mod = markers.get(specificResult.getModulator().getName());
                    dataRows.add(new MindyData.MindyResultRow(mod, transFac, target, specificResult.getScore(), 0f, myCollator.getCollationKey(mod.getShortName()), myCollator.getCollationKey(target.getShortName())));
                }
            }
            
            MindyData loadedData = new MindyData((CSMicroarraySet) mSet, dataRows, setFraction);
            
            // Pearson correlation
            ArrayList<DSMicroarray> maList = loadedData.getArraySetAsList();
            SimpleRegression sr;
            for(MindyData.MindyResultRow r: dataRows){
        		sr = new SimpleRegression();
        		for(DSMicroarray ma: maList){
        			sr.addData(ma.getMarkerValue(r.getTarget()).getValue(), ma.getMarkerValue(r.getTranscriptionFactor()).getValue());    			
        		}
        		r.setCorrelation(sr.getR());   
        	}
            
            MindyDataSet dataSet = new MindyDataSet(mSet, "MINDY Results", loadedData, params.getCandidateModulatorsFile());
            log.info("Done converting MINDY results. " + System.currentTimeMillis());
            
            return dataSet;
    	}
    	
    	/**
    	 * When the mindy analysis finishes, transfer the resulting mindy
    	 * data set back to the mindy analysis panel on the event thread.
    	 * Also disposes the progress bar dialog box.
    	 */
    	public void done(){
    		if(!this.isCancelled()){
	    		try{
	    			mindyDataSet = get();    			
	    			log.debug("Transferring mindy data set back to event thread.");
	    		} catch (Exception e) {
	    			log.error("Exception in finishing up worker thread that called MINDY: " + e.getMessage(), e);
	    		}
    		}
    		dialog.setVisible(false);
    		dialog.dispose();
    		log.debug("Closing progress bar dialog.");
    	}
    	
    	private MicroarraySet convert(DSMicroarraySet<DSMicroarray> inSet, DSPanel arraySet, DSPanel markerSet) {    		
            MarkerSet markers = new MarkerSet();
            if(markerSet != null){
            	int size = markerSet.size();
            	for (int i = 0; i < size; i++) {
	                markers.addMarker(new Marker(((DSGeneMarker) markerSet.get(i)).getLabel()));
	            }
            } else {
	            for (DSGeneMarker marker : inSet.getMarkers()) {
	                markers.addMarker(new Marker(marker.getLabel()));
	            }
            }           
            
            MicroarraySet returnSet = new MicroarraySet(inSet.getDataSetName(), "ID", "ChipType", markers);
            if(arraySet != null){
            	int size = arraySet.size();
            	for (int i = 0; i < size; i++) {
            		DSMicroarray ma = (DSMicroarray) arraySet.get(i);
            		returnSet.addMicroarray(new Microarray(ma.getLabel(), ma.getRawMarkerData())
            		);
	            }
            } else {
	            for (DSMicroarray microarray : inSet) {
	                returnSet.addMicroarray(new Microarray(microarray.getLabel(), microarray.getRawMarkerData()));
	            }
            }
            
            return returnSet;
        }
    }
}
