// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.bgbm.biovel.drf.client;

import java.io.IOException;
import java.util.Properties;

import org.bgbm.biovel.drf.query.SparqlClient;
import org.bgbm.biovel.drf.store.TripleStore;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @date Oct 5, 2015
 *
 */
public class SparqlClientTest {

    private static final String RDF_FILE_URL = "http://localhost/download/species.rdf.gz"; // http://eunis.eea.europa.eu/rdf/species.rdf.gz

    @Test
    public void testRdfGzipFile() throws IOException {

        Properties sysprops = System.getProperties();
        TripleStore tripleStore = new TripleStore();
        tripleStore.loadIntoStore(RDF_FILE_URL);
        SparqlClient client = new SparqlClient(RDF_FILE_URL);

    }

}
