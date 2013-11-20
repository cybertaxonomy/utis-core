package org.bgbm.biovel.drf.occurrences;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.rest.TaxoRESTClient;
import org.bgbm.biovel.drf.rest.TaxoRESTClient.ServiceProviderInfo;
import org.bgbm.biovel.drf.utils.CSVUtils;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GBIFOccurrencesClient extends BaseOccurrencesClient {


	public static final String ID = "gbif";
	public static final String LABEL = "GBIF Checklist Bank";
	public static final String URL = "http://uat.gbif.org/developer/species";
	public static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";
	private static final String MAX_PAGING_LIMIT = "1000";
	private static final String VERSION = "v0.9";
	private static final ServiceProviderInfo CINFO = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL,VERSION,false);

	private final Map<String, JSONObject> datasetCacheMap = new HashMap<String, JSONObject>();
	private final Map<String, JSONObject> orgCacheMap = new HashMap<String, JSONObject>();
	public final static List<String> nameidList = new ArrayList<String>();

	@Override
	public HttpHost getHost() {
		// TODO Auto-generated method stub
		return new HttpHost("api.gbif.org",80);
	}

	@Override
	public int getMaxPageSize() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	protected ServiceProviderInfo buildServiceProviderInfo() {
		ServiceProviderInfo ocbankInfo = CINFO;
		setChecklistInfo(ocbankInfo);
		return ocbankInfo;
	}

	@Override
	public String getOccurrences(String nameid) throws DRFChecklistException {

		URI namesUri = buildUriFromQueryString(nameid, "/" + CINFO.getVersion() + "/species/match", "name", null);
		String nameResponse = processRESTService(namesUri);
		JSONObject nameJsonResponse = (JSONObject) JSONUtils.parseJsonToObject(nameResponse);
		StringBuilder occurrences = new StringBuilder();
		if(nameJsonResponse.get("usageKey") != null) {
			String usageKey = Long.toString((Long) nameJsonResponse.get("usageKey"));

			if(!nameidList.contains(usageKey)) {
				nameidList.add(usageKey);
				//http://api.gbif.org/v0.9/occurrence/search?offset=100&limit=100&taxonKey=2818622
				Map<String, String> paramMap = new HashMap<String, String>();			
				paramMap.put("limit", MAX_PAGING_LIMIT);		
				boolean endOfRecords = false;
				int offset = 0;


				int count = 0;
				do {							
					paramMap.put("offset", Integer.toString(offset));
					URI occUri = buildUriFromQueryString(usageKey, "/" + CINFO.getVersion() + "/occurrence/search", "taxonKey", paramMap);

					String occResponse = processRESTService(occUri);			

					JSONObject jsonOccResponse = (JSONObject) JSONUtils.parseJsonToObject(occResponse);
					JSONArray results = (JSONArray) jsonOccResponse.get("results");		
					System.out.println("actual results size : " + results.size());
					if(results != null) {				
						Iterator<JSONObject> resIterator = results.iterator();

						while (resIterator.hasNext()) {
							JSONObject jsonOccurence = resIterator.next();					
							occurrences.append(",");

							if(jsonOccurence.get("genus") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("genus")));
							} 
							occurrences.append(",");


							occurrences.append(",");


							occurrences.append(",");


							occurrences.append(",");

							if(jsonOccurence.get("scientificName") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("scientificName")));
							} 
							occurrences.append(",");

							occurrences.append(",");

							if(jsonOccurence.get("scientificName") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("scientificName"))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("key") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Long.toString((Long) jsonOccurence.get("key")))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("latitude") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Double.toString((Double) jsonOccurence.get("latitude")))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("longitude") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Double.toString((Double) jsonOccurence.get("longitude")))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("occurrencesDate") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("occurrencesDate"))); 
							} 
							occurrences.append(",");


							if(jsonOccurence.get("occurrencesDate") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("occurrencesDate"))); 
							} 
							occurrences.append(",");


							if(jsonOccurence.get("coordinateAccurracyInMeters") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Integer.toString((Integer) jsonOccurence.get("coordinateAccurracyInMeters"))));
							} 
							occurrences.append(",");

							if(jsonOccurence.get("country") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("country")));
							} 
							occurrences.append(",");

							if(jsonOccurence.get("collectorName") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("collectorName"))); 
							} 
							occurrences.append(",");

							occurrences.append(",");

							if(jsonOccurence.get("locality") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) jsonOccurence.get("locality"))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("depth") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Long.toString((Long) jsonOccurence.get("depth")))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("altitude") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Long.toString((Long) jsonOccurence.get("altitude")))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("depth") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Long.toString((Long) jsonOccurence.get("depth")))); 
							} 
							occurrences.append(",");

							if(jsonOccurence.get("altitude") != null) {
								occurrences.append(CSVUtils.wrapWhenComma(Long.toString((Long) jsonOccurence.get("altitude"))));  
							} 
							occurrences.append(",");

							occurrences.append(",");

							JSONObject datasetJsonResponse = null;
							if(jsonOccurence.get("datasetKey") != null) {
								String datasetKey = (String) jsonOccurence.get("datasetKey"); 
								datasetJsonResponse = datasetCacheMap.get(datasetKey);
								if(datasetJsonResponse == null) {
									URI datasetUri = buildUriFromQueryString("/" + CINFO.getVersion() + "/dataset/" + datasetKey, null);
									String datasetResponse = processRESTService(datasetUri);
									datasetJsonResponse = (JSONObject) JSONUtils.parseJsonToObject(datasetResponse);
									datasetCacheMap.put(datasetKey, datasetJsonResponse);
								}
							}

							JSONObject orgJsonResponse = null;
							if(datasetJsonResponse != null && datasetJsonResponse.get("owningOrganizationKey") != null) {
								String owningOrganizationKey = (String) datasetJsonResponse.get("owningOrganizationKey");						
								orgJsonResponse = orgCacheMap.get(owningOrganizationKey);
								if(orgJsonResponse == null) {
									URI orgUri = buildUriFromQueryString("/" + CINFO.getVersion() + "/organization/" + owningOrganizationKey, null);
									String orgResponse = processRESTService(orgUri);
									orgJsonResponse = (JSONObject) JSONUtils.parseJsonToObject(orgResponse);
									orgCacheMap.put(owningOrganizationKey, orgJsonResponse);
								}
								if(orgJsonResponse != null && orgJsonResponse.get("title") != null) {
									occurrences.append(CSVUtils.wrapWhenComma((String) orgJsonResponse.get("title"))); 
								}

							} 
							occurrences.append(",");

							if(datasetJsonResponse != null && datasetJsonResponse.get("title") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) datasetJsonResponse.get("title"))); 
							}
							occurrences.append(",");

							if(datasetJsonResponse != null && datasetJsonResponse.get("rights") != null) {
								occurrences.append(CSVUtils.wrapWhenComma((String) datasetJsonResponse.get("rights"))); 
							}
							occurrences.append(",");

							if(datasetJsonResponse != null && datasetJsonResponse.get("citation") != null) {
								JSONObject citationJson = (JSONObject) datasetJsonResponse.get("citation");
								if(citationJson.get("text") != null) {
									occurrences.append(CSVUtils.wrapWhenComma((String) citationJson.get("text"))); 
								}						
							}																				
							occurrences.append(System.getProperty("line.separator"));
							count++;

						}
					}
					endOfRecords = (Boolean) jsonOccResponse.get("endOfRecords");
					System.out.println("usageKey : " + usageKey + ", count : " + Long.toString((Long) jsonOccResponse.get("count")) + ", offset : " + offset + ",  + occ count : " + count);
					offset = offset + Integer.parseInt(MAX_PAGING_LIMIT);
				} while(!endOfRecords);	
				System.out.println("occ count : " + count);
			}
		}
		return occurrences.toString();
	}




}
