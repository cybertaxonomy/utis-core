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
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GBIFBackboneClient extends AggregateChecklistClient {
	
	private static final String ID = "gbif";
	private static final String LABEL = "GBIF Checklist Bank";
	private static final String URL = "http://dev.gbif.org/wiki/display/POR/Webservice+API";
	private static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";
	public static final ChecklistInfo CINFO = new ChecklistInfo(ID,LABEL,URL,DATA_AGR_URL);

	public GBIFBackboneClient(List<String> checklistKeys) {
		super();		
	}
	
	@Override
	public HttpHost getHost() {
		// TODO Auto-generated method stub
		return new HttpHost("api.gbif.org",80);
	}

	
	@Override
	protected ChecklistInfo buildChecklistInfo() {
		ChecklistInfo checklistInfo = CINFO;
		int offset = 0;
		
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme("http");
		uriBuilder.setHost(getHost().getHostName());
		uriBuilder.setPath("/dataset/search");
		uriBuilder.setParameter("type", "CHECKLIST");
		uriBuilder.setParameter("limit", "20");
		uriBuilder.setParameter("offset", "0");
		URI uri;
		boolean endOfRecords = false;
		try {
			do {							
				uriBuilder.setParameter("offset", Integer.toString(offset));
				uri = uriBuilder.build();
				System.out.println("buildChecklistMap");
				String response = processRESTService(uri);

				JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(response);
				JSONArray results = (JSONArray) jsonResponse.get("results");
				Iterator<JSONObject> itrResults = results.iterator();
				while(itrResults.hasNext()) {
					JSONObject result = itrResults.next();
					String key = (String)result.get("key");
					String title = (String)result.get("title");
					String url =  "http://api.gbif.org/dataset/" + key;
					checklistInfo.addSubChecklist(new ChecklistInfo(key, title,  url));
				}
				
				endOfRecords = (Boolean) jsonResponse.get("endOfRecords");
				
				offset = offset + 20;
			} while(!endOfRecords);			
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
		//http://api.gbif.org/name_usage?q=Abies%20alba&datasetKey=fab88965-e69d-4491-a04d-e3198b626e52
		while(itrKeys.hasNext()) {
			ChecklistInfo checklistInfo = itrKeys.next();
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("datasetKey", checklistInfo.getId());					

			URI namesUri = buildUriFromQuery(query, "/name_usage/" + QUERY_PLACEHOLDER,	paramMap);

			String response = processRESTService(namesUri);

			updateQueryWithResponse(query,response, paramMap, checklistInfo);

			try {
				System.out.println(TnrMsgUtils.convertTnrMsgToXML(tnrMsg));
			} catch (TnrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getMaxPageSize() {		
		return 10;
	}
	
	private void updateQueryWithResponse(Query query , 
			String response, 
			Map<String, String> paramMap,
			ChecklistInfo ci) throws DRFChecklistException {

		JSONArray jsonArray = (JSONArray ) JSONUtils.parseJsonToArray(response);
		
		if(jsonArray != null) {
			String accTaxonId = "";
			Iterator<JSONObject> resIterator = jsonArray.iterator();
			while (resIterator.hasNext()) {
				JSONObject res = resIterator.next();
				Number acceptedKey = (Number)res.get("acceptedKey");
				boolean synonym = (Boolean)res.get("synonym");
				// case when accepted name
				if(!synonym && (acceptedKey == null)) {
					Long key = (Long)res.get("key");
					accTaxonId = key.toString();

				}
				// case when synonym
				if(synonym && (acceptedKey != null)) {
					Long key = (Long)res.get("acceptedKey");
					accTaxonId = key.toString();
				}	
			
				TnrResponse tnrResponse = new TnrResponse();
				
				tnrResponse.setChecklist(ci.getLabel());
				tnrResponse.setChecklistUrl(ci.getUrl());
				
				URI taxonUri = buildUriFromQuery(query, "/name_usage/" + accTaxonId, paramMap);
				String taxonResponse = processRESTService(taxonUri);
				
				JSONObject taxon = (JSONObject) JSONUtils.parseJsonToObject(taxonResponse);				
				AcceptedName accName = generateAccName(taxon);
				tnrResponse.setAcceptedName(accName);						
				if(query != null) {
					query.getTnrResponse().add(tnrResponse);
				}
				int offset = 0;
				paramMap.put("limit", "20");
				
				boolean endOfRecords = false;

				do {							
					paramMap.put("offset", Integer.toString(offset));
					
					URI synonymsUri = buildUriFromQuery(query, "/name_usage/" + accTaxonId + "/synonyms", paramMap);
					String synResponse = processRESTService(synonymsUri);
					
					JSONObject pagedSynonyms = (JSONObject) JSONUtils.parseJsonToObject(synResponse);				
					generateSynonyms(pagedSynonyms, tnrResponse);

					endOfRecords = (Boolean) pagedSynonyms.get("endOfRecords");
					
					offset = offset + 20;
				} while(!endOfRecords);									
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
	    String sourceDatasetID = "";
	    String sourceDatasetName = "";
	    String sourceName = "";

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
		c.setClazz((String) taxon.get("clazz"));
		c.setOrder((String) taxon.get("order"));
		c.setFamily((String) taxon.get("family"));
		c.setGenus((String) taxon.get("genus"));
		accName.setClassification(c);				
		
		return accName;
	}
	
	private void generateSynonyms(JSONObject pagedSynonyms, TnrResponse tnrResponse) {
		TnrResponse.Synonym synonym = new Synonym();
			
		JSONArray synonyms = (JSONArray)pagedSynonyms.get("results");
		Iterator<JSONObject> itrSynonyms = synonyms.iterator();
		while(itrSynonyms.hasNext()) {
			JSONObject synonymjs = (JSONObject) itrSynonyms.next();
			TaxonNameType taxonName = new TaxonNameType();
			NameType name = new NameType();
			
			String resName = (String) synonymjs.get("scientificName");
			name.setNameComplete(resName);
			
			name.setNameCanonical((String) synonymjs.get("canonicalName"));
			name.setNameStatus((String)synonymjs.get("taxonomicStatus"));
			
			taxonName.setRank((String) synonymjs.get("rank"));
			taxonName.setName(name);
			
			synonym.setTaxonName(taxonName);
						
			//FIXME : To fill in		
			String sourceUrl = "";
		    String sourceDatasetID =  "";
		    String sourceDatasetName = "";
		    String sourceName = "";

		    SourceType source = new SourceType();
		    source.setDatasetID(sourceDatasetID);
		    source.setDatasetName(sourceDatasetName);
		    source.setName(sourceName);
		    source.setUrl(sourceUrl);
		    synonym.setSource(source);
		    
		    //FIXME : To fill in					
		    String accordingTo = "";            
		    String modified = "";            
		    
		    ScrutinyType scrutiny = new ScrutinyType();	    
			scrutiny.setAccordingTo(accordingTo);
			scrutiny.setModified(modified);
			synonym.setScrutiny(scrutiny);
			
			tnrResponse.getSynonym().add(synonym);
		}
	}



	
}
