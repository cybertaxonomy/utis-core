package org.bgbm.biovel.drf.checklist;


import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.tnr.msg.Classification;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.bgbm.biovel.drf.utils.IdentifierUtils;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.gbif.nameparser.NameParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.deser.std.JacksonDeserializers;

public class BgbmEditClient extends AggregateChecklistClient {

    public static final String ID = "bgbm-cdm-server";
    public static final String LABEL = "Name catalogues served by the BGBM CDM Server";
    public static final String DOC_URL = "http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html";
    public static final String COPYRIGHT_URL = "http://wp5.e-taxonomy.eu/cdmlib/license.html";


    private final Map<String,Query> taxonIdQueryMap = new HashMap<String,Query>();

    private final Map<String,String> taxonIdMatchStringMap = new HashMap<String, String>();

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.findByIdentifier
            );

    public BgbmEditClient() {
        super();
    }

    public BgbmEditClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    @Override
    public HttpHost getHost() {
//        return new HttpHost("dev.e-taxonomy.eu", 80);
        return new HttpHost("test.e-taxonomy.eu", 80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,DOC_URL,COPYRIGHT_URL, getSearchModes());
        checklistInfo.addSubChecklist(new ServiceProviderInfo("col",
                "Catalogue Of Life (EDIT - name catalogue end point)",
                "http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "http://www.catalogueoflife.org/col/info/copyright", ServiceProviderInfo.DEFAULT_SEARCH_MODE));
        return checklistInfo;
    }

    @Override
    public int getMaxPageSize() {
        return 10;
    }

    /**
     * Adds the acceptedTaxonUuids found in the <code>responseBody</code> to the
     * private field <code>taxonIdQueryMap</code>
     * and populates the <code>taxonIdMatchStringMap</code>
     *
     * @param queryList
     * @param responseBodyJson
     * @throws DRFChecklistException
     */
    private void buildTaxonIdMapsFromCatalogueServiceResponse(List<Query> queryList , String responseBody) throws DRFChecklistException {

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
                    String matchName = res.get("title").toString();
                    Iterator<String> atIterator = accTaxonUuidArray.iterator();
                    while (atIterator.hasNext()) {
                        String accTaxonId = atIterator.next();
                        boolean isAcceptedTaxonMatch = res.get("taxonConceptUuids").toString().contains(accTaxonId);
                        if(!taxonIdQueryMap.containsKey(accTaxonId) || isAcceptedTaxonMatch){
                            // matches for accepted taxa should be preferred here
                            // mathches for synomymy or other typs should never overrwite
                            // accepted taxon matches
                            taxonIdQueryMap.put(accTaxonId, query);
                            taxonIdMatchStringMap.put(accTaxonId, matchName);
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
     */
    private void addTaxaToTaxonIdMapFromIdentifierServiceResponse(List<Query> queryList , String responseBody) throws DRFChecklistException {

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

    private void addTaxonToTaxonIdMap(List<Query> queryList , String responseBody) throws DRFChecklistException {


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
     */
    private <T extends JSONAware> T parseResponseBody(String responseBody, Class<T> jsonType) throws DRFChecklistException {
        // TODO use Jackson instead? it is much faster!
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            obj = parser.parse(responseBody);
        } catch (ParseException e) {
            logger.error("parseResponseBody() - ", e);
            throw new DRFChecklistException(e);
        } catch (org.json.simple.parser.ParseException e) {

            logger.error("parseResponseBody() - ", e);
            throw new DRFChecklistException(e);
        }

        if(jsonType.isAssignableFrom(obj.getClass())){
            return jsonType.cast(obj);
        } else {
            throw new DRFChecklistException("parseResponseBody() - deserialized responseBody is not of type " + jsonType ) ;
        }

    }

    private Taxon generateAccName(JSONObject taxon) {
        Taxon accTaxon = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = (String) taxon.get("name");
        taxonName.setFullName(resName);
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

        JSONObject classification =(JSONObject)taxon.get("classification");
        if(classification != null) {
            Classification c = new Classification();
            c.setKingdom((String) classification.get("Kingdom"));
            c.setPhylum((String) classification.get("Phylum"));
            c.setClazz((String) classification.get("Class"));
            c.setOrder((String) classification.get("Order"));
            c.setFamily((String) classification.get("Family"));
            c.setGenus((String) classification.get("Genus"));
            accTaxon.setClassification(c);
        }
        return accTaxon;
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
                taxonName.setFullName(resName);
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
            URI namesUri = buildUriFromQueryList(queryList,
                    "/cdmserver/" + checklistInfo.getId() + "/name_catalogue.json",
                    "query",
                    "*", null);

            String searchResponseBody = processRESTService(namesUri);

            buildTaxonIdMapsFromCatalogueServiceResponse(queryList, searchResponseBody);

            List<String> taxonIdList = new ArrayList<String>(taxonIdQueryMap.keySet());

            if(taxonIdList.size() > 0) {
                URI taxonUri = buildUriFromQueryStringList(taxonIdList,
                        "/cdmserver/" + checklistInfo.getId() + "/name_catalogue/taxon.json",
                        "taxonUuid",
                        null);
                String taxonResponseBody = processRESTService(taxonUri);
                updateQueriesWithResponse(taxonResponseBody, checklistInfo, request);
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

        List<Query> queryList = tnrMsg.getQuery();

        if(queryList.size() > 1){
            throw new DRFChecklistException("Only single Querys are supported");
        }

        Query.Request request = queryList.get(0).getRequest();
        Map<String, String> findByIdentifierParameters = new HashMap<String,String>();
        findByIdentifierParameters.put("includeEntity", "1");

        String identifier = request.getQueryString();


        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {
            // taxon/findByIdentifier.json?identifier=1&includeEntity=1
            if(IdentifierUtils.checkLSID(identifier)){
                URI namesUri = buildUriFromQueryList(queryList,
                        "/cdmserver/" + checklistInfo.getId() + "/authority/metadata.do",
                        "lsid",
                        null,
                        null );

                String responseBody = processRESTService(namesUri);
                addTaxonToTaxonIdMap(queryList, responseBody);
            } else {
                URI namesUri = buildUriFromQueryList(queryList,
                        "/cdmserver/" + checklistInfo.getId() + "/taxon/findByIdentifier.json",
                        "identifier",
                        null, // like search for identifiers not supported by this client
                        findByIdentifierParameters );

                String responseBody = processRESTService(namesUri);
                addTaxaToTaxonIdMapFromIdentifierServiceResponse(queryList, responseBody);
            }

            List<String> taxonIdList = new ArrayList<String>(taxonIdQueryMap.keySet());

            if(taxonIdList.size() > 0) {
                URI taxonUri = buildUriFromQueryStringList(taxonIdList,
                        "/cdmserver/" + checklistInfo.getId() + "/name_catalogue/taxon.json",
                        "taxonUuid",
                        null);
                String taxonResponseBody = processRESTService(taxonUri);
                updateQueriesWithResponse(taxonResponseBody, checklistInfo, request);
            }
        }

    }

    private void updateQueriesWithResponse(String responseBody, ServiceProviderInfo ci, Query.Request request) throws DRFChecklistException {

        if(responseBody == null || responseBody.isEmpty()){
            return;
        }

        JSONArray responseBodyJson = parseResponseBody(responseBody, JSONArray.class);

        Iterator<JSONObject> itrTaxonMsgs = responseBodyJson.iterator();

        int i = -1;
        while(itrTaxonMsgs.hasNext()) {
            i++;
            JSONObject taxonInfo = itrTaxonMsgs.next();
            JSONObject taxonResponse = (JSONObject) taxonInfo.get("response");
            JSONObject taxon = (JSONObject) taxonResponse.get("taxon");
            JSONArray relatedTaxa = (JSONArray) taxonResponse.get("relatedTaxa");

            JSONObject taxonRequest = (JSONObject) taxonInfo.get("request");
            String taxonUuid = (String) taxonRequest.get("taxonUuid");

            if(taxon != null) {
                Response tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

                String matchingName = taxonIdMatchStringMap.get(taxonUuid);
                if(matchingName != null){
                    tnrResponse.setMatchingNameString(matchingName);
                    tnrResponse.setMatchingNameType(matchingName.equals(taxon.get("name").toString()) ? NameType.TAXON : NameType.SYNONYM);
                }

                Taxon accName = generateAccName(taxon);
                tnrResponse.setTaxon(accName);

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
