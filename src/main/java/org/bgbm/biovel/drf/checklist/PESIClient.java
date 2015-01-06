package org.bgbm.biovel.drf.checklist;


import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.pesi.PESINameServiceLocator;
import org.bgbm.biovel.drf.checklist.pesi.PESINameServicePortType;
import org.bgbm.biovel.drf.checklist.pesi.PESIRecord;
import org.bgbm.biovel.drf.tnr.msg.Classification;
import org.bgbm.biovel.drf.tnr.msg.Scrutiny;
import org.bgbm.biovel.drf.tnr.msg.Source;
import org.bgbm.biovel.drf.tnr.msg.Synonym;
import org.bgbm.biovel.drf.tnr.msg.Taxon;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;


public class PESIClient extends BaseChecklistClient {

    public static final String ID = "pesi";
    public static final String LABEL = "PESI";
    public static final String URL = "http://www.eu-nomen.eu/portal/index.php";
    public static final String DATA_AGR_URL = "";

    private static final EnumSet<SearchMode> capability = EnumSet.of(SearchMode.scientificNameExact);


    public PESIClient() {
        super();

    }

    @Override
    public HttpHost getHost() {
        // TODO Auto-generated method stub
        return new HttpHost("http://www.eu-nomen.eu",80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL);
        return checklistInfo;
    }


    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);

        //http://www.catalogueoflife.org/col/webservice?response=full&name={sciName}

        String name = query.getTnrRequest().getTaxonName().getFullName();

        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt;
        try {
            pesinspt = pesins.getPESINameServicePort();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DRFChecklistException("Error in accessing PESINameService");
        }
        updateQueryWithResponse(query,pesinspt,name,getServiceProviderInfo());
    }


    @Override
    public int getMaxPageSize() {
        return 10;
    }



    private void updateQueryWithResponse(Query query ,
            PESINameServicePortType pesinspt,
            String name,
            ServiceProviderInfo ci) throws DRFChecklistException {

        try {
            String nameGUID = pesinspt.getGUID(name);
            System.out.println("nameGUID : " + nameGUID);
            PESIRecord record = pesinspt.getPESIRecordByGUID(nameGUID);
            if(record != null) {
                TnrResponse tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

                String accNameGUID = record.getValid_guid();

                // case when accepted name
                if(record.getGUID().equals(record.getValid_guid())) {
                    Taxon accName = generateAccName(record);
                    tnrResponse.setTaxon(accName);
                } else {
                    // case when synonym
                    PESIRecord accNameRecord = pesinspt.getPESIRecordByGUID(accNameGUID);
                    Taxon accName = generateAccName(accNameRecord);
                    tnrResponse.setTaxon(accName);
                }

                PESIRecord[] records = pesinspt.getPESISynonymsByGUID(accNameGUID);
                if(records != null && records.length > 0) {
                    generateSynonyms(records,tnrResponse);
                }
                if(query != null) {
                    query.getTnrResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DRFChecklistException("Error in getGUID method in PESINameService");
        }

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
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolveVernacularNames(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return capability;
    }
}


