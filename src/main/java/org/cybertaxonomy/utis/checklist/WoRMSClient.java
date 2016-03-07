package org.cybertaxonomy.utis.checklist;


import java.rmi.RemoteException;
import java.util.EnumSet;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.cybertaxonomy.utis.checklist.worms.AphiaNameServiceLocator;
import org.cybertaxonomy.utis.checklist.worms.AphiaNameServicePortType;
import org.cybertaxonomy.utis.checklist.worms.AphiaRecord;
import org.cybertaxonomy.utis.checklist.worms.Classification;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.SoapClient;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Source;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.IdentifierUtils;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WoRMSClient extends BaseChecklistClient<SoapClient> {

    private static final Logger logger = LoggerFactory.getLogger(WoRMSClient.class);


    private static final HttpHost HTTP_HOST = new HttpHost("http://www.marinespecies.org",80);
    public static final String ID = "worms";
    public static final String LABEL = "WoRMS";
    public static final String URL = "http://www.marinespecies.org/index.php";
    public static final String DATA_AGR_URL = "";

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.vernacularNameExact,
            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier,
            SearchMode.higherClassification,
            SearchMode.taxonomicChildren
            );

    public static final EnumSet<SearchMode> SCIENTIFICNAME_SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike
            );



    public WoRMSClient() {
        super();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatelessClient() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initQueryClient() {
        queryClient = new SoapClient();
    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL, getSearchModes());
        return checklistInfo;
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
     * @param addClassification TODO
     * @return
     * @throws RemoteException
     */
    private Response tnrResponseFromRecord(AphiaNameServicePortType aphianspt, AphiaRecord record, Query.Request request, boolean addClassification)
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
            Taxon accName = generateTaxon(record, false);
            tnrResponse.setTaxon(accName);
            if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                tnrResponse.setMatchingNameType(NameType.TAXON);
            }
        } else {
            // case when synonym
            AphiaRecord accNameRecord = aphianspt.getAphiaRecordByID(accNameGUID);
            Taxon accName = generateTaxon(accNameRecord, addClassification);
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

    private Taxon generateTaxon(AphiaRecord taxonRecord, boolean addClassification) {
        Taxon taxon = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = taxonRecord.getScientificname();
        taxonName.setScientificName(resName + " " + taxonRecord.getAuthority());

        taxonName.setCanonicalName(resName);

        taxonName.setRank(taxonRecord.getRank());
        taxonName.setAuthorship(taxonRecord.getAuthority());

        taxon.setTaxonName(taxonName);
        taxon.setTaxonomicStatus(taxonRecord.getStatus());

        taxon.setUrl(taxonRecord.getUrl());
        taxon.setIdentifier(taxonRecord.getLsid());


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
        taxon.getSources().add(source);

        if(addClassification) {
         // the classification information contained in the taxon is incomplete
            // it is necessary to to a second request to obtain the full classification
            try {
                AphiaNameServicePortType aphianspt = getAphiaNameService();
                Classification classification = aphianspt.getAphiaClassificationByID(taxonRecord.getAphiaID());

                while(classification != null && classification.getRank() != null) {
                    HigherClassificationElement hce = new HigherClassificationElement();
                    hce.setScientificName(classification.getScientificname());
                    hce.setRank(classification.getRank());
                    AphiaRecord taxonAtRankRecord = aphianspt.getAphiaRecordByID(classification.getAphiaID());
                    hce.setTaxonID(taxonAtRankRecord.getLsid());
                    taxon.getHigherClassification().add(hce);
                    System.err.println(classification.getRank());
                    classification = classification.getChild();
                }

            } catch (DRFChecklistException | RemoteException e) {
                String msg = "Error while obtainig classification informtation for " + taxonRecord.getLsid();
                logger.error(msg, e);
            }
        }

        return taxon;
    }


    private void generateSynonyms(AphiaRecord[] synonyms, Response tnrResponse) {

        for(AphiaRecord synRecord : synonyms) {
            Synonym synonym = new Synonym();

            TaxonName taxonName = new TaxonName();

            String resName = synRecord.getScientificname();
            taxonName.setScientificName(resName + " " + synRecord.getAuthority());

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
                Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
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
                    Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
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
                    Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
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
                    Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
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

        _findByIdentifier(tnrMsg, false);

    }

    /**
     * @param tnrMsg
     * @param addClassification TODO
     * @throws DRFChecklistException
     */
    private void _findByIdentifier(TnrMsg tnrMsg, boolean addClassification) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();

        try {
            AphiaRecord record = null;
            try {
                record = aphianspt.getAphiaRecordByExtID(name, "lsid");
                Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
                query.getResponse().add(tnrResponse);
            } catch(NullPointerException npe) {
                //FIXME : Workaround for NPE thrown by the aphia stub due to a,
                //        null aphia id (Integer), when the name is not present
                //        in the db
            }
        }  catch (RemoteException e) {
            logger.error("Error in autogenerated getAphiaRecordByExtID method", e);
            throw new DRFChecklistException("Error in getAphiaRecordByExtID method", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();

        try {
            AphiaRecord parentTaxonRecord = null;
            try {
                parentTaxonRecord = aphianspt.getAphiaRecordByExtID(name, "lsid");
                AphiaRecord[] records = aphianspt.getAphiaChildrenByID(parentTaxonRecord.getAphiaID(), 1, false);
                if(records != null){
                    for (AphiaRecord record : records) {
                        Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
                        query.getResponse().add(tnrResponse);
                    }
                }
            } catch(NullPointerException npe) {
                //FIXME : Workaround for NPE thrown by the aphia stub due to a,
                //        null aphia id (Integer), when the name is not present
                //        in the db
            }
        }  catch (RemoteException e) {
            String msg = "Error in autogenerated getAphiaRecordByExtID or getAphiaChildrenByID method";
            logger.error(msg, e);
            throw new DRFChecklistException(msg, e);
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {

        _findByIdentifier(tnrMsg, true);

    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        return IdentifierUtils.checkLSID(value);
    }
}


