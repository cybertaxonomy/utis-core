package org.bgbm.biovel.drf.checklist;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.bgbm.biovel.drf.checklist.BaseChecklistClient.ChecklistInfo;
import org.bgbm.biovel.drf.tnr.msg.AcceptedName;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.ScrutinyType;
import org.bgbm.biovel.drf.tnr.msg.SourceType;
import org.bgbm.biovel.drf.tnr.msg.TaxonNameType;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse.Synonym;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GBIFBetaBackboneClient extends AggregateChecklistClient {

	public static final String ID = "gbif";
	public static final String LABEL = "GBIF Checklist Bank";
	public static final String URL = "http://ecat-dev.gbif.org/";
	public static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";
	public static final ChecklistInfo CINFO = new ChecklistInfo(ID,LABEL,URL,DATA_AGR_URL);
	
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
	public ChecklistInfo buildChecklistInfo()  {
		ChecklistInfo checklistInfo = CINFO;

		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme("http");
		uriBuilder.setHost(getHost().getHostName());
		uriBuilder.setPath("/ws/checklist");
		URI uri;

		try {						
			uri = uriBuilder.build();
			System.out.println("buildChecklistMap");
			String response = processRESTService(uri);

			JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(response);
			JSONArray data = (JSONArray) jsonResponse.get("data");
			Iterator<JSONObject> itrResults = data.iterator();
			while(itrResults.hasNext()) {
				JSONObject result = itrResults.next();
				String key = ((Long)result.get("datasetID")).toString();
				String title = (String)result.get("title");
				String url =  "http://ecat-dev.gbif.org/checklist/" + key;
				checklistInfo.addSubChecklist(new ChecklistInfo(key, title,  url, DATA_AGR_URL));
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
	public void resolveNames(TnrMsg tnrMsg) throws DRFChecklistException {
		List<TnrMsg.Query> queryList = tnrMsg.getQuery();
		if(queryList.size() ==  0) {
			throw new DRFChecklistException("GBIF query list is empty");
		}

		if(queryList.size() > 1) {
			throw new DRFChecklistException("GBIF query list has more than one query");
		}
		Query query = queryList.get(0);
		Iterator<ChecklistInfo> itrKeys = getChecklistInfo().getSubChecklists().iterator();
		//http://ecat-dev.gbif.org/ws/usage/?rkey={datasetID}&q={sciName}&pagesize=100&searchType=canonical
		while(itrKeys.hasNext()) {
			ChecklistInfo checklistInfo = itrKeys.next();
			if(checklistInfo.getUse()) {
				Map<String, String> paramMap = new HashMap<String, String>();
				paramMap.put("rKey", checklistInfo.getId());		
				paramMap.put("pageSize", "100");
				paramMap.put("searchType", "canonical");		

				URI namesUri = buildUriFromQuery(query,
						"/ws/usage",									
						"q",
						paramMap);

				String response = processRESTService(namesUri);

				updateQueryWithResponse(query,response, paramMap, checklistInfo);
			}
		}
	}

	@Override
	public int getMaxPageSize() {		
		return 10;
	}
	
	private void updateQueryWithResponse(Query query, 
			String response, 
			Map<String, String> paramMap,
			ChecklistInfo checklistInfo) throws DRFChecklistException {
	

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

			TnrResponse tnrResponse = new TnrResponse();
			
			tnrResponse.setChecklist(checklistInfo.getLabel());
			tnrResponse.setChecklistUrl(checklistInfo.getUrl());

			URI taxonUri = buildUriFromQuery(query, "/ws/usage/" + taxonId, null);
			String taxonResponse = processRESTService(taxonUri);

			JSONObject res = (JSONObject) JSONUtils.parseJsonToObject(taxonResponse);	
			JSONObject jsonAccName = (JSONObject)res.get("data");
			AcceptedName accName = generateAccName(jsonAccName);
			tnrResponse.setAcceptedName(accName);						
			if(query != null) {
				query.getTnrResponse().add(tnrResponse);
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
	
	private AcceptedName generateAccName(JSONObject taxon) {
		AcceptedName accName = new AcceptedName();
		TaxonNameType taxonName = new TaxonNameType();
		NameType name = new NameType();
		
		String resName = (String) taxon.get("scientificName");
		name.setNameComplete(resName);

		name.setNameCanonical((String) taxon.get("canonicalName"));
		name.setNameStatus((String)taxon.get("taxonomicStatus"));
		
		taxonName.setRank((String) taxon.get("rank"));
		taxonName.setName(name);
		
		accName.setTaxonName(taxonName);
		
		//FIXME : To fill in		
		String sourceUrl = "";
		Number datasetIDNumber = (Number)taxon.get("datasetID");		
	    String sourceDatasetID = datasetIDNumber.toString();
	   
	    String sourceDatasetName = (String)taxon.get("datasetName");		
	    String sourceName = (String)taxon.get("accordingTo");		;

	    SourceType source = new SourceType();
	    source.setDatasetID(sourceDatasetID);
	    source.setDatasetName(sourceDatasetName);
	    source.setName(sourceName);
	    source.setUrl(sourceUrl);
	    accName.setSource(source);
	    
	    //FIXME : To fill in		
	    String accordingTo = "";            
	    String modified = "";            
	    
	    ScrutinyType scrutiny = new ScrutinyType();	    
		scrutiny.setAccordingTo(accordingTo);
		scrutiny.setModified(modified);
		accName.setScrutiny(scrutiny);

		AcceptedName.Classification c = new AcceptedName.Classification();
		c.setKingdom((String) taxon.get("kingdom"));
		c.setPhylum((String) taxon.get("phylum"));
		c.setClazz((String) taxon.get("class"));
		c.setOrder((String) taxon.get("order"));
		c.setFamily((String) taxon.get("family"));
		c.setGenus((String) taxon.get("genus"));
		accName.setClassification(c);				
		
		return accName;
	}
	
	private void generateSynonyms(JSONObject synonym, TnrResponse tnrResponse) {
		TnrResponse.Synonym syn = new Synonym();
					
		TaxonNameType taxonName = new TaxonNameType();
		NameType name = new NameType();
		
		String resName = (String) synonym.get("scientificName");
		name.setNameComplete(resName);

		name.setNameCanonical((String) synonym.get("canonicalName"));
		name.setNameStatus((String) synonym.get("taxonomicStatus"));
		
		taxonName.setRank((String) synonym.get("rank"));
		taxonName.setName(name);
		
		syn.setTaxonName(taxonName);
		
		//FIXME : To fill in		
		String sourceUrl = "";
		Number datasetIDNumber = (Number) synonym.get("datasetID");		
	    String sourceDatasetID = datasetIDNumber.toString();
	   
	    String sourceDatasetName = (String) synonym.get("datasetName");		
	    String sourceName = (String) synonym.get("accordingTo");		;

	    SourceType source = new SourceType();
	    source.setDatasetID(sourceDatasetID);
	    source.setDatasetName(sourceDatasetName);
	    source.setName(sourceName);
	    source.setUrl(sourceUrl);
	    syn.setSource(source);
	    
	    //FIXME : To fill in		
	    String accordingTo = "";            
	    String modified = "";            
	    
	    ScrutinyType scrutiny = new ScrutinyType();	    
		scrutiny.setAccordingTo(accordingTo);
		scrutiny.setModified(modified);
		syn.setScrutiny(scrutiny);
		
		tnrResponse.getSynonym().add(syn);
	}


	
}

