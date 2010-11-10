package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;

import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;


/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Defines a contract to obtain Gene Information
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public interface GeneAnnotation {
    /**
     * Gets Gene name
     *
     * @return Gene name
     */
    String getGeneName();
    Gene getGene();
    /**
     * Gets Gene Description
     *
     * @return Gene Description
     */
    String getGeneDescription();

    List<GeneAnnotationImpl.CGAPUrl> getCGAPGeneURLs();

    /**
     * Gets Gene Locus Link ID
     *
     * @return Gene Locus Link ID
     */
    String getLocusLinkId();

    /**
     * Gets Gene Unigene Cluster ID
     *
     * @return Gene Unigene Cluster ID
     */
    Long getUnigeneClusterId();

    /**
     * Gets Organism abbreviation
     *
     * @return Organism abbreviation
     */
    String getOrganismAbbreviation();

    /**
     * Gets associated pathways
     *
     * @return associated pathways
     */
    org.geworkbench.util.annotation.Pathway[] getPathways();

//    boolean equals(Object object);

    String getGeneSymbol();

    void setSymbol(String symbol);
    
    String getEntrezId(Gene gene);    
    
    AgentDiseaseResults retrieveAll(RetrieveAllTask task, DSItemList<DSGeneMarker> retrieveMarkerInfo, CGITableModel diseaseModel, CGITableModel agentModel);
    AgentDiseaseResults showAnnotation(CGITask task, DSItemList<DSGeneMarker> retrieveMarkerInfo);
}