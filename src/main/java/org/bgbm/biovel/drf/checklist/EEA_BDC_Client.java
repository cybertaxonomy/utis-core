package org.bgbm.biovel.drf.checklist;

import java.net.URI;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.bgbm.biovel.drf.client.ServiceProviderInfo;
import org.bgbm.biovel.drf.query.SparqlClient;
import org.bgbm.biovel.drf.tnr.msg.Classification;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Query.Request;
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.gbif.nameparser.NameParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class EEA_BDC_Client extends AggregateChecklistClient<SparqlClient> {

    /**
     *
     */
    public static final String ID = "eea_bdc";
    public static final String LABEL = "European Environment Agency (EEA) Biodiversity data centre (BDC)";
    public static final String DOC_URL = "http://semantic.eea.europa.eu/documentation";
    public static final String COPYRIGHT_URL = "http://www.eea.europa.eu/legal/eea-data-policy";
    private static final String SPARQL_ENDPOINT_URL = "http://semantic.eea.europa.eu/sparql";
    private static final String RDF_FILE_URL = "http://localhost/download/species.rdf.gz"; // http://eunis.eea.europa.eu/rdf/species.rdf.gz
    private static final boolean USE_REMOTE_SERVICE = false;

    private static final int MAX_PAGING_LIMIT = 50;

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
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
        RDFS("rdf", "http://www.w3.org/2000/01/rdf-schema#"),
        SKOS_CORE("scos_core", "http://www.w3.org/2004/02/skos/core#");

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

    }

    public enum subCheckListIds {

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

        if(USE_REMOTE_SERVICE) {
            // use SPARQL end point
            queryClient = new SparqlClient(SPARQL_ENDPOINT_URL, SparqlClient.Opmode.SPARCLE_ENDPOINT);
        } else {
            // use downloadable rdf
            queryClient = new SparqlClient(RDF_FILE_URL, SparqlClient.Opmode.RDF_ARCHIVE);
            // reuse existing TDB_STORE
            // queryClient = new SparqlClient(null, SparqlClient.Opmode.RDF_ARCHIVE);
        }
    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {

        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID, LABEL, DOC_URL, COPYRIGHT_URL, getSearchModes());
        checklistInfo.addSubChecklist(new ServiceProviderInfo(subCheckListIds.eunis.name(), "EUNIS",
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

    private Taxon generateTaxon(Model model, Resource taxonR) {

        Taxon taxon = new Taxon();
        TaxonName taxonName = new TaxonName();

        // TaxonName
        taxonName.setFullName(queryClient.objectAsString(taxonR, RdfSchema.RDFS, "label"));
        NameParser ecatParser = new NameParser();
        String nameCanonical = ecatParser.parseToCanonical(taxonName.getFullName());
        taxonName.setCanonicalName(nameCanonical);
        taxonName.setRank(queryClient.objectAsString(taxonR, RdfSchema.EUNIS_SPECIES, "taxonomicRank"));

        // Taxon
        taxon.setTaxonName(taxonName);
        taxon.setIdentifier(taxonR.getURI());
        taxon.setAccordingTo(queryClient.objectAsString(taxonR, RdfSchema.DWC, "nameAccordingToID"));
        taxon.setTaxonomicStatus(queryClient.objectAsURI(taxonR, RdfSchema.RDF, "type").getFragment());

        // Sources are source references, re there others like data bases?
        for ( StmtIterator refIt = taxonR.listProperties(model.getProperty("rdf", "hasLegalReference")); refIt.hasNext();) {
            try {
            Source source = new Source();
            Resource sourceR = refIt.next().getObject().asResource();
            String sourceName = queryClient.objectAsString(sourceR, RdfSchema.RDFS, "source");
            source.setName(sourceName);
            taxon.getSources().add(source);
            } catch (NoSuchElementException e) {
                logger.debug("No statements for rdf:hasLegalReference" , e);
            }
        }

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
        return taxon;
    }




    private void generateSynonyms(JSONArray relatedTaxa, Response tnrResponse) {

        Iterator<JSONObject> itrSynonyms = relatedTaxa.iterator();
        while (itrSynonyms.hasNext()) {

            JSONObject synonymjs = itrSynonyms.next();
            String status = (String) synonymjs.get("taxonStatus");
            if (status != null && status.equals("synonym")) {
                Synonym synonym = new Synonym();
                TaxonName taxonName = new TaxonName();

                String resName = (String) synonymjs.get("name");
                taxonName.setFullName(resName);
                NameParser ecatParser = new NameParser();
                String nameCanonical = ecatParser.parseToCanonical(resName);
                taxonName.setCanonicalName(nameCanonical);
                synonym.setTaxonomicStatus((String) synonymjs.get("taxonStatus"));

                taxonName.setRank((String) synonymjs.get("rank"));

                synonym.setTaxonName(taxonName);

                JSONObject scrutinyjs = (JSONObject) synonymjs.get("taxonomicScrutiny");
                synonym.setAccordingTo((String) scrutinyjs.get("accordingTo"));

                JSONObject sourcejs = (JSONObject) synonymjs.get("source");
                String sourceUrl = (String) sourcejs.get("url");
                String sourceDatasetID = (String) sourcejs.get("datasetID");
                String sourceDatasetName = (String) sourcejs.get("datasetName");
                String sourceName = "";

                Source source = new Source();
                source.setIdentifier(sourceDatasetID);
                source.setDatasetName(sourceDatasetName);
                source.setName(sourceName);
                source.setUrl(sourceUrl);
                synonym.getSources().add(source);

                tnrResponse.getSynonym().add(synonym);
            }
        }
    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        List<Query> queryList = tnrMsg.getQuery();

        // selecting one request as representative, only
        // the search mode and addSynonmy flag are important
        // for the further usage of the request object

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            Query query = singleQueryFrom(tnrMsg);
            StringBuilder queryString = prepareQueryString();

            String filter = "";
            if(query.getRequest().getSearchMode().equals(SearchMode.scientificNameLike.name())) {
                filter = "regex(?name, \"" + query.getRequest().getQueryString() + "\")";
            } else {
                filter = "(?name = \"" + query.getRequest().getQueryString() + "\")";
            }

            queryString.append(
                    "DESCRIBE ?eunisurl \n"
                    + "WHERE {\n"
                    + "     ?eunisurl es:validName true .  \n"
                    + "     ?eunisurl es:binomialName ?name . \n"
                    + "     ?eunisurl rdf:label ?fullName . \n"
                    + "     ?eunisurl dwc:scientificNameAuthorship ?author . \n"
                    + "     OPTIONAL {  \n"
                    + "       ?eunisurl es:sameSynonymCoL ?sameSpecies . \n"
                    + "     } \n"
                    + "     OPTIONAL {  \n"
                    + "       ?eunisurl dwc:vernacularName ?vernacularName . \n"
                    + "     } \n"

                    + "     OPTIONAL {  \n"
                    + "       ?eunisurl es:eunisPrimaryName ?eunisPrimaryName . \n" // accepted taxon
                    + "     } \n"
                    + "     OPTIONAL {  \n"
                    + "       ?eunisurl rdf:hasLegalReference ?sourceReference . \n"
                    + "     } \n"
                    + "     OPTIONAL {  \n"
                    + "       ?eunisurl rdf:taxonomy ?rank . \n"
                    + "     } \n"
                    + "     FILTER " + filter  + " \n"
                    + "} \n"
                    + "LIMIT " + MAX_PAGING_LIMIT + " OFFSET 0"
                    );

            logger.debug("\n" + queryString.toString());

            Model model = queryClient.describe(queryString.toString());
            updateQueriesWithResponse(model, checklistInfo, query);
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // delegate to resolveScientificNamesExact, since the like search mode
        // is handled in buildUriFromQueryList
        resolveScientificNamesExact(tnrMsg);

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub
    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub
    }

    private void updateQueriesWithResponse(Model model, ServiceProviderInfo ci, Query query)
            throws DRFChecklistException {

        if (model == null) {
            return;
        }

        ResIterator subjectIt = model.listSubjects();

        int i = -1;
        while (subjectIt.hasNext()) {
            i++;
            Resource subject = subjectIt.next();
            Resource taxonR;
            URI matchedResourceURI = queryClient.objectAsURI(subject, RdfSchema.SKOS_CORE, "exactMatch");
            if(matchedResourceURI != null) {
                // need to follow the exactMatch uri in this case
                taxonR = queryClient.getFromUri(matchedResourceURI);
            } else {
                // the subject is already a species
                taxonR = subject;
            }
            Response tnrResponse = tnrResponseFromResource(model, taxonR, query.getRequest());
            query.getResponse().add(tnrResponse);
        }
    }

    /**
     * @param model
     * @param taxonR
     * @param request
     * @return
     */
    private Response tnrResponseFromResource(Model model, Resource taxonR, Request request) {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        SearchMode searchMode = SearchMode.valueOf(request.getSearchMode());

        // A synonym has always taxonomicRank = "Synonym", validName usually is false but in two cases it is true
        boolean isAccepted = taxonR.hasLiteral(model.getProperty(RdfSchema.EUNIS_SPECIES.schemaUri(), "taxonomicRank"), "Synonym");

        // TODO: is this possible with this service?
        //    tnrResponse.setMatchingNameString(record.getScientificname());

            // case when accepted name
            if(isAccepted) {
                Taxon taxon = generateTaxon(model, taxonR);
                tnrResponse.setTaxon(taxon);
                tnrResponse.setMatchingNameType(NameType.TAXON);

            } else {
                // case when synonym
                Resource synonymR = taxonR;
                taxonR = queryClient.objectAsResource(taxonR, RdfSchema.EUNIS_SPECIES, "eunisPrimaryName");
                if(taxonR != null) {
                    Taxon taxon = generateTaxon(model, taxonR);
                    tnrResponse.setTaxon(taxon);
                } else {
                    logger.error("No accepted taxon found for " + synonymR.getURI());
                }
                tnrResponse.setMatchingNameType(NameType.SYNONYM);
                // TODO find accepted it is linked via eunisPrimaryName and (synonymFor)
            }

            if(request.isAddSynonymy()) {
                // add Synonyms
//                generateSynonyms(records,tnrResponse);
            }

        return tnrResponse;
    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES;
    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        // return IdentifierUtils.checkLSID(value) ||
        // IdentifierUtils.checkUUID(value);
        return value != null;
    }

}
