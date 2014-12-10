package org.bgbm.biovel.drf.checklist;


import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.worms.AphiaNameServiceLocator;
import org.bgbm.biovel.drf.checklist.worms.AphiaNameServicePortType;
import org.bgbm.biovel.drf.checklist.worms.AphiaRecord;
import org.bgbm.biovel.drf.tnr.msg.AcceptedName;
import org.bgbm.biovel.drf.tnr.msg.NameType;
import org.bgbm.biovel.drf.tnr.msg.ScrutinyType;
import org.bgbm.biovel.drf.tnr.msg.SourceType;
import org.bgbm.biovel.drf.tnr.msg.TaxonNameType;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse.Synonym;


public class WoRMSClient extends BaseChecklistClient {

    public static final String ID = "worms";
    public static final String LABEL = "WoRMS";
    public static final String URL = "http://www.marinespecies.org/index.php";
    public static final String DATA_AGR_URL = "";


    public WoRMSClient() {
        super();

    }

    @Override
    public HttpHost getHost() {
        return new HttpHost("http://www.marinespecies.org",80);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL);
        return checklistInfo;
    }


    @Override
    public void resolveNames(TnrMsg tnrMsg) throws DRFChecklistException {
        List<TnrMsg.Query> queryList = tnrMsg.getQuery();
        if(queryList.size() ==  0) {
            throw new DRFChecklistException("WoRMS query list is empty");
        }

        if(queryList.size() > 1) {
            throw new DRFChecklistException("WoRMS query list has more than one query");
        }
        Query query = queryList.get(0);

        String name = query.getTnrRequest().getTaxonName().getName().getNameComplete();

        AphiaNameServiceLocator aphiansl = new AphiaNameServiceLocator();

        AphiaNameServicePortType aphianspt;
        try {
            aphianspt = aphiansl.getAphiaNameServicePort();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DRFChecklistException("Error in accessing PESINameService");
        }
        updateQueryWithResponse(query,aphianspt,name,getServiceProviderInfo());
    }


    @Override
    public int getMaxPageSize() {
        return 10;
    }



    private void updateQueryWithResponse(Query query ,
            AphiaNameServicePortType aphianspt,
            String name,
            ServiceProviderInfo ci) throws DRFChecklistException {

        try {
            AphiaRecord record = null;
            try {
                Integer nameAphiaID = aphianspt.getAphiaID(name, false);
                System.out.println("nameAphiaID : " + nameAphiaID);
                record = aphianspt.getAphiaRecordByID(nameAphiaID);
            } catch(NullPointerException npe) {
                //FIXME : Workaround for NPE thrown by the aphia stub due to a,
                //        null aphia id (Integer), when the name is not present
                //        in the db
                record = null;
            }
            if(record != null) {
                TnrResponse tnrResponse = new TnrResponse();

                tnrResponse.setChecklist(ci.getLabel());
                tnrResponse.setChecklistUrl(ci.getUrl());

                int accNameGUID = record.getValid_AphiaID();

                // case when accepted name
                if(record.getAphiaID() == accNameGUID) {
                    AcceptedName accName = generateAccName(record);
                    tnrResponse.setAcceptedName(accName);
                } else {
                    // case when synonym
                    AphiaRecord accNameRecord = aphianspt.getAphiaRecordByID(accNameGUID);
                    AcceptedName accName = generateAccName(accNameRecord);
                    tnrResponse.setAcceptedName(accName);
                }

                AphiaRecord[] records = aphianspt.getAphiaSynonymsByID(accNameGUID);
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

    private AcceptedName generateAccName(AphiaRecord taxon) {
        AcceptedName accName = new AcceptedName();
        TaxonNameType taxonName = new TaxonNameType();
        NameType name = new NameType();

        String resName = taxon.getScientificname();
        name.setNameComplete(resName + " " + taxon.getAuthority());

        name.setNameCanonical(resName);
        name.setTaxonomicStatus(taxon.getStatus());

        taxonName.setRank(taxon.getRank());
        taxonName.setAuthorship(taxon.getAuthority());
        taxonName.setName(name);

        accName.setTaxonName(taxonName);

        AcceptedName.Info info = new AcceptedName.Info();
        info.setUrl(taxon.getUrl());
        accName.setInfo(info);


        //FIXME : To fill in
        String sourceUrl = taxon.getUrl();
        String sourceDatasetID = "";
        String sourceDatasetName = "";
        String sourceName = "";

        SourceType source = new SourceType();
        source.setDatasetID(sourceDatasetID);
        source.setDatasetName(sourceDatasetName);
        source.setName(sourceName);
        source.setUrl(sourceUrl);
        accName.setSource(source);

        //FIXME : To fill in
        String accordingTo = taxon.getAuthority();
        String modified = "";

        ScrutinyType scrutiny = new ScrutinyType();
        scrutiny.setAccordingTo(accordingTo);
        scrutiny.setModified(modified);
        accName.setScrutiny(scrutiny);

        AcceptedName.Classification c = new AcceptedName.Classification();
        c.setKingdom(taxon.getKingdom());
        c.setPhylum(taxon.getPhylum());
        c.setClazz("");
        c.setOrder(taxon.getOrder());
        c.setFamily(taxon.getFamily());
        c.setGenus(taxon.getGenus());
        accName.setClassification(c);

        return accName;
    }


    private void generateSynonyms(AphiaRecord[] synonyms, TnrResponse tnrResponse) {

        for(AphiaRecord synRecord : synonyms) {
            TnrResponse.Synonym synonym = new Synonym();

            TaxonNameType taxonName = new TaxonNameType();
            NameType name = new NameType();

            String resName = synRecord.getScientificname();
            name.setNameComplete(resName + " " + synRecord.getAuthority());

            name.setNameCanonical(resName);
            name.setTaxonomicStatus(synRecord.getStatus());

            taxonName.setRank(synRecord.getRank());
            taxonName.setAuthorship(synRecord.getAuthority());
            taxonName.setName(name);

            synonym.setTaxonName(taxonName);

            Synonym.Info info = new Synonym.Info();
            info.setUrl(synRecord.getUrl());
            synonym.setInfo(info);

            //FIXME : To fill in
            String sourceUrl = synRecord.getUrl();
            String sourceDatasetID =  "";
            String sourceDatasetName = "";
            String sourceName = "";

            SourceType source = new SourceType();
            source.setDatasetID(sourceDatasetID);
            source.setDatasetName(sourceDatasetName);
            source.setName(sourceName);
            source.setUrl(sourceUrl);
            synonym.setSource(source);

            //FIXME : To fill in
            String accordingTo = synRecord.getAuthority();
            String modified = "";

            ScrutinyType scrutiny = new ScrutinyType();
            scrutiny.setAccordingTo(accordingTo);
            scrutiny.setModified(modified);
            synonym.setScrutiny(scrutiny);

            tnrResponse.getSynonym().add(synonym);
        }
    }
}


