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

    public static Neo4jStore provideStoreFor(UpdatableStoreInfo storeClient) {
        if(!storemap.containsKey(storeClient.getClass())) {
            Neo4jStore neo4jStore;
            try {
                neo4jStore = new Neo4jStore();
                Neo4jStoreUpdater updater = new Neo4jStoreUpdater(neo4jStore, storeClient.getTestUrl());
                updater.addResources(storeClient.updatableResources());
                updater.watch(storeClient.pollIntervalMinutes());
            } catch (Exception e1) {
                throw new RuntimeException("Creation of Neo4jStore failed",  e1);
            }
            storemap.put(storeClient.getClass(), neo4jStore);
        }
        return storemap.get(storeClient.getClass());
    }

}