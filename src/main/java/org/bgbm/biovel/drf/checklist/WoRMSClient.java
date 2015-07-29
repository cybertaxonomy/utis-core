package org.bgbm.biovel.drf.checklist;


import java.rmi.RemoteException;
import java.util.EnumSet;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.worms.AphiaNameServiceLocator;
import org.bgbm.biovel.drf.checklist.worms.AphiaNameServicePortType;
import org.bgbm.biovel.drf.checklist.worms.AphiaRecord;
import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.tnr.msg.Classification;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.bgbm.biovel.drf.utils.IdentifierUtils;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;


public class WoRMSClient extends BaseChecklistClient {

    public static final String ID = "worms";
    public static final String LABEL = "WoRMS";
    public static final String URL = "http://www.marinespecies.org/index.php";
    public static final String DATA_AGR_URL = "";

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.vernacularNameExact,
            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<SearchMode> SCIENTIFICNAME_SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike
            );



    public WoRMSClient() {
        super();

    }

    @Override
    public HttpHost getHost() {
        return new HttpHost("http://www.marinespecies.org",80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL, getSearchModes());
        return checklistInfo;
    }


    @Override
    public int getMaxPageSize() {
        return 10;
    }



    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES;
    }

    /**
     * @return
     * @throws DRFChecklistException
     */
    private AphiaNameServicePortType getAphiaNameService() throws DRFChecklistException {
        AphiaNameServicePortType aphianspt;

        AphiaNameServiceLocator aphiansl = new AphiaNameServiceLocator();

        try {
            aphianspt = aphiansl.getAphiaNameServicePort();
        } catch (ServiceException e) {
            logger.error("Error in accessing AphiaNameSerice", e);
            throw new DRFChecklistException("Error in accessing AphiaNameService");
        }
        return aphianspt;
    }

    /**
     * @param aphianspt
     * @param record
     * @return
     * @throws RemoteException
     */
    private Response tnrResponseFromRecord(AphiaNameServicePortType aphianspt, AphiaRecord record, Query.Request request)
            throws RemoteException {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        SearchMode searchMode = SearchMode.valueOf(request.getSearchMode());

        int accNameGUID = record.getValid_AphiaID();
        String matchingName = record.getScientificname();
        if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
            tnrResponse.setMatchingNameString(matchingName);
        }

        // case when accepted name
        if(record.getAphiaID() == accNameGUID) {
            Taxon accName = generateAccName(record);
            tnrResponse.setTaxon(accName);
            if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                tnrResponse.setMatchingNameType(NameType.TAXON);
            }
        } else {
            // case when synonym
            AphiaRecord accNameRecord = aphianspt.getAphiaRecordByID(accNameGUID);
            Taxon accName = generateAccName(accNameRecord);
            tnrResponse.setTaxon(accName);
            if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                tnrResponse.setMatchingNameType(NameType.SYNONYM);
            }
        }

        AphiaRecord[] synonyms = aphianspt.getAphiaSynonymsByID(accNameGUID);

        if(request.isAddSynonymy() && synonyms != null && synonyms.length > 0) {
            generateSynonyms(synonyms, tnrResponse);
        }
        return tnrResponse;
    }

    private Taxon generateAccName(AphiaRecord taxonRecord) {
        Taxon accName = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = taxonRecord.getScientificname();
        taxonName.setFullName(resName + " " + taxonRecord.getAuthority());

        taxonName.setCanonicalName(resName);

        taxonName.setRank(taxonRecord.getRank());
        taxonName.setAuthorship(taxonRecord.getAuthority());

        accName.setTaxonName(taxonName);
        accName.setTaxonomicStatus(taxonRecord.getStatus());

        accName.setUrl(taxonRecord.getUrl());
        accName.setIdentifier(taxonRecord.getLsid());


        //FIXME : To fill in
        String sourceUrl = taxonRecord.getUrl();
        String sourceDatasetID = "";
        String sourceDatasetName = "";
        String sourceName = "";

        Source source = new Source();
        source.setIdentifier(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accName.getSources().add(source);

        Classification c = new Classification();
        c.setKingdom(taxonRecord.getKingdom());
        c.setPhylum(taxonRecord.getPhylum());
        c.setClazz("");
        c.setOrder(taxonRecord.getOrder());
        c.setFamily(taxonRecord.getFamily());
        c.setGenus(taxonRecord.getGenus());
        accName.setClassification(c);

        return accName;
    }


    private void generateSynonyms(AphiaRecord[] synonyms, Response tnrResponse) {

        for(AphiaRecord synRecord : synonyms) {
            Synonym synonym = new Synonym();

            TaxonName taxonName = new TaxonName();

            String resName = synRecord.getScientificname();
            taxonName.setFullName(resName + " " + synRecord.getAuthority());

            taxonName.setCanonicalName(resName);

            taxonName.setRank(synRecord.getRank());
            taxonName.setAuthorship(synRecord.getAuthority());

            synonym.setTaxonName(taxonName);
            synonym.setTaxonomicStatus(synRecord.getStatus());

            synonym.setUrl(synRecord.getUrl());

            //FIXME : To fill in
            String sourceUrl = synRecord.getUrl();
            String sourceDatasetID =  "";
            String sourceDatasetName = "";
            String sourceName = "";

            Source source = new Source();
            source.setIdentifier(sourceDatasetID);
            source.setDatasetName(sourceDatasetName);
            source.setName(sourceName);
            source.setUrl(sourceUrl);
            synonym.getSources().add(source);

            tnrResponse.getSynonym().add(synonym);
        }
    }



    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();

        try {
            AphiaRecord record = null;
            try {
                Integer nameAphiaID = aphianspt.getAphiaID(name, false);
                logger.debug("nameAphiaID : " + nameAphiaID);
                record = aphianspt.getAphiaRecordByID(nameAphiaID);
                Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest());
                query.getResponse().add(tnrResponse);
            } catch(NullPointerException npe) {
                //FIXME : Workaround for NPE thrown by the aphia stub due to a,
                //        null aphia id (Integer), when the name is not present
                //        in the db
            }
        }  catch (RemoteException e) {
            logger.error("Error in autogenerated getGUID method", e);
            throw new DRFChecklistException("Error in getGUID method", e);
        }

    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();
        boolean fuzzy = false;

        try {
            AphiaRecord[] records = aphianspt.getAphiaRecords(name + "%", true, fuzzy, false, 1);
            if(records != null){
                for (AphiaRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
                }
            }

        } catch (RemoteException e) {
            logger.error("Error in getGUID method in AphiaNameSerice", e);
            throw new DRFChecklistException("Error in getGUID method in AphiaNameSerice", e);
        }

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();

        try {
            AphiaRecord[] records = aphianspt.getAphiaRecordsByVernacular(name, false, 1);
            if(records != null){
                for (AphiaRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
                }
            }

        } catch (RemoteException e) {
            logger.error("Error in getAphiaRecordsByVernacular() in AphiaNameSerice", e);
            throw new DRFChecklistException("Error in getGUID method in AphiaNameSerice", e);
        }

    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();

        try {
            AphiaRecord[] records = aphianspt.getAphiaRecordsByVernacular(name, true, 1);
            if(records != null){
                for (AphiaRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
                }
            }

        } catch (RemoteException e) {
            logger.error("Error in getAphiaRecordsByVernacular() AphiaNameSerice", e);
            throw new DRFChecklistException("Error in getGUID method in AphiaNameSerice", e);
        }

    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();

        try {
            AphiaRecord record = null;
            try {
                record = aphianspt.getAphiaRecordByExtID(name, "lsid");
                Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest());
                query.getResponse().add(tnrResponse);
            } catch(NullPointerException npe) {
                //FIXME : Workaround for NPE thrown by the aphia stub due to a,
                //        null aphia id (Integer), when the name is not present
                //        in the db
            }
        }  catch (RemoteException e) {
            logger.error("Error in autogenerated getGUID method", e);
            throw new DRFChecklistException("Error in getGUID method", e);
        }

    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        return IdentifierUtils.checkLSID(value);
    }
}


