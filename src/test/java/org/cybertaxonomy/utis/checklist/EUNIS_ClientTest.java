package org.cybertaxonomy.utis.checklist;

import java.util.List;

import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EUNIS_ClientTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(EUNIS_ClientTest.class);

    static EUNIS_Client client;

    @BeforeClass
    public static void  setup() {
        client =  new EUNIS_Client();
        client.setChecklistInfo(client.buildServiceProviderInfo());
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

    @Test
    public void scientificNameExact_Synonym_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Canis dalmatinus", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Canis dalmatinus", response.getMatchingNameString());
        assertEquals("Canis aureus", response.getTaxon().getTaxonName().getCanonicalName());
        assertTrue(response.getTaxon().getSources().size() > 0);
        assertEquals(0, response.getSynonym().size());
    }

    @Test
    public void scientificNameExact_Accepted_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Canis aureus", true, true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Canis aureus", response.getMatchingNameString());
        assertEquals("Canis aureus", response.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response.getTaxon().getTaxonName().getScientificName() + " (" + response.getTaxon().getUrl() + ")");
        assertTrue(response.getTaxon().getUrl() != null);
        assertTrue(response.getTaxon().getIdentifier() != null);
        assertEquals("Linnaeus, 1758", response.getTaxon().getTaxonName().getAuthorship());
        assertTrue(response.getSynonym().size() > 0);
        for(Synonym syn : response.getSynonym()) {
            logger.info("Synonym: " + syn.getTaxonName().getScientificName() + " (" + syn.getUrl() + ")");
        }

        assertNotNull(response.getTaxon().getParentTaxon());
        assertEquals("Canidae", response.getTaxon().getParentTaxon().getScientificName());
        assertEquals("http://eunis.eea.europa.eu/taxonomy/2425", response.getTaxon().getParentTaxon().getIdentifier());
    }

    @Test
    public void scientificNameExact_Accepted_Test_2() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Bellis perennis", true, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Bellis perennis", response.getMatchingNameString());
        assertEquals("Bellis perennis", response.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response.getTaxon().getTaxonName().getScientificName() + " (" + response.getTaxon().getUrl() + ")");
        assertEquals("Flora Europaea Website", response.getTaxon().getAccordingTo());
        assertEquals("L.", response.getTaxon().getTaxonName().getAuthorship());
    }

    @Test
    public void scientificNameExact_withSynonym_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Prinobius myardi", true, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Prinobius myardi", response.getMatchingNameString());
        assertEquals("Prinobius myardi", response.getTaxon().getTaxonName().getCanonicalName());
        assertTrue(response.getSynonym().size() > 0);
        boolean prionus_germari_found = false;
        for(Synonym syn : response.getSynonym()) {
            logger.info(syn.getTaxonName().getScientificName());
            if(syn.getTaxonName().getCanonicalName().equals("Prionus germari")) {
                prionus_germari_found = true;
            }
        }
        assertTrue(prionus_germari_found);
    }

    @Test
    public void genus_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Prionus", true, true);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(8, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertTrue(response.getMatchingNameString().startsWith("Prionus"));
        assertTrue(
                (response.getTaxon().getTaxonName().getCanonicalName().startsWith("Prionus") && response.getMatchingNameType().equals(NameType.TAXON))
                ||
                (response.getTaxon().getTaxonName().getCanonicalName().startsWith("Prinobius") && response.getMatchingNameType().equals(NameType.SYNONYM))
                );
        List<HigherClassificationElement> hc = response.getTaxon().getHigherClassification();

    }

    @Test
    public void higherClassification_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(ClassificationAction.higherClassification, "http://eunis.eea.europa.eu/species/116371", false, false);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Prinobius myardi", response.getTaxon().getTaxonName().getCanonicalName());
        List<HigherClassificationElement> hc = response.getTaxon().getHigherClassification();
        assertNotNull(hc);
        assertTrue(!hc.isEmpty());
        assertEquals("Cerambycidae", getHigherClassification("Family", hc).getScientificName());
        assertEquals("Coleoptera", getHigherClassification("Order", hc).getScientificName());
        assertEquals("Arthropoda", getHigherClassification("Phylum", hc).getScientificName());
        assertEquals("Animalia", getHigherClassification("Kingdom", hc).getScientificName());
    }

    @Test
    public void scientificNameLikeTest_1() throws DRFChecklistException, TnrMsgException {
        String queryString = "Cani";
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, queryString, false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() > 1);
        for(Response r : tnrMsg.getQuery().get(0).getResponse()) {
            assertTrue(r.getMatchingNameString().startsWith(queryString));
        }
    }

    @Test
    public void scientificNameLikeTest_2() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Abies par", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
    }

    @Test
    public void scientificNameLikeTest_3() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "ies par", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue("The query string should not match anything in the middle of the name.", tnrMsg.getQuery().get(0).getResponse().size() == 0);
    }

    @Test
    public void vernacularNameExactTest_1() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.vernacularNameExact, "Farkas", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
        tnrMsg.getQuery().get(0).getResponse().get(0).getTaxon().getTaxonName().getCanonicalName().equals("Canis lupus");
    }

    @Test
    public void vernacularNameLikeTest_1() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.vernacularNameLike, "egwart", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() > 0);
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Cichorium intybus", response.getTaxon().getTaxonName().getCanonicalName());
        assertEquals("Wegwarte", response.getMatchingNameString());
        assertEquals(NameType.VERNACULAR_NAME, response.getMatchingNameType());
    }

    @Test
    public void findByIdentifierTest_1() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.findByIdentifier, "http://eunis.eea.europa.eu/species/1367", false, false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
        tnrMsg.getQuery().get(0).getResponse().get(0).getTaxon().getTaxonName().getCanonicalName().equals("Canis lupus");
    }
}

