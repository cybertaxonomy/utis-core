package org.cybertaxonomy.utis.occurrences;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.RestClient;
import org.cybertaxonomy.utis.utils.CSVUtils;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GBIFOccurrencesClient extends BaseOccurrencesClient<RestClient> {


    /**
     *
     */
    private static final HttpHost HTTP_HOST = new HttpHost("api.gbif.org",80);
    public static final String ID = "gbif";
    public static final String LABEL = "GBIF Occurrence Bank";
    public static final String URL = "http://uat.gbif.org/developer/species";
    public static final String DATA_AGR_URL = "http://data.gbif.org/tutorial/datauseagreement";
    // in v0.9 the max limit is 300
    private static final String MAX_PAGING_LIMIT = "300";
    private static final String VERSION = "v0.9";
    private static final ServiceProviderInfo CINFO = new ServiceProviderInfo(ID,LABEL,ServiceProviderInfo.DEFAULT_SEARCH_MODE,URL,DATA_AGR_URL, VERSION);

    private final Map<String, JSONObject> datasetCacheMap = new HashMap<String, JSONObject>();
    private final Map<String, JSONObject> orgCacheMap = new HashMap<String, JSONObject>();
    public final static List<String> nameidList = new ArrayList<String>();


    /**
     * {@inheritDoc}
     */
    @Override
    public void initQueryClient() {
        queryClient = new RestClient(HTTP_HOST);

    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo ocbankInfo = CINFO;
        return ocbankInfo;
    }

    @Override
    public String getOccurrences(String nameid) throws DRFChecklistException {

        URI namesUri = queryClient.buildUriFromQueryString(nameid, "/" + CINFO.getVersion() + "/species/match", "name", null);
        String nameResponse = queryClient.get(namesUri);
        JSONObject nameJsonResponse = JSONUtils.parseJsonToObject(nameResponse);
        StringBuilder occurrences = new StringBuilder();
        if(nameJsonResponse.get("usageKey") != null) {
            String usageKey = String.valueOf(nameJsonResponse.get("usageKey"));

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
                    URI occUri = queryClient.buildUriFromQueryString(usageKey, "/" + CINFO.getVersion() + "/occurrence/search", "taxonKey", paramMap);

                    String occResponse = queryClient.get(occUri);

                    JSONObject jsonOccResponse = JSONUtils.parseJsonToObject(occResponse);
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
                                occurrences.append(CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("key"))));
                            }
                            occurrences.append(",");

                            if(jsonOccurence.get("decimalLatitude") != null) {
                                occurrences.append(CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("decimalLatitude"))));
                            }
                            occurrences.append(",");

                            if(jsonOccurence.get("decimalLongitude") != null) {
                                occurrences.append(CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("decimalLongitude"))));
                            }
                            occurrences.append(",");

                            String formattedDate = "";
                            SimpleDateFormat gbifFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                            SimpleDateFormat biovelFormatter = new SimpleDateFormat("yyyy-MM-dd");

                            if(jsonOccurence.get("occurrenceDate") != null) {
                                String strDate = (String) jsonOccurence.get("occurrenceDate");
                                try {
                                    Date date = gbifFormatter.parse(strDate);
                                    formattedDate = biovelFormatter.format(date);
                                } catch (ParseException e) {
                                    formattedDate = "";
                                }
                            } else if((jsonOccurence.get("year") != null) && (jsonOccurence.get("month") != null) && (jsonOccurence.get("day") != null)) {
                                String year = String.valueOf(jsonOccurence.get("year"));
                                String month = String.valueOf(jsonOccurence.get("month"));
                                String day = String.valueOf(jsonOccurence.get("day"));
                                formattedDate = year + "-" + month + "-" + day;
                                System.out.println("date : " + formattedDate);
                            }
                            occurrences.append(CSVUtils.wrapWhenComma(formattedDate));
                            occurrences.append(",");


                            //if(jsonOccurence.get("occurrenceDate") != null) {
                                occurrences.append(CSVUtils.wrapWhenComma(formattedDate));
                            //}
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
                                occurrences.append(CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("depth"))));
                            }
                            occurrences.append(",");

                            if(jsonOccurence.get("altitude") != null) {
                                occurrences.append(CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("altitude"))));
                            }
                            occurrences.append(",");

                            if(jsonOccurence.get("depth") != null) {
                                String depth = CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("depth")));
                                occurrences.append(depth);
                                System.out.println("depth : " + depth);
                            }
                            occurrences.append(",");

                            if(jsonOccurence.get("altitude") != null) {
                                occurrences.append(CSVUtils.wrapWhenComma(String.valueOf(jsonOccurence.get("altitude"))));
                            }
                            occurrences.append(",");

                            occurrences.append(",");

                            JSONObject datasetJsonResponse = null;
                            if(jsonOccurence.get("datasetKey") != null) {
                                String datasetKey = (String) jsonOccurence.get("datasetKey");
                                datasetJsonResponse = datasetCacheMap.get(datasetKey);
                                if(datasetJsonResponse == null) {
                                    URI datasetUri = queryClient.buildUriFromQueryString("/" + CINFO.getVersion() + "/dataset/" + datasetKey, null);
                                    String datasetResponse = queryClient.get(datasetUri);
                                    datasetJsonResponse = JSONUtils.parseJsonToObject(datasetResponse);
                                    datasetCacheMap.put(datasetKey, datasetJsonResponse);
                                }
                            }

                            JSONObject orgJsonResponse = null;
                            if(datasetJsonResponse != null && datasetJsonResponse.get("owningOrganizationKey") != null) {
                                String owningOrganizationKey = (String) datasetJsonResponse.get("owningOrganizationKey");
                                orgJsonResponse = orgCacheMap.get(owningOrganizationKey);
                                if(orgJsonResponse == null) {
                                    URI orgUri = queryClient.buildUriFromQueryString("/" + CINFO.getVersion() + "/organization/" + owningOrganizationKey, null);
                                    String orgResponse = queryClient.get(orgUri);
                                    orgJsonResponse = JSONUtils.parseJsonToObject(orgResponse);
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
                                occurrences.append(CSVUtils.wrapWhenComma(((String) datasetJsonResponse.get("rights")).replaceAll("\r\n|\r|\n", " ")));
                                //System.out.println("rights : " + CSVUtils.wrapWhenComma(((String) datasetJsonResponse.get("rights")).replaceAll("\r\n|\r|\n", " ")));
                            }
                            occurrences.append(",");

                            if(datasetJsonResponse != null && datasetJsonResponse.get("citation") != null) {
                                JSONObject citationJson = (JSONObject) datasetJsonResponse.get("citation");
                                if(citationJson.get("text") != null) {
                                    occurrences.append(CSVUtils.wrapWhenComma(((String) citationJson.get("text")).replaceAll("\r\n|\r|\n", " ")));

                                }
                            }
                            occurrences.append(System.getProperty("line.separator"));
                            count++;

                        }
                    }
                    endOfRecords = (Boolean) jsonOccResponse.get("endOfRecords");
                    System.out.println("usageKey : " + usageKey + ", count : " + String.valueOf(jsonOccResponse.get("count")) + ", offset : " + offset + ",  + occ count : " + count);
                    offset = offset + Integer.parseInt(MAX_PAGING_LIMIT);
                } while(!endOfRecords);
                System.out.println("occ count : " + count);
            }
        }
        return occurrences.toString();
    }

}
