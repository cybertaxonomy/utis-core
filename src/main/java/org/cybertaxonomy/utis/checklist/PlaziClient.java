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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.TinkerPopClient;
import org.cybertaxonomy.utis.store.LastModifiedProvider;
import org.cybertaxonomy.utis.store.Neo4jStore;
import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.cybertaxonomy.utis.store.ResourceProvider;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Query.Request;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Source;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.gbif.api.vocabulary.TaxonomicStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author a.kohlbecker
 * @date Jun 17, 2016
 *
 */
public class PlaziClient extends TinkerPopChecklistClient implements UpdatableStoreInfo {

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
            SearchMode.scientificNameLike,
//            SearchMode.vernacularNameExact,
//            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.of(
            ClassificationAction.higherClassification
            );

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


    String[] dwcClassificationTerms =  new String[]{
            "species",
            "genus",
            "family",
            "order",
            "class",
            "phylum",
            "kingdom",
            };
    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        ServiceProviderInfo checklistInfo = getServiceProviderInfo();

        // selecting one request as representative, only
        // the search mode and addSynonmy flag are important
        // for the further usage of the request object
        Query query = singleQueryFrom(tnrMsg);

        PagerRange pagerRange = pagerRange(query);

        String queryString = query.getRequest().getQueryString();
        logger.debug("original queryString: "+ queryString);
        queryString = QueryParser.escape(queryString);
        queryString = queryString.replace(" ", "\\ ");
        if(query.getRequest().getSearchMode().equals(SearchMode.scientificNameLike.name())) {
            queryString += "*";
        }
        logger.debug("prepared queryString: "+ queryString);

        GremlinPipeline<Graph, Vertex> pipe = null;

        ArrayList<Vertex> hitVs = queryClient.vertexIndexQuery("value:" + queryString);
        pipe = new GremlinPipeline<Graph, Vertex>(hitVs);

        List<Vertex> vertices = new ArrayList<Vertex>();

        pipe.inE(RdfSchema.CITO.property("cites")).outV().outE(RdfSchema.TRT.property("definesTaxonConcept")).inV()
            .range(pagerRange.low, pagerRange.high).fill(vertices);

        updateQueriesWithResponse(vertices, null, null, checklistInfo, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // delegate to resolveScientificNamesExact,
        resolveScientificNamesExact(tnrMsg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // UNSUPORTED
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // UNSUPORTED
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
            Response response = tnrResponseFromResource(hitVs.get(0), query.getRequest(), null, null, checklistInfo, addClassification);
            query.getResponse().add(response);
        } else if(hitVs.size() > 1) {
            throw new DRFChecklistException("More than one node with the id '" + queryString + "' found");
        }

    }

    /**
     * Checks if the given <code>identifier</code> already is in the database and if it is connected to a taxon concept.
     *
     * @param String identifier
     * @return the persistent identifier for the according taxon concept
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
     * @param request
     * @param matchNode
     * @param matchType
     * @param ci
     * @param addClassification TODO
     * @param model
     * @param taxonR
     * @return
     */
    @Override
    protected Response tnrResponseFromResource(Vertex taxonV, Request request, Vertex matchNode, NameType matchType, ServiceProviderInfo ci, boolean addClassification) {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

        GremlinPipeline<Graph, Vertex> pipe = new GremlinPipeline<Graph, Vertex>(taxonV);

        if(matchNode != null) {
            String matchingName = matchNode.getProperty(GraphSail.VALUE).toString();
            tnrResponse.setMatchingNameString(matchingName);
            tnrResponse.setMatchingNameType(matchType);
        }


        Taxon taxon = createTaxon(taxonV, addClassification, request.isAddParentTaxon());
        tnrResponse.setTaxon(taxon);
        if(matchNode == null) {
            tnrResponse.setMatchingNameType(NameType.TAXON);
            String matchingName = taxon.getTaxonName().getCanonicalName();
            tnrResponse.setMatchingNameString(matchingName);
        }


        logger.debug("processing  accepted taxon " + taxonV.getId() + " DONE");
        return tnrResponse;
    }

    /**
     * @param taxonV
     * @param addClassification
     * @param addParentTaxon
     * @return
     */
    private Taxon createTaxon(Vertex taxonV, boolean addClassification, boolean addParentTaxon) {

        Taxon taxon = new Taxon();

        TaxonName taxonName = createTaxonName(taxonV);

        // Taxon
        taxon.setTaxonName(taxonName);
        taxon.setUrl(taxonV.getProperty(GraphSail.VALUE).toString());
        taxon.setIdentifier(taxon.getUrl());
        // According is gotten from the sources, see below
        taxon.setTaxonomicStatus(TaxonomicStatus.ACCEPTED.name());

        // source
        Source source = new Source();

        GremlinPipeline<Graph, Vertex> publishedInPipe = new GremlinPipeline<Graph, Vertex>(taxonV);
        Vertex publishedIn = publishedInPipe.inE(RdfSchema.TRT.property("definesTaxonConcept")).outV().outE(RdfSchema.TRT.property("publishedIn")).inV().next();

        String plaziID = queryClient.relatedVertexValue(taxonV, RdfSchema.TRT, "definesTaxonConcept");
        String doi = publishedIn.getProperty(GraphSail.VALUE).toString().replace("http://dx.doi.org/", "");
        source.setIdentifier(plaziID);
        source.setUrl(doi);
        source.setDatasetName("Plazi TreatmentBase");

        taxon.getSources().add(source);

        taxon.setAccordingTo(taxon.getSources().get(0).getTitle());

        if (addClassification) {
            for (String rank : dwcClassificationTerms) {
                String higherTaxonName = queryClient.relatedVertexValue(taxonV, RdfSchema.DWC, rank);
                if (higherTaxonName != null) {
                    HigherClassificationElement hce = new HigherClassificationElement();
                    hce.setRank(StringUtils.capitalize(rank));
                    hce.setScientificName(higherTaxonName);
                    taxon.getHigherClassification().add(hce);
                }
            }

        }

        return taxon;
    }

    /**
     * @param taxonV
     * @return
     */
    private TaxonName createTaxonName(Vertex taxonV) {
        TaxonName taxonName = new TaxonName();


        GremlinPipeline<Graph, Vertex> nomRefPipe = new GremlinPipeline<Graph, Vertex>(taxonV);
        Vertex nomRefV = nomRefPipe.inE(RdfSchema.TRT.property("definesTaxonConcept")).outV().outE(RdfSchema.DC.property("description")).inV().next();

//        GremlinPipeline<Graph, Vertex> fullScientificNamePipe = new GremlinPipeline<Graph, Vertex>(taxonV);
//        Vertex fullScientificNameV = fullScientificNamePipe.inE(RdfSchema.TRT.property("definesTaxonConcept")).outV().outE(RdfSchema.DC.property("title")).inV().next();
//        if(fullScientificNameV != null){
//            taxonName.setScientificName(fullScientificNameV.getProperty(GraphSail.VALUE).toString());
//        }

        if(nomRefV != null){
            taxonName.setNomenclaturalReference(nomRefV.getProperty(GraphSail.VALUE).toString());
        }
        taxonName.setScientificName(queryClient.relatedVertexValue(taxonV, RdfSchema.DWC, "scientificName"));
        taxonName.setAuthorship(queryClient.relatedVertexValue(taxonV, RdfSchema.DWC, "scientificNameAuthorship"));
        taxonName.setCanonicalName(taxonName.getScientificName().replace(taxonName.getAuthorship(), ""));
        taxonName.setRank(queryClient.relatedVertexValue(taxonV, RdfSchema.DWC, "rank"));
        //TODO add status: taxonName.setStatus(queryClient.relatedVertexValue(v, RdfSchema.DWC, "status"));
        return taxonName;
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
        _findByIdentifier(tnrMsg, true);
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
        return value.startsWith("http://taxon-concept.plazi.org/id/");
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


    /**
     * WARNING! Only use this method for testing purposes.
     *
     * @param url
     */
    public void setTestUrl(String url) {
        if(resourceProvider != null){
            resourceProvider.expireRssFile();
        }
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

    /**
     * only for testing
     * @return
     */
    protected TinkerPopClient queryClient() {
        return queryClient;
    }

}
