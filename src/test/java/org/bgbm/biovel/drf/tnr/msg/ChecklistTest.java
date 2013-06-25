package org.bgbm.biovel.drf.tnr.msg;

import java.util.List;

import org.bgbm.biovel.drf.checklist.BaseChecklistClient.ChecklistInfo;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.utils.ChecklistUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChecklistTest {
	
	@BeforeClass 
	public static void  setup() {
	}
	
	@Test
	public void generateChecklistInfoList() throws DRFChecklistException {
		String checklistInfoListJson = ChecklistUtils.generateChecklistInfoList();
		System.out.println("Json : " + checklistInfoListJson);
	}
	

}

