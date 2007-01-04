/**
 * HierarchicalClusteringServiceAddressingLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package edu.columbia.geworkbench.cagrid.cluster.stubs.service;

public class HierarchicalClusteringServiceAddressingLocator extends edu.columbia.geworkbench.cagrid.cluster.stubs.service.HierarchicalClusteringServiceLocator implements edu.columbia.geworkbench.cagrid.cluster.stubs.service.HierarchicalClusteringServiceAddressing {
    public edu.columbia.geworkbench.cagrid.cluster.stubs.HierarchicalClusteringPortType getHierarchicalClusteringPortTypePort(org.apache.axis.message.addressing.EndpointReferenceType reference) throws javax.xml.rpc.ServiceException {
	org.apache.axis.message.addressing.AttributedURI address = reference.getAddress();
	if (address == null) {
		throw new javax.xml.rpc.ServiceException("No address in EndpointReference");
	}
	java.net.URL endpoint;
	try {
		endpoint = new java.net.URL(address.toString());
	} catch (java.net.MalformedURLException e) {
		throw new javax.xml.rpc.ServiceException(e);
	}
	edu.columbia.geworkbench.cagrid.cluster.stubs.HierarchicalClusteringPortType _stub = getHierarchicalClusteringPortTypePort(endpoint);
	if (_stub != null) {
		org.apache.axis.message.addressing.AddressingHeaders headers =
			new org.apache.axis.message.addressing.AddressingHeaders();
		headers.setTo(address);
		headers.setReferenceProperties(reference.getProperties());
		((javax.xml.rpc.Stub)_stub)._setProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_SHARED_HEADERS, headers);
	}
	return _stub;
    }


}
