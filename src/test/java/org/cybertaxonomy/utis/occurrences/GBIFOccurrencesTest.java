package org.cybertaxonomy.utis.occurrences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.checklist.GBIFBackboneClient;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.input.DRFCSVInputParser;
import org.cybertaxonomy.utis.input.DRFInputException;
import org.cybertaxonomy.utis.occurrences.GBIFOccurrencesClient;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.BiovelUtils;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.TnrMsgException;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class GBIFOccurrencesTest {
	private static DRFCSVInputParser parser;
	private static List<String> nameCompleteList;
	
	private static ServiceProviderInfo ci;
	
	@BeforeClass 
	public static void  setup() {				
		nameCompleteList = new ArrayList<String>();
		nameCompleteList.add("Ameira divagans");
		nameCompleteList.add("Boccardiella ligerica");
		//nameCompleteList.add("Coscinodiscus wailesii");
			
	}
	
	@Test
	public void convertChecklistInfoToJson() throws DRFChecklistException {
		String checklistInfoJson = JSONUtils.convertObjectToJson(ci);
		System.out.println("Json : " + checklistInfoJson);
	}
	
	@Test
	public void getOccurrencesTest() throws DRFChecklistException, IOException {
		GBIFOccurrencesClient goc = new GBIFOccurrencesClient();
		String occurrences = goc.queryOccurrenceBank(nameCompleteList);
		//System.out.println("occurrences : " + occurrences);
/*		File file = new File("/home/cmathew/Temp/AchilleaMillefolium/occurrences.txt");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(occurrences);
		bw.close();*/

		System.out.println("Done");
		
	}
}
