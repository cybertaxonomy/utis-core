package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cybertaxonomy.utis.checklist.BaseChecklistClient;
import org.cybertaxonomy.utis.checklist.BgbmEditClient;
import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.checklist.SearchMode;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.input.DRFInputException;
import org.cybertaxonomy.utis.query.RestClient;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.BiovelUtils;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class BgbmEditChecklistTest {

    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;

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


        ci = new ServiceProviderInfo(BgbmEditClient.ID,
                BgbmEditClient.LABEL,
                BgbmEditClient.DOC_URL,
                BgbmEditClient.COPYRIGHT_URL, ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        ci.addSubChecklist(new ServiceProviderInfo("col",
                "EDIT - Catalogue Of Life",
                "http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "http://www.catalogueoflife.org/col/info/copyright", ServiceProviderInfo.DEFAULT_SEARCH_MODE));
    }

    @Test
    public void convertChecklistInfoToJson() throws DRFChecklistException {
        String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
        System.out.println("Json : " + checklistInfoJson);
    }

    @Test
    public void nameCompleteTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
        parser = new DRFCSVInputParser();
        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/vibrant.csv","UTF-8"));
        //List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/nameCompleteOnly.csv","UTF-8"));
        TnrMsg tnrMsg = TnrMsgUtils.mergeTnrMsgs(tnrMsgs);
        String json = JSONUtils.convertObjectToJson(ci);
        BaseChecklistClient<RestClient> bec = new BgbmEditClient(json);


        for(Query query : tnrMsg.getQuery()) {
            query.getRequest().setSearchMode(SearchMode.scientificNameExact.toString());
        }
        TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
        bec.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
    }
}
