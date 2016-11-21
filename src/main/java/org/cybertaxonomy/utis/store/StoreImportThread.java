/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  a.kohlbecker
 * @date  Nov 18, 2016
 */
public class StoreImportThread extends Thread {

    protected static final Logger logger = LoggerFactory.getLogger(StoreImportThread.class);

    private static final int MAX_RETRIES = 3;

    private List<? extends URI> rdfFileUris;
    private Store store;

    public StoreImportThread(Store store, List<URI> rdfFileUris){
        this.rdfFileUris = rdfFileUris;
        this.store = store;
    }

    @Override
    public void run() {
        int i = 1;
        LinkedList<URI> rdfFileUriQueue = new LinkedList<>();
        rdfFileUriQueue.addAll(rdfFileUris);
        Map<URI, Integer> errorRdfFileRetries = new HashMap<>();
        while (!rdfFileUriQueue.isEmpty()) {
            logger.info("importing resource " + i++ + " of " + rdfFileUris.size());
            URI uri = rdfFileUriQueue.removeFirst();
            try {
                File localF = store.downloadAndExtract(uri);
                store.load(localF);
                localF.delete();
            } catch (Exception e) {
                Integer retryCount = errorRdfFileRetries.get(uri);
                if (retryCount == null) {
                    logger.error("Error while loading " + uri + " into store, re-enqueued for second attempt.");
                    rdfFileUriQueue.addLast(uri); // come back to this one
                    errorRdfFileRetries.put(uri, 0);
                }else if(retryCount < MAX_RETRIES) {
                    logger.error("Error while loading " + uri + " into store, re-enqueued for attempt " + (retryCount + 1) + "." );
                    rdfFileUriQueue.addLast(uri); // come back to this one
                    errorRdfFileRetries.put(uri, retryCount + 1);
                } else {
                    logger.error("Error while loading " + uri + " into store, giving up after " + MAX_RETRIES + " attempts.");
                }
            }
        }
        store.importFinished();
    }
}