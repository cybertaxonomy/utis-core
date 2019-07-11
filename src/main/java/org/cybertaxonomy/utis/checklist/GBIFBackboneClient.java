package org.cybertaxonomy.utis.checklist;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.RestClient;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Source;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBIFBackboneClient extends AggregateChecklistClient<RestClient> {

    /**
     *
     */
    private static final String GBIF_DATASET_BASE_URL = "http://uat.gbif.org/dataset/";

    private static final Logger logger = LoggerFactory.getLogger(GBIFBackboneClient.class);

    private static final HttpHost HTTP_HOST = new HttpHost("api.gbif.org",80);
    public static final String ID = "gbif";
    public static final String LABEL = "GBIF Checklist Bank";
    public static final String URL = "http://uat.gbif.org/developer/species";
    public static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";
    private static final String VERSION = "v1";

    private static Map<String, UUID> datasetMap = new HashMap<>();

    static {
        datasetMap.put("GBIF Backbone Taxonomy", UUID.fromString("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c"));
        datasetMap.put("Paleobiology Database", UUID.fromString("bb5b30b4-827e-4d5e-a86a-825d65cb6583"));
        datasetMap.put("Naturgucker", UUID.fromString("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c"));
    }

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(SearchMode.scientificNameExact, SearchMode.scientificNameLike);

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.noneOf(ClassificationAction.class);

    public GBIFBackboneClient() {
        super();
    }

    public GBIFBackboneClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    public GBIFBackboneClient(ServiceProviderInfo spInfo) throws DRFChecklistException {
        super(spInfo);
    }

    @Override
    public void initQueryClient() {
        queryClient = new RestClient(HTTP_HOST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatelessClient() {
        return true;
    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {

            ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,ServiceProviderInfo.DEFAULT_SEARCH_MODE,URL,DATA_AGR_URL, VERSION);

            for(String datasetName : datasetMap.keySet()){
                checklistInfo.addSubChecklist(createSubChecklist(datasetMap.get(datasetName), datasetName));
            }

        return checklistInfo;
    }

    /**
     * @param checklistInfo
     * @param offset
     * @return
     * @throws URISyntaxException
     */
    private URI gbifApiListDatasetsUri(int offset) throws URISyntaxException {
        URI uri;
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http");
        uriBuilder.setHost(HTTP_HOST.getHostName());
        uriBuilder.setPath("/" + VERSION + "/dataset/search");
        uriBuilder.setParameter("type", "CHECKLIST");
        uri = uriBuilder.build();
        return uri;
    }

    /**
     * @param checklistInfo
     * @param offset
     * @return
     * @throws URISyntaxException
     */
    private URI gbifApiGetDatasetUri(UUID uuid) throws URISyntaxException {
        URI uri;
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http");
        uriBuilder.setHost(HTTP_HOST.getHostName());
        uriBuilder.setPath("/" + VERSION + "/dataset/" + uuid.toString());
        uri = uriBuilder.build();
        return uri;
    }

    /**
     * @param key
     * @param title
     * @param url
     * @return
     */
    private ServiceProviderInfo createSubChecklist(UUID uuid, String title) {
        String url =  GBIF_DATASET_BASE_URL + uuid.toString();
        return new ServiceProviderInfo(uuid.toString(), title,  url, DATA_AGR_URL, getSearchModes());
    }



    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        resolveScientificName(tnrMsg, false);
    }

    /**
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    private void resolveScientificName(TnrMsg tnrMsg, boolean likeMode) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);

        Iterator<ServiceProviderInfo> itrKeys = getServiceProviderInfo().getSubChecklists().iterator();
        //http://api.gbif.org/name_usage?q=Abies%20alba&datasetKey=fab88965-e69d-4491-a04d-e3198b626e52
        while(itrKeys.hasNext()) {
            ServiceProviderInfo checklistInfo = itrKeys.next();
            checklistInfo = validateSubChecklist(checklistInfo);
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("datasetKey", checklistInfo.getId());
//            if(likeMode){
//                paramMap.put("verbose", "true");
//            }

            String queryKey = likeMode ? "q" : "name";

            URI namesUri = queryClient.buildUriFromQuery(query, "/" + VERSION + "/species" + (likeMode? "/suggest" : ""),	queryKey, paramMap);

            String responseBody = queryClient.get(namesUri);
            if(likeMode){
                updateQueryWithLikeResponse(query,responseBody, checklistInfo);
            } else {
                updateQueryWithExactResponse(query,responseBody, checklistInfo);
            }
        }
    }

    private ServiceProviderInfo validateSubChecklist(ServiceProviderInfo checklistInfo){
        if(checklistInfo.getLabel() == null){
            try {
                checklistInfo = loadDataSetInfo(checklistInfo);
            } catch (URISyntaxException | DRFChecklistException e) {
                logger.error("loading the GIBF dataset info for " + checklistInfo.getId() + " failed. Sub checklist will be skipped", e);
                return null;
            }
        }
        return checklistInfo;
    }

    /**
     * @param checklistInfo
     */
    private ServiceProviderInfo loadDataSetInfo(ServiceProviderInfo checklistInfo) throws URISyntaxException, DRFChecklistException {

        UUID uuid;
        try {
            uuid = UUID.fromString(checklistInfo.getId());
        } catch (IllegalArgumentException e){
            throw new DRFChecklistException(e);
        }
        URI uri = gbifApiGetDatasetUri(uuid);
        String responseBody = queryClient.get(uri);

        JSONObject result = JSONUtils.parseJsonToObject(responseBody);

        String key = (String)result.get("key");
        String title = (String)result.get("title");
        String url =  "http://uat.gbif.org/dataset/" + key;
        return new ServiceProviderInfo(key, title,  url, DATA_AGR_URL, getSearchModes());

    }

    private void updateQueryWithExactResponse(Query query, String response, ServiceProviderInfo ci) throws DRFChecklistException {

        JSONObject jsonResponse = JSONUtils.parseJsonToObject(response);
        JSONArray results = (JSONArray) jsonResponse.get("results");

        List<JSONObject> resultsPage = extractResultPage(query, results);

        if(results != null) {
            Iterator<JSONObject> resIterator = resultsPage.iterator();
            while (resIterator.hasNext()) {
                JSONObject taxonRecord = resIterator.next();
                addTaxon(query, ci, taxonRecord);
            }
        }
    }

    /**
     * @param query
     * @param results
     * @return
     */
    private List<JSONObject> extractResultPage(Query query, JSONArray results) {
        PagerRange pagerRange = pagerRange(query);
        List<JSONObject> resultsPage = new ArrayList<>(0);
        if(pagerRange.isDefinedRange()){
            try {
            resultsPage = results.subList(pagerRange.low, Math.min(pagerRange.high + 1, results.size()));
            } catch (IllegalArgumentException e) {
                // we are out of the available range, just ignore the exception
            }

        } else {
            resultsPage = results.subList(0, results.size());
        }
        return resultsPage;
    }

    private void updateQueryWithLikeResponse(Query query, String response, ServiceProviderInfo ci) throws DRFChecklistException {

//        JSONObject jsonResponse = JSONUtils.parseJsonToObject(response);
//
//        if(jsonResponse.containsKey("usageKey")){
//            // gbif has added a goot match which is not always the case
//            addTaxon(query, ci, jsonResponse);
//        }
//
//        JSONArray alternatives = (JSONArray) jsonResponse.get("alternatives");
//
        JSONArray taxonRecords =  JSONUtils.parseJsonToArray(response);

        List<JSONObject> resultsPage = extractResultPage(query, taxonRecords);

        if(resultsPage != null) {
            Iterator<JSONObject> resIterator = resultsPage.iterator();
            while (resIterator.hasNext()) {
                JSONObject taxonRecord = resIterator.next();
                addTaxon(query, ci, taxonRecord);
            }
        }
    }

    /**
     * @param query
     * @param ci
     * @param taxonRecord
     * @throws DRFChecklistException
     */
    private void addTaxon(Query query, ServiceProviderInfo ci, JSONObject taxonRecord) throws DRFChecklistException {
        String accTaxonId = "";

        boolean synonymFlag = (Boolean)taxonRecord.get("synonym");

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

        // case when accepted name
        if(!synonymFlag) {
            Long key = (Long)taxonRecord.get("key");
//            if(key == null){
//                // the response objects in the like search are sightly different
//                key = (Long)taxonRecord.get("usageKey");
//            }
            accTaxonId = key.toString();

            Taxon accName = generateTaxon(taxonRecord, false);
            tnrResponse.setTaxon(accName);

        } else {
            Long acceptedKey = (Long)taxonRecord.get("acceptedKey");
            if(acceptedKey != null){
                accTaxonId = acceptedKey.toString();

                URI taxonUri = queryClient.buildUriFromQuery(query, "/" + VERSION + "/species/" + accTaxonId, null);
                String responseBody = queryClient.get(taxonUri);

                JSONObject taxon = JSONUtils.parseJsonToObject(responseBody);
                Taxon accName = generateTaxon(taxon, false);
                tnrResponse.setTaxon(accName);
            } else {
                logger.warn("no acceptedKey supplied, adding the sysnonym (TODO implement other way to get the accepted name)");
                Taxon synName = generateTaxon(taxonRecord, false);
                tnrResponse.setTaxon(synName);
            }

        }


        if(query != null) {
            query.getResponse().add(tnrResponse);
        }
        int offset = 0;

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("datasetKey", ci.getId());

        boolean endOfRecords = false;

        if(accTaxonId != null && !accTaxonId.isEmpty()){
            do {
                paramMap.put("offset", Integer.toString(offset));

                URI synonymsUri = queryClient.buildUriFromQuery(query, "/" + VERSION + "/species/" + accTaxonId + "/synonyms", paramMap);
                String synResponse = queryClient.get(synonymsUri);

                JSONObject pagedSynonyms = JSONUtils.parseJsonToObject(synResponse);
                generateSynonyms(pagedSynonyms, tnrResponse);

                endOfRecords = (Boolean) pagedSynonyms.get("endOfRecords");

            } while(!endOfRecords);
        }
    }

    private Taxon generateTaxon(JSONObject taxon, boolean addClassification) {
        Taxon accTaxon = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = (String) taxon.get("scientificName");
        taxonName.setScientificName(resName);

        taxonName.setCanonicalName((String) taxon.get("canonicalName"));

        taxonName.setRank((String) taxon.get("rank"));

        taxonName.setAuthorship((String) taxon.get("authorship"));

        accTaxon.setTaxonName(taxonName);
        accTaxon.setTaxonomicStatus((String)taxon.get("taxonomicStatus"));
        accTaxon.setAccordingTo((String) taxon.get("accordingTo"));

        Long key = (Long)taxon.get("key");
        if(key == null){
            key = (Long)taxon.get("usageKey");
        }
        String taxonId = key.toString();
        accTaxon.setUrl("http://uat.gbif.org/species/" + taxonId);


        //FIXME : To fill in
        String sourceUrl = "http://uat.gbif.org/species/" + taxonId;
        String sourceDatasetID = "";
        String sourceDatasetName = "";
        String sourceName = "";

        Source source = new Source();
        source.setIdentifier(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accTaxon.getSources().add(source);

        if(addClassification) {
            String[] rankNames = new String[] {"genus", "family", "order", "clazz", "phylum", "kingdom"};
            for(String rankName : rankNames) {
                try {
                String higherTaxonName = taxon.get(rankName).toString();
                HigherClassificationElement hce = new HigherClassificationElement();
                hce.setScientificName(higherTaxonName);
                if(rankName.equals("clazz")) {
                    rankName = "class";
                }
                hce.setRank(StringUtils.capitalize(rankName));
                accTaxon.getHigherClassification().add(hce);
                } catch(NullPointerException e) {
                    // IGNORE, just try the next rank

                }
            }
        }

        return accTaxon;

    }

    private void generateSynonyms(JSONObject pagedSynonyms, Response tnrResponse) {


        JSONArray synonyms = (JSONArray)pagedSynonyms.get("results");
        Iterator<JSONObject> itrSynonyms = synonyms.iterator();
        while(itrSynonyms.hasNext()) {
            Synonym synonym = new Synonym();
            JSONObject synonymjs = itrSynonyms.next();
            TaxonName taxonName = new TaxonName();

            String resName = (String) synonymjs.get("scientificName");
            taxonName.setScientificName(resName);

            taxonName.setCanonicalName((String) synonymjs.get("canonicalName"));

            taxonName.setRank((String) synonymjs.get("rank"));
            taxonName.setAuthorship((String) synonymjs.get("authorship"));

            synonym.setTaxonName(taxonName);
            synonym.setTaxonomicStatus((String)synonymjs.get("taxonomicStatus"));
            synonym.setAccordingTo((String) synonymjs.get("accordingTo"));

            Long key = (Long)synonymjs.get("key");
            String synId = key.toString();
            synonym.setUrl("http://uat.gbif.org/species/" + synId);

            //FIXME : To fill in
            String sourceUrl = "http://uat.gbif.org/species/" + synId;
            String sourceDatasetID =  "";
            String sourceDatasetName = "";
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

    private String getDatasetNameById(String datasetKey) throws DRFChecklistException {
        try{
            //Add a comment to this line
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("http");
            uriBuilder.setHost(HTTP_HOST.getHostName());
            uriBuilder.setPath("/" + VERSION + "/dataset/" + datasetKey);

            URI uri = uriBuilder.build();
            String responseBody = queryClient.get(uri);

            JSONObject datasetInfo = JSONUtils.parseJsonToObject(responseBody);

            return  (String) datasetInfo.get("title");
        }
        catch(URISyntaxException e){
            throw new DRFChecklistException(e);
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        resolveScientificName(tnrMsg, true);

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<ClassificationAction> getClassificationActions() {
        return CLASSIFICATION_ACTION;
    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("resolveVernacularNamesLike mode not supported by " + this.getClass().getSimpleName());

    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        return value != null;
    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("findByIdentifier mode not supported by " + this.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("taxonomicChildren mode not supported by " + this.getClass().getSimpleName());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("higherClassification mode not supported by " + this.getClass().getSimpleName());

    }

}
