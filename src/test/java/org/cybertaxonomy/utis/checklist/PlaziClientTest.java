package org.cybertaxonomy.utis.checklist;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaziClientTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PlaziClientTest.class);

    public static PlaziClient client = null;

    private static File tmpRss = null;

    @BeforeClass
    public static void initStore() throws IOException{

        Neo4jStoreManager.dontWatch = true;
        Neo4jStoreManager.clearStore = true;

        tmpRss  = new File("./target/test-classes/PlaziClientTest-xml-rss.xml");

        URL rssA = PlaziClientTest.class.getClassLoader().getResource("A-xml.rss.xml");
        FileUtils.copyURLToFile(rssA, tmpRss);
        client = new PlaziClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());

        client.setTestUrl(tmpRss.toURI().toURL().toString());
        Neo4jStoreManager.lastStoreUpdater.updateStore(new GregorianCalendar(1900, 1, 1).getTime());

        System.err.println(client.queryClient().sizeInfo());
    }


    @Test
    public void clientTest() throws DRFChecklistException, IOException {

        URL rssB = PlaziClientTest.class.getClassLoader().getResource("B-xml.rss.xml"); // has 3 items more than A-xml.rss.xml
        FileUtils.copyURLToFile(rssB, tmpRss);

        assertNotNull(client.checkTreatmentIdentifier("http://treatment.plazi.org/id/03B0878BFFDDF357EEFDB627FC2FFA14"));


        Date now = new GregorianCalendar(2016, 11, 16).getTime(); // one day after the lastBuildDate in the rss feed
        List<URI> updates = client.getResourceProvider().getResources(now);
        assertEquals(3, updates.size());
        Neo4jStoreManager.lastStoreUpdater.updateStore(now);

        assertNotNull(client.checkTreatmentIdentifier("http://treatment.plazi.org/id/E32787BBFFB36F6FFF24612CFC99FCFF"));
    }

    @Test
    public void getByIdentifierTest() throws DRFChecklistException, TnrMsgException, IOException {


        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.findByIdentifier, "http://taxon-concept.plazi.org/id/03B0878BFFDDF357EEFDB627FC2FFA14", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
        tnrMsg.getQuery().get(0).getResponse().get(0).getTaxon().getTaxonName().getScientificName().equals("Clothoda tocantinensis");
    }

    @Test
    public void scientificNameExactTest() throws DRFChecklistException, TnrMsgException, IOException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Clothoda tocantinensis", true, true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Clothoda tocantinensis", response.getTaxon().getTaxonName().getScientificName());
    }

    @Test
    public void scientificNameLikeTest() throws DRFChecklistException, TnrMsgException, IOException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Clothoda toc", true, true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Clothoda tocantinensis", response.getTaxon().getTaxonName().getScientificName());
    }

    // @Test // this test has problems
    public void clientTest_dev() throws InterruptedException {

        Thread testRun = new Thread() {

            @Override
            public void run() {
                boolean interrupted = false;
                PlaziClient client = new PlaziClient();
                while(!interrupted) {
                    try {
                        sleep(1000 * 60 * 3); // time limit for the test to run
                    } catch (InterruptedException e) {
                        logger.info("Test run has ended.");
                        interrupted = true;
                    }
                }
            }
        };

        testRun.start();
        testRun.join();
    }
}

