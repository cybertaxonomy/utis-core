package org.bgbm.biovel.drf.checklist;


import java.rmi.RemoteException;
import java.util.EnumSet;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.worms.AphiaNameServiceLocator;
import org.bgbm.biovel.drf.checklist.worms.AphiaNameServicePortType;
import org.bgbm.biovel.drf.checklist.worms.AphiaRecord;
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


public class WoRMSClient extends BaseChecklistClient {

    public static final String ID = "worms";
    public static final String LABEL = "WoRMS";
    public static final String URL = "http://www.marinespecies.org/index.php";
    public static final String DATA_AGR_URL = "";

    private static final EnumSet<SearchMode> capability = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike);


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
    public int getMaxPageSize() {
        return 10;
    }



    private Taxon generateAccName(AphiaRecord taxon) {
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


    private void generateSynonyms(AphiaRecord[] synonyms, TnrResponse tnrResponse) {

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

        AphiaNameServiceLocator aphiansl = new AphiaNameServiceLocator();

        AphiaNameServicePortType aphianspt;
        try {
            aphianspt = aphiansl.getAphiaNameServicePort();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new DRFChecklistException("Error in accessing PESINameService");
        }
        resolveScientificNamesExactUpdateQueryWithResponse(query,aphianspt,name,getServiceProviderInfo());
    }

    private void resolveScientificNamesExactUpdateQueryWithResponse(Query query ,
            AphiaNameServicePortType aphianspt,
            String name,
            ServiceProviderInfo ci) throws DRFChecklistException {

        try {
            AphiaRecord record = null;
            try {
                Integer nameAphiaID = aphianspt.getAphiaID(name, false);
                logger.debug("nameAphiaID : " + nameAphiaID);
                record = aphianspt.getAphiaRecordByID(nameAphiaID);
            } catch(NullPointerException npe) {
                //FIXME : Workaround for NPE thrown by the aphia stub due to a,
                //        null aphia id (Integer), when the name is not present
                //        in the db
                record = null;
            }
            if(record != null) {
                TnrResponse tnrResponse = TnrMsgUtils.tnrResponseFor(ci);

                int accNameGUID = record.getValid_AphiaID();

                // case when accepted name
                if(record.getAphiaID() == accNameGUID) {
                    Taxon accName = generateAccName(record);
                    tnrResponse.setTaxon(accName);
                } else {
                    // case when synonym
                    AphiaRecord accNameRecord = aphianspt.getAphiaRecordByID(accNameGUID);
                    Taxon accName = generateAccName(accNameRecord);
                    tnrResponse.setTaxon(accName);
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


