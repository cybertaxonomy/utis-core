package org.bgbm.biovel.drf.checklist;


import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.pesi.PESINameServiceLocator;
import org.bgbm.biovel.drf.checklist.pesi.PESINameServicePortType;
import org.bgbm.biovel.drf.checklist.pesi.PESIRecord;
import org.bgbm.biovel.drf.checklist.worms.AphiaRecord;
import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.tnr.msg.Classification;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.Scrutiny;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;


public class PESIClient extends BaseChecklistClient {

    public static final String ID = "pesi";
    public static final String LABEL = "PESI";
    public static final String URL = "http://www.eu-nomen.eu/portal/index.php";
    public static final String DATA_AGR_URL = "";

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.vernacularNameExact,
            SearchMode.vernacularNameLike
            );

    public static final EnumSet<SearchMode> SCIENTIFICNAME_SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike
            );


    public PESIClient() {
        super();

    }

    @Override
    public HttpHost getHost() {
        return new HttpHost("http://www.eu-nomen.eu",80);
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
     * @param pesins
     * @return
     * @throws DRFChecklistException
     */
    private PESINameServicePortType getPESINameService(PESINameServiceLocator pesins) throws DRFChecklistException {
        PESINameServicePortType pesinspt;
        try {
            pesinspt = pesins.getPESINameServicePort();
        } catch (ServiceException e) {
            logger.error("Error in accessing PESINameService", e);
            throw new DRFChecklistException("Error in accessing PESINameService");
        }
        return pesinspt;
    }

    private Taxon generateAccName(PESIRecord taxon) {

        // parse the pesi citation string and source information
        String pesiCitation = taxon.getCitation();

        String secReference = pesiCitation;
        String sourceString = null;

//        System.err.println(pesiCitation);
        String[] citationTokens = pesiCitation.split("\\sAccessed through:\\s");
        if(false && citationTokens.length == 2){ // TODO understand citatino string and implment parsing
            secReference = citationTokens[0];
            sourceString = citationTokens[1];
        }

        //
        Taxon accName = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = taxon.getScientificname();
        taxonName.setFullName(resName + " " + taxon.getAuthority());

        taxonName.setCanonicalName(resName);

        taxonName.setRank(taxon.getRank());
        taxonName.setAuthorship(taxon.getAuthority());

        accName.setTaxonName(taxonName);
        accName.setTaxonomicStatus(taxon.getStatus());

        accName.setAccordingTo(secReference);

        accName.setUrl(taxon.getUrl());


        //FIXME : To fill in
        String sourceDatasetID = "";
        String sourceName = "";

        if(sourceString != null){
            Source source = new Source();
            String[] sourceTokens = sourceString.split("\\sat\\shttp");
            if(sourceTokens.length == 2){
                source.setDatasetName(sourceTokens[0]);
                source.setUrl("http" + sourceTokens[1]);
            }
            source.setDatasetID(sourceDatasetID);
            source.setName(sourceName);
            accName.setSource(source);
        }

        //FIXME : To fill in
        String accordingTo = taxon.getAuthority();
        String modified = "";

        Scrutiny scrutiny = new Scrutiny();
        scrutiny.setAccordingTo(accordingTo);
        scrutiny.setModified(modified);
        accName.setScrutiny(scrutiny);

        Classification c = new Classification();
        c.setKingdom(taxon.getKingdom());
        c.setPhylum(taxon.getPhylum());
        c.setClazz("");
        c.setOrder(taxon.getOrder());
        c.setFamily(taxon.getFamily());
        c.setGenus(taxon.getGenus());
        accName.setClassification(c);

        return accName;
    }


    private void generateSynonyms(PESIRecord[] synonyms, TnrResponse tnrResponse) {

        for(PESIRecord synRecord : synonyms) {

         // parse the pesi citation string and source information
            String pesiCitation = synRecord.getCitation();

            String secReference = pesiCitation;
            String sourceString = null;

//            System.err.println(pesiCitation);
            String[] citationTokens = pesiCitation.split("\\sAccessed through:\\s");
            if(false && citationTokens.length == 2){ // TODO understand citatino string and implment parsing
                secReference = citationTokens[0];
                sourceString = citationTokens[1];
            }

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
            String sourceDatasetID = "";
            String sourceName = "";

            if(sourceString != null){
                Source source = new Source();
                String[] sourceTokens = sourceString.split("\\sat\\shttp");
                if(sourceTokens.length == 2){
                    source.setDatasetName(sourceTokens[0]);
                    source.setUrl("http" + sourceTokens[1]);
                }
                source.setDatasetID(sourceDatasetID);
                source.setName(sourceName);
                synonym.setSource(source);
            }

            //FIXME : To fill in
            String accordingTo = synRecord.getAuthority();
            String modified = "";

            Scrutiny scrutiny = new Scrutiny();
            scrutiny.setAccordingTo(accordingTo);
            scrutiny.setModified(modified);
            synonym.setScrutiny(scrutiny);

            tnrResponse.getSynonym().add(synonym);
        }
    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getTnrRequest().getTaxonName().getFullName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            String nameGUID = pesinspt.getGUID(name);
            if(nameGUID != null){
                logger.debug("nameGUID : " + nameGUID);
                PESIRecord record = pesinspt.getPESIRecordByGUID(nameGUID);
                tnrResponseFromRecord(pesinspt, record, null);
            } else {
                logger.debug("no match for " + name);
            }
        }  catch (RemoteException e) {
            logger.error("Error in getGUID method in PESINameService", e);
            throw new DRFChecklistException("Error in getGUID method in PESINameService");
        }

    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getTnrRequest().getTaxonName().getFullName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecords(name, true);
            if(records != null){
                for (PESIRecord record : records) {
                    TnrResponse tnrResponse = tnrResponseFromRecord(pesinspt, record, null);
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecords method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecords method in PESINameService");
        }

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getTnrRequest().getTaxonName().getFullName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecordsByVernacular(name);
            if(records != null){
                for (PESIRecord record : records) {
                    TnrResponse tnrResponse = tnrResponseFromRecord(pesinspt, record, null);
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecords method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecords method in PESINameService");
        }

    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getTnrRequest().getTaxonName().getFullName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecordsByVernacular("%" + name + "%");
            if(records != null){
                for (PESIRecord record : records) {
                    TnrResponse tnrResponse = tnrResponseFromRecord(pesinspt, record, null);
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecords method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecords method in PESINameService");
        }

    }

    /**
     * @param pesinspt
     * @param record
     * @param searchMode TODO
     * @throws RemoteException
     */
    private TnrResponse tnrResponseFromRecord(PESINameServicePortType pesinspt, PESIRecord record, SearchMode searchMode) throws RemoteException {

        TnrResponse tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        String accNameGUID = record.getValid_guid();
        if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
            tnrResponse.setMatchingNameString(record.getScientificname());
        }

        if(accNameGUID != null){

            // case when accepted name
            if(record.getGUID() != null && record.getGUID().equals(accNameGUID)) {
                Taxon accName = generateAccName(record);
                tnrResponse.setTaxon(accName);
                if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                    tnrResponse.setMatchingNameType(NameType.TAXON);
                }
            } else {
                // case when synonym
                PESIRecord accNameRecord = pesinspt.getPESIRecordByGUID(accNameGUID);
                Taxon accName = generateAccName(accNameRecord);
                tnrResponse.setTaxon(accName);
                if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                    tnrResponse.setMatchingNameType(NameType.SYNONYM);
                }
            }

            PESIRecord[] records = pesinspt.getPESISynonymsByGUID(accNameGUID);
            if(records != null && records.length > 0) {
                generateSynonyms(records,tnrResponse);
            }
        }

        return tnrResponse;
    }
}


