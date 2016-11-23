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

public class PlaziClientUpdateTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziClientUpdateTest.class);

    public static PlaziClient client = null;


    /**
     * WARNING!!!!!
     *
     * Do not use this test as reference implementation as it is using the api
     * in a special way which can lead to unexpected store content. This code it
     * only suitable for comparing the counts of resources to be updated returned
     * by the resource provider. The client test urls must never be changed in
     * production environments.
     *
     * @throws DRFChecklistException
     */
    @Test
    public void updateTest() throws DRFChecklistException {

        Neo4jStoreManager.dontWatch = true;
        Neo4jStoreManager.clearStore = true;

        URL rssA = PlaziClientUpdateTest.class.getClassLoader().getResource("A-xml.rss.xml");
        client = new PlaziClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());

        client.setTestUrl(rssA.toString());
        Neo4jStoreManager.lastStoreUpdater.updateStore(new GregorianCalendar(1900, 1, 1).getTime());

        System.err.println(client.queryClient().sizeInfo());

        URL rssB = PlaziClientUpdateTest.class.getClassLoader().getResource("B-xml.rss.xml"); // has 3 items more than A-xml.rss.xml

        Date now = new GregorianCalendar(2016, 11, 16).getTime(); // one day after the lastBuildDate in the rss feed
        client.setTestUrl(rssB.toString());
        List<URI> updates = client.getResourceProvider().getResources(now);
        assertEquals(3, updates.size());
        Neo4jStoreManager.lastStoreUpdater.updateStore(now);

    }


}

