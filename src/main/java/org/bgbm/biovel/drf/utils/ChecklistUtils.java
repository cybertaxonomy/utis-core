package org.bgbm.biovel.drf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bgbm.biovel.drf.checklist.BaseChecklistClient.ChecklistInfo;
import org.bgbm.biovel.drf.checklist.BgbmEditClient;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.GBIFBetaBackboneClient;
import org.bgbm.biovel.drf.checklist.Species2000ColClient;

public class ChecklistUtils {
	
	public static String generateChecklistInfoList() throws DRFChecklistException {
		String checklistInfoList = "[]";
		List<ChecklistInfo> cilist = new ArrayList<ChecklistInfo>();
		
		Species2000ColClient col = new Species2000ColClient();
		cilist.add(col.getChecklistInfo());
		
		BgbmEditClient bec = new BgbmEditClient();
		cilist.add(bec.getChecklistInfo());
		
		GBIFBetaBackboneClient gbc = new GBIFBetaBackboneClient();
		cilist.add(gbc.getChecklistInfo());
		checklistInfoList = JSONUtils.convertObjectToJson(cilist);
		return checklistInfoList;
	}
	
	public static List<ChecklistInfo> convertStringToChecklistInfo(List<String> ciStrList) throws DRFChecklistException {
		Map<String,ChecklistInfo> ciMap = new HashMap<String, ChecklistInfo>();
		
		Iterator<String> ciItr = ciStrList.iterator();
		while(ciItr.hasNext()) {
			String[] ciStrArray = ciItr.next().split(";",5);
			System.out.println("ci array length : " + ciStrArray.length);
			if(ciStrArray.length == 5) {
				String key = ciStrArray[0];			
				System.out.println("ci key : " + key);
				ChecklistInfo ci = ciMap.get(ciStrArray[0]);
				if(ci == null) {
					if(key.equals(Species2000ColClient.ID)) {
						ci = new ChecklistInfo(Species2000ColClient.ID,
								Species2000ColClient.LABEL,
								Species2000ColClient.URL,
								Species2000ColClient.DATA_AGR_URL);
					}
					if(key.equals(BgbmEditClient.ID)) {
						ci = new ChecklistInfo(BgbmEditClient.ID,
								BgbmEditClient.LABEL,
								BgbmEditClient.URL,
								BgbmEditClient.DATA_AGR_URL);
					}
					if(key.equals(GBIFBetaBackboneClient.ID)) {
						ci = new ChecklistInfo(GBIFBetaBackboneClient.ID,
								GBIFBetaBackboneClient.LABEL,
								GBIFBetaBackboneClient.URL,
								GBIFBetaBackboneClient.DATA_AGR_URL);
					}
					if(ci != null) {
						ciMap.put(key, ci);
						System.out.println("put key : " + key);
						ci.addSubChecklist(ChecklistInfo.create(Arrays.copyOfRange(ciStrArray, 1, 5)));
						System.out.println("put sub checklist : " + ciStrArray[1]);
						
					}
				} else {
					ci.addSubChecklist(ChecklistInfo.create(Arrays.copyOfRange(ciStrArray, 1, 5)));
				}				
			}						
		}
		return new ArrayList<ChecklistInfo>(ciMap.values());
	}

}
