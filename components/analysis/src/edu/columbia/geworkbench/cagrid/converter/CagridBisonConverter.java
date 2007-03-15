package edu.columbia.geworkbench.cagrid.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.CSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.DSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.DSSOMClusterDataSet;
import org.geworkbench.bison.model.clusters.DefaultSOMCluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.LeafSOMCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.model.clusters.SOMCluster;
import org.geworkbench.engine.management.Script;
import org.ginkgo.labs.converter.BasicConverter;

import edu.columbia.geworkbench.cagrid.cluster.client.HierarchicalClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.client.SomClusteringClient;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusterNode;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method;
import edu.columbia.geworkbench.cagrid.cluster.som.SomCluster;
import edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter;
import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.duke.cabig.rproteomics.model.statml.Array;
import edu.duke.cabig.rproteomics.model.statml.Data;
import edu.duke.cabig.rproteomics.model.statml.Scalar;

/**
 * Converts to/from cagrid microarray set types from/to geworkbench microarray
 * set types.
 * 
 * @author keshav
 * @version $Id: CagridMicroarrayTypeConverter.java,v 1.2 2007/01/04 22:03:15
 *          watkinson Exp $
 */
public class CagridBisonConverter {
	private static final String HIERARCHICAL_CLUSTERING_NAME = "Hierarchical Clustering";

	private static final String SOM_CLUSTERING_NAME = "Som Clustering";

	private static final String MICROARRAY = "Microarray";

	private static final String MARKER = "Marker";

	private static final String BOTH = "Both";

	private static final String SPEARMAN = "Spearman";

	private static final String PEARSON = "Pearson";

	private static final String EUCLIDEAN = "Euclidean";

	private static final String TOTAL = "Total";

	private static final String AVERAGE = "Average";

	private static final String SINGLE = "Single";

	private static Log log = LogFactory.getLog(CagridBisonConverter.class);

	/**
	 * Convert to edu.columbia.geworkbench.cagrid.microarray.MicroarraySet from
	 * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView
	 * 
	 * @param microarraySetView
	 * @return MicroarraySet
	 */
	public static MicroarraySet convertFromBisonToCagridMicroarray(
			DSMicroarraySetView microarraySetView) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();
		String microarraySetName = microarraySet.getDataSetName();

