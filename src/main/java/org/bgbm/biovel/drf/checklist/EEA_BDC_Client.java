package org.bgbm.biovel.drf.checklist;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.bgbm.biovel.drf.client.ServiceProviderInfo;
import org.bgbm.biovel.drf.query.IQueryClient;
import org.bgbm.biovel.drf.query.SparqlClient;
import org.bgbm.biovel.drf.query.TinkerPopClient;
import org.bgbm.biovel.drf.store.Neo4jStore;
import org.bgbm.biovel.drf.store.Store;
import org.bgbm.biovel.drf.store.TDBStore;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Query.Request;
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonBase;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.IdentifierUtils;
import org.bgbm.biovel.drf.utils.Profiler;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.neo4j.graphdb.Relationship;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.FastNoSuchElementException;

public class EEA_BDC_Client extends AggregateChecklistClient<TinkerPopClient> {

    /**
     *
     */
    public static final String ID = "eea_bdc";
    public static final String LABEL = "European Environment Agency (EEA) Biodiversity data centre (BDC)";
    public static final String DOC_URL = "http://semantic.eea.europa.eu/documentation";
    public static final String COPYRIGHT_URL = "http://www.eea.europa.eu/legal/eea-data-policy";

    private static final String SPARQL_ENDPOINT_URL = "http://semantic.eea.europa.eu/sparql";
    private static final boolean USE_REMOTE_SERVICE = false;

    private static final String SPECIES_RDF_FILE_URL = "http://localhost/download/species.rdf.gz"; // http://eunis.eea.europa.eu/rdf/species.rdf.gz
    private static final String LEGALREFS_RDF_FILE_URL = "http://localhost/download/legalrefs.rdf.gz"; // http://eunis.eea.europa.eu/rdf/legalrefs.rdf.gz
    private static final String REFERENCES_RDF_FILE_URL = "http://localhost/download/references.rdf.gz"; // http://eunis.eea.europa.eu/rdf/references.rdf.gz
    private static final boolean REFRESH_TDB = false;

    private static final Class<? extends IQueryClient> clientClass = TinkerPopClient.class;

    private static final int MAX_PAGING_LIMIT = 50;

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

