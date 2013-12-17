package org.bgbm.biovel.drf.tnr.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.input.DRFInputException;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TnrRequestTest {
	
	private static DRFCSVInputParser parser;
	private static List<String> nameCompleteList;
	
	@BeforeClass 
	public static void  setup() {
		parser = new DRFCSVInputParser();
		
		nameCompleteList = new ArrayList<String>();
		nameCompleteList.add("Abies pardei");
		nameCompleteList.add("Ameira divagans");
		nameCompleteList.add("Boccardiella ligerica");
		nameCompleteList.add("Bougainvillia rugosa");
		nameCompleteList.add("Branchiura sowerbyi");
		nameCompleteList.add("Cercopagis pengoi");
		nameCompleteList.add("Chelicorophium curvispinum");
		nameCompleteList.add("Chalcis biguttata");


	}	
	
	@Test
	public void nameCompleteOnlyTest() {
		List<TnrMsg> tnrRequests = parser.parse(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));		
		Assert.assertEquals("Number of names compared not equal",tnrRequests.size(),nameCompleteList.size());
		
		Iterator<TnrMsg> itrTr = tnrRequests.iterator();
		Iterator<String> itrNc = nameCompleteList.iterator();
		while(itrTr.hasNext() && itrNc.hasNext()) {
			TnrMsg tnrMsg = itrTr.next();
			String sciName = tnrMsg.getQuery().get(0).getTnrRequest().getTaxonName().getName().getNameComplete();
			System.out.println("Scientific Name : " + sciName);
			
			Assert.assertEquals("nameComplete differs",sciName,itrNc.next());
		}				
	}
	
	@Test
	public void nameCompleteOnlyDomTest() throws DRFInputException {		
		List<Node> tnrRequestNodes = parser.parseToDomNodes(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));
	}
	
	@Test
	public void nameCompleteOnlyJsonTest() throws DRFInputException {		
		List<String> tnrRequestJsonList = parser.parseToJsonList(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/nameCompleteOnly.csv","UTF-8"));
	}
	
	@Test
	public void tnrResponseTest() throws DRFInputException, JAXBException, ParserConfigurationException, SAXException, IOException {		
		TnrResponse tnrResponse  = TnrMsgUtils.convertXMLToTnrResponse(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/TnrResponse.xml","UTF-8"));
		System.out.println("name canonical : " + tnrResponse.getAcceptedName().getTaxonName().getName().getNameCanonical());
	}
	
		


}
