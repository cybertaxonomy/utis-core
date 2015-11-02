package org.cybertaxonomy.utis.checklist;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryParser.QueryParser;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.TinkerPopClient;
import org.cybertaxonomy.utis.store.Neo4jStore;
import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Query.Request;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Source;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonBase;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.IdentifierUtils;
import org.cybertaxonomy.utis.utils.Profiler;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.neo4j.graphdb.Relationship;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Vertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.util.FastNoSuchElementException;
import com.tinkerpop.pipes.util.structures.Table;

public class EEA_BDC_Client extends AggregateChecklistClient<TinkerPopClient> implements UpdatableStoreInfo {

    /**
     *
     */
    public static final String ID = "eea_bdc";
    public static final String LABEL = "European Environment Agency (EEA) Biodiversity data centre (BDC)";
    public static final String DOC_URL = "http://semantic.eea.europa.eu/documentation";
    public static final String COPYRIGHT_URL = "http://www.eea.europa.eu/legal/eea-data-policy";

//    private static final String DOWNLOAD_BASE_URL = "http://localhost/download/";
    private static final String DOWNLOAD_BASE_URL = "http://eunis.eea.europa.eu/rdf/";

    private static final String SPECIES_RDF_FILE_URL = DOWNLOAD_BASE_URL + "species.rdf.gz";
    private static final String TAXONOMY_RDF_FILE_URL = DOWNLOAD_BASE_URL + "taxonomy.rdf.gz";
    private static final String LEGALREFS_RDF_FILE_URL = DOWNLOAD_BASE_URL + "legalrefs.rdf.gz";
    private static final String REFERENCES_RDF_FILE_URL = DOWNLOAD_BASE_URL + "references.rdf.gz";