        public String propertyURI(String name) {
            return schemaUri + name;
        }

    }

    public enum SubCheckListId {

        eunis, natura_2000;
    }

    private enum RankLevel{

        Kingdom, Phylum, Clazz, Order, Family, Genus;
    }

    public EEA_BDC_Client() {

        super();
    }

    public EEA_BDC_Client(String checklistInfoJson) throws DRFChecklistException {

        super(checklistInfoJson);
    }

    @Override
    public void initQueryClient() {

        if(SparqlClient.class.isAssignableFrom(clientClass)) {
            if(USE_REMOTE_SERVICE) {
                // use SPARQL end point
                //FIXME queryClient = new SparqlClient(SPARQL_ENDPOINT_URL);
            } else {
                TDBStore tripleStore;
                try {
                    tripleStore = new TDBStore();
                } catch (Exception e1) {
                    throw new RuntimeException("Creation of TripleStore failed",  e1);
                }
                if(REFRESH_TDB) {
                    updateStore(tripleStore);
                }
              //FIXME queryClient = new SparqlClient(tripleStore);

            }
        } else if(TinkerPopClient.class.isAssignableFrom(clientClass)) {
            if(USE_REMOTE_SERVICE) {
                throw new RuntimeException("USE_REMOTE_SERVICE not suported by QueryClient class "+ clientClass);
            } else {
                Neo4jStore neo4jStore;
                try {
                    neo4jStore = new Neo4jStore();
                } catch (Exception e1) {
                    throw new RuntimeException("Creation of Neo4jStore failed",  e1);
                }
                if(REFRESH_TDB) {
                    updateStore(neo4jStore);
                }
                queryClient = new TinkerPopClient(neo4jStore);

            }

        } else {
            throw new RuntimeException("Unsuported QueryClient class "+ clientClass);
        }
    }

    /**
     * @param neo4jStore
     */
    private void updateStore(Store neo4jStore) {
        try {
            neo4jStore.loadIntoStore(
                    //SPECIES_RDF_FILE_URL,
                    LEGALREFS_RDF_FILE_URL,
                    REFERENCES_RDF_FILE_URL
                    );
        } catch (Exception e) {
            throw new RuntimeException("Loading "
                    + SPECIES_RDF_FILE_URL + ", "
                    + LEGALREFS_RDF_FILE_URL + ", "
                    + REFERENCES_RDF_FILE_URL +
                    " into Neo4jStore failed",  e);
        }
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
        taxon.setAccordingTo(queryClient.relatedVertexValue(v, RdfSchema.DWC, "nameAccordingToID"));
        URI typeUri = queryClient.relatedVertexURI(v, RdfSchema.RDF, "type");
        taxon.setTaxonomicStatus(typeUri.getFragment());

        createSources(v, taxon);

        /*

        // classification
        Classification c = null;
        Resource parentR = queryClient.objectAsResource(taxonR, RdfSchema.EUNIS_SPECIES, "taxonomy");
        while (parentR != null) {

            String level = queryClient.objectAsString(parentR, RdfSchema.EUNIS_TAXONOMY, "level");
            String parentTaxonName = queryClient.objectAsString(parentR, RdfSchema.EUNIS_TAXONOMY, "name");

            RankLevel rankLevel = null;
            try {
                rankLevel = RankLevel.valueOf(level);
            } catch (Exception e) {
                // IGNORE
            }
            if(rankLevel != null) {
                if(c == null) {
                 c = new Classification();
                }
                switch(rankLevel) {
                case Clazz:
                    c.setClazz(parentTaxonName);
                    break;
                case Family:
                    c.setFamily(parentTaxonName);
                    break;
                case Genus:
                    c.setGenus(parentTaxonName);
                    break;
                case Kingdom:
                    c.setKingdom(parentTaxonName);
                    break;
                case Order:
                    c.setOrder(parentTaxonName);
                    break;
                case Phylum:
                    c.setPhylum(parentTaxonName);
                    break;
                default:
                    break;
                }
            }
            Resource lastParentR = parentR;
            parentR = queryClient.objectAsResource(parentR, RdfSchema.EUNIS_TAXONOMY, "parent");
            if(lastParentR.equals(parentR)) {
                // avoid endless looping when data is not correct
                break;
            }
        }
        if(c != null) {
            taxon.setClassification(c);
        }
        */
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
                    .outE(RdfSchema.EUNIS_SPECIES.propertyURI("hasLegalReference")).inV()
                    .outE(RdfSchema.DCTERMS.propertyURI("source")).inV().dedup()
                    .outE(RdfSchema.DCTERMS.propertyURI("title")).inV()
                    .toList();
            for(Vertex tv : titleVs) {
                Source source = new Source();
                logger.error(tv.toString());
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
        taxonName.setFullName(queryClient.relatedVertexValue(v, RdfSchema.RDFS, "label"));
        // TODO rename CanonicalName to scientificName? compare with dwc:scientificName
        taxonName.setCanonicalName(queryClient.relatedVertexValue(v, RdfSchema.EUNIS_SPECIES, "binomialName"));
        taxonName.setRank(queryClient.relatedVertexValue(v, RdfSchema.EUNIS_SPECIES, "taxonomicRank"));
        return taxonName;
    }


    private void createSynonyms(Vertex taxonV, Response tnrResponse) {


        GremlinPipeline<Graph, Vertex> taxonPipe = new GremlinPipeline<Graph, Vertex>(taxonV);

        try {
            List<Vertex> synonymVs = taxonPipe
                    .inE(RdfSchema.EUNIS_SPECIES.propertyURI("eunisPrimaryName")).outV().dedup()
                    .toList();
            for(Vertex synonymV : synonymVs) {
                String typeUri = queryClient.relatedVertexValue(synonymV, RdfSchema.RDF, "type");
                String status = null;
                try {
                    status = URI.create(typeUri).getFragment();
                } catch (Exception e) {

                }

                if (status != null && status.equals("SpeciesSynonym")) {

                    Synonym synonym = new Synonym();

                    TaxonName taxonName = createTaxonName(synonymV);
                    synonym.setTaxonomicStatus(status);
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

    /**
     * Returns all subjects that are related to the taxonR
     * via the es:eunisPrimaryName property.
     *
     * @param taxonR
     * @return
     */
    private List<Resource> queryForSynonyms(Resource taxonR) {
 /* FIXME
        List<Resource> synonymRList = null;

        try {
            StringBuilder queryString = prepareQueryString();

            queryString.append("DESCRIBE ?synonym es:eunisPrimaryName <" + taxonR.getURI() + ">");
            logger.debug("\n" + queryString.toString());

            Model model = queryClient.describe(queryString.toString());
            synonymRList = listSynonymResources(model, taxonR);

        } catch (DRFChecklistException e) {
            logger.error("SPARQL query error in queryForSynonyms()", e);
        } finally {
            if(synonymRList == null) {
                synonymRList = new ArrayList<Resource>(0);
            }
        }

        return synonymRList;
*/ return null;
    }

    /**
     * @param model
     * @return
     */
    private List<Resource> listSynonymResources(Model model, Resource taxonR) {
        List<Resource> synonymRList;
        /*
        Property filterProperty = model.createProperty(RdfSchema.EUNIS_SPECIES.schemaUri, "eunisPrimaryName");
        synonymRList = queryClient.listResources(model, filterProperty, null, taxonR);
        return synonymRList;
         */
        return null;
    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        List<Query> queryList = tnrMsg.getQuery();

        // selecting one request as representative, only
        // the search mode and addSynonmy flag are important
        // for the further usage of the request object

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            Query query = singleQueryFrom(tnrMsg);

            boolean  TUPLEQUERY = false;
            boolean Neo4jINDEX = true;

            String filter;
            String queryString = query.getRequest().getQueryString();
            logger.debug("original queryString: "+ queryString);
            PipeFunction<Vertex, Boolean> matchFilter;
            if(query.getRequest().getSearchMode().equals(SearchMode.scientificNameLike.name())) {
                filter = "(regex(?name, \"^" + queryString + "\"))";
                matchFilter = queryClient.createStarttWithFilter(queryString);
                queryString = "\"" + queryString + "*\"";
            } else {
                filter = "(?name = \"" + queryString + "\")";
                matchFilter = queryClient.createEqualsFilter(queryString);
            }

            logger.debug("prepared queryString: "+ queryString);

            if(TUPLEQUERY) {
                StringBuilder sparql = prepareQueryString();
                sparql.append(
                        "SELECT ?eunisurl \n"
                        + "WHERE {\n"
                        + "     ?eunisurl es:binomialName ?name . \n"
                        + "     FILTER " + filter  + " \n"
                        + "}"
                        );

                Neo4jGraph neo4jGraph = (Neo4jGraph)queryClient.graph();
                Vertex v = neo4jGraph.getVertex(2);

                SailRepositoryConnection connection = null;
                try {

                    Profiler profiler = Profiler.newCpuProfiler(true);

                    connection = queryClient.connection();
                    TupleQuery tquery = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql.toString());
                    TupleQueryResult tqresult = tquery.evaluate();
                    queryClient.showResults(tqresult);

                    profiler.end(System.err);

                } catch (MalformedQueryException | RepositoryException | QueryEvaluationException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (Exception e1) {
                    // yourkit
                    e1.printStackTrace();
                } finally {
                    try {
                        connection.close();
                    } catch (RepositoryException e1) {
                        // IGNORE //
                    }
                    connection = null;
                }

            }
            GremlinPipeline<Graph, Vertex> pipe = null;

            if(Neo4jINDEX) {

                Profiler profiler = Profiler.newCpuProfiler(false);

                logger.debug("Neo4jINDEX");

                Graph graph = queryClient.graph();
                pipe = new GremlinPipeline<Graph, Vertex>(
                        graph.getVertices("value", queryString)
                );


                // using the Neo4j index directly
//                Neo4jGraph graph = (Neo4jGraph)queryClient.graph();
//                Index<Neo4jVertex> vertexAutoIndex = graph.getIndex("node_auto_index", Neo4jVertex.class);
//                CloseableIterable<Neo4jVertex> nodes = vertexAutoIndex.query("value", "\"" + queryString + "\"");
//                pipe = new GremlinPipeline<Graph, Vertex>(nodes);

                List<Vertex> vertices = new ArrayList<Vertex>();
                pipe.in("http://eunis.eea.europa.eu/rdf/species-schema.rdf#binomialName").fill(vertices);

                for(Vertex v : vertices) {
                    logger.debug("  " + v.toString());
                }

                profiler.end(System.err);
                updateQueriesWithResponse(vertices, checklistInfo, query);
            }
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // delegate to resolveScientificNamesExact,
        resolveScientificNamesExact(tnrMsg);

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        /*
        List<Query> queryList = tnrMsg.getQuery();

        // selecting one request as representative, only
        // the search mode and addSynonmy flag are important
        // for the further usage of the request object

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            Query query = singleQueryFrom(tnrMsg);
            StringBuilder queryString = prepareQueryString();

            String filter;
            if(query.getRequest().getSearchMode().equals(SearchMode.vernacularNameLike.name())) {
                filter = "(regex(?name, \"" + query.getRequest().getQueryString() + "\"))";
            } else {
                // STR returns the lexical form of a literal so this matches literals in any language
                filter = "(STR(?name)='" + query.getRequest().getQueryString() + "')";
            }

            queryString.append(
                    "DESCRIBE ?eunisurl \n"
                    + "WHERE {\n"
                    + "     ?eunisurl dwc:vernacularName ?name . \n"
                    + "     FILTER " + filter  + " \n"
                    + "} \n"
                    + "LIMIT " + MAX_PAGING_LIMIT + " OFFSET 0"
                    );

            logger.debug("\n" + queryString.toString());
         */
 /* FIXME
            Model model = queryClient.describe(queryString.toString());
            updateQueriesWithResponse(model, checklistInfo, query); */
//        }
    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        resolveVernacularNamesExact(tnrMsg);
    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        /*for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            Query query = singleQueryFrom(tnrMsg);
            Resource taxonR = queryClient.getFromUri(query.getRequest().getQueryString());

            Response response = tnrResponseFromResource(taxonR.getModel(), taxonR, query.getRequest());
            query.getResponse().add(response);
        }
        */
    }

    private void updateQueriesWithResponse(List<Vertex> nodes, ServiceProviderInfo ci, Query query){

        if (nodes == null) {
            return;
        }

        logger.debug("matching nodes:");
        for (Vertex v : nodes) {
            logger.debug("  " + v.toString());
            printPropertyKeys(v, System.err);
            if(v.getProperty("kind").equals("url")) {
                logger.error("vertex of type 'url' expected, but was " + v.getProperty("type").equals("url"));
                continue;
            }
            Response tnrResponse = tnrResponseFromResource(v, query.getRequest());
            if(tnrResponse != null) {
                query.getResponse().add(tnrResponse);
            }
        }
    }

    /**
     * @param model
     * @param taxonR
     * @param request
     * @return
     */
    @SuppressWarnings("unused")
    private Response tnrResponseFromResource(Vertex taxonV, Request request) {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        SearchMode searchMode = SearchMode.valueOf(request.getSearchMode());

        GremlinPipeline<Graph, Vertex> pipe = new GremlinPipeline<Graph, Vertex>(taxonV);

        String validName = queryClient.relatedVertexValue(taxonV, RdfSchema.EUNIS_SPECIES, "validName");

        boolean isAccepted = validName != null && validName.equals("true");
        boolean skipThis = false;

        logger.debug("processing " + (isAccepted ? "accepted taxon" : "synonym or other")  + " " + taxonV.getId());

        // case when accepted name
        if(isAccepted) {
            Taxon taxon = createTaxon(taxonV);
            tnrResponse.setTaxon(taxon);
            tnrResponse.setMatchingNameType(NameType.TAXON);
            String matchingName = taxon.getTaxonName().getCanonicalName();
            tnrResponse.setMatchingNameString(matchingName);

        }
        else {
            // case when synonym
            Vertex synonymV = taxonV;
            taxonV = null;
            try {
                taxonV = synonymV.getEdges(Direction.OUT, RdfSchema.EUNIS_SPECIES.propertyURI("eunisPrimaryName")).iterator().next().getVertex(Direction.IN);
            } catch(Exception e) {
                logger.error("No accepted taxon found for " + synonymV.toString() + " (" + synonymV.getProperty(GraphSail.VALUE) + ")");
            }

            if(taxonV != null) {
                Taxon taxon = createTaxon(taxonV);
                tnrResponse.setTaxon(taxon);
            } else {
            }
            tnrResponse.setMatchingNameType(NameType.SYNONYM);
            String matchingName = queryClient.relatedVertexValue(synonymV, RdfSchema.EUNIS_SPECIES, "binomialName");
            tnrResponse.setMatchingNameString(matchingName);
        }

        if(!skipThis && request.isAddSynonymy()) {
            createSynonyms(taxonV, tnrResponse);
        }

        logger.debug("processing " + (isAccepted ? "accepted taxon" : "synonym or other")  + " " + taxonV.getId() + " DONE");
        return tnrResponse;
    }

    /**
     * @param vertex
     */
    private void printEdges(Neo4jVertex vertex) {
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
