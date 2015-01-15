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
import org.bgbm.biovel.drf.tnr.msg.Scrutiny;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.gbif.nameparser.NameParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BgbmEditClient extends AggregateChecklistClient {

    public static final String ID = "bgbm-cdm-server";
    public static final String LABEL = "Name catalogues served by the BGBM CDM Server";
    public static final String DOC_URL = "http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html";
    public static final String COPYRIGHT_URL = "http://wp5.e-taxonomy.eu/cdmlib/license.html";


    private final Map<String,Query> taxonIdQueryMap = new HashMap<String,Query>();

    private final Map<String,String> taxonIdMatchStringMap = new HashMap<String, String>();

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike);

    public BgbmEditClient() {
        super();
    }

    public BgbmEditClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    @Override
    public HttpHost getHost() {
        //return new HttpHost("dev.e-taxonomy.eu",80);
        return new HttpHost("test.e-taxonomy.eu", 8080);
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
     * Adds the acceptedTaxonUuids found in the <code>responseBody</code> to the private field <code>taxonIdQueryMap</code>
     * and populates the <code>taxonIdMatchStringMap</code>
     *
     * @param queryList
     * @param responseBodyJson
     * @throws DRFChecklistException
     */
    private void buildTaxonIdMaps(List<Query> queryList , String responseBody) throws DRFChecklistException {

        JSONArray responseBodyJson = parseResponseBody(responseBody);

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
     * @param responseBody
     * @return
     * @throws DRFChecklistException
     */
    private JSONArray parseResponseBody(String responseBody) throws DRFChecklistException {
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            obj = parser.parse(responseBody);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DRFChecklistException(e);
        } catch (org.json.simple.parser.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DRFChecklistException(e);
        }

        JSONArray jsonArray = (JSONArray ) obj;
        return jsonArray;
    }

    private Taxon generateAccName(JSONObject taxon) {
        Taxon accName = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = (String) taxon.get("name");
        taxonName.setFullName(resName);
        NameParser ecatParser = new NameParser();
        String nameCanonical = ecatParser.parseToCanonical(resName);
        taxonName.setCanonicalName(nameCanonical);

        taxonName.setRank((String) taxon.get("rank"));

        accName.setTaxonName(taxonName);
        accName.setTaxonomicStatus((String)taxon.get("taxonStatus"));

        JSONObject sourcejs = (JSONObject)taxon.get("source");
        String sourceUrl = (String) sourcejs.get("url");
        String sourceDatasetID =  (String) sourcejs.get("datasetID");
        String sourceDatasetName = (String) sourcejs.get("datasetName");
        String sourceName = "";

        Source source = new Source();
        source.setDatasetID(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accName.setSource(source);

        JSONObject scrutinyjs = (JSONObject)taxon.get("taxonomicScrutiny");
        String accordingTo = (String) scrutinyjs.get("accordingTo");
        String modified = (String) scrutinyjs.get("modified");

        Scrutiny scrutiny = new Scrutiny();
        scrutiny.setAccordingTo(accordingTo);
        scrutiny.setModified(modified);
        accName.setScrutiny(scrutiny);

        JSONObject classification =(JSONObject)taxon.get("classification");
        if(classification != null) {
            Classification c = new Classification();
            c.setKingdom((String) classification.get("Kingdom"));
            c.setPhylum((String) classification.get("Phylum"));
            c.setClazz((String) classification.get("Class"));
            c.setOrder((String) classification.get("Order"));
            c.setFamily((String) classification.get("Family"));
            c.setGenus((String) classification.get("Genus"));
            accName.setClassification(c);
        }
        return accName;
    }

    private void generateSynonyms(JSONArray relatedTaxa, TnrResponse tnrResponse) {


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

                JSONObject sourcejs = (JSONObject)synonymjs.get("source");
                String sourceUrl = (String) sourcejs.get("url");
                String sourceDatasetID =  (String) sourcejs.get("datasetID");
                String sourceDatasetName = (String) sourcejs.get("datasetName");
                String sourceName = "";

                Source source = new Source();
                source.setDatasetID(sourceDatasetID);
                source.setDatasetName(sourceDatasetName);
                source.setName(sourceName);
                source.setUrl(sourceUrl);
                synonym.setSource(source);

                JSONObject scrutinyjs = (JSONObject)synonymjs.get("taxonomicScrutiny");
                String accordingTo = (String) scrutinyjs.get("accordingTo");
                String modified = (String) scrutinyjs.get("modified");

                Scrutiny scrutiny = new Scrutiny();
                scrutiny.setAccordingTo(accordingTo);
                scrutiny.setModified(modified);
                synonym.setScrutiny(scrutiny);

                tnrResponse.getSynonym().add(synonym);
            }
        }
    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        List<Query> queryList = tnrMsg.getQuery();

        for (ServiceProviderInfo checklistInfo : getServiceProviderInfo().getSubChecklists()) {
            URI namesUri = buildUriFromQueryList(queryList,
                    "/cdmserver/" + checklistInfo.getId() + "/name_catalogue.json",
                    "query",
                    "*", null);

            String searchResponseBody = processRESTService(namesUri);
            buildTaxonIdMaps(queryList, searchResponseBody);

            List<String> taxonIdList = new ArrayList<String>(taxonIdQueryMap.keySet());

            if(taxonIdList.size() > 0) {
                URI taxonUri = buildUriFromQueryStringList(taxonIdList,
                        "/cdmserver/" + checklistInfo.getId() + "/name_catalogue/taxon.json",
                        "taxonUuid",
                        null);
                String taxonResponseBody = processRESTService(taxonUri);
                updateQueriesWithResponse(taxonResponseBody, checklistInfo);
            }
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // delegate to resolveScientificNamesExact, since the like search mode is handled in buildUriFromQueryList
        resolveScientificNamesExact(tnrMsg);

    }

    private void updateQueriesWithResponse(String responseBody, ServiceProviderInfo ci) throws DRFChecklistException {

        JSONArray responseBodyJson = parseResponseBody(responseBody);

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
                TnrResponse tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

                String matchingName = taxonIdMatchStringMap.get(taxonUuid);
                tnrResponse.setMatchingNameString(matchingName);
                tnrResponse.setMatchingNameType(matchingName.equals(taxon.get("name").toString()) ? NameType.TAXON : NameType.SYNONYM);

                Taxon accName = generateAccName(taxon);
                tnrResponse.setTaxon(accName);
                generateSynonyms(relatedTaxa, tnrResponse);
                Query query = taxonIdQueryMap.get(taxonUuid);
                if(query != null) {
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }
    }

    @Override
    public void resolveVernacularNames(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES ;
    }



}
