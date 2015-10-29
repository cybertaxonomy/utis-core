package org.cybertaxonomy.utis.checklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.cybertaxonomy.utis.tnr.msg.Classification;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class EEA_BDC_ClientTest {

    static EEA_BDC_Client client;

    @BeforeClass
    public static void  setup() {
        client =  new EEA_BDC_Client();
        client.setChecklistInfo(client.buildServiceProviderInfo());
    }

    @Test
    public void scientificNameExact_Synonym_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Canis dalmatinus", false);
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

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Canis aureus", true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());
        Response response = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Canis aureus", response.getMatchingNameString());
        assertEquals("Canis aureus", response.getTaxon().getTaxonName().getCanonicalName());
        assertTrue(response.getSynonym().size() > 0);
        Classification c = response.getTaxon().getClassification();
        assertEquals("Canidae", c.getFamily());
        assertEquals("Carnivora", c.getOrder());
        assertEquals("Chordata", c.getPhylum());
        assertEquals("Animalia", c.getKingdom());
    }

    @Test
    public void genus_Test() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Prionus", true);
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
        Classification c = response.getTaxon().getClassification();
        assertEquals("Cerambycidae", c.getFamily());
        assertEquals("Coleoptera", c.getOrder());
        assertEquals("Arthropoda", c.getPhylum());
        assertEquals("Animalia", c.getKingdom());
    }

    @Test
    public void scientificNameLikeTest_1() throws DRFChecklistException, TnrMsgException {
        String queryString = "Cani";
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, queryString, false);
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
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Abies par", false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
    }

    @Test
    public void scientificNameLikeTest_3() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "ies par", false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue("The query string should not match anything in the middle of the name.", tnrMsg.getQuery().get(0).getResponse().size() == 0);
    }

    @Test
    public void vernacularNameExactTest_1() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.vernacularNameExact, "Farkas", false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
        tnrMsg.getQuery().get(0).getResponse().get(0).getTaxon().getTaxonName().getCanonicalName().equals("Canis lupus");
    }

    @Test
    public void vernacularNameLikeTest_1() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.vernacularNameLike, "egwart", false);
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
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.findByIdentifier, "http://eunis.eea.europa.eu/species/1367", false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
        tnrMsg.getQuery().get(0).getResponse().get(0).getTaxon().getTaxonName().getCanonicalName().equals("Canis lupus");
    }
}

