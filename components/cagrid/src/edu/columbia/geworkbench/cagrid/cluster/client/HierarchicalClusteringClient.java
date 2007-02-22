package edu.columbia.geworkbench.cagrid.cluster.client;

import edu.columbia.geworkbench.cagrid.cluster.common.HierarchicalClusteringI;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.HierarchicalClusteringPortType;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.service.HierarchicalClusteringServiceAddressingLocator;
import edu.columbia.geworkbench.cagrid.converter.CaGridConverter;
import edu.columbia.geworkbench.cagrid.microarray.Marker;
import edu.columbia.geworkbench.cagrid.microarray.Microarray;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;

import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ginkgo.labs.reader.TabFileReader;
import org.globus.gsi.GlobusCredential;

/**
 * This class is autogenerated, DO NOT EDIT. This class is not thread safe. A
 * new instance should be created for any threads using this class. On
 * construction the class instance will contact the remote service and retrieve
 * it's security metadata description which it will use to configure the Stub
 * specifically for each method call.
 * 
 * @created by Introduce Toolkit version 1.0
 * @author keshav
 * @version $Id: HierarchicalClusteringClient.java,v 1.8 2007/02/09 23:50:03
 *          keshav Exp $
 */
public class HierarchicalClusteringClient extends ServiceSecurityClient
		implements HierarchicalClusteringI {
	private static Log log = LogFactory
			.getLog(HierarchicalClusteringClient.class);
	protected HierarchicalClusteringPortType portType;
	private Object portTypeMutex;

	public HierarchicalClusteringClient(String url)
			throws MalformedURIException, RemoteException {
		this(url, null);
	}

	public HierarchicalClusteringClient(String url, GlobusCredential proxy)
			throws MalformedURIException, RemoteException {
		super(url, proxy);
		initialize();
	}

	public HierarchicalClusteringClient(EndpointReferenceType epr)
			throws MalformedURIException, RemoteException {
		this(epr, null);
	}

	public HierarchicalClusteringClient(EndpointReferenceType epr,
			GlobusCredential proxy) throws MalformedURIException,
			RemoteException {
		super(epr, proxy);
		initialize();
	}

	private void initialize() throws RemoteException {
		this.portTypeMutex = new Object();
		this.portType = createPortType();
	}

	private HierarchicalClusteringPortType createPortType()
			throws RemoteException {

		HierarchicalClusteringServiceAddressingLocator locator = new HierarchicalClusteringServiceAddressingLocator();
		// attempt to load our context sensitive wsdd file
		// InputStream resourceAsStream = ClassUtils.getResourceAsStream(
		// getClass(), "client-config.wsdd" );
		InputStream resourceAsStream = ClassUtils.getResourceAsStream(
				getClass(), "conf/client-config.wsdd");
		if (resourceAsStream != null) {
			// we found it, so tell axis to configure an engine to use it
			EngineConfiguration engineConfig = new FileProvider(
					resourceAsStream);
			// set the engine of the locator
			locator.setEngine(new AxisClient(engineConfig));
		}
		HierarchicalClusteringPortType port = null;
		try {
			port = locator
					.getHierarchicalClusteringPortTypePort(getEndpointReference());
		} catch (Exception e) {
			throw new RemoteException("Unable to locate portType:"
					+ e.getMessage(), e);
		}

		return port;
	}

	public static void usage() {
		System.out.println(HierarchicalClusteringClient.class.getName()
				+ " -url <service url>");
	}

	public static void main(String[] args) {
		log.debug("Running the Grid Service Client");
		try {
			// if (!(args.length < 2)) {
			if (!(args.length < 3)) {// keshav
				if (args[0].equals("-url")) {
					String url = args[1];
					HierarchicalClusteringClient client = new HierarchicalClusteringClient(
							url);

					/* my client side method invocation */
					log.debug("Invoking Hierarchical Clustering Service ... ");
					// MicroarraySet arraySet =
					// client.configureTestMicroarrays(); // test
					String filename = args[2];
					MicroarraySet arraySet = CaGridConverter
							.float2DToMicroarraySet(TabFileReader
									.readTabFile(filename));

					HierarchicalClusteringParameter parameters = new HierarchicalClusteringParameter();
					parameters.setDim(Dim.both);
					parameters.setDistance(Distance.euclidean);
					parameters.setMethod(Method.complete);
					HierarchicalCluster hierarchicalClustering = client
							.execute(arraySet, parameters);

					// TODO rename to getMicroarrayCluster and getMarkerCluster
					log.debug("hierarchical cluster: "
							+ hierarchicalClustering
							+ "\nmicroarray cluster height: "
							+ hierarchicalClustering.getMicroarrayCluster()
									.getHeight()
							+ "\nmarker cluster height: "
							+ hierarchicalClustering.getMarkerCluster()
									.getHeight());

					TabFileReader.serializeToXml(arraySet);
					TabFileReader.serializeToXml(hierarchicalClustering);

				} else {
					usage();
					System.exit(1);
				}
			} else {
				usage();
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param data
	 * @return MicroarraySet
	 */
	public static MicroarraySet float2DToMicroarraySet(float[][] data) {

		int numMarkers = data.length;
		int numMicroarrays = data[0].length;

		log.debug("data set contains " + numMicroarrays + " microarrays");
		log.debug("data set contains " + numMarkers + " markers");

		MicroarraySet microarraySet = new MicroarraySet();
		Microarray microarrays[] = new Microarray[numMicroarrays];
		Marker markers[] = new Marker[numMarkers];
		// FIXME should have a marker equivalent of constructing this matrix
		// set array data
		for (int j = 0; j < numMicroarrays; j++) {
			float[] col = new float[numMarkers];
			for (int i = 0; i < data.length; i++) {
				col[i] = data[i][j];
			}
			Microarray microarray = new Microarray();
			microarray.setArrayName("array" + j);
			microarray.setArrayData(col);
			microarrays[j] = microarray;
		}

		// set marker names
		for (int i = 0; i < numMarkers; i++) {
			Marker marker = new Marker();
			marker.setMarkerName(i + "_at");
			markers[i] = marker;
		}

		microarraySet.setMicroarray(microarrays);
		microarraySet.setMarker(markers);
		return microarraySet;
	}

	public gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata getServiceSecurityMetadata()
			throws RemoteException {
		synchronized (portTypeMutex) {
			configureStubSecurity((Stub) portType, "getServiceSecurityMetadata");
			gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest params = new gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest();
			gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataResponse boxedResult = portType
					.getServiceSecurityMetadata(params);
			return boxedResult.getServiceSecurityMetadata();
		}
	}

	public edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalCluster execute(
			edu.columbia.geworkbench.cagrid.microarray.MicroarraySet microarraySet,
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter hierarchicalClusteringParameter)
			throws RemoteException {
		synchronized (portTypeMutex) {
			configureStubSecurity((Stub) portType, "execute");
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest params = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequest();
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet microarraySetContainer = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestMicroarraySet();
			microarraySetContainer.setMicroarraySet(microarraySet);
			params.setMicroarraySet(microarraySetContainer);
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter hierarchicalClusteringParameterContainer = new edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteRequestHierarchicalClusteringParameter();
			hierarchicalClusteringParameterContainer
					.setHierarchicalClusteringParameter(hierarchicalClusteringParameter);
			params
					.setHierarchicalClusteringParameter(hierarchicalClusteringParameterContainer);
			edu.columbia.geworkbench.cagrid.cluster.hierarchical.stubs.ExecuteResponse boxedResult = portType
					.execute(params);
			return boxedResult.getHierarchicalCluster();
		}
	}

}
