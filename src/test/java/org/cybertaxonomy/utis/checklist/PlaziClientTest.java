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
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
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
        Taxon taxon = tnrMsg.getQuery().get(0).getResponse().get(0).getTaxon();
        assertEquals("Clothoda tocantinensis", taxon.getTaxonName().getScientificName());
        assertEquals("Clothoda tocantinensis", taxon.getTaxonName().getCanonicalName());
        assertEquals("Krolow, Tiago Kütter & Valadares, Ana Carolina B.", taxon.getTaxonName().getAuthorship());
        assertEquals("Krolow, Tiago Kütter & Valadares, Ana Carolina B. (2016) First record of order Embioptera (Insecta) for the State of Tocantins, Brazil, with description of a new species of Clothoda Enderlein. Zootaxa 4193 (1), pp. 184-188: 185-186", taxon.getTaxonName().getNomenclaturalReference());
    }

    @Test
    public void scientificNameExactTest() throws DRFChecklistException, TnrMsgException, IOException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Clothoda tocantinensis", true, true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Clothoda tocantinensis", response.getTaxon().getTaxonName().getCanonicalName());
    }

    @Test
    public void scientificNameLikeTest() throws DRFChecklistException, TnrMsgException, IOException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Clothoda toc", true, true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Clothoda tocantinensis", response.getTaxon().getTaxonName().getCanonicalName());
    }

    @Test
    public void higherClassification_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(ClassificationAction.higherClassification, "http://taxon-concept.plazi.org/id/AE4F6F58FFDBFFAFFF1D0EC5E367B2AF", false, false);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Trechisibus parvulus", response.getTaxon().getTaxonName().getCanonicalName());
        List<HigherClassificationElement> hc = response.getTaxon().getHigherClassification();
        assertNotNull(hc);
        assertTrue(!hc.isEmpty());

        assertEquals("Trechisibus", getHigherClassification("Genus", hc).getScientificName());
        assertEquals("Carabidae", getHigherClassification("Family", hc).getScientificName());
        assertEquals("Coleoptera", getHigherClassification("Order", hc).getScientificName());
        assertEquals("Arthropoda", getHigherClassification("Phylum", hc).getScientificName());
        assertEquals("Insecta", getHigherClassification("Class", hc).getScientificName());
        assertEquals("Animalia", getHigherClassification("Kingdom", hc).getScientificName());
    }


    /**
     * @param string
     * @param hc
     * @return
     */
    private HigherClassificationElement getHigherClassification(String string, List<HigherClassificationElement> hc) {
        for(HigherClassificationElement hce : hc) {
            if(hce.getRank().equals(string)) {
                return hce;
            }
        }
        return null;
    }
}

