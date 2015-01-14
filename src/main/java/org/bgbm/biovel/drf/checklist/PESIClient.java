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

    private static final EnumSet<SearchMode> capability = EnumSet.of(SearchMode.scientificNameExact, SearchMode.scientificNameLike);


    public PESIClient() {
        super();

    }

    @Override
    public HttpHost getHost() {
        return new HttpHost("http://www.eu-nomen.eu",80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL);
        return checklistInfo;
    }


    @Override
    public int getMaxPageSize() {
        return 10;
    }



    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return capability;
    }

    private Taxon generateAccName(PESIRecord taxon) {
        Taxon accName = new Taxon();
        TaxonName taxonName = new TaxonName();

        String resName = taxon.getScientificname();
        taxonName.setFullName(resName + " " + taxon.getAuthority());

        taxonName.setCanonicalName(resName);

        taxonName.setRank(taxon.getRank());
        taxonName.setAuthorship(taxon.getAuthority());

        accName.setTaxonName(taxonName);
        accName.setTaxonomicStatus(taxon.getStatus());

        Taxon.Info info = new Taxon.Info();
        info.setUrl(taxon.getUrl());
        accName.setInfo(info);


        //FIXME : To fill in
        String sourceUrl = taxon.getUrl();
        String sourceDatasetID = "";
        String sourceDatasetName = "";
        String sourceName = "";

        Source source = new Source();
        source.setDatasetID(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accName.setSource(source);

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
            Synonym synonym = new Synonym();

            TaxonName taxonName = new TaxonName();

            String resName = synRecord.getScientificname();
            taxonName.setFullName(resName + " " + synRecord.getAuthority());

            taxonName.setCanonicalName(resName);

            taxonName.setRank(synRecord.getRank());
            taxonName.setAuthorship(synRecord.getAuthority());

            synonym.setTaxonName(taxonName);
            synonym.setTaxonomicStatus(synRecord.getStatus());

            Synonym.Info info = new Synonym.Info();
            info.setUrl(synRecord.getUrl());
            synonym.setInfo(info);

            //FIXME : To fill in
            String sourceUrl = synRecord.getUrl();
            String sourceDatasetID =  "";
            String sourceDatasetName = "";
            String sourceName = "";

            Source source = new Source();
            source.setDatasetID(sourceDatasetID);
            source.setDatasetName(sourceDatasetName);
            source.setName(sourceName);
            source.setUrl(sourceUrl);
            synonym.setSource(source);

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
                tnrResponseFromRecord(pesinspt, record);
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
                    TnrResponse tnrResponse = tnrResponseFromRecord(pesinspt, record);
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecords method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecords method in PESINameService");
        }

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

    @Override
    public void resolveVernacularNames(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * @param pesinspt
     * @param record
     * @throws RemoteException
     */
    private TnrResponse tnrResponseFromRecord(PESINameServicePortType pesinspt, PESIRecord record) throws RemoteException {

        TnrResponse tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        String accNameGUID = record.getValid_guid();
        tnrResponse.setMatchingNameString(record.getScientificname());

        // case when accepted name
        if(record.getGUID().equals(record.getValid_guid())) {
            Taxon accName = generateAccName(record);
            tnrResponse.setTaxon(accName);
            tnrResponse.setMatchingNameType(NameType.TAXON);
        } else {
            // case when synonym
            PESIRecord accNameRecord = pesinspt.getPESIRecordByGUID(accNameGUID);
            Taxon accName = generateAccName(accNameRecord);
            tnrResponse.setTaxon(accName);
            tnrResponse.setMatchingNameType(NameType.SYNONYM);
        }

        PESIRecord[] records = pesinspt.getPESISynonymsByGUID(accNameGUID);
        if(records != null && records.length > 0) {
            generateSynonyms(records,tnrResponse);
        }

        return tnrResponse;
    }
}


