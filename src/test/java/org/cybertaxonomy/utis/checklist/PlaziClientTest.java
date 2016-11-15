package org.cybertaxonomy.utis.checklist;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaziClientTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziClientTest.class);


    @Test
    public void clientTest() throws DRFChecklistException {

        Neo4jStoreManager.testMode = true;

        URL rssA = PlaziClientTest.class.getClassLoader().getResource("A-xml.rss.xml"); // has 3 items less
        URL rssB = PlaziClientTest.class.getClassLoader().getResource("B-xml.rss.xml");

        PlaziClient client = new PlaziClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());

        client.setTestUrl(rssA.toString());
        Neo4jStoreManager.lastStoreUpdater.updateStore(new GregorianCalendar(1900, 1, 1).getTime());

        Date now = new GregorianCalendar(2016, 11, 16).getTime(); // one day after the lastBuildDate in the rss feed
        client.setTestUrl(rssB.toString());
        List<URI> updates = client.getResourceProvider().getResources(now);
        assertEquals(3, updates.size());
        Neo4jStoreManager.lastStoreUpdater.updateStore(now);

        client.checkTreatmentIdentifier("http://treatment.plazi.org/id/E32787BBFFB36F6FFF24612CFC99FCFF");

    }

}

