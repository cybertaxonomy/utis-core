package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

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

//@Ignore
public class GBIFChecklistTest {

    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;

    @BeforeClass
    public static void  setup() {
        parser = new DRFCSVInputParser();

        nameCompleteList = new ArrayList<String>();
        nameCompleteList.add("Ameira divagans");
        nameCompleteList.add("Boccardiella ligerica");
        nameCompleteList.add("Bougainvillia rugosa");
        nameCompleteList.add("Branchiura sowerbyi");
        nameCompleteList.add("Cercopagis pengoi");
        nameCompleteList.add("Chelicorophium curvispinum");

        ci = new ServiceProviderInfo(GBIFBackboneClient.ID,
                GBIFBackboneClient.LABEL,
                GBIFBackboneClient.URL,
                GBIFBackboneClient.DATA_AGR_URL, ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        ci.addSubChecklist(new ServiceProviderInfo("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c", "GBIF NUB Taxonomy", "http://uat.gbif.org/dataset/d7dddbf4-2cf0-4f39-9b2a-bb099caae36c"));
    }

    @Test
    public void convertChecklistInfoToJson() throws DRFChecklistException {
        String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
        System.out.println("Json : " + checklistInfoJson);
    }

    @Test
    public void buildServiceProviderInfo() throws DRFChecklistException {
        GBIFBackboneClient gbc =  new GBIFBackboneClient();
        ServiceProviderInfo spiInfo = gbc.buildServiceProviderInfo();
        List<ServiceProviderInfo> spiList = spiInfo.getSubChecklists();
        Iterator<ServiceProviderInfo> spiItr = spiList.iterator();
        while(spiItr.hasNext()) {
            ServiceProviderInfo spi = spiItr.next();
            System.out.println("Dataset : " + spi.getLabel() );
        }
        String checklistInfoJson = JSONUtils.convertObjectToJson(spiInfo);
        System.out.println("Json : " + checklistInfoJson);

    }

    @Test
    public void nameCompleteCsvTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
        parser = new DRFCSVInputParser();
        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/nameCompleteOnly.csv","UTF-8"));
        //List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/singleNameCompleteOnly.csv","UTF-8"));
        List<String> chosenKeyList = new ArrayList<String>();
        chosenKeyList.add("7ddf754f-d193-4cc9-b351-99906754a03b");

        GBIFBackboneClient gbc =  new GBIFBackboneClient(JSONUtils.convertObjectToJson(ci));
        Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
        while(tnrMsgItr.hasNext()) {
            TnrMsg tnrMsg = tnrMsgItr.next();
            TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
            gbc.queryChecklist(tnrMsg);
            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
            System.out.println(outputXML);
        }
    }

    @Test
    public void nameCompleteStringListTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
        parser = new DRFCSVInputParser();
        List<TnrMsg> tnrMsgs = TnrMsgUtils.convertStringListToTnrMsgList(nameCompleteList, SearchMode.scientificNameExact, false, false);
        //List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/singleNameCompleteOnly.csv","UTF-8"));
        List<String> chosenKeyList = new ArrayList<String>();
        chosenKeyList.add("7ddf754f-d193-4cc9-b351-99906754a03b");

        GBIFBackboneClient gbc =  new GBIFBackboneClient(JSONUtils.convertObjectToJson(ci));
        Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
        while(tnrMsgItr.hasNext()) {
            TnrMsg tnrMsg = tnrMsgItr.next();
            TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
            gbc.queryChecklist(tnrMsg);
            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
            System.out.println(outputXML);
        }
    }
}
