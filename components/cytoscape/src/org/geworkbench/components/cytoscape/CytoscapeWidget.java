package org.geworkbench.components.cytoscape;

import giny.model.Node;
import giny.view.EdgeView;
import giny.view.GraphViewChangeEvent;
import giny.view.GraphViewChangeListener;
import giny.view.NodeView;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet; 
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixCancelEvent;
import org.geworkbench.events.GeneTaggedEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.events.ProjectNodeRemovedEvent;
import org.geworkbench.util.Util;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;

import com.jgoodies.looks.Options;

import csplugins.layout.algorithms.force.ForceDirectedLayout;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.giny.FingCyNetwork;
import cytoscape.init.CyInitParams;
import cytoscape.layout.AbstractLayout;
import cytoscape.layout.LayoutTask;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CytoscapeToolBar;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.NetworkPanel;
import cytoscape.view.cytopanels.BiModalJSplitPane;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.DuplicateCalculatorNameException;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import cytoscape.visual.parsers.ObjectToString;
import ding.view.DNodeView;

/**
 * 
 * Visual component to display graph using cytoscape.
 * 
 * @author manjunath
 * @author yc2480
 * @version $Id$
 */
@SuppressWarnings("deprecation")
@AcceptTypes( { AdjacencyMatrixDataSet.class })
public class CytoscapeWidget implements VisualPlugin {

	public final static String NODE_FILL_COLOR = "node.fillColor";
	private static final String CYTOSCAPE_CARD = "cytoscape card";
	private static final String SAFE_CARD = "safe card";