		Microarray[] gridMicroarrays = new Microarray[numArrays];
		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			/* cagrid array */
			Microarray gridMicroarray = new Microarray();
			gridMicroarray.setArrayData(data);
			gridMicroarray.setArrayName(name);
			gridMicroarrays[i] = gridMicroarray;
		}

		/* extract marker info from DSMicroarraySet */
		int numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		Marker[] gridMarkers = new Marker[numMarkers];
		int i = 0;
		for (DSGeneMarker marker : (DSItemList<DSGeneMarker>) microarraySetView
				.markers()) {
			Marker gridMarker = new Marker();
			gridMarker.setMarkerName(marker.getLabel());
			gridMarkers[i] = gridMarker;
			i++;
		}

		/* cagrid array set */
		MicroarraySet gridMicroarraySet = new MicroarraySet();
		gridMicroarraySet.setName(microarraySetName);
		gridMicroarraySet.setMicroarray(gridMicroarrays);
		gridMicroarraySet.setMarker(gridMarkers);
		// TODO set to get(set)Microarrays and get(set)Markers

		return gridMicroarraySet;
	}

	/**
	 * 
	 * @param microarraySetView
	 * @return Data
	 */
	public static Data convertFromBisonToCagridData(
			DSMicroarraySetView microarraySetView) {

		DSMicroarraySet microarraySet = microarraySetView.getMicroarraySet();

		/* extract microarray info from DSMicroarraySet */
		int numArrays = microarraySetView.size();

		/* extract marker info from DSMicroarraySet */
		int numMarkers = ((DSMicroarray) microarraySet.get(0)).getMarkerNo();

		Array[] arrays = new Array[numArrays];

		for (int i = 0; i < numArrays; i++) {
			/* geworkbench array */
			DSMicroarray microarray = (DSMicroarray) microarraySetView.get(i);
			float data[] = microarray.getRawMarkerData();
			String name = microarray.getLabel();
			if (name == null || StringUtils.isEmpty(name))
				name = "i";// give array a name

			/* cagrid array */
			Array array = new Array();
			String base64Value = BasicConverter.base64Encode(data);
			array.setBase64Value(base64Value);
			array.setName(name);
			array.setType("float");
			array.setDimensions(String.valueOf(numMarkers));
			arrays[i] = array;
		}

		Scalar[] markers = new Scalar[numMarkers];

		int i = 0;
		for (DSGeneMarker marker : (DSItemList<DSGeneMarker>) microarraySetView
				.markers()) {
			Scalar scalar = new Scalar();
			scalar.setName(String.valueOf(i));
			scalar.setValue(marker.getLabel());
			scalar.setType("String");
			markers[i] = scalar;
			i++;
		}

		/* cagrid array set */
		Data dataType = new Data();
		dataType.setArray(arrays);
		dataType.setScalar(markers);

		return dataType;
	}

	/**
	 * Convert from edu.columbia.geworkbench.cagrid.microarray.MicroarraySet to
	 * org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView.
	 * 
	 * @param gridMicroarraySet
	 * @return DSMicroarraySet
	 */
	public static DSMicroarraySetView convertFromCagridMicroarrayToBison(
			MicroarraySet gridMicroarraySet) {

		/* microarray info */
		int numMarkers = gridMicroarraySet.getMicroarray().length;
		String microarraySetName = gridMicroarraySet.getName();
		Microarray[] gridMicroarrays = gridMicroarraySet.getMicroarray();

		DSMicroarraySetView microarraySetView = new CSMicroarraySetView();
		DSMicroarraySet microarraySet = new CSMicroarraySet();
		microarraySet.setLabel(microarraySetName);

		for (int i = 0; i < numMarkers; i++) {
			/* cagrid array */
			float[] arrayData = gridMicroarrays[i].getArrayData();
			String arrayName = gridMicroarrays[i].getArrayName();

			/* bison array */
			DSMicroarray microarray = new CSMicroarray(arrayData.length);
			microarray.setLabel(arrayName);
			for (int j = 0; j < arrayData.length; j++) {
				DSMarkerValue markerValue = new CSExpressionMarkerValue(
						arrayData[j]);
				microarray.setMarkerValue(j, markerValue);
			}
			microarraySet.add(i, microarray);
		}

		// I need to add the marker names
		microarraySetView.setMicroarraySet(microarraySet);

		return microarraySetView;
	}

	/**
	 * 
	 * @param hierarchicalCluster
	 * @param view
	 * @return CSHierClusterDataSet
	 */
	public CSHierClusterDataSet createBisonHierarchicalClustering(
			HierarchicalCluster hierarchicalCluster, DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		log.debug("creating bison hierarchical cluster");
		HierarchicalClusterNode microarrayCluster = hierarchicalCluster
				.getMarkerCluster();
		HierarchicalClusterNode markerCluster = hierarchicalCluster
				.getMicroarrayCluster();
		HierCluster[] resultClusters = new HierCluster[2];
		if (markerCluster != null) {
			resultClusters[0] = convertToMarkerHierCluster(markerCluster, view
					.getMicroarraySet());
		}
		if (microarrayCluster != null) {
			resultClusters[1] = convertToMicroarrayHierCluster(
					microarrayCluster, view.getMicroarraySet());
		}
		CSHierClusterDataSet dataSet = new CSHierClusterDataSet(resultClusters,
				HIERARCHICAL_CLUSTERING_NAME, view);
		return dataSet;
	}

	/**
	 * 
	 * @param somCluster
	 * @param view
	 * @return CSSOMClusterDataSet
	 */
	private CSSOMClusterDataSet createBisonSomClustering(SomCluster somCluster,
			CSMicroarraySetView view) {
		log.debug("creating bison som cluster");

		int width = somCluster.getWidth();
		int height = somCluster.getHeight();
		// Initialize width x height Bison SOM Cluster
		SOMCluster[][] bisonSomCluster = new SOMCluster[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bisonSomCluster[x][y] = new DefaultSOMCluster();
				bisonSomCluster[x][y].setGridCoordinates(x, y);
			}
		}
		// Assign each marker to its appropriate cluster
		for (int i = 0; i < somCluster.getXCoordinate().length; i++) {
			int x = somCluster.getXCoordinate(i);
			int y = somCluster.getYCoordinate(i);
			DSGeneMarker marker = (DSGeneMarker) view.getMicroarraySet()
					.getMarkers().get(i);
			LeafSOMCluster node = new LeafSOMCluster(marker);
			bisonSomCluster[x][y].addNode(node);
		}

		// Build final result set
		CSSOMClusterDataSet dataSet = new CSSOMClusterDataSet(bisonSomCluster,
				SOM_CLUSTERING_NAME, view);

		return dataSet;
	}

	/**
	 * 
	 * @param microarraySet
	 * @param method
	 * @param dimensions
	 * @param distance
	 * @param url
	 * @return DSHierClusterDataSet
	 * @throws Exception
	 */
	@Script
	public DSHierClusterDataSet doClustering(DSMicroarraySet microarraySet,
			String method, String dimensions, String distance, String url)
			throws Exception {
		log.debug("script method:  do clustering");
		CSMicroarraySetView view = new CSMicroarraySetView(microarraySet);
		MicroarraySet gridSet = CagridBisonConverter
				.convertFromBisonToCagridMicroarray(view);

		Dim dim = null;
		if (dimensions.equalsIgnoreCase(MARKER))
			dim = Dim.marker;
		else if (dimensions.equalsIgnoreCase(MICROARRAY))
			dim = Dim.microarray;
		else
			dim = Dim.both;

		Distance dist = null;
		if (distance.equalsIgnoreCase(EUCLIDEAN))
			dist = Distance.euclidean;
		else if (distance.equalsIgnoreCase(PEARSON))
			dist = Distance.pearson;
		else
			dist = Distance.spearman;

		Method meth = null;
		if (method.equalsIgnoreCase(SINGLE))
			meth = Method.single;
		else if (method.equalsIgnoreCase(AVERAGE))
			meth = Method.average;
		else
			meth = Method.complete;

		HierarchicalClusteringParameter parameters = new HierarchicalClusteringParameter(
				dim, dist, meth);

		HierarchicalClusteringClient client = new HierarchicalClusteringClient(
				url);
		HierarchicalCluster hierarchicalCluster = client.execute(gridSet,
				parameters);
		if (hierarchicalCluster != null) {
			CSHierClusterDataSet dataSet = createBisonHierarchicalClustering(
					hierarchicalCluster, view);
			return dataSet;
		} else {
			return null;
		}
	}

	@Script
	public DSSOMClusterDataSet doSOMClustering(DSMicroarraySet microarraySet,
			double alpha, int dim_x, int dim_y, int function, int iteration,
			double radius, String url) throws Exception {
		log.debug("script method:  do SOM clustering");
		CSMicroarraySetView view = new CSMicroarraySetView(microarraySet);
		MicroarraySet gridSet = CagridBisonConverter
				.convertFromBisonToCagridMicroarray(view);
		SomClusteringParameter parameters = new SomClusteringParameter(
				(float) alpha, dim_x, dim_y, function, iteration,
				(float) radius);
		SomClusteringClient client = new SomClusteringClient(url);
		SomCluster somCluster = client.execute(gridSet, parameters);
		if (somCluster != null) {
			CSSOMClusterDataSet bisonSomClustering = createBisonSomClustering(
					somCluster, view);
			return bisonSomClustering;
		}
		return null;
	}

	/**
	 * @param name
	 * @return DSMicroarray
	 */
	private DSMicroarray getArray(String name,
			DSMicroarraySet<DSMicroarray> microarraySet) {
		for (DSMicroarray array : microarraySet) {
			if (array.getLabel().equals(name)) {
				return array;
			}
		}
		return null;
	}

	/**
	 * @param node
	 * @return MicroarrayHierCluster
	 */
	private MicroarrayHierCluster convertToMicroarrayHierCluster(
			HierarchicalClusterNode node,
			DSMicroarraySet<DSMicroarray> microarraySet) {
		log
				.debug("converting hierarchical cluster from bison to grid microarray cluster");
		MicroarrayHierCluster cluster;
		if (node.isLeaf()) {
			cluster = new MicroarrayHierCluster();
			cluster.setMicroarray(getArray(node.getLeafLabel(), microarraySet));
		} else {
			MicroarrayHierCluster left = convertToMicroarrayHierCluster(node
					.getHierarchicalClusterNode(0), microarraySet);
			MicroarrayHierCluster right = convertToMicroarrayHierCluster(node
					.getHierarchicalClusterNode(1), microarraySet);
			cluster = new MicroarrayHierCluster();
			cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
			cluster.setHeight(node.getHeight());
			cluster.addNode(left, 0);
			cluster.addNode(right, 0);
		}
		return cluster;
	}

	/**
	 * @param node
	 * @return MarkerHierCluster
	 */
	private MarkerHierCluster convertToMarkerHierCluster(
			HierarchicalClusterNode node,
			DSMicroarraySet<DSMicroarray> microarraySet) {
		log
				.debug("convert hierarchical cluster from bison to grid marker cluster");
		MarkerHierCluster cluster;
		if (node.isLeaf()) {
			cluster = new MarkerHierCluster();
			cluster.setMarkerInfo(microarraySet.getMarkers().get(
					node.getLeafLabel()));
		} else {
			MarkerHierCluster left = convertToMarkerHierCluster(node
					.getHierarchicalClusterNode(0), microarraySet);
			MarkerHierCluster right = convertToMarkerHierCluster(node
					.getHierarchicalClusterNode(1), microarraySet);
			cluster = new MarkerHierCluster();
			cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
			cluster.setHeight(node.getHeight());
			cluster.addNode(left, 0);
			cluster.addNode(right, 0);
		}
		return cluster;
	}
}
