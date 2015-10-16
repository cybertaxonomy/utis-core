package org.bgbm.biovel.drf.checklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class EEA_BDC_ClientTest {

    private static DRFCSVInputParser parser;
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
    }

    @Test
    public void scientificNameLikeTest_1() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Cani", false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() > 1);
    }

    @Test
    public void scientificNameLikeTest_2() throws DRFChecklistException, TnrMsgException {
        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Abies par", false);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
        assertTrue(tnrMsg.getQuery().get(0).getResponse().size() == 1);
    }
}

