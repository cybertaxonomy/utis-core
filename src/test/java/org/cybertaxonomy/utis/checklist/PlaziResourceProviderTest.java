/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.httpclient.util.DateUtil;
import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.cybertaxonomy.utis.store.ResourceProvider;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Nov 10, 2016
 *
 */
public class PlaziResourceProviderTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziResourceProviderTest.class);

    @Test
    public void testParseRssLine() throws ParseException {
        PlaziResourceProvider lmp = new PlaziResourceProvider(null);
        Date timeStamp = lmp.parseLastBuildDate("<lastBuildDate>2016-11-09T03:36:39-02:00</lastBuildDate>");
        logger.debug(timeStamp.toString());
        logger.debug(DateUtil.formatDate(timeStamp));
        assertNotNull(timeStamp);
    }

    @Test
    public void testGetResources_remote() throws DRFChecklistException {

        Neo4jStoreManager.testMode = true;
        PlaziClient plaziClient = new PlaziClient();
        ResourceProvider resourceProvider = plaziClient.getResourceProvider();

        List<URI> resource = resourceProvider.getResources(new GregorianCalendar(1900, 1, 1).getTime());
        assertNotNull(resource);
        logger.debug(resource.size()+ " resources");
        assertTrue(resource.size() > 170000);
    }


}
