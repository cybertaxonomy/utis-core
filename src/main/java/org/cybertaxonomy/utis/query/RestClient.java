/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package org.cybertaxonomy.utis.query;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.checklist.SearchMode;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Query.Request;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Sep 30, 2015
 *
 */
public class RestClient implements IQueryClient{

    protected Logger logger = LoggerFactory.getLogger(RestClient.class);

    public final static String QUERY_PLACEHOLDER = "{q}";

    private final HttpHost httpHost;

    public HttpHost getHost() {
        return this.httpHost;
    }

    public RestClient(HttpHost httpHost) {
        this.httpHost = httpHost;
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
    public String get(URI uri) throws DRFChecklistException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);

        try {
            logger.debug(">> Request URI: " + request.getRequestLine().getUri());
            HttpResponse response = client.execute(request);

            String responseBody = EntityUtils.toString(response.getEntity(),Charset.forName("UTF-8"));
            logger.debug("<< Response: " + response.getStatusLine());
            logger.trace(responseBody);
            logger.trace("==============");

            return responseBody;

        } catch (IOException e) {
            throw new DRFChecklistException("Error on http get request for " + uri, e);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                /* IGNORE */
            }
        }
    }

    /**
     *
     * @param queryList
     * @param endpointSuffix
     * @param queryKey
     * @param likeModeWildcard the wildcard to add to the query string in case of like search modes
     * @param paramMap
     * @return
     */
    public URI buildUriFromQueryList(List<Query> queryList,
            String endpointSuffix,
            String queryKey,
            String likeModeWildcard,
            Map<String, String> paramMap) {

        List<String> queries = new ArrayList<String>();

        EnumSet<SearchMode> likeModes = EnumSet.of(SearchMode.scientificNameLike);

        for(Query query : queryList) {
            Request tnrRequest = query.getRequest();
            String queryString = tnrRequest.getQueryString();
            if(likeModes.contains(TnrMsgUtils.utisActionFrom(tnrRequest.getSearchMode()))){
                queryString += likeModeWildcard;
            }
            queries.add(queryString);
        }

        logger.debug("Query size : " + queries.size());

        return buildUriFromQueryStringList(queries,
                endpointSuffix,
                queryKey,
                paramMap);
    }

    public URI buildUriFromQuery(Query query,
            String endpointSuffix,
            String queryKey,
            Map<String, String> paramMap) {
        return buildUriFromQueryString(query.getRequest().getQueryString(),
                endpointSuffix,
                queryKey,
                paramMap);
    }

    public URI buildUriFromQuery(Query query,
            String regexpUrl,
            Map<String, String> paramMap) {
        String url = regexpUrl.replace(QUERY_PLACEHOLDER, query.getRequest().getQueryString());
        return buildUriFromQueryString(url, paramMap);
    }

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

    public URI buildURI(String endpointSuffix, Map<String, String> query) {

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(getHost().getSchemeName());
        uriBuilder.setHost(getHost().getHostName());
        uriBuilder.setPort(getHost().getPort());
        uriBuilder.setPath(endpointSuffix);

        if(query != null) {
            for(String key : query.keySet()) {
                uriBuilder.addParameter(key, query.get(key));
            }
        }

        URI uri = null;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        return uri;

    }



}
