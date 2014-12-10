package org.bgbm.biovel.drf.checklist;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.bgbm.biovel.drf.tnr.msg.AcceptedName;
import org.bgbm.biovel.drf.tnr.msg.Name;
import org.bgbm.biovel.drf.tnr.msg.Scrutiny;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse.Synonym;
import org.gbif.nameparser.NameParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BgbmEditClient extends AggregateChecklistClient {

    public static final String ID = "edit";
    public static final String LABEL = "EDIT Platform";
    public static final String URL = "http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html";
    public static final String DATA_AGR_URL = "http://wp5.e-taxonomy.eu/cdmlib/license.html";


    private final Map<String,Query> taxonIdQueryMap;

    public BgbmEditClient() {
        super();
        taxonIdQueryMap = new HashMap<String,Query>();
    }

    public BgbmEditClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
        taxonIdQueryMap = new HashMap<String,Query>();
    }

    @Override
    public HttpHost getHost() {
        //return new HttpHost("dev.e-taxonomy.eu",80);
        return new HttpHost("test.e-taxonomy.eu", 8080);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL);
        checklistInfo.addSubChecklist(new ServiceProviderInfo("col",
                "Catalogue Of Life (EDIT -  name catalogue end point)",
                "http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "http://www.catalogueoflife.org/col/info/copyright"));
        return checklistInfo;
    }

    @Override
    public void resolveNames(TnrMsg tnrMsg) throws DRFChecklistException {
        List<TnrMsg.Query> queryList = tnrMsg.getQuery();
        Iterator<ServiceProviderInfo> itrKeys = getServiceProviderInfo().getSubChecklists().iterator();
        while(itrKeys.hasNext()) {
            ServiceProviderInfo checklistInfo = itrKeys.next();
            //if(checklistInfo.getUse()) {
                URI namesUri = buildUriFromQueryList(queryList,
                        "/cdmserver/" + checklistInfo.getId() + "/name_catalogue.json",
                        "query",
                        null);

                String response = processRESTService(namesUri);
                buildTaxonIdList(queryList,response);
                List<String> taxonIdList = new ArrayList<String>(taxonIdQueryMap.keySet());
                if(taxonIdList.size() > 0) {
                    URI taxonUri = buildUriFromQueryStringList(taxonIdList,
                            "/cdmserver/" + checklistInfo.getId() + "/name_catalogue/taxon.json",
                            "taxonUuid",
                            null);
                    response = processRESTService(taxonUri);
                    updateQueriesWithResponse(response, checklistInfo);
                }


            //}

        }


    }

    @Override
    public int getMaxPageSize() {
        return 10;
    }

    private void buildTaxonIdList(List<TnrMsg.Query> queryList , String response) throws DRFChecklistException {

        JSONParser parser = new JSONParser();
        Object obj;
        try {
            obj = parser.parse(response);
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
        if(jsonArray.size() != queryList.size()){
            throw new DRFChecklistException("Query and Response lists have different lengths");
        }

        Iterator<JSONObject> itrNameMsgs = jsonArray.iterator();
        Iterator<TnrMsg.Query> itrQuery = queryList.iterator();

        while(itrNameMsgs.hasNext() && itrQuery.hasNext()) {
            Query query = itrQuery.next();
            JSONArray responseArray = (JSONArray) itrNameMsgs.next().get("response");
            if(responseArray != null) {
                Iterator<JSONObject> resIterator = responseArray.iterator();
                while (resIterator.hasNext()) {
                    JSONObject res = resIterator.next();
                    JSONArray accTaxonUuidArray = (JSONArray) res.get("acceptedTaxonUuids");
                    Iterator<String> atIterator = accTaxonUuidArray.iterator();
                    while (atIterator.hasNext()) {
                        String accTaxonId = atIterator.next();
                        taxonIdQueryMap.put(accTaxonId, query);
                        //System.out.println("Found accepted taxon id : " + accTaxonId);
                    }
                }
            }
        }
    }

    private void updateQueriesWithResponse(String response, ServiceProviderInfo ci) throws DRFChecklistException {
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            obj = parser.parse(response);
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
        Iterator<JSONObject> itrTaxonMsgs = jsonArray.iterator();

        while(itrTaxonMsgs.hasNext()) {
            JSONObject taxonInfo = itrTaxonMsgs.next();
            JSONObject taxonResponse = (JSONObject) taxonInfo.get("response");
            JSONObject taxon = (JSONObject) taxonResponse.get("taxon");
            JSONArray relatedTaxa = (JSONArray) taxonResponse.get("relatedTaxa");

            JSONObject taxonRequest = (JSONObject) taxonInfo.get("request");
            String taxonUuid = (String) taxonRequest.get("taxonUuid");

            if(taxon != null) {
                TnrResponse tnrResponse = new TnrResponse();
                tnrResponse.setChecklist(ci.getLabel());
                tnrResponse.setChecklistUrl(ci.getUrl());
                AcceptedName accName = generateAccName(taxon);
                tnrResponse.setAcceptedName(accName);
                generateSynonyms(relatedTaxa, tnrResponse);
                Query query = taxonIdQueryMap.get(taxonUuid);
                if(query != null) {
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }
    }

    private AcceptedName generateAccName(JSONObject taxon) {
        AcceptedName accName = new AcceptedName();
        TaxonName taxonName = new TaxonName();
        Name name = new Name();

        String resName = (String) taxon.get("name");
        name.setFullName(resName);
        NameParser ecatParser = new NameParser();
        String nameCanonical = ecatParser.parseToCanonical(resName);
        name.setCanonicalName(nameCanonical);

        taxonName.setRank((String) taxon.get("rank"));
        taxonName.setName(name);

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
            AcceptedName.Classification c = new AcceptedName.Classification();
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
                TnrResponse.Synonym synonym = new Synonym();
                TaxonName taxonName = new TaxonName();
                Name name = new Name();

                String resName = (String) synonymjs.get("name");
                name.setFullName(resName);
                NameParser ecatParser = new NameParser();
                String nameCanonical = ecatParser.parseToCanonical(resName);
                name.setCanonicalName(nameCanonical);
                synonym.setTaxonomicStatus((String)synonymjs.get("taxonStatus"));

                taxonName.setRank((String) synonymjs.get("rank"));
                taxonName.setName(name);

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



}
