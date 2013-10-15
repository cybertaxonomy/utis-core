package org.bgbm.biovel.drf.occret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.GBIFBackboneClient;
import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.input.DRFInputException;
import org.bgbm.biovel.drf.occurrences.GBIFOccurrencesClient;
import org.bgbm.biovel.drf.rest.TaxoRESTClient.ServiceProviderInfo;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class GBIFOccurrencesTest {
	private static DRFCSVInputParser parser;
	private static List<String> nameCompleteList;
	
	private static ServiceProviderInfo ci;
	
	@BeforeClass 
	public static void  setup() {
				
		nameCompleteList = new ArrayList<String>();
		//nameCompleteList.add("Ameira divagans");
		//nameCompleteList.add("Boccardiella ligerica");
		//nameCompleteList.add("Coscinodiscus wailesii");
		//nameCompleteList.add("Caprella mutica‬");
		//nameCompleteList.add("Caprella mutica Schurin, 1935");
		nameCompleteList.add("Foo bar");
//		nameCompleteList.add("Bougainvillia rugosa");
//		nameCompleteList.add("Branchiura sowerbyi");
//		nameCompleteList.add("Cercopagis pengoi");
//		nameCompleteList.add("Chelicorophium curvispinum");
			
	}
	
	@Test
	public void convertChecklistInfoToJson() throws DRFChecklistException {
		String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
		System.out.println("Json : " + checklistInfoJson);
	}
	
	@Test
	public void getOccurrencesTest() throws DRFChecklistException {
		GBIFOccurrencesClient goc = new GBIFOccurrencesClient();
		String occurrences = goc.queryOccurrenceBank(nameCompleteList);
		System.out.println("occurrences : " + occurrences);
	}
}
