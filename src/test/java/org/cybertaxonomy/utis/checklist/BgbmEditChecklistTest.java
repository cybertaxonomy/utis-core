package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.input.DRFInputException;
import org.cybertaxonomy.utis.query.RestClient;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.Query;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BgbmEditChecklistTest extends Assert {

    protected static final Logger logger = LoggerFactory.getLogger(BgbmEditChecklistTest.class);

    private static DRFCSVInputParser parser;
    private static List<String> nameCompleteList;

    private static ServiceProviderInfo ci;
    private static BgbmEditClient client;

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

        client =  new BgbmEditClient();
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
        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/vibrant.csv","UTF-8"));
        //List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/nameCompleteOnly.csv","UTF-8"));
        TnrMsg tnrMsg = TnrMsgUtils.mergeTnrMsgs(tnrMsgs);
        String json = JSONUtils.convertObjectToJson(ci);
        // BaseChecklistClient<RestClient> bec = new BgbmEditClient(json);
        BaseChecklistClient<RestClient> bec = client;


        for(Query query : tnrMsg.getQuery()) {
            query.getRequest().setSearchMode(SearchMode.scientificNameExact.toString());
        }
        TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
        bec.queryChecklist(tnrMsg);
        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);
    }

    @Test
    public void exactScientificNameTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameExact, "Lactuca perennis", true, true);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());

        Response response1 = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertNotNull(response1);
        assertEquals("Lactuca perennis L.", response1.getMatchingNameString());
        assertEquals("Lactuca perennis", response1.getTaxon().getTaxonName().getCanonicalName());
        logger.info("Accepted: " + response1.getTaxon().getTaxonName().getScientificName() + " (" + response1.getTaxon().getUrl() + ")");
        // assertTrue(response1.getTaxon().getUrl() != null); // TODO?
        assertEquals("urn:lsid:catalogueoflife.org:taxon:7edd6542-bfda-11e4-811c-020044200006:col20150401", response1.getTaxon().getIdentifier());
        assertTrue(response1.getSynonym().size() > 0);
        for(Synonym syn : response1.getSynonym()) {
            logger.info("Synonym: " + syn.getTaxonName().getScientificName() + " (" + syn.getUrl() + ")");
        }
        assertNotNull(response1.getTaxon().getParentTaxon());
        assertEquals("Asteraceae", response1.getTaxon().getParentTaxon().getScientificName());
        assertEquals("4dacf4c3-d489-43be-ac23-29efc26ecd9c", response1.getTaxon().getParentTaxon().getIdentifier());

    }

    @Test
    public void scientificNameLikeTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(SearchMode.scientificNameLike, "Crepi", true, true);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        int resultSize = tnrMsg.getQuery().get(0).getResponse().size();
        assertTrue("result set site only " + resultSize, resultSize > 450); // the actual result set size is 499 at the time being
    }

    @Test
    public void higherClassificationTest() throws DRFChecklistException, TnrMsgException {

        TnrMsg tnrMsg = TnrMsgUtils.createRequest(ClassificationAction.higherClassification, "urn:lsid:catalogueoflife.org:taxon:88fca8c1-bfda-11e4-811c-020044200006:col20150401", false, false);
        client.queryChecklist(tnrMsg);

        String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
        System.out.println(outputXML);

        assertEquals(1, tnrMsg.getQuery().get(0).getResponse().size());

        Response response1 = tnrMsg.getQuery().get(0).getResponse().get(0);
        assertEquals("Lactuca virosa", response1.getTaxon().getTaxonName().getCanonicalName());
        List<HigherClassificationElement> hcl = response1.getTaxon().getHigherClassification();
        assertNotNull(hcl);
        assertEquals(6, hcl.size());

        assertEquals("Genus", hcl.get(0).getRank());
        assertEquals("Lactuca", hcl.get(0).getScientificName());

        assertEquals("Family", hcl.get(1).getRank());
        assertEquals("Asteraceae", hcl.get(1).getScientificName());

        assertEquals("Order", hcl.get(2).getRank());
        assertEquals("Asterales", hcl.get(2).getScientificName());

        assertEquals("Class", hcl.get(3).getRank());
        assertEquals("Magnoliopsida", hcl.get(3).getScientificName());

        assertEquals("Phylum", hcl.get(4).getRank());
        assertEquals("Tracheophyta", hcl.get(4).getScientificName());

        assertEquals("Kingdom", hcl.get(5).getRank());
        assertEquals("Plantae", hcl.get(5).getScientificName());
    }

}
