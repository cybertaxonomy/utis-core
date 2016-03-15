package org.cybertaxonomy.utis.checklist;


import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.RestClient;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Source;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.IdentifierUtils;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.gbif.nameparser.NameParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BgbmEditClient extends AggregateChecklistClient<RestClient> {

    private static final Logger logger = LoggerFactory.getLogger(BgbmEditClient.class);

    public static final String ID = "bgbm-cdm-server";
    public static final String LABEL = "Name catalogues served by the BGBM CDM Server";
    public static final String DOC_URL = "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html";
    public static final String COPYRIGHT_URL = "http://cybertaxonomy.eu/cdmlib/license.html";
    private static final String SERVER_PATH_PREFIX = "/";
    private static final HttpHost HTTP_HOST = new HttpHost("api.cybertaxonomy.org", 80); // new HttpHost("test.e-taxonomy.eu", 80);


    private final Map<String,Query> taxonIdQueryMap = new HashMap<String,Query>();

    private final Map<String,String> taxonIdMatchStringMap = new HashMap<String, String>();

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.findByIdentifier,
            SearchMode.higherClassification,
            SearchMode.taxonomicChildren
            );

    public BgbmEditClient() {
        super();
    }

    public BgbmEditClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatelessClient() {
        // TODO move taxonIdQueryMap and taxonIdMatchStringMap into
        //      session state object to make this a stateless client
        return false;
    }

    @Override
    public void initQueryClient() {
        queryClient = new RestClient(HTTP_HOST);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,DOC_URL,COPYRIGHT_URL, getSearchModes());
        ServiceProviderInfo col = new ServiceProviderInfo("col",
                "Catalogue Of Life (EDIT - name catalogue end point)",
                "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "http://www.catalogueoflife.org/col/info/copyright", ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        col.setDefaultClassificationId("29d4011f-a6dd-4081-beb8-559ba6b84a6b");
        col.setSearchModes(SEARCH_MODES);
        checklistInfo.addSubChecklist(col);
        return checklistInfo;
    }

    /**
     * Adds the acceptedTaxonUuids found in the <code>responseBody</code> to the
     * private field <code>taxonIdQueryMap</code>
     * and populates the <code>taxonIdMatchStringMap</code>
     *
     * @param queryList
     * @param responseBodyJson
     * @throws DRFChecklistException
     * @throws ParseException
     */
    private void buildTaxonIdMapsFromCatalogueServiceResponse(List<Query> queryList , String responseBody) throws DRFChecklistException, ParseException {

        JSONArray responseBodyJson = parseResponseBody(responseBody, JSONArray.class);

        if(responseBodyJson.size() != queryList.size()){
            throw new DRFChecklistException("Query and Response lists have different lengths");
        }

        Iterator<JSONObject> itrNameMsgs = responseBodyJson.iterator();

        for (Query query : queryList) {
            JSONArray responseArray = (JSONArray) itrNameMsgs.next().get("response");
            if(responseArray != null) {
                Iterator<JSONObject> resIterator = responseArray.iterator();
                while (resIterator.hasNext()) {
                    JSONObject res = resIterator.next();
                    JSONArray accTaxonUuidArray = (JSONArray) res.get("acceptedTaxonUuids");
                    String matchingName = res.get("title").toString();
                    Iterator<String> atIterator = accTaxonUuidArray.iterator();
                    while (atIterator.hasNext()) {
                        String acceptedTaxonId = atIterator.next();
                        boolean isAcceptedTaxonMatch = res.get("taxonConceptUuids").toString().contains(acceptedTaxonId);
                        if(!taxonIdQueryMap.containsKey(acceptedTaxonId) || isAcceptedTaxonMatch){
                            // matches for accepted taxa should be preferred here
                            // matches for synomymy or other types should never overwrite
                            // accepted taxon matches
                            taxonIdQueryMap.put(acceptedTaxonId, query);
                            taxonIdMatchStringMap.put(acceptedTaxonId, matchingName);
                        }
                        //System.out.println("Found accepted taxon id : " + accTaxonId);
                    }
                }
            }
        }
    }

    /**
     * Adds the acceptedTaxonUuids found in the <code>responseBody</code> to the
     * private field <code>taxonIdQueryMap</code>
     * and populates the <code>taxonIdMatchStringMap</code>
     *
     * @param queryList
     * @param responseBodyJson
     * @throws DRFChecklistException
     * @throws ParseException
     */
    private void addTaxaToTaxonIdMapFromIdentifierServiceResponse(List<Query> queryList , String responseBody) throws DRFChecklistException, ParseException {

        /*
         * {"class":"DefaultPagerImpl","count":8,"currentIndex":0,"firstRecord":1,"indices":[0],"lastRecord":8,"nextIndex":0,"pageSize":30,"pagesAvailable":1,"prevIndex":0,
            "records":[
                  {
                  "cdmEntity":{
                      "cdmUuid":"0cf5a6fe-b1df-4f4f-85a8-31fef4be2a68",
                      "class":"CdmEntity",
                      "titleCache":"Abies alba Mill. sec. SCHMEIL-FITSCHEN, Flora von Deutschland und angrenzenden Ländern, 89. Aufl"},
                  "class":"FindByIdentifierDTO",
                   "identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"3d256539-d7f7-4dd1-ad7f-cd6e4c141f24","class":"CdmEntity","titleCache":"Abies alba Mill. sec. OBERDORFER, Pflanzensoziologische Exkursionsflora, ed. 7"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"61c2bc4f-a23d-4160-8f14-625b4484fc2f","class":"CdmEntity","titleCache":"Abies alba Mill. sec. HEGI, Illustrierte Flora von Mitteleuropa, Aufl. 2 u. 3"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"7a63f215-0a41-4b7e-9394-bda4521d6ad1","class":"CdmEntity","titleCache":"Abies alba Mill. sec. GREUTER et. al., Med-Checklist bisher Bde. 1, 3 und 4"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"872088a4-95f4-472c-ae79-a29028bb3fbf","class":"CdmEntity","titleCache":"Abies alba Mill. sec. Wisskirchen & Haeupler, 1998"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"90ee17be-d455-4564-949d-9c53e27a6a6f","class":"CdmEntity","titleCache":"Abies alba Mill. sec. TUTIN et al., Flora Europaea"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"b0d35335-63e6-41ab-bdb0-d01851134e9c","class":"CdmEntity","titleCache":"Abies alba Mill. sec. EHRENDORFER, Liste der Gefäßpflanzen Mitteleuropas, 2. Aufl"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}},{"cdmEntity":{"cdmUuid":"b7a352aa-1f73-41f3-a4e3-b24fc1c2cd5f","class":"CdmEntity","titleCache":"Abies alba Mill. sec. ROTHMALER, 1990"},"class":"FindByIdentifierDTO","identifier":{"class":"AlternativeIdentifier","identifier":"1","typeLabel":"Florein Identifier","typeUuid":"8b67291e-96e0-4556-8d6a-c94e8750b301"}}],"suggestion":""}
        */

        if(queryList.size() > 1){
            throw new DRFChecklistException("Only single Querys are supported");
        }

        Query query = queryList.get(0);

        JSONObject jsonPager = parseResponseBody(responseBody, JSONObject.class);

        JSONArray jsonRecords = (JSONArray) jsonPager.get("records");


        Iterator<JSONObject> resIterator = jsonRecords.iterator();
        while (resIterator.hasNext()) {
            JSONObject record = resIterator.next();
            JSONObject cdmEntity = (JSONObject) record.get("cdmEntity");
            String uuid = cdmEntity.get("cdmUuid").toString();
            taxonIdQueryMap.put(uuid, query);
        }

    }

    private void addTaxonToTaxonIdMap(List<Query> queryList , String responseBody) throws DRFChecklistException, ParseException {


        if(queryList.size() > 1){
            throw new DRFChecklistException("Only single Querys are supported");
        }

        Query query = queryList.get(0);

        JSONObject cdmEntity = parseResponseBody(responseBody, JSONObject.class);
        String uuid = cdmEntity.get("uuid").toString();
        taxonIdQueryMap.put(uuid, query);

    }

    /**
     * @param responseBody
     * @return
     * @throws DRFChecklistException
     * @throws org.json.simple.parser.ParseException
     */
    private <T extends JSONAware> T parseResponseBody(String responseBody, Class<T> jsonType) throws DRFChecklistException, org.json.simple.parser.ParseException {
        // TODO use Jackson instead? it is much faster!
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        if(jsonType.isAssignableFrom(obj.getClass())){
            return jsonType.cast(obj);
        } else {
            throw new DRFChecklistException("parseResponseBody() - deserialized responseBody is not of type " + jsonType ) ;
        }

    }

    private Taxon generateTaxon(JSONObject taxon, boolean addClassification, ServiceProviderInfo ci, String taxonUuid) throws DRFChecklistException {
        Taxon accTaxon = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = (String) taxon.get("name");
        taxonName.setScientificName(resName);
        NameParser ecatParser = new NameParser();
        String nameCanonical = ecatParser.parseToCanonical(resName);
        taxonName.setCanonicalName(nameCanonical);

        taxonName.setRank((String) taxon.get("rank"));
        String lsid = (String) taxon.get("lsid");

        JSONObject scrutinyjs = (JSONObject)taxon.get("taxonomicScrutiny");
        String accordingTo = (String) scrutinyjs.get("accordingTo");
        String modified = (String) scrutinyjs.get("modified");

        accTaxon.setTaxonName(taxonName);
        accTaxon.setTaxonomicStatus((String)taxon.get("taxonStatus"));
        accTaxon.setAccordingTo(accordingTo);
        accTaxon.setIdentifier(lsid);

        JSONObject sourcejs = (JSONObject)taxon.get("source");
        String sourceUrl = (String) sourcejs.get("url");
        String sourceDatasetID =  (String) sourcejs.get("datasetID");
        String sourceDatasetName = (String) sourcejs.get("datasetName");
        String sourceName = "";

        Source source = new Source();
        source.setIdentifier(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accTaxon.getSources().add(source);

        if(addClassification) {
            addClassification(taxon, accTaxon, ci, taxonUuid);
        }
        return accTaxon;
    }

    /**
     * @param taxon
     * @param accTaxon
     * @param ci
     * @param taxonUuid
     * @throws DRFChecklistException
     */
    private void addClassification(JSONObject taxon, Taxon accTaxon, ServiceProviderInfo ci, String taxonUuid) throws DRFChecklistException {

        // need to fetch the full classification from the cdm REST api:

        URI classificationUri = queryClient.buildURI(SERVER_PATH_PREFIX + ci.getId()
                + "/portal/classification/" + ci.defaultClassificationId() + "/pathFrom/" + taxonUuid + ".json", null);

        String responseBody = queryClient.get(classificationUri);

        JSONArray pathToRoot  = null;
        try {
            // pathtoroot i a list of TaxonNode objects
            pathToRoot = parseResponseBody(responseBody, JSONArray.class);
        } catch (ParseException e1) {
            throw new DRFChecklistException("Error parsing response from " + classificationUri.toString(), e1);
        }

        for(Object o: pathToRoot) {
            JSONObject taxonNodeJson = (JSONObject)o;

            HigherClassificationElement hce = new HigherClassificationElement();
            hce.setScientificName(taxonNodeJson.get("titleCache").toString());
            hce.setRank(taxonNodeJson.get("rankLabel").toString());
            hce.setTaxonID(taxonNodeJson.get("uuid").toString()); // need to get the LSId instead!!!!
            accTaxon.getHigherClassification().add(hce);
        }
    }

    private void generateSynonyms(JSONArray relatedTaxa, Response tnrResponse) {


        Iterator<JSONObject> itrSynonyms = relatedTaxa.iterator();
        while(itrSynonyms.hasNext()) {

            JSONObject synonymjs = itrSynonyms.next();
            String status = (String) synonymjs.get("taxonStatus");
            if(status != null && status.equals("synonym")) {
                Synonym synonym = new Synonym();
                TaxonName taxonName = new TaxonName();

                String resName = (String) synonymjs.get("name");
                taxonName.setScientificName(resName);
                NameParser ecatParser = new NameParser();
                String nameCanonical = ecatParser.parseToCanonical(resName);
                taxonName.setCanonicalName(nameCanonical);
                synonym.setTaxonomicStatus((String)synonymjs.get("taxonStatus"));

                taxonName.setRank((String) synonymjs.get("rank"));

                synonym.setTaxonName(taxonName);

                JSONObject scrutinyjs = (JSONObject)synonymjs.get("taxonomicScrutiny"); // TODO this will change in future releases of the service
                synonym.setAccordingTo((String) scrutinyjs.get("accordingTo"));

                JSONObject sourcejs = (JSONObject)synonymjs.get("source");
                String sourceUrl = (String) sourcejs.get("url");
                String sourceDatasetID =  (String) sourcejs.get("datasetID");
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
        Query.Request request = queryList.get(0).getRequest();

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            URI namesUri = queryClient.buildUriFromQueryList(queryList,
                    SERVER_PATH_PREFIX + checklistInfo.getId() + "/name_catalogue.json",
                    "query",
                    "*", null);
            try {
                String searchResponseBody = queryClient.get(namesUri);
                buildTaxonIdMapsFromCatalogueServiceResponse(queryList, searchResponseBody);
            } catch (ParseException e) {
               throw new DRFChecklistException("Error while parsing the response of " + namesUri, e);
            }

            List<String> taxonIdList = new ArrayList<String>(taxonIdQueryMap.keySet());

            if(taxonIdList.size() > 0) {
                URI taxonUri = queryClient.buildUriFromQueryStringList(taxonIdList,
                        SERVER_PATH_PREFIX + checklistInfo.getId() + "/name_catalogue/taxon.json",
                        "taxonUuid",
                        null);
                String taxonResponseBody = queryClient.get(taxonUri);
                // buildTaxonIdMapsFromCatalogueServiceResponse(queryList, taxonResponseBody);
                try {
                    updateQueriesWithResponse(taxonResponseBody, checklistInfo, request, false);
                } catch (ParseException e) {
                    throw new DRFChecklistException("Error while parsing the response of " + namesUri, e);
                }
            }
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // delegate to resolveScientificNamesExact, since the like search mode is handled in buildUriFromQueryList
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

        _findByIdentifier(tnrMsg, false);

    }

    /**
     * @param tnrMsg
     * @param addClassification TODO
     * @throws DRFChecklistException
     */
    private void _findByIdentifier(TnrMsg tnrMsg, boolean addClassification) throws DRFChecklistException {
        List<Query> queryList = tnrMsg.getQuery();

        if(queryList.size() > 1){
            throw new DRFChecklistException("Only single Querys are supported");
        }

        Query.Request request = queryList.get(0).getRequest();
        Map<String, String> findByIdentifierParameters = new HashMap<String,String>();
        findByIdentifierParameters.put("includeEntity", "1");

        String identifier = request.getQueryString();


        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {

            queryForTaxonByID(queryList, findByIdentifierParameters, identifier, checklistInfo);

            List<String> taxonIdList = new ArrayList<String>(taxonIdQueryMap.keySet());
            if(taxonIdList.size() > 0) {
                URI taxonUri = queryClient.buildUriFromQueryStringList(taxonIdList,
                        SERVER_PATH_PREFIX + checklistInfo.getId() + "/name_catalogue/taxon.json",
                        "taxonUuid",
                        null);
                String taxonResponseBody = queryClient.get(taxonUri);
                try {
                    updateQueriesWithResponse(taxonResponseBody, checklistInfo, request, addClassification);
                } catch (ParseException e) {
                    throw new DRFChecklistException("Error while parsing the response of " + taxonUri, e);
                }
            }
        } // end of per subchecklist loop
    }

    /**
     * Populates the <code>taxonIdQueryMap</code> with the taxa that are found using the given id string
     *
     * @param queryList
     * @param findByIdentifierParameters
     * @param identifier
     * @param checklistInfo
     * @throws DRFChecklistException
     */
    private void queryForTaxonByID(List<Query> queryList, Map<String, String> findByIdentifierParameters,
            String identifier, ServiceProviderInfo checklistInfo) throws DRFChecklistException {
        URI namesUri = null;
        try {
            if(IdentifierUtils.checkLSID(identifier)){
                namesUri = queryClient.buildUriFromQueryList(queryList,
                        SERVER_PATH_PREFIX + checklistInfo.getId() + "/authority/metadata.do",
                        "lsid",
                        null,
                        null );

                String responseBody = queryClient.get(namesUri);
                addTaxonToTaxonIdMap(queryList, responseBody);
            } else {
                namesUri = queryClient.buildUriFromQueryList(queryList,
                        SERVER_PATH_PREFIX + checklistInfo.getId() + "/taxon/findByIdentifier.json",
                        "identifier",
                        null, // like search for identifiers not supported by this client
                        findByIdentifierParameters );

                String responseBody = queryClient.get(namesUri);
                addTaxaToTaxonIdMapFromIdentifierServiceResponse(queryList, responseBody);
            }
        } catch (ParseException e) {
            throw new DRFChecklistException("Error while parsing the response of " + namesUri, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {

        List<Query> queryList = tnrMsg.getQuery();

        if(queryList.size() > 1){
            throw new DRFChecklistException("Only single Querys are supported");
        }

        Query query = queryList.get(0);
        Query.Request request = query.getRequest();
        Map<String, String> findByIdentifierParameters = new HashMap<String,String>();
        findByIdentifierParameters.put("includeEntity", "1");

        String identifier = request.getQueryString();

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {
            queryForTaxonByID(queryList, findByIdentifierParameters, identifier, checklistInfo);

            // copy all found uuid into a new list, taxonIdQueryMap will be overwritten alter on again and needs to be cleared now
            List<String> taxonIdList = new ArrayList<String>();
            taxonIdList.addAll(taxonIdQueryMap.keySet());
            taxonIdQueryMap.clear();

            if(taxonIdList.size() == 1) {
                // /portal/classification/{treeUuid}/childNodesOf/{taxonUuid}.json
                    URI taxonChildren = queryClient.buildURI(SERVER_PATH_PREFIX + checklistInfo.getId()
                            + "/portal/classification/" + checklistInfo.defaultClassificationId() + "/childNodesOf/" + taxonIdList.get(0) + ".json",
                            null);
                    String responseBody = queryClient.get(taxonChildren);

                    if(responseBody == null || responseBody.isEmpty()){
                        return;
                    }

                    try {
                        JSONArray taxonNodesJson = parseResponseBody(responseBody, JSONArray.class);
                        List<String> childtaxonIdList = new ArrayList<String>(taxonNodesJson.size());

                        for(Object o : taxonNodesJson) {

                            JSONObject tnodeJson = ((JSONObject)o);
                            childtaxonIdList.add(tnodeJson.get("taxonUuid").toString());
                            taxonIdQueryMap.put(tnodeJson.get("taxonUuid").toString(), query);
                        }

                        URI taxonUri = queryClient.buildUriFromQueryStringList(childtaxonIdList,
                                SERVER_PATH_PREFIX + checklistInfo.getId() + "/name_catalogue/taxon.json",
                                "taxonUuid",
                                null);
                        String taxonResponseBody = queryClient.get(taxonUri);


                        try {
                            updateQueriesWithResponse(taxonResponseBody, checklistInfo, request, false);
                        } catch (ParseException e) {
                            throw new DRFChecklistException("Error while parsing the response of " + taxonUri, e);
                        }

                    } catch (ParseException e) {
                        logger.error("Exception parsing the response on " + taxonChildren, e);
                    }


            } else if(taxonIdList.size() > 1) {
                throw new DRFChecklistException("More than one taxa is mathiching the given id " + identifier + ", this should never happen.");
            }

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {

        _findByIdentifier(tnrMsg, true);

    }

    private void updateQueriesWithResponse(String responseBody, ServiceProviderInfo ci, Query.Request request, boolean addClassification) throws DRFChecklistException, ParseException {

        if(responseBody == null || responseBody.isEmpty()){
            return;
        }

        JSONArray responseBodyJson = parseResponseBody(responseBody, JSONArray.class);

        Iterator<JSONObject> itrTaxonMsgs = responseBodyJson.iterator();


        while(itrTaxonMsgs.hasNext()) {

            JSONObject taxonInfo = itrTaxonMsgs.next();
            JSONObject taxonResponse = (JSONObject) taxonInfo.get("response");
            if(taxonResponse == null) {
                // an error occurred continue to the next
                continue;
            }
            JSONObject jsonTaxon = (JSONObject) taxonResponse.get("taxon");
            JSONArray relatedTaxa = (JSONArray) taxonResponse.get("relatedTaxa");

            JSONObject taxonRequest = (JSONObject) taxonInfo.get("request");
            String taxonUuid = (String) taxonRequest.get("taxonUuid");

            if(jsonTaxon != null) {
                Response tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

                String matchingName = taxonIdMatchStringMap.get(taxonUuid);
                if(matchingName != null){
                    tnrResponse.setMatchingNameString(matchingName);
                    tnrResponse.setMatchingNameType(matchingName.equals(jsonTaxon.get("name").toString()) ? NameType.TAXON : NameType.SYNONYM);
                }

                Taxon taxon = generateTaxon(jsonTaxon, addClassification, ci, taxonUuid);
                tnrResponse.setTaxon(taxon);

                if(request.isAddSynonymy()){
                    generateSynonyms(relatedTaxa, tnrResponse);
                }

                Query query = taxonIdQueryMap.get(taxonUuid);
                if(query != null) {
                    query.getResponse().add(tnrResponse);
                }
            }
        }
    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES ;
    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        // return IdentifierUtils.checkLSID(value) || IdentifierUtils.checkUUID(value);
        return value != null;
    }



}
