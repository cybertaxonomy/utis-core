// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.client;

import org.cybertaxonomy.utis.store.Neo4jStore;
import org.cybertaxonomy.utis.store.StaticArchiveResourceProvider;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @date Oct 5, 2015
 *
 */
public class StoreTests {

    private static final String RDF_FILE_URL = "http://localhost/download/taxonomy.rdf.gz"; // http://eunis.eea.europa.eu/rdf/species.rdf.gz

//    @Test
//    @Ignore
//    public void tDBStore_RdfGzipFile_test() throws Exception {
//
//        TDBStore tripleStore = new TDBStore();
//        tripleStore.loadIntoStore(RDF_FILE_URL);
//    }

    @Test
    public void neo4jStore_RdfGzipFile_test() throws Exception {

        Neo4jStore graphStore = new Neo4jStore("neo4j_test");
        StaticArchiveResourceProvider resourceProvider = new StaticArchiveResourceProvider(RDF_FILE_URL);
        graphStore.loadIntoStore(resourceProvider.getResources(null), true);
    }

}
