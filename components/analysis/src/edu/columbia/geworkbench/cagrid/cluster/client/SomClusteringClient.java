package edu.columbia.geworkbench.cagrid.cluster.client;

import edu.columbia.geworkbench.cagrid.cluster.common.SomClusteringI;
import edu.columbia.geworkbench.cagrid.cluster.som.SomCluster;
import edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.som.stubs.SomClusteringPortType;
import edu.columbia.geworkbench.cagrid.cluster.som.stubs.service.SomClusteringServiceAddressingLocator;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySet;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySetGenerator;
import edu.columbia.geworkbench.cagrid.microarray.MicroarraySetGeneratorImpl;
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
 * @author keshav
 * @version $Id: SomClusteringClient.java,v 1.5 2007-04-02 14:56:03 keshav Exp $
 */
public class SomClusteringClient extends ServiceSecurityClient implements
		SomClusteringI {
	private static Log log = LogFactory.getLog(SomClusteringClient.class);

	protected SomClusteringPortType portType;

	private Object portTypeMutex;

	public SomClusteringClient(String url) throws MalformedURIException,
			RemoteException {
		this(url, null);
	}

	public SomClusteringClient(String url, GlobusCredential proxy)
			throws MalformedURIException, RemoteException {
		super(url, proxy);
		initialize();
	}

	public SomClusteringClient(EndpointReferenceType epr)
			throws MalformedURIException, RemoteException {
		this(epr, null);
	}

	public SomClusteringClient(EndpointReferenceType epr, GlobusCredential proxy)
			throws MalformedURIException, RemoteException {
		super(epr, proxy);
		initialize();
	}

	private void initialize() throws RemoteException {
		this.portTypeMutex = new Object();
		this.portType = createPortType();
	}

	private SomClusteringPortType createPortType() throws RemoteException {

		SomClusteringServiceAddressingLocator locator = new SomClusteringServiceAddressingLocator();
		// attempt to load our context sensitive wsdd file
		InputStream resourceAsStream = ClassUtils.getResourceAsStream(
				getClass(), "client-config.wsdd");
		if (resourceAsStream != null) {
			// we found it, so tell axis to configure an engine to use it
			EngineConfiguration engineConfig = new FileProvider(
					resourceAsStream);
			// set the engine of the locator
			locator.setEngine(new AxisClient(engineConfig));
		}
		SomClusteringPortType port = null;
		try {
			port = locator.getSomClusteringPortTypePort(getEndpointReference());
		} catch (Exception e) {
			throw new RemoteException("Unable to locate portType:"
					+ e.getMessage(), e);
		}

		return port;
	}

	public static void usage() {
		System.out.println(SomClusteringClient.class.getName()
				+ " -url <service url>");
	}

	public static void main(String[] args) {
		log.debug("Running the Grid Service Client");
		try {
			if (!(args.length < 2)) {
				if (args[0].equals("-url")) {
					String url = args[1];
					SomClusteringClient client = new SomClusteringClient(url);

					/* my client side method invocation */
					log
							.debug("Invoking Self Organized Maps Clustering Service ... ");

					String filename = "src//edu//columbia//geworkbench//cagrid//data//aTestDataSet_without_headers_30.txt";

					float[][] fdata = TabFileReader.readTabFile(filename);

					String[] rowNames = new String[fdata.length];
					for (int i = 0; i < rowNames.length; i++) {
						rowNames[i] = i + "_at";
					}

					String[] colNames = new String[fdata[0].length]; // non-ragged
					for (int j = 0; j < colNames.length; j++) {
						colNames[j] = String.valueOf(j);
					}

					MicroarraySetGenerator microarraySetGenerator = new MicroarraySetGeneratorImpl();
					MicroarraySet arraySet = microarraySetGenerator
							.float2DToMicroarraySet(fdata, rowNames, colNames);

					/* set parameters */
					SomClusteringParameter somParameter = new SomClusteringParameter();
					somParameter.setDim_x(4);
					somParameter.setDim_y(3);
					somParameter.setFunction(0);
					somParameter.setRadius(2f);
					somParameter.setAlpha(0.02f);
					somParameter.setIteration(4000);
					SomCluster somCluster = client.execute(arraySet,
							somParameter);
					for (int i = 0; i < somCluster.getXCoordinate().length; i++) {
						log.debug("name at " + i + ":"
								+ somCluster.getName()[i]);
						log.debug("x coordinate at " + i + ":"
								+ somCluster.getXCoordinate()[i]);
						log.debug("y coordinate at " + i + ":"
								+ somCluster.getYCoordinate()[i] + "\n");
					}
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

	public edu.columbia.geworkbench.cagrid.cluster.som.SomCluster execute(
			edu.columbia.geworkbench.cagrid.microarray.MicroarraySet microarraySet,
			edu.columbia.geworkbench.cagrid.cluster.som.SomClusteringParameter somClusteringParameter)
			throws RemoteException {
		synchronized (portTypeMutex) {
			configureStubSecurity((Stub) portType, "execute");
			edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequest params = new edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequest();
			edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestMicroarraySet microarraySetContainer = new edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestMicroarraySet();
			microarraySetContainer.setMicroarraySet(microarraySet);
			params.setMicroarraySet(microarraySetContainer);
			edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestSomClusteringParameter somClusteringParameterContainer = new edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteRequestSomClusteringParameter();
			somClusteringParameterContainer
					.setSomClusteringParameter(somClusteringParameter);
			params.setSomClusteringParameter(somClusteringParameterContainer);
			edu.columbia.geworkbench.cagrid.cluster.som.stubs.ExecuteResponse boxedResult = portType
					.execute(params);
			return boxedResult.getSomCluster();
		}
	}

}
