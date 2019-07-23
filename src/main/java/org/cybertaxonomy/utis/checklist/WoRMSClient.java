package org.cybertaxonomy.utis.checklist;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.xml.rpc.ServiceException;

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
import org.cybertaxonomy.utis.tnr.msg.Taxon.ParentTaxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.IdentifierUtils;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WoRMSClient extends BaseChecklistClient<SoapClient> {

    private static final Logger logger = LoggerFactory.getLogger(WoRMSClient.class);

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

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.of(
            ClassificationAction.higherClassification,
            ClassificationAction.taxonomicChildren
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
     * {@inheritDoc}
     */
    @Override
    public EnumSet<ClassificationAction> getClassificationActions() {
        return CLASSIFICATION_ACTION;
    }

    /**
     * @return
     * @throws DRFChecklistException
     */
    private AphiaNameServicePortType getAphiaNameService() throws DRFChecklistException {
        AphiaNameServicePortType aphianspt;

        AphiaNameServiceLocator aphiansl = aphiaServiceLocator();

        try {
            aphianspt = aphiansl.getAphiaNameServicePort();
        } catch (ServiceException e) {
            logger.error("Error in accessing AphiaNameSerice", e);
            throw new DRFChecklistException("Error in accessing AphiaNameService");
        }
        return aphianspt;
    }

    /**
     * @return
     */
    protected AphiaNameServiceLocator aphiaServiceLocator() {
        AphiaNameServiceLocator aphiansl = new AphiaNameServiceLocator();
        return aphiansl;
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

        UtisAction searchMode = TnrMsgUtils.utisActionFrom(request.getSearchMode());

        int nameGUID = record.getAphiaID();
        String matchingName = record.getScientificname();
        if(record.getAuthority() != null) {
            matchingName += " " + record.getAuthority();
        }
        if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
            tnrResponse.setMatchingNameString(matchingName);
        }

        // case when accepted name
        if(record.getValid_AphiaID() == nameGUID) {
            Taxon accName = generateTaxon(record, addClassification, request.isAddParentTaxon());
            logger.debug("    > " + accName.getTaxonName().getScientificName());
            tnrResponse.setTaxon(accName);
            if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                tnrResponse.setMatchingNameType(NameType.TAXON);
            }
        } else {
            // case when synonym
            AphiaRecord accNameRecord = aphianspt.getAphiaRecordByID(record.getValid_AphiaID());
            if(accNameRecord != null){
                Taxon accName = generateTaxon(accNameRecord, false, false);
                tnrResponse.setTaxon(accName);
            } else {
                logger.error("WoRMS has no accepted name for " + nameGUID);
            }
            if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                tnrResponse.setMatchingNameType(NameType.SYNONYM);
            }
        }

        int offset = 1; // default is 1 in Aphia
        AphiaRecord[] synonyms = aphianspt.getAphiaSynonymsByID(nameGUID, offset);

        if(request.isAddSynonymy() && synonyms != null && synonyms.length > 0) {
            generateSynonyms(synonyms, tnrResponse);
        }
        return tnrResponse;
    }

    /**
     * A valid record has not one of the status:
     *
     * quarantined:  hidden from public interface after decision from an editor
     * deleted:  AphiaID should NOT be used anymore, please replace it by the valid_AphiaID
     *
     * @param record
     * @return
     */
    private boolean isValidRecord(AphiaRecord record) {
        return !( record.getStatus().equals("quarantined") || record.getStatus().equals("deleted"));
    }

    private Taxon generateTaxon(AphiaRecord taxonRecord, boolean addClassification, boolean addParentTaxon) {
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

        if(addClassification | addParentTaxon) {
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

            // remove last item since this is the taxon itself
            taxon.getHigherClassification().remove(taxon.getHigherClassification().size() -1);
            // revert order
            Collections.reverse(taxon.getHigherClassification());

        }

        if(addParentTaxon) {
            if(taxon.getHigherClassification().size() > 2) {
                ParentTaxon parentTaxon = new ParentTaxon();
                int parentTaxonPosition = 0;
                parentTaxon.setScientificName(taxon.getHigherClassification().get(parentTaxonPosition).getScientificName());
                parentTaxon.setIdentifier(taxon.getHigherClassification().get(parentTaxonPosition).getTaxonID());
                taxon.setParentTaxon(parentTaxon);
            }
        }
        if(!addClassification) {
            taxon.getHigherClassification().clear();
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

        resolveWithPager(tnrMsg, SearchMode.scientificNameLike);
    }

    /**
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    private void resolveWithPager(TnrMsg tnrMsg, SearchMode searchMode) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        AphiaNameServicePortType aphianspt = getAphiaNameService();
        boolean fuzzy = false;

        Integer pageIndex = query.getRequest().getPageIndex().intValue();
        Integer pageSize = query.getRequest().getPageSize().intValue();
        boolean tryNextPage = true;
        int offset = (pageIndex * pageSize) + 1;

        try {

            // collect aphia records for the requested page
            List<AphiaRecord> recordsForPage = new ArrayList<>(pageSize);
            AphiaRecord[] records;
            while (tryNextPage) {
                switch(searchMode){
                case scientificNameLike:
                    records = aphianspt.getAphiaRecords(name, true, fuzzy, false, offset);
                    break;
                case vernacularNameLike:
                    records = aphianspt.getAphiaRecordsByVernacular(name, true, offset);
                    break;
                case findByIdentifier:
                case scientificNameExact:
                case vernacularNameExact:
                default:
                    throw new RuntimeException("Ivalid search mode used within this method");

                }
                if(records != null){
                    logger.debug("page at offset " + offset + " has " + records.length + " records");
                    for (AphiaRecord record : records) {
                        if(recordsForPage.size() < pageSize){
                            recordsForPage.add(record);
                        }
                    }
                    offset = offset + records.length;
                    tryNextPage = recordsForPage.size() < pageSize;
                }  else {
                    tryNextPage = false;
                }
            }

            // make response objects
            for (AphiaRecord record : recordsForPage) {
                Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), false);
                query.getResponse().add(tnrResponse);
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

        resolveWithPager(tnrMsg, SearchMode.vernacularNameLike);

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
                Response tnrResponse = tnrResponseFromRecord(aphianspt, record, query.getRequest(), addClassification);
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


