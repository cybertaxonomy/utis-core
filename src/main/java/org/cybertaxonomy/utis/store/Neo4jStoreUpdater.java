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
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cybertaxonomy.utis.checklist.DRFChecklistException;
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
    private boolean incrementalUpdate = false;


    private LastModifiedProvider lastModifiedProvider;

    private ResourceProvider resourceProvider;

    /**
     * @return the lastModifiedProvider
     */
    public LastModifiedProvider getLastModifiedProvider() {
        return lastModifiedProvider;
    }

    /**
     * @param lastModifiedProvider the lastModifiedProvider to set
     */
    public void setLastModifiedProvider(LastModifiedProvider lastModifiedProvider) {
        this.lastModifiedProvider = lastModifiedProvider;
    }

    /**
     * @param resourceProvider
     */
    public void setResourceProvider(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;

    }

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
        if(lastModifiedProvider != null){
            try {
                return lastModifiedProvider.getLastModified();
            } catch (DRFChecklistException e) {
                logger.error("Error in LastModifiedProvider, if this problem persists it will block from updating neo4jStore", e);
            }
        } else {

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

    public void watch(int intervalMinutes) {

        if (isRunningAsTest()) {
            updateIfNeeded();
        } else {
            this.interval_ms = 1000 * 60 * intervalMinutes;

            Thread updateThread = new Thread() {

                @Override
                public void run() {
                    boolean interrupted = false;
                    while (!interrupted) {
                        updateIfNeeded();
                        try {
                            sleep(interval_ms);
                        } catch (InterruptedException e) {
                            logger.info("Neo4jStoreUpdater has been interrupted");
                            interrupted = true;
                        }
                    }
                }
            };
            updateThread.setName(Neo4jStoreUpdater.class.getSimpleName());
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
     *
     * @param newLastModified The new last modified time stamp to set for the store
     */
    public void updateStore(Date newLastModified) {

        logger.info("Starting store update");

        try {
            List<URI> resources = resourceProvider.getResources(store.getLastModified());
            store.loadIntoStore(resources, !isIncrementalUpdate());
            store.setLastModified(newLastModified);
        } catch (Exception e) {
            throw new RuntimeException("Loading resources into Neo4jStore failed", e);
        }


        logger.info("Store update done.");
    }

    /**
     *
     */
    public void updateIfNeeded() {
        Date lastModified = checkNewerVersion();
        if(lastModified != null) {
            updateStore(lastModified);
        }
    }

    /**
     * @return the incrementalUpdate
     */
    public boolean isIncrementalUpdate() {
        return incrementalUpdate;
    }

    /**
     * @param incrementalUpdate the incrementalUpdate to set
     */
    public void setIncrementalUpdate(boolean incrementalUpdate) {
        this.incrementalUpdate = incrementalUpdate;
    }

}
