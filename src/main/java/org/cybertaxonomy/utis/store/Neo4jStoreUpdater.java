// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Oct 28, 2015
 *
 */
public class Neo4jStoreUpdater {

    protected Logger logger = LoggerFactory.getLogger(Neo4jStoreUpdater.class);

    private final URI testUrl;
    private final Neo4jStore store;
    private long interval_ms;

    private String[] resources = new String[0];

    public Neo4jStoreUpdater(Neo4jStore store, String testUrl) throws URISyntaxException {
        this.testUrl = new URI(testUrl);
        this.store = store;
    }

    /**
     * @param lastModified
     * @return
     */
    private Date getRemoteLastModified() {

        Date lastModified = null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpHead request = new HttpHead(testUrl);
        try {
            HttpResponse response = client.execute(request);
            Header lastModifiedH = response.getFirstHeader("Last-Modified");
            lastModified = DateUtil.parseDate(lastModifiedH.getValue());
            logger.debug("Last-Modified: " + lastModifiedH.getValue());

        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException, if this problem persists it will block from updating neo4jStore", e);
        } catch (DateParseException e) {
            logger.error("Could not parse Last-Modified value from HTTP response, if this problem persists it will block from updating neo4jStore");
        } catch (IOException e) {
            logger.error("IOException, if this problem persists it will block from updating the neo4jStore", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                // IGNORE //
            }
        }
        return lastModified;
    }

    private Date checkNewerVersion() {

        logger.info("polling for updates at " + testUrl.toString());
        Date lastModified = getRemoteLastModified();
        if(store.getLastModified() == null || lastModified != null && lastModified.after(store.getLastModified())) {
            logger.info("remote resource is more recent:  " + DateUtil.formatDate(lastModified));
            return lastModified;
        }
        return null;
    }

    public void addResources(String ... resources) throws URISyntaxException {
       this.resources = resources;
    }

    public void watch(int intervalMinutes) {

        if(isRunningAsTest()) {
            updateIfNeeded();
        } else {
            this.interval_ms = 1000 * 60 * intervalMinutes;
            Thread updateThread = new Thread() {

                @Override
                public void run() {
                    updateIfNeeded();
                    try {
                        sleep(interval_ms);
                    } catch (InterruptedException e) {
                        logger.info("Neo4jStoreUpdater has been interrupted");
                    }
                }
            };
            updateThread.start();
        }


    }


    /**
     * @return
     */
    private boolean isRunningAsTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if(stackTraceElement.getClassName().startsWith("org.junit.runners")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param neo4jStore
     */
    public void updateStore(Date lastModified) {

        logger.info("Starting store update");

        try {
            store.loadIntoStore(resources);
            store.setLastModified(lastModified);
        } catch (Exception e) {
            throw new RuntimeException("Loading "
                    + Arrays.toString(resources) +
                    " into Neo4jStore failed",  e);
        }


        logger.info("Store update done.");
    }

    /**
     *
     */
    private void updateIfNeeded() {
        Date lastModified = checkNewerVersion();
        if(lastModified != null) {
            updateStore(lastModified);
        }
    }

}
