package org.cybertaxonomy.utis.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cybertaxonomy.utis.checklist.BaseChecklistClient;
import org.cybertaxonomy.utis.checklist.BgbmEditClient;
import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.checklist.GBIFBackboneClient;
import org.cybertaxonomy.utis.checklist.PESIClient;
import org.cybertaxonomy.utis.checklist.Species2000ColClient;
import org.cybertaxonomy.utis.checklist.WoRMSClient;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.occurrences.GBIFOccurrencesClient;
import org.cybertaxonomy.utis.query.RestClient;

public class ServiceProviderInfoUtils {

    public static String generateChecklistInfoListAsJson() throws DRFChecklistException {
        String checklistInfoList = "[]";
        List<ServiceProviderInfo> cilist = generateChecklistInfoList();

        checklistInfoList = JSONUtils.convertObjectToJson(cilist);
        return checklistInfoList;
    }

    /**
     * @return
     */
    public static List<ServiceProviderInfo> generateChecklistInfoList() {
        List<ServiceProviderInfo> cilist = new ArrayList<ServiceProviderInfo>();

        Species2000ColClient col = new Species2000ColClient();
        cilist.add(col.getServiceProviderInfo());

        PESIClient pesi = new PESIClient();
        cilist.add(pesi.getServiceProviderInfo());

        WoRMSClient worms = new WoRMSClient();
        cilist.add(worms.getServiceProviderInfo());

        BaseChecklistClient<RestClient> bec = new BgbmEditClient();
        cilist.add(bec.getServiceProviderInfo());

        GBIFBackboneClient gbc = new GBIFBackboneClient();
        cilist.add(gbc.getServiceProviderInfo());

        return cilist;
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
                                Species2000ColClient.DATA_AGR_URL,
                                Species2000ColClient.SEARCH_MODES);
                    }
                    if(key.equals(BgbmEditClient.ID)) {
                        ci = new ServiceProviderInfo(BgbmEditClient.ID,
                                BgbmEditClient.LABEL,
                                BgbmEditClient.DOC_URL,
                                BgbmEditClient.COPYRIGHT_URL,
                                BgbmEditClient.SEARCH_MODES);
                    }
                    if(key.equals(GBIFBackboneClient.ID)) {
                        ci = new ServiceProviderInfo(GBIFBackboneClient.ID,
                                GBIFBackboneClient.LABEL,
                                GBIFBackboneClient.URL,
                                GBIFBackboneClient.DATA_AGR_URL,
                                GBIFBackboneClient.SEARCH_MODES);
                    }
                    if(key.equals(PESIClient.ID)) {
                        ci = new ServiceProviderInfo(PESIClient.ID,
                                PESIClient.LABEL,
                                PESIClient.URL,
                                PESIClient.DATA_AGR_URL,
                                PESIClient.SEARCH_MODES);
                    }
                    if(key.equals(WoRMSClient.ID)) {
                        ci = new ServiceProviderInfo(WoRMSClient.ID,
                                WoRMSClient.LABEL,
                                WoRMSClient.URL,
                                WoRMSClient.DATA_AGR_URL,
                                WoRMSClient.SEARCH_MODES);
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
                                GBIFOccurrencesClient.DATA_AGR_URL,
                                ServiceProviderInfo.DEFAULT_SEARCH_MODE);
                    }
                }
            }
        }
        return new ArrayList<ServiceProviderInfo>(ciMap.values());
    }

}
