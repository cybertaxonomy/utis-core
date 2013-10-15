package org.bgbm.biovel.drf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bgbm.biovel.drf.checklist.BgbmEditClient;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.GBIFBackboneClient;
import org.bgbm.biovel.drf.checklist.PESIClient;
import org.bgbm.biovel.drf.checklist.Species2000ColClient;
import org.bgbm.biovel.drf.occurrences.GBIFOccurrencesClient;
import org.bgbm.biovel.drf.rest.TaxoRESTClient.ServiceProviderInfo;

public class ServiceProviderInfoUtils {
	
	public static String generateChecklistInfoList() throws DRFChecklistException {
		String checklistInfoList = "[]";
		List<ServiceProviderInfo> cilist = new ArrayList<ServiceProviderInfo>();
		
		Species2000ColClient col = new Species2000ColClient();
		cilist.add(col.getServiceProviderInfo());
		
		PESIClient pesi = new PESIClient();
		cilist.add(pesi.getServiceProviderInfo());
		
		BgbmEditClient bec = new BgbmEditClient();
		cilist.add(bec.getServiceProviderInfo());
		
//		GBIFBetaBackboneClient gbc = new GBIFBetaBackboneClient();
//		cilist.add(gbc.getChecklistInfo());
		
		GBIFBackboneClient gbc = new GBIFBackboneClient();
		cilist.add(gbc.getServiceProviderInfo());
		
		checklistInfoList = JSONUtils.convertObjectToJson(cilist);
		return checklistInfoList;
	}
	
	public static String generateOccurrencesBankInfoList() throws DRFChecklistException {
		String occurrencesbankInfoList = "[]";
		List<ServiceProviderInfo> oblist = new ArrayList<ServiceProviderInfo>();
		
		GBIFOccurrencesClient gob = new GBIFOccurrencesClient();
		oblist.add(gob.getServiceProviderInfo());		
		
		occurrencesbankInfoList = JSONUtils.convertObjectToJson(oblist);
		return occurrencesbankInfoList;
	}
	
	public static List<ServiceProviderInfo> convertStringToChecklistInfo(List<String> ciStrList) throws DRFChecklistException {
		Map<String,ServiceProviderInfo> ciMap = new HashMap<String, ServiceProviderInfo>();
		
		Iterator<String> ciItr = ciStrList.iterator();
		while(ciItr.hasNext()) {
			String[] ciStrArray = ciItr.next().split(";",5);
			System.out.println("ci array length : " + ciStrArray.length);
			if(ciStrArray.length == 5) {
				String key = ciStrArray[0];			
				System.out.println("ci key : " + key);
				ServiceProviderInfo ci = ciMap.get(ciStrArray[0]);
				if(ci == null) {
					if(key.equals(Species2000ColClient.ID)) {
						ci = new ServiceProviderInfo(Species2000ColClient.ID,
								Species2000ColClient.LABEL,
								Species2000ColClient.URL,
								Species2000ColClient.DATA_AGR_URL);
					}
					if(key.equals(BgbmEditClient.ID)) {
						ci = new ServiceProviderInfo(BgbmEditClient.ID,
								BgbmEditClient.LABEL,
								BgbmEditClient.URL,
								BgbmEditClient.DATA_AGR_URL);
					}
					if(key.equals(GBIFBackboneClient.ID)) {
						ci = new ServiceProviderInfo(GBIFBackboneClient.ID,
								GBIFBackboneClient.LABEL,
								GBIFBackboneClient.URL,
								GBIFBackboneClient.DATA_AGR_URL);
					}
					if(key.equals(PESIClient.ID)) {
						ci = new ServiceProviderInfo(PESIClient.ID,
								PESIClient.LABEL,
								PESIClient.URL,
								PESIClient.DATA_AGR_URL);
					}
					if(ci != null) {
						ciMap.put(key, ci);
						System.out.println("put key : " + key);
						ci.addSubChecklist(ServiceProviderInfo.create(Arrays.copyOfRange(ciStrArray, 1, 5)));
						System.out.println("put sub checklist : " + ciStrArray[1]);
						
					}
				} else {
					ci.addSubChecklist(ServiceProviderInfo.create(Arrays.copyOfRange(ciStrArray, 1, 5)));
				}				
			}						
		}
		return new ArrayList<ServiceProviderInfo>(ciMap.values());
	}
	
	public static List<ServiceProviderInfo> convertStringToOccurrenceBankInfo(List<String> ciStrList) throws DRFChecklistException {
		Map<String,ServiceProviderInfo> ciMap = new HashMap<String, ServiceProviderInfo>();
		
		Iterator<String> ciItr = ciStrList.iterator();
		while(ciItr.hasNext()) {
			String[] ciStrArray = ciItr.next().split(";",5);
			System.out.println("ci array length : " + ciStrArray.length);
			if(ciStrArray.length == 5) {
				String key = ciStrArray[0];			
				System.out.println("ci key : " + key);
				ServiceProviderInfo ci = ciMap.get(ciStrArray[0]);
				if(ci == null) {
					if(key.equals(GBIFOccurrencesClient.ID)) {
						ci = new ServiceProviderInfo(GBIFOccurrencesClient.ID,
								GBIFOccurrencesClient.LABEL,
								GBIFOccurrencesClient.URL,
								GBIFOccurrencesClient.DATA_AGR_URL);
					}					
				} 			
			}						
		}
		return new ArrayList<ServiceProviderInfo>(ciMap.values());
	}

}