    /**
     * check for updates once a day
     */
    private static final int CHECK_UPDATE_MINUTES = 60 * 24;

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.vernacularNameExact,
            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier);

    public static enum RdfSchema {

        /*
         *     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dwc="http://rs.tdwg.org/dwc/terms/"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns="http://eunis.eea.europa.eu/rdf/species-schema.rdf#"
    xmlns:sioc="http://rdfs.org/sioc/ns#"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:bibo="http://purl.org/ontology/bibo/"
    xmlns:cc="http://creativecommons.org/ns#"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
         */
        EUNIS_SPECIES("es","http://eunis.eea.europa.eu/rdf/species-schema.rdf#"),
        EUNIS_TAXONOMY("et", "http://eunis.eea.europa.eu/rdf/taxonomies-schema.rdf#"),
        DWC("dwc", "http://rs.tdwg.org/dwc/terms/"),
        RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
        SKOS_CORE("scos_core", "http://www.w3.org/2004/02/skos/core#"),
        DC("dc", "http://purl.org/dc/terms/source"),
        DCTERMS("dcterms", "http://purl.org/dc/terms/");

        private String schemaUri;
        private String abbreviation;
        RdfSchema(String abbreviation, String schemaUri) {
            this.abbreviation = abbreviation;
            this.schemaUri = schemaUri;
        }

        public String schemaUri() {

            return schemaUri;
        }

        public String abbreviation() {

            return abbreviation;
        }

        public String property(String name) {
            return schemaUri + name;
        }

    }

    public enum SubCheckListId {

        eunis
        // , natura_2000; // not yet implemented
    }

    public EEA_BDC_Client() {

        super();
    }

    public EEA_BDC_Client(String checklistInfoJson) throws DRFChecklistException {

        super(checklistInfoJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatelessClient() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestUrl() {
        return SPECIES_RDF_FILE_URL;
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
    public String[] updatableResources() {
        return new String[] {SPECIES_RDF_FILE_URL, TAXONOMY_RDF_FILE_URL, LEGALREFS_RDF_FILE_URL, REFERENCES_RDF_FILE_URL};
    }

    @Override
    public void initQueryClient() {

        Neo4jStore neo4jStore = Neo4jStoreManager.provideStoreFor(this);
        queryClient = new TinkerPopClient(neo4jStore);
    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {

        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID, LABEL, DOC_URL, COPYRIGHT_URL, getSearchModes());
        checklistInfo.addSubChecklist(new ServiceProviderInfo(SubCheckListId.eunis.name(), "EUNIS",
                "http://www.eea.europa.eu/themes/biodiversity/eunis/eunis-db#tab-metadata",
                "http://www.eea.europa.eu/legal/copyright", SEARCH_MODES));
        return checklistInfo;
    }


    /**
     * @param queryString
     * @throws DRFChecklistException
     */
    private void addPrexfixes(StringBuilder queryString) throws DRFChecklistException {

        for(RdfSchema schema : RdfSchema.values()) {
            queryString.append(String.format("PREFIX %s: <%s>\n", schema.abbreviation(), schema.schemaUri()));
        }
    }

    /**
     * @param checklistInfo
     * @return
     * @throws DRFChecklistException
     */
    private StringBuilder prepareQueryString() throws DRFChecklistException {

        StringBuilder queryString = new StringBuilder();
        addPrexfixes(queryString);
        return queryString;
    }

    private Taxon createTaxon(Vertex v) {

        Taxon taxon = new Taxon();

        TaxonName taxonName = createTaxonName(v);

        // Taxon
        taxon.setTaxonName(taxonName);
        taxon.setIdentifier(v.getId().toString());
        taxon.setUrl(v.getProperty(GraphSail.VALUE).toString());
        taxon.setAccordingTo(queryClient.relatedVertexValue(v, RdfSchema.DWC, "nameAccordingToID"));
        URI typeUri = queryClient.vertexURI(v, RdfSchema.RDF, "type");
        taxon.setTaxonomicStatus(typeUri.getFragment());

        createSources(v, taxon);

        // classification
        Vertex parentV= null;
        try {
            parentV = queryClient.relatedVertex(v, RdfSchema.EUNIS_SPECIES, "taxonomy");
        } catch (Exception e) {
            logger.error("No taxonomy information for " + v.toString());
        }

        while (parentV != null) {
            logger.debug("parent taxon: " + parentV.toString());
            String level = queryClient.relatedVertexValue(parentV, RdfSchema.EUNIS_TAXONOMY, "level");
            String parentTaxonName = queryClient.relatedVertexValue(parentV, RdfSchema.EUNIS_TAXONOMY, "name");

            if(level != null) {
                HigherClassificationElement hce = new HigherClassificationElement();
                hce.setRank(level);
                hce.setScientificName(parentTaxonName);
                hce.setTaxonID(parentV.getProperty(GraphSail.VALUE).toString());
                taxon.getHigherClassification().add(hce );
            }
            Vertex lastParentV = parentV;
            parentV = queryClient.relatedVertex(parentV, RdfSchema.EUNIS_TAXONOMY, "parent");
            if(lastParentV.equals(parentV)) {
                // avoid endless looping when data is not correct
                break;
            }
        }

        return taxon;
    }

    /**
     * @param model
     * @param taxonR
     * @param taxonBase
     */
    private void createSources(Vertex v, TaxonBase taxonBase) {

        // Sources are source references, re there others like data bases?

        GremlinPipeline<Graph, Vertex> taxonPipe = new GremlinPipeline<Graph, Vertex>(v);

        try {
            List<Vertex> titleVs = taxonPipe
                    .outE(RdfSchema.EUNIS_SPECIES.property("hasLegalReference")).inV()
                    .outE(RdfSchema.DCTERMS.property("source")).inV().dedup()
                    .outE(RdfSchema.DCTERMS.property("title")).inV()
                    .toList();
            for(Vertex tv : titleVs) {
                Source source = new Source();
                logger.debug(tv.toString());
                source.setName(tv.getProperty(GraphSail.VALUE).toString());
                taxonBase.getSources().add(source);
            }
        } catch (FastNoSuchElementException e) {
            logger.debug("No sources found");
        }
    }

    /**
     * @param taxonR
     * @return
     */
    private TaxonName createTaxonName(Vertex v) {

        TaxonName taxonName = new TaxonName();
        // TaxonName
        taxonName.setScientificName(queryClient.relatedVertexValue(v, RdfSchema.RDFS, "label"));
        taxonName.setCanonicalName(queryClient.relatedVertexValue(v, RdfSchema.EUNIS_SPECIES, "binomialName"));
        taxonName.setRank(queryClient.relatedVertexValue(v, RdfSchema.EUNIS_SPECIES, "taxonomicRank"));
        return taxonName;
    }


    private void createSynonyms(Vertex taxonV, Response tnrResponse) {


        GremlinPipeline<Graph, Vertex> taxonPipe = new GremlinPipeline<Graph, Vertex>(taxonV);

        try {
            List<Vertex> synonymVs = taxonPipe
                    .inE(RdfSchema.EUNIS_SPECIES.property("eunisPrimaryName")).outV().dedup()
                    .toList();
            for(Vertex synonymV : synonymVs) {
                // http://www.w3.org/1999/02/22-rdf-syntax-ns#type is used inconsistently, accepted taxa can have type SpeciesSynonym
                // and are their own synonym in this case !
                // using http://eunis.eea.europa.eu/rdf/species-schema.rdf#taxonomicRank is the recommended way to detect synonyms and to avoid
                // adding the accepted taxon as its own synonym
                String taxonomicRank = queryClient.relatedVertexValue(synonymV, RdfSchema.EUNIS_SPECIES, "taxonomicRank");


                if (taxonomicRank != null && taxonomicRank.equals("Synonym")) {

                    Synonym synonym = new Synonym();

                    TaxonName taxonName = createTaxonName(synonymV);
                    synonym.setTaxonomicStatus(taxonomicRank);
                    synonym.setUrl(synonymV.getProperty(GraphSail.VALUE).toString());
                    synonym.setTaxonName(taxonName);
                    synonym.setAccordingTo(queryClient.relatedVertexValue(synonymV, RdfSchema.DWC, "nameAccordingToID"));

                    createSources(synonymV, synonym);

                    tnrResponse.getSynonym().add(synonym);
                }
            }
        } catch (FastNoSuchElementException e) {
            logger.debug("No sources found");
        }

    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            // FIXME query specific subchecklist

            // selecting one request as representative, only
            // the search mode and addSynonmy flag are important
            // for the further usage of the request object
            Query query = singleQueryFrom(tnrMsg);

            String queryString = query.getRequest().getQueryString();
            logger.debug("original queryString: "+ queryString);
            queryString = QueryParser.escape(queryString);
            queryString = queryString.replace(" ", "\\ ");
            if(query.getRequest().getSearchMode().equals(SearchMode.scientificNameLike.name())) {
                queryString += "*";
            }
            logger.debug("prepared queryString: "+ queryString);

            GremlinPipeline<Graph, Vertex> pipe = null;

//            Profiler profiler = Profiler.newCpuProfiler(false);

            logger.debug("Neo4jINDEX");

            ArrayList<Vertex> hitVs = queryClient.vertexIndexQuery("value:" + queryString);
            pipe = new GremlinPipeline<Graph, Vertex>(hitVs);

            List<Vertex> vertices = new ArrayList<Vertex>();
            pipe.in(RdfSchema.EUNIS_SPECIES.property("binomialName"),
                    RdfSchema.DWC.property("subgenus"), // EUNIS has no subgenera but this is added for future compatibility
                    RdfSchema.DWC.property("genus")
                    // no taxa for higher ranks in EUNIS
                    ).fill(vertices);

            updateQueriesWithResponse(vertices, null, null, checklistInfo, query);
//            profiler.end(System.err);
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // delegate to resolveScientificNamesExact,
        resolveScientificNamesExact(tnrMsg);

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        List<Query> queryList = tnrMsg.getQuery();

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            // FIXME query specific subchecklist

            // selecting one request as representative, only
            // the search mode and addSynonmy flag are important
            // for the further usage of the request object
            Query query = singleQueryFrom(tnrMsg);

            String queryString = query.getRequest().getQueryString();
            logger.debug("original queryString: "+ queryString);
            queryString = QueryParser.escape(queryString);
            queryString = queryString.replace(" ", "\\ ");
            if(query.getRequest().getSearchMode().equals(SearchMode.vernacularNameLike.name())) {
                queryString = "*" + queryString + "*";
            }

            logger.debug("prepared queryString: "+ queryString);

            GremlinPipeline<Graph, Vertex> pipe = null;

            Profiler profiler = Profiler.newCpuProfiler(false);

            // by using the Neo4j index directly it is possible to
            // take full advantage of the underlying Lucene search engine
            ArrayList<Vertex> hitVs = queryClient.vertexIndexQuery("value:" + queryString);

//            List<String> matchingNames = new ArrayList<String>(hitVs.size());
//            for(Vertex v : hitVs) {
//                String matchValue = v.getProperty(GraphSail.VALUE).toString();
//                matchingNames.add(matchValue);
//                logger.debug("matchingName  " + matchValue);
//            }

            List<Vertex> vertices = new ArrayList<Vertex>();
            pipe = new GremlinPipeline<Graph, Vertex>(hitVs);
            Table table = new Table();
            pipe.as("match").in(RdfSchema.DWC.property("vernacularName")).as("taxon").table(table).iterate();

            updateQueriesWithResponse(
                    table.getColumn("taxon"), table.getColumn("match"),
                    NameType.VERNACULAR_NAME, checklistInfo, query);
            profiler.end(System.err);
        }
    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        resolveVernacularNamesExact(tnrMsg);
    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            // FIXME query specific subchecklist
            Query query = singleQueryFrom(tnrMsg);
            String queryString = query.getRequest().getQueryString();

            // by using the Neo4j index directly it is possible to
            // take full advantage of the underlying Lucene search engine
            queryString = QueryParser.escape(queryString);
            ArrayList<Vertex> hitVs = queryClient.vertexIndexQuery("value:" + queryString);
            if(hitVs.size() > 0) {
                Response response = tnrResponseFromResource(hitVs.get(0), query.getRequest(), null, null, checklistInfo);
                query.getResponse().add(response);
            } else if(hitVs.size() > 1) {
                throw new DRFChecklistException("More than one node with the id '" + queryString + "' found");
            }
        }
    }

    private void updateQueriesWithResponse(List<Vertex> taxonNodes, List<Vertex> matchNodes, NameType matchType, ServiceProviderInfo ci, Query query){

        if (taxonNodes == null) {
            return;
        }

        logger.debug("matching taxon nodes:");
        int i = -1;
        for (Vertex v : taxonNodes) {
            i++;
            logger.debug("  " + v.toString());
            if(logger.isTraceEnabled()) {
                logger.trace("updateQueriesWithResponse() : printing propertyKeys to System.err");
                printPropertyKeys(v, System.err);
            }
            if(v.getProperty("kind").equals("url")) {
                logger.error("vertex of type 'url' expected, but was " + v.getProperty("type").equals("url"));
                continue;
            }
            Vertex matchNode = null;
            if(matchNodes != null) {
                matchNode = matchNodes.get(i);
            }
            Response tnrResponse = tnrResponseFromResource(v, query.getRequest(), matchNode, matchType, ci);
            if(tnrResponse != null) {
                query.getResponse().add(tnrResponse);
            }
        }
    }

    /**
     * @param model
     * @param taxonR
     * @param request
     * @param matchType
     * @param matchNode
     * @param ci
     * @return
     */
    private Response tnrResponseFromResource(Vertex taxonV, Request request, Vertex matchNode, NameType matchType, ServiceProviderInfo ci) {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

        GremlinPipeline<Graph, Vertex> pipe = new GremlinPipeline<Graph, Vertex>(taxonV);

        String validName = queryClient.relatedVertexValue(taxonV, RdfSchema.EUNIS_SPECIES, "validName");

        boolean isAccepted = validName != null && validName.equals("true");

        logger.debug("processing " + (isAccepted ? "accepted taxon" : "synonym or other")  + " " + taxonV.getId());

        //
        if(matchNode != null) {
            String matchingName = matchNode.getProperty(GraphSail.VALUE).toString();
            tnrResponse.setMatchingNameString(matchingName);
            tnrResponse.setMatchingNameType(matchType);
        }

        // case when accepted name
        if(isAccepted) {
            Taxon taxon = createTaxon(taxonV);
            tnrResponse.setTaxon(taxon);
            if(matchNode == null) {
                tnrResponse.setMatchingNameType(NameType.TAXON);
                String matchingName = taxon.getTaxonName().getCanonicalName();
                tnrResponse.setMatchingNameString(matchingName);
            }

        }
        else {
            // case when synonym
            Vertex synonymV = taxonV;
            taxonV = null;
            try {
                taxonV = queryClient.relatedVertex(synonymV, RdfSchema.EUNIS_SPECIES, "eunisPrimaryName");
            } catch(Exception e) {
                logger.error("No accepted taxon found for " + synonymV.toString() + " (" + synonymV.getProperty(GraphSail.VALUE) + ")");
            }

            if(taxonV != null) {
                Taxon taxon = createTaxon(taxonV);
                tnrResponse.setTaxon(taxon);
            } else {
            }
            if(matchNode == null) {
                tnrResponse.setMatchingNameType(NameType.SYNONYM);
                String matchingName = queryClient.relatedVertexValue(synonymV, RdfSchema.EUNIS_SPECIES, "binomialName");
                tnrResponse.setMatchingNameString(matchingName);
            }
        }

        if(request.isAddSynonymy()) {
            createSynonyms(taxonV, tnrResponse);
        }

        logger.debug("processing " + (isAccepted ? "accepted taxon" : "synonym or other")  + " " + taxonV.getId() + " DONE");
        return tnrResponse;
    }

    /**
     * @param vertex
     */
    private void printEdges(Neo4j2Vertex vertex) {
        Iterable<Relationship> rels = vertex.getRawVertex().getRelationships();
        Iterator<Relationship> iterator = rels.iterator();
        if(iterator.hasNext()) {
            Relationship rel = iterator.next();
            System.err.println(rel.toString() + ": " + rel.getStartNode().toString() + "-[" +  rel.getType() + "]-" + rel.getEndNode().toString());
        }
    }

    private void printPropertyKeys(Vertex v, PrintStream ps) {
        StringBuilder out = new StringBuilder();
        out.append(v.toString());
        for(String key : v.getPropertyKeys()) {
            out.append(key).append(": ").append(v.getProperty(key)).append(" ");
        }
        ps.println(out.toString());
    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES;
    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        return IdentifierUtils.checkURI(value);
    }


}
