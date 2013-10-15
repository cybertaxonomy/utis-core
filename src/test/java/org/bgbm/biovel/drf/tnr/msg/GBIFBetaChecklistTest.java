package org.bgbm.biovel.drf.tnr.msg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.GBIFBetaBackboneClient;
import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.input.DRFInputException;
import org.bgbm.biovel.drf.rest.TaxoRESTClient.ServiceProviderInfo;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class GBIFBetaChecklistTest {

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
		

		ci = new ServiceProviderInfo(GBIFBetaBackboneClient.ID,
				GBIFBetaBackboneClient.LABEL,
				GBIFBetaBackboneClient.URL,
				GBIFBetaBackboneClient.DATA_AGR_URL);
		ci.addSubChecklist(new ServiceProviderInfo("1", "GBIF NUB Taxonomy", "http://ecat-dev.gbif.org/checklist/1"));
	}
	
	@Test
	public void convertChecklistInfoToJson() throws DRFChecklistException {
		String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
		System.out.println("Json : " + checklistInfoJson);
	}
	
	@Test
	public void nameCompleteTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
		parser = new DRFCSVInputParser();
		List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));
		//List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/singleNameCompleteOnly.csv","UTF-8"));
		
		
		GBIFBetaBackboneClient gbbc =  new GBIFBetaBackboneClient(JSONUtils.convertObjectToJson(ci));
		Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
		while(tnrMsgItr.hasNext()) {
			TnrMsg tnrMsg = tnrMsgItr.next();
			gbbc.queryChecklist(tnrMsg);
			String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
			System.out.println(outputXML);
		}
	}
}

