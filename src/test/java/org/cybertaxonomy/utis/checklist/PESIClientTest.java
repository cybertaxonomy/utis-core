package org.cybertaxonomy.utis.checklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.input.DRFInputException;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.BiovelUtils;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PESIClientTest {

    protected static final Logger logger = LoggerFactory.getLogger(PESIClientTest.class);

    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;


    static PESIClient client;

    @BeforeClass
    public static void  setup() {
        parser = new DRFCSVInputParser();

        nameCompleteList = new ArrayList<String>();
        nameCompleteList.add("Ameira divagans");
        nameCompleteList.add("Boccardi redeki");
        nameCompleteList.add("Bougainvillia rugosa");
        nameCompleteList.add("Branchiura sowerbyi");
        nameCompleteList.add("Cercopagis pengoi");
        nameCompleteList.add("Chelicorophium curvispinum");


        ci = new ServiceProviderInfo(Species2000ColClient.ID,
                Species2000ColClient.LABEL,
                Species2000ColClient.URL,
                Species2000ColClient.DATA_AGR_URL, ServiceProviderInfo.DEFAULT_SEARCH_MODE);

        client =  new PESIClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());

    }

    @Test
    public void convertChecklistInfoToJson() throws DRFChecklistException {
        String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
        System.out.println("Json : " + checklistInfoJson);
    }

    @Test
    public void nameCompleteTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
        parser = new DRFCSVInputParser();
        //List<String> inputXMLList = parser.parseToXML(BiovelUtils.getCSVAsString("org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));
        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/nameCompleteOnly.csv","UTF-8"));
//		List<String> chosenKeyList = new ArrayList<String>();
//		chosenKeyList.add("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c");

        PESIClient pesic =  new PESIClient();
        Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
        while(tnrMsgItr.hasNext()) {
            TnrMsg tnrMsg = tnrMsgItr.next();
            TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
            pesic.queryChecklist(tnrMsg);
            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
            System.out.println(outputXML);
        }
    }

    @Test
    public void exactScientificNameTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Lactuca virosa", true);
        client.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);

        System.out.println(outputXML);
        assertEquals(2, tnrMsg.getQuery().get(0).getResponse().size());

        Response response1 = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Lactuca virosa", response1.getMatchingNameString());
        assertEquals("Lactuca virosa", response1.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response1.getTaxon().getTaxonName().getScientificName() + " (" + response1.getTaxon().getUrl() + ")");
        assertTrue(response1.getTaxon().getUrl() != null);
        assertTrue(response1.getTaxon().getIdentifier() != null);
        assertEquals("L.", response1.getTaxon().getTaxonName().getAuthorship());
        assertTrue(response1.getSynonym().size() > 0);
        for(Synonym syn : response1.getSynonym()) {
            logger.info("Synonym: " + syn.getTaxonName().getScientificName() + " (" + syn.getUrl() + ")");
        }

        Response response2 = tnrMsg.getQuery().get(0).getResponse().get(1);
        assertEquals("Lactuca virosa", response2.getMatchingNameString());
        assertEquals("Lactuca serriola", response2.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response2.getTaxon().getTaxonName().getScientificName() + " (" + response2.getTaxon().getUrl() + ")");
        assertTrue(response2.getTaxon().getUrl() != null);
        assertTrue(response2.getTaxon().getIdentifier() != null);
        assertEquals("L.", response2.getTaxon().getTaxonName().getAuthorship());
        assertTrue(response2.getSynonym().size() > 0);
        for(Synonym syn : response2.getSynonym()) {
            logger.info("Synonym: " + syn.getTaxonName().getScientificName() + " (" + syn.getUrl() + ")");
        }

    }
}

