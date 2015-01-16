package org.bgbm.biovel.drf.checklist;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.tnr.msg.Classification;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GBIFBackboneClient extends AggregateChecklistClient {

    public static final String ID = "gbif";
    public static final String LABEL = "GBIF Checklist Bank";
    public static final String URL = "http://uat.gbif.org/developer/species";
    public static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";
    private static final String MAX_PAGING_LIMIT = "1000";
    private static final String VERSION = "v0.9";
    public static final ServiceProviderInfo CINFO = new ServiceProviderInfo(ID,LABEL,ServiceProviderInfo.DEFAULT_SEARCH_MODE,URL,DATA_AGR_URL, VERSION);

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(SearchMode.scientificNameExact);

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
    public HttpHost getHost() {
        // TODO Auto-generated method stub
        return new HttpHost("api.gbif.org",80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = CINFO;
        int offset = 0;

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http");
        uriBuilder.setHost(getHost().getHostName());
        uriBuilder.setPath("/" + checklistInfo.getVersion() + "/dataset/search");
        uriBuilder.setParameter("type", "CHECKLIST");
        uriBuilder.setParameter("limit", MAX_PAGING_LIMIT);
        uriBuilder.setParameter("offset", "0");
        URI uri;
        boolean endOfRecords = false;
        try {
            do {
                uriBuilder.setParameter("offset", Integer.toString(offset));
                uri = uriBuilder.build();
                System.out.println("buildChecklistMap");
                String responseBody = processRESTService(uri);

                JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(responseBody);
                JSONArray results = (JSONArray) jsonResponse.get("results");
                Iterator<JSONObject> itrResults = results.iterator();
                while(itrResults.hasNext()) {
                    JSONObject result = itrResults.next();
                    String key = (String)result.get("key");
                    String title = (String)result.get("title");

                    String url =  "http://uat.gbif.org/dataset/" + key;
                    checklistInfo.addSubChecklist(new ServiceProviderInfo(key, title,  url, DATA_AGR_URL, getSearchModes()));
                }

                endOfRecords = (Boolean) jsonResponse.get("endOfRecords");

                offset = offset + Integer.parseInt(MAX_PAGING_LIMIT);
            } while(!endOfRecords);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // FIXME : We should process the exceptions and if we can't do nothing with them pass to the next level
        } catch (DRFChecklistException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // FIXME : We should process the exceptions and if we can't do nothing with them pass to the next level
        }

        return checklistInfo;
    }



    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);

        Iterator<ServiceProviderInfo> itrKeys = getServiceProviderInfo().getSubChecklists().iterator();
        //http://api.gbif.org/name_usage?q=Abies%20alba&datasetKey=fab88965-e69d-4491-a04d-e3198b626e52
        while(itrKeys.hasNext()) {
            ServiceProviderInfo checklistInfo = itrKeys.next();
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("datasetKey", checklistInfo.getId());
            paramMap.put("limit", MAX_PAGING_LIMIT);

            URI namesUri = buildUriFromQuery(query, "/" + CINFO.getVersion() + "/species",	"name", paramMap);

            String responseBody = processRESTService(namesUri);

            updateQueryWithResponse(query,responseBody, paramMap, checklistInfo);
        }
    }

    @Override
    public int getMaxPageSize() {
        return 10;
    }

    private void updateQueryWithResponse(Query query ,
            String response,
            Map<String, String> paramMap,
            ServiceProviderInfo ci) throws DRFChecklistException {

        JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(response);
        JSONArray results = (JSONArray) jsonResponse.get("results");

        if(results != null) {
            String accTaxonId = "";
            Iterator<JSONObject> resIterator = results.iterator();
            while (resIterator.hasNext()) {
                JSONObject res = resIterator.next();
                Number acceptedKey = (Number)res.get("acceptedKey");
                boolean synonym = (Boolean)res.get("synonym");

                TnrResponse tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

                // case when accepted name
                if(!synonym && (acceptedKey == null)) {
                    Long key = (Long)res.get("key");
                    accTaxonId = key.toString();

                    Taxon accName = generateAccName(res);
                    tnrResponse.setTaxon(accName);

                } else
                // case when synonym
                if(synonym && (acceptedKey != null)) {
                    Long key = (Long)res.get("acceptedKey");
                    accTaxonId = key.toString();

                    URI taxonUri = buildUriFromQuery(query, "/" + CINFO.getVersion() + "/species/" + accTaxonId, null);
                    String responseBody = processRESTService(taxonUri);

                    JSONObject taxon = (JSONObject) JSONUtils.parseJsonToObject(responseBody);
                    Taxon accName = generateAccName(taxon);
                    tnrResponse.setTaxon(accName);

                } else {
                    throw new DRFChecklistException("Name is neither accepted nor a synonym");
                }


                if(query != null) {
                    query.getTnrResponse().add(tnrResponse);
                }
                int offset = 0;
                paramMap.put("limit", MAX_PAGING_LIMIT);

                boolean endOfRecords = false;

                do {
                    paramMap.put("offset", Integer.toString(offset));

                    URI synonymsUri = buildUriFromQuery(query, "/" + CINFO.getVersion() + "/species/" + accTaxonId + "/synonyms", paramMap);
                    String synResponse = processRESTService(synonymsUri);

                    JSONObject pagedSynonyms = (JSONObject) JSONUtils.parseJsonToObject(synResponse);
                    generateSynonyms(pagedSynonyms, tnrResponse);

                    endOfRecords = (Boolean) pagedSynonyms.get("endOfRecords");

                    offset = offset + Integer.parseInt(MAX_PAGING_LIMIT);
                } while(!endOfRecords);
            }
        }
    }

    private Taxon generateAccName(JSONObject taxon) {
        Taxon accTaxon = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = (String) taxon.get("scientificName");
        taxonName.setFullName(resName);

        taxonName.setCanonicalName((String) taxon.get("canonicalName"));

        taxonName.setRank((String) taxon.get("rank"));

        taxonName.setAuthorship((String) taxon.get("authorship"));

        accTaxon.setTaxonName(taxonName);
        accTaxon.setTaxonomicStatus((String)taxon.get("taxonomicStatus"));
        accTaxon.setAccordingTo((String) taxon.get("accordingTo"));

        Long key = (Long)taxon.get("key");
        String taxonId = key.toString();
        accTaxon.setUrl("http://uat.gbif.org/species/" + taxonId);


        //FIXME : To fill in
        String sourceUrl = "http://uat.gbif.org/species/" + taxonId;
        String sourceDatasetID = "";
        String sourceDatasetName = "";
        String sourceName = "";

        Source source = new Source();
        source.setDatasetID(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accTaxon.getSources().add(source);

        Classification c = new Classification();
        c.setKingdom((String) taxon.get("kingdom"));
        c.setPhylum((String) taxon.get("phylum"));
        c.setClazz((String) taxon.get("clazz"));
        c.setOrder((String) taxon.get("order"));
        c.setFamily((String) taxon.get("family"));
        c.setGenus((String) taxon.get("genus"));
        accTaxon.setClassification(c);

        return accTaxon;
    }

    private void generateSynonyms(JSONObject pagedSynonyms, TnrResponse tnrResponse) {


        JSONArray synonyms = (JSONArray)pagedSynonyms.get("results");
        Iterator<JSONObject> itrSynonyms = synonyms.iterator();
        while(itrSynonyms.hasNext()) {
            Synonym synonym = new Synonym();
            JSONObject synonymjs = (JSONObject) itrSynonyms.next();
            TaxonName taxonName = new TaxonName();

            String resName = (String) synonymjs.get("scientificName");
            taxonName.setFullName(resName);

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
            source.setDatasetID(sourceDatasetID);
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
            uriBuilder.setHost(getHost().getHostName());
            uriBuilder.setPath("/" + CINFO.getVersion() + "/dataset/" + datasetKey);

            URI uri = uriBuilder.build();
            String responseBody = processRESTService(uri);

            JSONObject datasetInfo = (JSONObject) JSONUtils.parseJsonToObject(responseBody);

            return  (String) datasetInfo.get("title");
        }
        catch(URISyntaxException e){
            throw new DRFChecklistException(e);
        }
    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES;
    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }


}
