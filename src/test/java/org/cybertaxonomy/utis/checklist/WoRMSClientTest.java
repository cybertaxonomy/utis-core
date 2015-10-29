package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.checklist.PESIClient;
import org.cybertaxonomy.utis.checklist.SearchMode;
import org.cybertaxonomy.utis.checklist.Species2000ColClient;
import org.cybertaxonomy.utis.checklist.WoRMSClient;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.input.DRFInputException;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.BiovelUtils;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class WoRMSClientTest {
    private static final Logger logger = Logger.getLogger(WoRMSClientTest.class.getName());
    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;

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
}
