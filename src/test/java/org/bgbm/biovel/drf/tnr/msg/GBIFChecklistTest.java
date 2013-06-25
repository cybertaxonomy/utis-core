package org.bgbm.biovel.drf.tnr.msg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.GBIFBackboneClient;
import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.input.DRFInputException;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GBIFChecklistTest {

	private static DRFCSVInputParser parser;
	private static List<String> nameCompleteList;
	
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
	}
	
	
	@Test
	public void nameCompleteTest() throws DRFChecklistException, DRFInputException, JAXBException, TnrMsgException {
		parser = new DRFCSVInputParser();
		//List<String> inputXMLList = parser.parseToXML(BiovelUtils.getCSVAsString("org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));
		List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));
		List<String> chosenKeyList = new ArrayList<String>();
		chosenKeyList.add("d7dddbf4-2cf0-4f39-9b2a-bb099caae36c");
		
		GBIFBackboneClient gbc =  new GBIFBackboneClient(chosenKeyList);
		Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
		while(tnrMsgItr.hasNext()) {
			TnrMsg tnrMsg = tnrMsgItr.next();
			gbc.queryChecklist(tnrMsg);
			String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
			System.out.println(outputXML);
		}
	}
}
