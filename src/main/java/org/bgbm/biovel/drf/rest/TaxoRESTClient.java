package org.bgbm.biovel.drf.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.RESTURIBuilder;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TaxoRESTClient {

    protected Logger logger = LoggerFactory.getLogger(TaxoRESTClient.class);

    private ServiceProviderInfo spInfo;

    public TaxoRESTClient() {
        spInfo = buildServiceProviderInfo();
    }

    public TaxoRESTClient(String checklistInfoJson) throws DRFChecklistException {
        setChecklistInfo(JSONUtils.convertJsonToObject(checklistInfoJson, ServiceProviderInfo.class));
    }

    public TaxoRESTClient(ServiceProviderInfo spInfo) throws DRFChecklistException {
        setChecklistInfo(spInfo);
    }

    public abstract ServiceProviderInfo buildServiceProviderInfo();

    public ServiceProviderInfo getServiceProviderInfo() {
        return spInfo;
    }

    public String getChecklistInfoAsJson() throws DRFChecklistException {

        if(getServiceProviderInfo() != null) {
            return JSONUtils.convertObjectToJson(spInfo);
        }
        return null;
    }

    public void setChecklistInfo(ServiceProviderInfo checklistInfo) {
        this.spInfo = checklistInfo;
    }

    /**
     * Sends a GET request to the REST service specified by the <code>uri</code>
     * parameter and returns the response body as <code>String</code>
     *
     * @param uri
     *            the REST service uri to send a GET request to
     * @return the response body
     * @throws DRFChecklistException
     */
    public String processRESTService(URI uri) throws DRFChecklistException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(uri);

        try {
            logger.debug(">> Request URI: " + request.getRequestLine().getUri());
            HttpResponse response = client.execute(request);

            String responseBody = EntityUtils.toString(response.getEntity(),Charset.forName("UTF-8"));
            logger.debug("<< Response: " + response.getStatusLine());
            logger.trace(responseBody);
            logger.trace("==============");

            return responseBody;

        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
//            throw new DRFChecklistException(e1);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DRFChecklistException(e);
        }
        return null;
    }

    public abstract HttpHost getHost();

    public abstract int getMaxPageSize();


    public URI buildUriFromQueryStringList(List<String> queryList,
            String endpointSuffix,
            String queryKey,
            Map<String, String> paramMap) {

        RESTURIBuilder builder = new RESTURIBuilder(getHost().getHostName(),
                    getHost().getPort(),
                    endpointSuffix,
                    queryKey,
                    paramMap);

        URI uri = null;
        Iterator<String> itrQuery = queryList.iterator();
        while(itrQuery.hasNext()) {
            builder.addQuery(itrQuery.next());
        }

        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        return uri;
    }

    public URI buildUriFromQueryString(String query,
            String endpointSuffix,
            String queryKey,
            Map<String, String> paramMap) {

        RESTURIBuilder builder = new RESTURIBuilder(getHost().getHostName(),
                    getHost().getPort(),
                    endpointSuffix,
                    queryKey,
                    paramMap);

        URI uri = null;

        builder.addQuery(query);

        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        return uri;
    }

    public URI buildUriFromQueryString(String endpointUrl,
            Map<String, String> paramMap) {

        RESTURIBuilder builder = new RESTURIBuilder(getHost().getHostName(), getHost().getPort(),endpointUrl, paramMap);

        URI uri = null;

        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        return uri;
    }

}
