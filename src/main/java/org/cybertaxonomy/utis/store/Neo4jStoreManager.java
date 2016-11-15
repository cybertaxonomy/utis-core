/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

import java.util.HashMap;
import java.util.Map;

import org.cybertaxonomy.utis.checklist.UpdatableStoreInfo;

/**
 * @author a.kohlbecker
 * @date Oct 29, 2015
 *
 */
public class Neo4jStoreManager {

    private final static Map<Class<? extends UpdatableStoreInfo>, Neo4jStore> storemap = new HashMap<Class<? extends UpdatableStoreInfo>, Neo4jStore>();

    /**
     * Flag to disable the automatic store update for special testing purposes.
     * If the testmode is enabled the store will be cleared.
     *
     */
    public static boolean testMode = false;

    /**
     * Only used when <code>testMode = true</code>
     */
    public static Neo4jStoreUpdater lastStoreUpdater = null;

    public static Neo4jStore provideStoreFor(UpdatableStoreInfo storeInfo) {
        if(!storemap.containsKey(storeInfo.getClass())) {
            Neo4jStore neo4jStore;
            try {
                neo4jStore = new Neo4jStore(storeInfo.getInstanceName());
                Neo4jStoreUpdater updater = new Neo4jStoreUpdater(neo4jStore, storeInfo.getTestUrl());
                updater.setIncrementalUpdate(storeInfo.doIncrementalUpdates());
                updater.setLastModifiedProvider(storeInfo.getLastModifiedProvider());
                updater.setResourceProvider(storeInfo.getResourceProvider());

                // updater is prepared, start watching
                if(testMode){
                    neo4jStore.stopStoreEngine();
                    neo4jStore.clear();
                    neo4jStore.initStoreEngine();
                    lastStoreUpdater = updater;
                } else {
                    updater.watch(storeInfo.pollIntervalMinutes());
                }

            } catch (Exception e1) {
                throw new RuntimeException("Creation of Neo4jStore failed",  e1);
            }
            storemap.put(storeInfo.getClass(), neo4jStore);
        }
        return storemap.get(storeInfo.getClass());
    }

}
