package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.input.DRFInputException;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.BiovelUtils;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class WoRMSClientTest extends Assert {
    private static final Logger logger = Logger.getLogger(WoRMSClientTest.class.getName());
    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;
    private static WoRMSClient client;

    @BeforeClass
    public static void  setup() {

        logger.setLevel(Level.INFO);
        parser = new DRFCSVInputParser();

        nameCompleteList = new ArrayList<String>();
        nameCompleteList.add("Ameira divagans");
        nameCompleteList.add("Boccardi redeki");
        nameCompleteList.add("Bougainvillia rugosa");
        nameCompleteList.add("Branchiura sowerbyi");
        nameCompleteList.add("Cercopagis pengoi");
        nameCompleteList.add("Chelicorophium curvispinum");


        ci = new ServiceProviderInfo(WoRMSClient.ID,
                WoRMSClient.LABEL,
                WoRMSClient.URL,
                WoRMSClient.DATA_AGR_URL, ServiceProviderInfo.DEFAULT_SEARCH_MODE);

        client =  new WoRMSClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());
    }

    @Test
    public void convertChecklistInfoToJson() throws DRFChecklistException {
        String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
        logger.info("ChecklistInfo : " +  checklistInfoJson);
    }

    @Test
    public void nameCompleteTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
        parser = new DRFCSVInputParser();
        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/nameCompleteOnly.csv","UTF-8"));

        WoRMSClient wormsc =  new WoRMSClient();
        Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
        while(tnrMsgItr.hasNext()) {
            TnrMsg tnrMsg = tnrMsgItr.next();
            logger.info("Querying WoRMS for name : " + tnrMsg.getQuery().get(0).getRequest().getQueryString());
            TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
            wormsc.queryChecklist(tnrMsg);
            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
            logger.info(outputXML);
        }
    }

    @Test
    public void resolveScientificNamesLikeTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {

        parser = new DRFCSVInputParser();
        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/nameCompleteOnly.csv","UTF-8"));

        WoRMSClient wormsc =  new WoRMSClient();
        // strip off the last to characters of the names since we will do a like query
        for (TnrMsg tnrMsg : tnrMsgs) {
            String name = tnrMsg.getQuery().get(0).getRequest().getQueryString();
            tnrMsg.getQuery().get(0).getRequest().setSearchMode(SearchMode.scientificNameExact.toString());
            String nameTrunk = name.substring(0, name.length() - 2);
            logger.info("Querying WoRMS for name : " + nameTrunk);

            tnrMsg.getQuery().get(0).getRequest().setQueryString(nameTrunk);
            wormsc.resolveScientificNamesLike(tnrMsg);
            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
            logger.info(outputXML);
        }
    }

    @Test
    public void exactScientificNameTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Bolinus brandaris", true, true);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());

        Response response1 = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Bolinus brandaris", response1.getMatchingNameString());
        assertEquals("Bolinus brandaris", response1.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response1.getTaxon().getTaxonName().getScientificName() + " (" + response1.getTaxon().getUrl() + ")");
        assertEquals("http://www.marinespecies.org/aphia.php?p=taxdetails&id=140389", response1.getTaxon().getUrl());
        assertEquals("urn:lsid:marinespecies.org:taxname:140389", response1.getTaxon().getIdentifier());
        assertEquals("(Linnaeus, 1758)", response1.getTaxon().getTaxonName().getAuthorship());
        assertTrue(response1.getSynonym().size() > 0);
        for(Synonym syn : response1.getSynonym()) {
            logger.info("Synonym: " + syn.getTaxonName().getScientificName() + " (" + syn.getUrl() + ")");
        }
        assertNotNull(response1.getTaxon().getParentTaxon());
        assertEquals("Bolinus", response1.getTaxon().getParentTaxon().getScientificName());
    }


    @Test
    public void higherClassificationTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(ClassificationAction.higherClassification, "urn:lsid:marinespecies.org:taxname:140389", false, false);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());

        Response response1 = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Bolinus brandaris", response1.getTaxon().getTaxonName().getCanonicalName());
        List<HigherClassificationElement> hcl = response1.getTaxon().getHigherClassification();
        assertNotNull(hcl);
        // assertEquals(6, hcl.size());

        assertEquals("Genus", hcl.get(0).getRank());
        assertEquals("Bolinus", hcl.get(0).getScientificName());

        assertEquals("Subfamily", hcl.get(1).getRank());
        assertEquals("Muricinae", hcl.get(1).getScientificName());

        assertEquals("Family", hcl.get(2).getRank());
        assertEquals("Muricidae", hcl.get(2).getScientificName());

        assertEquals("Superfamily", hcl.get(3).getRank());
        assertEquals("Muricoidea", hcl.get(3).getScientificName());

        assertEquals("Order", hcl.get(4).getRank());
        assertEquals("Neogastropoda", hcl.get(4).getScientificName());

        assertEquals("Subclass", hcl.get(5).getRank());
        assertEquals("Caenogastropoda", hcl.get(5).getScientificName());
    }
}

