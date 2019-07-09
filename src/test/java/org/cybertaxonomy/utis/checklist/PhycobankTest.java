package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.List;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhycobankTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(PhycobankTest.class);

    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;
    private static PhycobankClient client;

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


        ci = new ServiceProviderInfo(PhycobankClient.ID,
                PhycobankClient.LABEL,
                PhycobankClient.DOC_URL,
                PhycobankClient.COPYRIGHT_URL, ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        ci.addSubChecklist(new ServiceProviderInfo("phycobank",
                "Phycobank (EDIT - name catalogue end point)",
                "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "http://www.catalogueoflife.org/col/info/copyright", ServiceProviderInfo.DEFAULT_SEARCH_MODE));

        client =  new PhycobankClient();
        client.setChecklistInfo(client.buildServiceProviderInfo());
    }

    @Test
    public void convertChecklistInfoToJson() throws DRFChecklistException {
        String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
        System.out.println("Json : " + checklistInfoJson);
    }

    @Test
    public void exactScientificNameTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Eunotia tauntoniensis", true, false);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());

        Response response1 = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertNotNull(response1);
        assertEquals("Eunotia tauntoniensis Hust.", response1.getMatchingNameString());
        assertEquals("Eunotia tauntoniensis", response1.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response1.getTaxon().getTaxonName().getScientificName() + " (" + response1.getTaxon().getUrl() + ")");
    }

    @Test
    public void scientificNameLikeTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Planothidium vict", true, false);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        int resultSize = tnrMsg.getQuery().get(0).getResponse().size();
        assertTrue("result set site only " + resultSize, resultSize == 1); // the actual result set size is 1 at the time being
    }


}
