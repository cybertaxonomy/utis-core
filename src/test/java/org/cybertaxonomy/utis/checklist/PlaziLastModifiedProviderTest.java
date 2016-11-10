/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.httpclient.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Nov 10, 2016
 *
 */
public class PlaziLastModifiedProviderTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziLastModifiedProviderTest.class);

    @Test
    public void testParseRssLine() throws ParseException {
        PlaziLastModifiedProvider lmp = new PlaziLastModifiedProvider(null);
        Date timeStamp = lmp.parseRssLine("<lastBuildDate>2016-11-09T03:36:39-02:00</lastBuildDate>");
        logger.debug(timeStamp.toString());
        logger.debug(DateUtil.formatDate(timeStamp));
        assertNotNull(timeStamp);
    }

}