	private class GenewaysNetworkListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evnt) {
			if (evnt.getPropertyName() == cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED) {
				Cytoscape.getCurrentNetworkView().addNodeContextMenuListener(
						new ExpandMenuListener(CytoscapeWidget.this));
			}
			log.debug(evnt.getPropertyName());

			/*
			 * This particular case in cytoscape needs context class loader to
			 * be set. It is in a separate thread, we cannot and don't need to
			 * reset the context class loader.
			 */
			StackTraceElement[] stackTrace = Thread.currentThread()
					.getStackTrace();
			boolean isTaskWrapperRun = false;
			boolean isWriteSessionToDisk = false;
			for (StackTraceElement e : stackTrace) {
				if (e
						.toString()
						.equals(
								"cytoscape.data.writers.CytoscapeSessionWriter.writeSessionToDisk(CytoscapeSessionWriter.java:240)"))
					isWriteSessionToDisk = true;
				if (e
						.toString()
						.equals(
								"cytoscape.task.util.TaskWrapper.run(TaskManager.java:125)"))
					isTaskWrapperRun = true;
			}
			/* to make sure it does not happen for unintended cases */
			if (isTaskWrapperRun && isWriteSessionToDisk) {
				Thread.currentThread().setContextClassLoader(
						CytoscapeDesktop.class.getClassLoader());
			}
		}

	}

	static private Log log = LogFactory.getLog(CytoscapeWidget.class);
	private AdjacencyMatrixDataSet adjSet = null;
	private Set<String> dataSetIDs = new HashSet<String>();
	private volatile Set<Integer> cancelList = new HashSet<Integer>();
	private DiscreteMapping nodeDm = null, edgeDm = null;

	private int shapeIndex = 0;
	private NodeShape[] shapes = { NodeShape.HEXAGON, NodeShape.RECT,
			NodeShape.DIAMOND, NodeShape.ELLIPSE, NodeShape.TRIANGLE,
			NodeShape.OCTAGON, NodeShape.PARALLELOGRAM, NodeShape.ROUND_RECT,
			NodeShape.TRAPEZOID_2 };

	private boolean uiSetup = false;

	private CyNetworkView view = null;

	private CyNetwork cytoNetwork = null;
	DSMicroarraySet<? extends DSMicroarray> maSet = null;
	boolean publishEnabled = true;

	Map<String, List<Integer>> geneIdToMarkerIdMap = new HashMap<String, List<Integer>>();

	Map<String, String> interactionTypeSifMap = null;

	private static CytoscapeWidget INSTANCE = null;

	/*
	 * make sure the CytoscapeWidget() constructor only be called one time.
	 */
	public static CytoscapeWidget getInstance() {
		if (INSTANCE != null)
			return INSTANCE;
		else
			try {
				return new CytoscapeWidget();
			} catch (Exception e) { // exception only for INSTANCE is not null
				return INSTANCE;
			}
	}

	public CytoscapeWidget() throws Exception {
		// singleton: this constructor should never be called the second time.
		if (INSTANCE != null)
			throw new Exception(
					"Second instance of CytoscapeWidget cannot be created.");

		UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
		Options.setDefaultIconSize(new Dimension(18, 18));

		init();
		publishEnabled = true;

		INSTANCE = this;

	}

	JPanel masterPanel = new JPanel(new CardLayout());
	JPanel safePanel = new JPanel();
	/**
	 * <code>VisualPlugin</code> method
	 * 
	 * @return <code>Component</code> the view for this component
	 */
	public Component getComponent() {

		// quit
		Container contentPane = Cytoscape.getDesktop().getContentPane();
		if (!uiSetup) {
			JMenuBar menuBar = Cytoscape.getDesktop().getCyMenus().getMenuBar();
			JMenu fileMenu = menuBar.getMenu(0);
			fileMenu.remove(fileMenu.getItemCount() - 1); // remove the last
			// item

			Component[] components = contentPane.getComponents();
			contentPane.removeAll();
			Box box = Box.createVerticalBox();
			Component comp1 = menuBar;
			BiModalJSplitPane comp2 = (BiModalJSplitPane) components[0];
			CytoscapeToolBar comp3 = (CytoscapeToolBar) components[1];

			menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
			comp2.setAlignmentX(Component.LEFT_ALIGNMENT);
			comp3.setAlignmentX(Component.LEFT_ALIGNMENT);

			box.add(comp1);
			box.add(comp2);
			box.add(comp3);

			contentPane.add(box);

			uiSetup = true;
			
			masterPanel.add(contentPane, CYTOSCAPE_CARD);
			masterPanel.add(safePanel, SAFE_CARD);
		}

		return masterPanel;
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent pe) {
		return pe;
	}

	public static final String GENE_SEPARATOR = " /// ";
	private static final int SAFE_NODE_NUMBER = 1000;

	/**
	 * Update selection in visualization when the gene selection is changed
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe
	public void receive(GeneTaggedEvent e, Object source) {

		Color color = null;
		CyAttributes attrs = null;
		DSPanel<DSGeneMarker> panel = e.getPanel();
		if (panel == null) {
			log.error("panel is null in "
					+ new Exception().getStackTrace()[0].getMethodName());
			return;
		}
       attrs = Cytoscape.getNodeAttributes();
		if (e.getType() == GeneTaggedEvent.USE_VISUAL_PROPERTY) {
			PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager
					.getInstance();
			PanelVisualProperties properties = propertiesManager
					.getVisualProperties(panel);
			if (properties == null) {
				properties = propertiesManager.getDefaultVisualProperties(e
						.getPanelIndex());
			}

			color = properties.getColor();
			 
		}

		List<String> selected = new ArrayList<String>();
		for (DSGeneMarker m : panel) {
			String name = m.getShortName().trim().toUpperCase();
			if (name.contains(GENE_SEPARATOR)) {
				String[] names = name.split(GENE_SEPARATOR);
				for (int i = 0; i < names.length; i++) {
					selected.add(names[i]);
				}
			} else {
				selected.add(m.getShortName().trim().toUpperCase());
			}
			log.debug(m.getShortName().trim().toUpperCase());
		}

		Iterator<?> iter = Cytoscape.getCurrentNetworkView()
				.getNodeViewsIterator();
		Color defaultNodeSelectionColor = Cytoscape.getVisualMappingManager()
				.getVisualStyle().getGlobalAppearanceCalculator()
				.getDefaultNodeSelectionColor();
		publishEnabled = false;
		while (iter.hasNext()) {
			NodeView nodeView = (NodeView) iter.next();
			nodeView.unselect();
			nodeView.setSelectedPaint(defaultNodeSelectionColor);
			String id = nodeView.getNode().getIdentifier();
			String displayedName = attrs.getStringAttribute(id, "displayedName");
			log.debug("Check if " + selected + " contains " + displayedName);
			if (selected.contains(displayedName)) {
				if (e.getType() == GeneTaggedEvent.USE_VISUAL_PROPERTY) {
					attrs.setAttribute(id,
							NODE_FILL_COLOR, ObjectToString
									.getStringValue(color));

					nodeView.setUnselectedPaint(color);
					// nodeView.setSelectedPaint(color);
					// nodeView.select();
					nodeView.unselect();

				} else {
					nodeView.select();
				}
				log.debug("^^^Select^^^");

			} else

				nodeView.unselect();
		}

		this.getComponent().repaint();

		publishEnabled = true;
	}

	@Subscribe
	public void receive(AdjacencyMatrixCancelEvent ae, Object source) {

		cancelList.add(ae.getAdjacencyMatrix().hashCode());
		log.info("got AdjacencyMatrixEvent.action.CANCEL event");
		try {
			Thread.sleep(100);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		AdjacencyMatrix adjMatrix = adjSet.getMatrix();
		if (ae.getAdjacencyMatrix() == adjMatrix)
			ProjectPanel.getInstance().removeAddedSubNode(adjSet);
	}

	/**
	 * receiveProjectSelection
	 * 
	 * @param e
	 *            ProjectEvent
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		int adjMatrixId;
		try {
			DSDataSet<?> dataSet = e.getDataSet();

			if (dataSet instanceof AdjacencyMatrixDataSet) {

				adjSet = (AdjacencyMatrixDataSet) dataSet;

				int nodeNumber = adjSet.getMatrix().getNodeNumber();

				CardLayout cl = (CardLayout) (masterPanel.getLayout());
				if (nodeNumber > SAFE_NODE_NUMBER) {
					StringBuffer sb = new StringBuffer(
							"This network's node number is too large for cytoscape to handle safely.\n");
					sb.append("node number " + nodeNumber+ "\n");
					sb.append("edge number "
							+ adjSet.getMatrix().getEdges().size());
					safePanelContent.setText(sb.toString());
					cl.show(masterPanel, SAFE_CARD);
					return;
				} else {
					cl.show(masterPanel, CYTOSCAPE_CARD);
				}

				AdjacencyMatrix adjMatrix = adjSet.getMatrix();
				adjMatrixId = adjMatrix.hashCode();
				maSet = adjSet.getMatrix().getMicroarraySet();

				if (maSet != null) {
					geneIdToMarkerIdMap = AnnotationParser
							.getGeneIdToMarkerIDMapping(maSet);

				}
				boolean found = false;
				String foundID = null;
				if (!dataSetIDs.contains(adjSet.getID())) {
					dataSetIDs.add(adjSet.getID());
				} else {
					Set<?> networks = Cytoscape.getNetworkSet();
					for (Iterator<?> iterator = networks.iterator(); iterator
							.hasNext();) {
						Object next = iterator.next();
						// String id = (String) iterator.next();
						String id = "";
						if (next instanceof String) {
							id = (String) next;
						} else if (next instanceof FingCyNetwork) {
							id = ((FingCyNetwork) next).getIdentifier();
						}
						CyNetwork network = Cytoscape.getNetwork(id);
						String title = network.getTitle();
						log.debug("compare: " + title + " AND "
								+ adjSet.getNetworkName());
						if (title.equals(adjSet.getNetworkName())) {
							found = true;
							foundID = id;
							break;
						}
					}
				}

				if (!found) {
					receiveMatrix(adjMatrixId);
				} else {
					Cytoscape.getDesktop().getNetworkPanel().focusNetworkNode(
							foundID);
				}
				if (cancelList.contains(adjMatrixId))
					cancelList.remove(adjMatrixId);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage());
		}

	}

	@Subscribe
	public void receive(ProjectNodeRemovedEvent event, Object source) {
		log.info("receive ProjectNodeRemovedEvent event");
		DSDataSet<? extends DSBioObject> dataSet = event.getAncillaryDataSet();
		if (!(dataSet instanceof AdjacencyMatrixDataSet)) {
			// if the event is published by other types, do nothing.
			return;
		}

		AdjacencyMatrixDataSet adjMatrixDataSet = (AdjacencyMatrixDataSet) dataSet;
		Set<?> networkSet = Cytoscape.getNetworkSet();
		for (Object network : networkSet) {
			String id = null;
			if (network instanceof FingCyNetwork) {
				id = ((FingCyNetwork) network).getIdentifier();
				CyNetwork cyNetwork = Cytoscape.getNetwork(id);
				if (cyNetwork.getTitle().equals(
						adjMatrixDataSet.getNetworkName())) {
					Cytoscape.destroyNetwork(cyNetwork);
					// Cytoscape.getVisualMappingManager()
					// .getCalculatorCatalog().removeVisualStyle(cyNetwork.getTitle()
					// + " Style");
					return;
				}
			} else {
				log
						.warn("Cytoscape network set contains something that is not FingCyNetwork.");
			}
		}
		log
				.warn("No network in the Cytoscape network set matches ProjectNodeRemovedEvent's dataSet name.");
	}

	// this static method is copied from cutenetManager so we don't depend on
	// that class any more
	static private Set<String> getSwissProtIDsForMarkers(
			Vector<DSGeneMarker> markerSet) {
		HashSet<String> results = new HashSet<String>();
		if (markerSet != null) {
			for (DSGeneMarker marker : markerSet) {
				try {
					results.addAll(AnnotationParser.getSwissProtIDs(marker
							.getLabel()));
				} catch (Exception e) {
					continue;
				}
			}
		}
		return results;
	}

	private void createEdge(CyNode n1, CyNode n2, String geneId1,
			String geneId2, String type) {
		if (type != null) {

			String typeName = interactionTypeSifMap.get(type);
			if (typeName == null || typeName.trim().equals(""))
				typeName = type;
			CyEdge e = Cytoscape.getCyEdge(n1.getIdentifier(), n1
					.getIdentifier()
					+ " (" + type + ") " + n1.getIdentifier(), n2
					.getIdentifier(), type);
			// Aracne result will not have an type, so we should
			// not need to check it here.
			if (!cytoNetwork.edgeExists(n2, n1))
				cytoNetwork.addEdge(e);

			e.setIdentifier(n1.getIdentifier() + " (" + type + ") "
					+ n2.getIdentifier());

			try {
				Cytoscape.getEdgeAttributes().setAttribute(e.getIdentifier(),
						"type", typeName);

				if (edgeDm.getMapValue(typeName) == null) {
					edgeDm.putMapValue(typeName, getRandomCorlor());

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			CyEdge e = Cytoscape.getCyEdge(n1.getIdentifier(), n1
					.getIdentifier()
					+ " () " + n2.getIdentifier(), n2.getIdentifier(), "");
			// For Aracne edges, if a reverse edge exist, we skip it.
			if (!cytoNetwork.edgeExists(n2, n1)) {
				// Aracne result will not have an type,
				// so we should check it here.
				cytoNetwork.addEdge(e);
			}
			e.setIdentifier(n1.getIdentifier() + " () " + n2.getIdentifier());

		}

	}

	private Comparator<DSGeneMarker> geneSymbolComparator = new Comparator<DSGeneMarker>() {
		@Override
		public int compare(DSGeneMarker m1, DSGeneMarker m2) {
			return m1.getGeneName().compareTo(m2.getGeneName());
		}
	};

	@SuppressWarnings("unchecked")
	private CyNode createNode(AdjacencyMatrix.Node node) {

		// cp1 will be the unique ID; displayName will be what it is called
		String nodeId = null;
		String displayedName = null;

		DSGeneMarker marker = null;

		if (node.type == NodeType.MARKER) {
			marker = node.marker;
			String geneName = node.marker.getGeneName().trim();
			nodeId = node.marker.getLabel();
			if (geneName.equals("") || geneName.equals("---"))
				displayedName = marker.getLabel();
			else
				displayedName = geneName;
		} else if (node.type == NodeType.GENE_SYMBOL) {
			String geneName = node.stringId;
			marker = maSet.getMarkers().get(geneName);
			nodeId = geneName;
			displayedName = geneName;
		} else if (node.type == NodeType.STRING) {
			nodeId = node.stringId;
			displayedName = node.stringId;
		} else if (node.type == NodeType.PROBESET_ID) {
			nodeId = node.stringId;
			displayedName = node.stringId;
		} else if (node.type == NodeType.OTHER) {
			String str = "Unknown Type (" + node.stringId + ", " + node.intId
					+ ")";
			nodeId = str;
			displayedName = str;
		}

		boolean n1new = true;
		CyNode cyNode = null;

		if (nodeId != null && !nodeId.trim().equals("")) {
			cyNode = Cytoscape.getCyNode(nodeId);
		} else {
			nodeId = "unknown";
			log.error("Unique ID is null. This should never happen.");
		}

		if (cyNode == null) { // new node
			cyNode = Cytoscape.getCyNode(nodeId, true);
			log.debug("I create " + cyNode.getIdentifier());

			cyNode.setIdentifier(nodeId);
			log.debug("I name it " + node.stringId + cyNode.getIdentifier());

			Cytoscape.getNodeAttributes().setAttribute(cyNode.getIdentifier(),
					"displayedName", displayedName);

		} else { // existing node
			n1new = false;
		}

		try {

			Cytoscape.getNodeAttributes().setAttribute(cyNode.getIdentifier(),
					"nodeType", node.type.name());

			List<String> spIDs = new Vector<String>();

			if (marker != null) {

				Cytoscape.getNodeAttributes().setAttribute(
						cyNode.getIdentifier(), "geneID", marker.getGeneId());

				Cytoscape.getNodeAttributes().setAttribute(
						cyNode.getIdentifier(), "geneName",
						marker.getGeneName());

				String geneType = GeneOntologyUtil.checkMarkerFunctions(marker);

				if (geneType == null || geneType.trim().equals(""))
					geneType = "non K/P/TF, in microarray set";

				Cytoscape.getNodeAttributes().setAttribute(
						cyNode.getIdentifier(), "geneType", geneType);
				if (nodeDm.getMapValue(geneType.trim()) == null) {
					nodeDm.putMapValue(geneType.trim(), shapes[++shapeIndex]);
					nodeDm.fireStateChanged();
				}

				try {
					Vector<DSGeneMarker> markerSet = new Vector<DSGeneMarker>();
					if (node.type == NodeType.MARKER)
						markerSet.add(marker);
					else if (node.type == NodeType.GENE_SYMBOL) {
						markerSet = ((CSMicroarraySet<DSMicroarray>) maSet)
								.getMarkers().getMatchingMarkers(marker);
					}
					Set<String> swissProtIDs = getSwissProtIDsForMarkers(markerSet);
					spIDs.addAll(swissProtIDs);
				} catch (NullPointerException npe) {
					log
							.error("unexpected null pointer in createNode of CytoscapeWidget");
				}

			} else {
				Cytoscape.getNodeAttributes().setAttribute(
						cyNode.getIdentifier(), "geneType", "null");
			}

			Cytoscape.getNodeAttributes().setListAttribute(
					cyNode.getIdentifier(), "swissprotIDs", spIDs);

		} catch (Exception e) {
			// we only try to add what we can add, if there's no
			// data, we add nothing
		}
		if (n1new) {
			cytoNetwork.addNode(cyNode);
			log.debug("I add " + cyNode.getIdentifier());
		}

		return cyNode;
	}

	/*
	 * replace
	 * 
	 * private void createSubNetwork(int node1, HashMap<String, String>
	 * geneIdToNameMap, double threshold) {
	 * 
	 * private void createSubNetwork(String node1, HashMap<String, String>
	 * geneIdToNameMap, double threshold) {
	 */
	private void createSubNetwork(AdjacencyMatrix.Node node1, double threshold) {

		AdjacencyMatrix adjMatrix = adjSet.getMatrix();
		List<AdjacencyMatrix.Edge> edges = adjMatrix.getEdges(node1);

		if (edges == null || edges.size() == 0) {
			CyNode n = createNode(node1);
			cytoNetwork.addNode(n);
			return;
		}

		for (AdjacencyMatrix.Edge edge : edges) {
			if (edge.node2 == node1)
				continue;

			String type = edge.info.type;

			if (edge.info.value <= threshold)
				continue;

			// process the two nodes
			CyNode n1 = createNode(node1);
			CyNode n2 = createNode(edge.node2);

			createEdge(n1, n2, String.valueOf(node1), String
					.valueOf(edge.node2), type);

		} // end of the loop for edges
	}

	private void cyNetWorkView_graphViewChanged(GraphViewChangeEvent gvce) {
		if (Cytoscape.getCurrentNetworkView() != null
				&& Cytoscape.getCurrentNetwork() != null) {
			java.util.List<?> nodes = Cytoscape.getCurrentNetworkView()
					.getSelectedNodes();
			java.util.List<?> edges = Cytoscape.getCurrentNetworkView()
					.getSelectedEdges();
			CytoscapeDesktop test = Cytoscape.getDesktop();

			if (edges.size() > 0) {
				test.getCytoPanel(SwingConstants.SOUTH).setSelectedIndex(1);
			} else {
				test.getCytoPanel(SwingConstants.SOUTH).setSelectedIndex(0);
			}

			if (publishEnabled)
				this.setNodeSelectColorToDefault();

			DSItemList<DSGeneMarker> sortedList = new CSItemList<DSGeneMarker>();
			sortedList.addAll(maSet.getMarkers());
			Collections.sort(sortedList, geneSymbolComparator);

			DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(
					"Selected Genes", "Cytoscape");
			for (int i = 0; i < nodes.size(); i++) {
				DNodeView pnode = (DNodeView) nodes.get(i);

				Node node = pnode.getNode();
				if (node instanceof CyNode) {
					String id = node.getIdentifier();
					// System.out.println("id = "+id);
					Integer geneId = Cytoscape.getNodeAttributes()
							.getIntegerAttribute(id, "geneID");
					if (geneId != null) {
						Collection<Integer> markerIds = geneIdToMarkerIdMap
								.get(geneId.toString());
						if (markerIds != null) {
							for (Integer markerId : markerIds) {
								selectedMarkers.add(maSet.getMarkers().get(
										markerId));
							}
						}

					} else { // node of the type of gene symbol
						if (id.trim().equals("---"))
							continue;

						int index = Collections.binarySearch(sortedList,
								new CSGeneMarker(id), geneSymbolComparator);
						if (index >= 0) {
							for (int j = index; j < sortedList.size(); j++) {
								DSGeneMarker marker = sortedList.get(j);
								if (!marker.getGeneName().equals(id))
									break;
								selectedMarkers.add(marker);
							}
							for (int j = index - 1; j >= 0; j--) {
								DSGeneMarker marker = sortedList.get(j);
								if (!marker.getGeneName().equals(id))
									break;
								selectedMarkers.add(marker);
							}
						}
					}
					if (geneIdToMarkerIdMap.size() == 0)
						// probably user doesn't load annotation file, so
						// geneIdToMarkerIdMap contains nothing
						selectedMarkers.add(maSet.getMarkers().get(id));

				}
			}
			selectedMarkers.setActive(true);
			if (publishEnabled) // skip if GeneTaggedEvent is being processed to
				// avoid event cycle
				publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
						DSGeneMarker.class,
						selectedMarkers,
						org.geworkbench.events.SubpanelChangedEvent.SET_CONTENTS));
		}
	}

	private Color getRandomCorlor() {
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);

		Color c = new Color(r, g, b);

		return c;
	}

	private void init() {

		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"Cytoscape");
		}
		Cytoscape.getDesktop().setVisible(false);
		CytoscapeInit initializer = new CytoscapeInit();

		CyInitParams param = new InitParam();
		// previously 'this' is passed to init method at the time when this is
		// not constructed yet. not better than this

		if (!initializer.init(param)) {
			log.warn("cytoscape initialization failed");
			Cytoscape.exit(1);
		}

		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(
				new GenewaysNetworkListener());

		VisualMappingManager manager = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = manager.getCalculatorCatalog();
		Calculator nc = catalog.getCalculator(VisualPropertyType.NODE_SHAPE,
				"Nested Network Style-Node Shape-Discrete Mapper");

		Vector<?> v = nc.getMappings();
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof DiscreteMapping) {
				nodeDm = (DiscreteMapping) v.get(i);
				break;
			}

		}

		nodeDm.setControllingAttributeName("geneType", cytoNetwork, false);
		nodeDm.putMapValue("null", shapes[shapeIndex]);
		nodeDm.putMapValue("K", shapes[++shapeIndex]);
		nodeDm.putMapValue("P", shapes[++shapeIndex]);
		nodeDm.putMapValue("TF", shapes[++shapeIndex]);
		// non K/E/TF, in microarray set
		nodeDm.putMapValue("non K/P/TF, in microarray set",
				shapes[++shapeIndex]);

		Calculator ec = catalog.getCalculator(VisualPropertyType.EDGE_COLOR,
				"BasicDiscrete");
		v = ec.getMappings();
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof DiscreteMapping) {
				edgeDm = (DiscreteMapping) v.get(i);
				break;
			}
		}
		edgeDm.setControllingAttributeName("type", cytoNetwork, false);

		for (VisualStyle vs : catalog.getVisualStyles()) {
			vs.getNodeAppearanceCalculator().setCalculator((nc));
			vs.getEdgeAppearanceCalculator().setCalculator(ec);

		}

		safePanel.setLayout(new BorderLayout());
		safePanel.add(safePanelContent, BorderLayout.CENTER);
		JPanel p = new JPanel();
		p.add(new JButton("Save as a file"));
		safePanel.add(p, BorderLayout.SOUTH);

	}
	
	private JTextField safePanelContent = new JTextField(); 

	private void receiveMatrix(int adjMatrixId) {
		// 1) RECEIVE event

		String name = adjSet.getNetworkName();
		String tmpname = adjSet.getMatrix().getLabel();
		if ((tmpname != null) && (!name.contains(tmpname))) {
			name = tmpname + " [" + name + "]";
		}
		Set<?> networks = Cytoscape.getNetworkSet();
		HashSet<String> names = new HashSet<String>();
		for (Iterator<?> iterator = networks.iterator(); iterator.hasNext();) {
			FingCyNetwork network = (FingCyNetwork) iterator.next();
			String title = network.getTitle();
			names.add(title);
		}
		name = Util.getUniqueName(name, names);
		adjSet.setNetworkName(name);

		cytoNetwork = Cytoscape.createNetwork(name);

		try {
			JInternalFrame[] frames = Cytoscape.getDesktop()
					.getNetworkViewManager().getDesktopPane().getAllFrames();

			for (int i = 0; i < frames.length; i++) {
				frames[i].setMaximum(true);
			}

		} catch (Exception e) {
			log.error(e, e);
			// we just try to maximize the window, if failed, no big
			// deal
		}

		// 2) DRAW NETWORK event
		drawCompleteNetwork(adjMatrixId, adjSet.getThreshold());
		if (cancelList.contains(adjMatrixId)) {
			log.info("got cancel action");
			return;

		}

		CalculatorCatalog catalog = Cytoscape.getVisualMappingManager()
				.getCalculatorCatalog();
		VisualStyle visualStyle = new VisualStyle(catalog
				.getVisualStyle("Nested Network Style"), name + " Style");

		try {
			catalog.addVisualStyle(visualStyle);
		} catch (DuplicateCalculatorNameException ex) {
			String existStyle = visualStyle.getName();
			visualStyle = new VisualStyle(catalog
					.getVisualStyle("Nested Network Style"), catalog
					.checkVisualStyleName(name + " Style"));
			catalog.addVisualStyle(visualStyle);
			catalog.removeVisualStyle(existStyle);

		}

		CytoPanel temp = Cytoscape.getDesktop().getCytoPanel(
				SwingConstants.WEST);

		NetworkPanel tempp = null;
		for (int cx = 0; cx < temp.getCytoPanelComponentCount(); cx++) {
			if (temp.getComponentAt(cx) instanceof NetworkPanel)
				tempp = (NetworkPanel) temp.getComponentAt(cx);
		}
		tempp.hide(); // this line fixed wrong node number and wrong
		// edge

		// number.
		// 3) FINISH event
		if (maSet != null) {
			view = Cytoscape.createNetworkView(cytoNetwork, maSet.getLabel());

			NodeAppearanceCalculator nac = visualStyle
					.getNodeAppearanceCalculator();
			ObjectMapping oMapping = nac.getCalculator(
					VisualPropertyType.NODE_LABEL).getMapping(0);
			if (oMapping instanceof PassThroughMapping) {
				PassThroughMapping m = (PassThroughMapping) oMapping;
				m.setControllingAttributeName("displayedName", Cytoscape
						.getCurrentNetwork(), false);
			} else {
				log.error("Wrong type of ObjectMapping: "
						+ oMapping.getClass().getName());
			}
			view.applyVizmapper(visualStyle);

			view.addGraphViewChangeListener(new GraphViewChangeListener() {
				public void graphViewChanged(
						GraphViewChangeEvent graphViewChangeEvent) {
					cyNetWorkView_graphViewChanged(graphViewChangeEvent);
				}
			});

			view.getComponent().addMouseListener(
					new ExpandMenuListener(CytoscapeWidget.this));

			log.info("DrawAction finished.");
			resetNetwork();
		}

	}

	public AdjacencyMatrix getAdjMatrix() {
		AdjacencyMatrix adjMatrix = adjSet.getMatrix();
		return adjMatrix;

	}

	public AdjacencyMatrixDataSet getAdjMatrixDataSet() {
		return this.adjSet;

	}

	void drawCompleteNetwork(int adjMatrixId, double threshold) {

		for (int cx = 0; cx < Cytoscape.getCurrentNetwork().getEdgeCount(); cx++) {
			Cytoscape.getCurrentNetwork().removeEdge(cx, true);
		}

		AdjacencyMatrix adjMatrix = adjSet.getMatrix();
		interactionTypeSifMap = adjMatrix.getInteractionTypeSifMap();
		if (interactionTypeSifMap == null)
			interactionTypeSifMap = new HashMap<String, String>();
		int i = 0;
		for (AdjacencyMatrix.Node node : adjMatrix.getNodes()) {
			if (cancelList.contains(adjMatrixId)) {
				log.info("got cancel action");
				return;
			}
			i++;
			createSubNetwork(node, threshold);
			log.debug("iteration: " + i);
		}

		if (cancelList.contains(adjMatrixId)) {
			log.info("got cancel action");
			return;
		}

		AbstractLayout layout = new ForceDirectedLayout();
		TaskManager.executeTask(new LayoutTask(layout, Cytoscape
				.getCurrentNetworkView()), LayoutTask.getDefaultTaskConfig());

		if (cancelList.contains(adjMatrixId)) {
			log.info("got cancel action");
			return;
		}

		// new SpringEmbeddedLayouter(Cytoscape.getCurrentNetworkView())
		// .doLayout();

		if (cancelList.contains(adjMatrixId)) {
			log.info("got cancel action");
			return;
		}

		Cytoscape.getCurrentNetworkView().fitContent();

	}

	public void setNodeSelectColorToDefault() {
		Color c = Cytoscape.getVisualMappingManager().getVisualStyle()
				.getGlobalAppearanceCalculator().getDefaultNodeSelectionColor();
		Iterator<?> iter = Cytoscape.getCurrentNetworkView()
				.getNodeViewsIterator();

		while (iter.hasNext()) {
			NodeView nodeView = (NodeView) iter.next();
			nodeView.setSelectedPaint(c);
		}

	}

	/*
	 * This function restore the original status for current selected network.
	 */
	public void resetNetwork() {
		CyAttributes attrs = null;
		CyNetworkView view = Cytoscape.getCurrentNetworkView();
		if (view != null && Cytoscape.getCurrentNetwork() != null) {
			attrs = Cytoscape.getNodeAttributes();
			Iterator<?> nodeIter = view.getNodeViewsIterator();

			while (nodeIter.hasNext()) {
				NodeView nodeView = (NodeView) nodeIter.next();
				nodeView.unselect();
				String id = nodeView.getNode().getIdentifier();
				if (attrs.hasAttribute(id, CytoscapeWidget.NODE_FILL_COLOR))
					attrs.deleteAttribute(id, CytoscapeWidget.NODE_FILL_COLOR);

			}

			Iterator<?> edgeIter = view.getEdgeViewsIterator();
			while (edgeIter.hasNext()) {
				EdgeView edgeView = (EdgeView) edgeIter.next();
				view.showGraphObject(edgeView);
			}

			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
		}
	}

}
