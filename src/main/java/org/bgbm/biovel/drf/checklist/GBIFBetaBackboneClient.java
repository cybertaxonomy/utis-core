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
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GBIFBetaBackboneClient extends AggregateChecklistClient {

    public static final String ID = "gbif";
    public static final String LABEL = "GBIF Checklist Bank";
    public static final String URL = "http://ecat-dev.gbif.org/";
    public static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(SearchMode.scientificNameExact);


    public GBIFBetaBackboneClient() {
        super();
    }

    public GBIFBetaBackboneClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    @Override
    public HttpHost getHost() {
        // TODO Auto-generated method stub
        return new HttpHost("ecat-dev.gbif.org",80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo()  {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL, getSearchModes());

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http");
        uriBuilder.setHost(getHost().getHostName());
        uriBuilder.setPath("/ws/checklist");
        URI uri;

        try {
            uri = uriBuilder.build();
            System.out.println("buildChecklistMap");
            String responseBody = processRESTService(uri);

            JSONObject jsonResponse = JSONUtils.parseJsonToObject(responseBody);
            JSONArray data = (JSONArray) jsonResponse.get("data");
            Iterator<JSONObject> itrResults = data.iterator();
            while(itrResults.hasNext()) {
                JSONObject result = itrResults.next();
                String key = ((Long)result.get("datasetID")).toString();
                String title = (String)result.get("title");
                String url =  "http://ecat-dev.gbif.org/checklist/" + key;
                checklistInfo.addSubChecklist(new ServiceProviderInfo(key, title,  url, DATA_AGR_URL, getSearchModes()));
            }

        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DRFChecklistException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return checklistInfo;
    }



    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);

        Iterator<ServiceProviderInfo> itrKeys = getServiceProviderInfo().getSubChecklists().iterator();
        //http://ecat-dev.gbif.org/ws/usage/?rkey={datasetID}&q={sciName}&pagesize=100&searchType=canonical
        while(itrKeys.hasNext()) {
            ServiceProviderInfo checklistInfo = itrKeys.next();
            //if(checklistInfo.getUse()) {
                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("rKey", checklistInfo.getId());
                paramMap.put("pageSize", "100");
                paramMap.put("searchType", "canonical");

                URI namesUri = buildUriFromQuery(query,
                        "/ws/usage",
                        "q",
                        paramMap);

                String responseBody = processRESTService(namesUri);

                updateQueryWithResponse(query,responseBody, paramMap, checklistInfo);
            //}
        }
    }

    @Override
    public int getMaxPageSize() {
        return 10;
    }

    private void updateQueryWithResponse(Query query,
            String response,
            Map<String, String> paramMap,
            ServiceProviderInfo checklistInfo) throws DRFChecklistException {


        JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(response);
        JSONArray dataArray = (JSONArray)jsonResponse.get("data");
        Iterator<JSONObject> itrNameMsgs = dataArray.iterator();

        String taxonId = null;
        while (itrNameMsgs.hasNext()) {
            JSONObject data = itrNameMsgs.next();
            Boolean isSynonym = (Boolean)data.get("isSynonym");
            String taxonomicStatus = (String)data.get("taxonomicStatus");
            String rank = (String)data.get("rank");
            if(rank == null || isSynonym == null){
                continue;
            }
            if(!isSynonym.booleanValue() ||  (taxonomicStatus != null && taxonomicStatus.equals("Accepted"))) {
                Number taxonIDNumber = (Number)data.get("taxonID");
                taxonId = taxonIDNumber.toString();
            }
            if(isSynonym.booleanValue()) {
                Number taxonIDNumber = (Number)data.get("higherTaxonID");
                taxonId = taxonIDNumber.toString();

            }

            Response tnrResponse = TnrMsgUtils.tnrResponseFor(checklistInfo);

            URI taxonUri = buildUriFromQuery(query, "/ws/usage/" + taxonId, null);
            String responseBody = processRESTService(taxonUri);

            JSONObject res = (JSONObject) JSONUtils.parseJsonToObject(responseBody);
            JSONObject jsonAccName = (JSONObject)res.get("data");
            Taxon accName = generateAccName(jsonAccName);
            tnrResponse.setTaxon(accName);
            if(query != null) {
                query.getResponse().add(tnrResponse);
            }
            JSONArray synonyms = (JSONArray) jsonAccName.get("synonyms");

            if(synonyms != null) {
                Iterator iterator = synonyms.iterator();
                System.out.println("Synonms");
                String synTaxonId = null;
                while (iterator.hasNext()) {
                    JSONObject syn = (JSONObject)iterator.next();
                    Number synIdNumber = (Number) syn.get("taxonID");
                    synTaxonId = String.valueOf(synIdNumber);

                    URI synonymsUri = buildUriFromQuery(query, "/ws/usage/" + synTaxonId, null);
                    String synResponse = processRESTService(synonymsUri);

                    JSONObject synonym = (JSONObject) JSONUtils.parseJsonToObject(synResponse);
                    generateSynonyms((JSONObject)synonym.get("data"), tnrResponse);

                }
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

        accTaxon.setTaxonName(taxonName);
        accTaxon.setTaxonomicStatus((String)taxon.get("taxonomicStatus"));
        accTaxon.setAccordingTo((String)taxon.get("accordingTo"));

        //FIXME : To fill in
        String sourceUrl = "";
        Number datasetIDNumber = (Number)taxon.get("datasetID");
        String sourceDatasetID = datasetIDNumber.toString();

        String sourceDatasetName = (String)taxon.get("datasetName");
        String sourceName = ""; // TODO

        Source source = new Source();
        source.setIdentifier(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accTaxon.getSources().add(source);

        Classification c = new Classification();
        c.setKingdom((String) taxon.get("kingdom"));
        c.setPhylum((String) taxon.get("phylum"));
        c.setClazz((String) taxon.get("class"));
        c.setOrder((String) taxon.get("order"));
        c.setFamily((String) taxon.get("family"));
        c.setGenus((String) taxon.get("genus"));
        accTaxon.setClassification(c);

        return accTaxon;
    }

    private void generateSynonyms(JSONObject synonym, Response tnrResponse) {
        Synonym syn = new Synonym();

        TaxonName taxonName = new TaxonName();

        String resName = (String) synonym.get("scientificName");
        taxonName.setFullName(resName);

        taxonName.setCanonicalName((String) synonym.get("canonicalName"));

        taxonName.setRank((String) synonym.get("rank"));

        syn.setTaxonName(taxonName);
        syn.setTaxonomicStatus((String) synonym.get("taxonomicStatus"));
        syn.setAccordingTo((String) synonym.get("accordingTo"));

        //FIXME : To fill in
        String sourceUrl = "";
        Number datasetIDNumber = (Number) synonym.get("datasetID");
        String sourceDatasetID = datasetIDNumber.toString();

        String sourceDatasetName = (String) synonym.get("datasetName");
        String sourceName = (String) synonym.get("accordingTo");		;

        Source source = new Source();
        source.setIdentifier(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        syn.getSources().add(source);

        tnrResponse.getSynonym().add(syn);
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

    @Override
    public boolean isSupportedIdentifier(String value) {
        return value != null;
    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }



}

