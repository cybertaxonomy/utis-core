/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.EnumSet;

import org.apache.lucene.queryParser.QueryParser;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.TinkerPopClient;
import org.cybertaxonomy.utis.store.LastModifiedProvider;
import org.cybertaxonomy.utis.store.Neo4jStore;
import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.cybertaxonomy.utis.store.ResourceProvider;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

/**
 * @author a.kohlbecker
 * @date Jun 17, 2016
 *
 */
public class PlaziClient extends BaseChecklistClient<TinkerPopClient> implements UpdatableStoreInfo {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziClient.class);

    public static final String ID = "plazi";
    public static final String LABEL = "Plazi TreatmentBank";
    public static final String DOC_URL = "http://plazi.org/fusszeilen/info1/legal-disclaimer/";
    public static final String COPYRIGHT_URL = "https://creativecommons.org/publicdomain/zero/1.0/";

    /**
     * check for updates once a day
     */
    private static final int CHECK_UPDATE_MINUTES = 60 * 24;

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
//            SearchMode.scientificNameLike,
//            SearchMode.vernacularNameExact,
//            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.noneOf(ClassificationAction.class);

    private static final String DOWNLOAD_BASE_URL = "http://localhost/download/";

    public static final String TREATMENTBANK_RSS_FEED = "http://tb.plazi.org/GgServer/xml.rss.xml";

    private String testUrl = null;

    private PlaziResourceProvider resourceProvider = null;

    public static enum RdfSchema implements IRdfSchema {

        BIBO("bibo", "http://purl.org/ontology/bibo/"),
        CITO("cito", "http://purl.org/spar/cito/"),
        CNT("cnt", "http://www.w3.org/2011/content#"),
        DC("dc", "http://purl.org/dc/elements/1.1/"),
        DWC("dwc", "http://rs.tdwg.org/dwc/terms/"),
        DWCFP("dwcFP", "http://filteredpush.org/ontologies/oa/dwcFP#"),
        FABIO("fabio", "http://purl.org/spar/fabio/"),
        RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
        SSD("sdd", "http://tdwg.org/sdd#"),
        SDO("sdo", "http://schema.org/"),
        SPM("spm", "http://rs.tdwg.org/ontology/voc/SpeciesProfileModel"),
        TRT("trt", "http://plazi.org/vocab/treatment#"),
        XSD("xsd", "http://www.w3.org/2001/XMLSchema#");

        private String schemaUri;
        private String abbreviation;
        RdfSchema(String abbreviation, String schemaUri) {
            this.abbreviation = abbreviation;
            this.schemaUri = schemaUri;
        }

        @Override
        public String schemaUri() {

            return schemaUri;
        }

        @Override
        public String abbreviation() {

            return abbreviation;
        }

        @Override
        public String property(String name) {
            return schemaUri + name;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        _findByIdentifier(tnrMsg, false);
    }

    /**
     * @param tnrMsg
     * @param addClassification TODO
     * @throws DRFChecklistException
     */
    private void _findByIdentifier(TnrMsg tnrMsg, boolean addClassification) throws DRFChecklistException {


        ServiceProviderInfo checklistInfo = getServiceProviderInfo();

        Query query = singleQueryFrom(tnrMsg);
        String queryString = query.getRequest().getQueryString();

        // by using the Neo4j index directly it is possible to
        // take full advantage of the underlying Lucene search engine
        queryString = QueryParser.escape(queryString);
        ArrayList<Vertex> hitVs = queryClient.vertexIndexQuery("value:" + queryString);
        if(hitVs.size() > 0) {
            //FIXME implement tnrResponseFromResource:
            //Response response = tnrResponseFromResource(hitVs.get(0), query.getRequest(), null, null, checklistInfo, addClassification);
            //query.getResponse().add(response);
        } else if(hitVs.size() > 1) {
            throw new DRFChecklistException("More than one node with the id '" + queryString + "' found");
        }

    }

    /**
     * Checks if the given <code>identifier</code> already is in the database and if it is connected to a taxon concept.
     *
     * @param String identifier
     * @return
     *
     * @throws DRFChecklistException
     */
    public String checkTreatmentIdentifier(String identifier) throws DRFChecklistException {


        String queryString = identifier;

        // by using the Neo4j index directly it is possible to
        // take full advantage of the underlying Lucene search engine
        queryString = QueryParser.escape(queryString);
        ArrayList<Vertex> hitVs = queryClient.vertexIndexQuery("value:" + queryString);
        if(hitVs.size() > 0) {
            String taxonConceptID = queryClient.relatedVertexValue(hitVs.get(0), RdfSchema.TRT, "definesTaxonConcept");
            if(taxonConceptID != null){
                return taxonConceptID;
            }
        } else if(hitVs.size() > 1) {
            throw new DRFChecklistException("More than one node with the id '" + queryString + "' found");
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet getSearchModes() {
        return SEARCH_MODES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet getClassificationActions() {

        return CLASSIFICATION_ACTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupportedIdentifier(String value) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initQueryClient() {
        Neo4jStore neo4jStore = Neo4jStoreManager.provideStoreFor(this);
        queryClient = new TinkerPopClient(neo4jStore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID, LABEL, DOC_URL, COPYRIGHT_URL, getSearchModes());
        return checklistInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatelessClient() {
        return true;
    }


    public void setTestUrl(String url) {
        this.testUrl = url;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestUrl() {
        if(testUrl == null){
            // this is the default case
            // testUrl != null is only true in test cases
            return TREATMENTBANK_RSS_FEED;
        }
        return testUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int pollIntervalMinutes() {
        return CHECK_UPDATE_MINUTES;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doIncrementalUpdates(){
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LastModifiedProvider getLastModifiedProvider() {
        if(resourceProvider == null){
            resourceProvider = new PlaziResourceProvider(this);
        }
        return resourceProvider;
    }


    @Override
    public String getInstanceName(){
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceProvider getResourceProvider() {
        if(resourceProvider == null){
            resourceProvider = new PlaziResourceProvider(this);
        }
        return resourceProvider;
    }

}
