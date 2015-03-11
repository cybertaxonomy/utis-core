package org.bgbm.biovel.drf.checklist;


import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.checklist.pesi.PESINameServiceLocator;
import org.bgbm.biovel.drf.checklist.pesi.PESINameServicePortType;
import org.bgbm.biovel.drf.checklist.pesi.PESIRecord;
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
import org.bgbm.biovel.drf.utils.TnrMsgUtils;


public class PESIClient extends BaseChecklistClient {

    public static final String ID = "pesi";
    public static final String LABEL = "PESI";
    public static final String URL = "http://www.eu-nomen.eu/portal/index.php";
    public static final String DATA_AGR_URL = "";

    private static Pattern citationPattern = Pattern.compile("^(.*)\\p{Punct} [Aa]ccessed through\\p{Punct}? (PESI|Euro\\+Med PlantBase|Index Fungorum|Fauna Europaea|European Register of Marine Species) at (.*)$");

    // to match e.g.: http://www.eu-nomen.eu/portal/taxon.php?GUID=urn:lsid:marinespecies.org:taxname:140389
    private static Pattern Lsid_pattern = Pattern.compile(".*(urn:lsid:\\S*).*");

    // to match e.g.: http://www.eu-nomen.eu/portal/taxon.php?GUID=0EB6CA37-3365-4AF5-A800-8FC4B8C366FA
    private static Pattern uuid_pattern = Pattern.compile(".*GUID=([A-Z0-9\\-]{36}).*");

    public enum PESISources {
        EUROMED("Euro+Med Plantbase"),
        FAUNA_EUROPAEA ("Fauna Europaea"),
        ERMS ("European Register of Marine Species"),
        INDEX_FUNGORUM ("Index Fungorum");

        private String value;

        PESISources(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static PESISources fromValue(String v) {
            for (PESISources c: PESISources.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }

    }

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

    /**
     * @param sourceString
     * @return
     */
    private ParsedCitation parsePesiCitation(String sourceString) {

        ParsedCitation parsed = new ParsedCitation();

        Matcher m = citationPattern.matcher(sourceString);
        if (m.matches()) {

            if(!m.group(2).equals(PESISources.INDEX_FUNGORUM)){
                parsed.accordingTo = m.group(1);
            }
            parsed.sourceTaxonUrl = m.group(3);

        }
        return parsed;
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
        accName.setUrl(taxon.getUrl());

        String lsid = taxon.getValid_guid();
        accName.setIdentifier(lsid);

        String sourceString = taxon.getCitation(); // concatenation of sec. reference and url
        ParsedCitation parsed = parsePesiCitation(sourceString);
        accName.setAccordingTo(parsed.accordingTo);


        // TODO ask VLIZ for adding all the the sourceFKs to the service and fill additional the data in here
        if(parsed.sourceTaxonUrl != null){
            Source source = new Source();
            source.setUrl(parsed.sourceTaxonUrl);
            source.setTitle(parsed.accordingTo);
            source.setIdentifier(lsid);
            accName.getSources().add(source);
        }

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

    private void generateSynonyms(PESIRecord[] synonyms, Response tnrResponse) {

        for(PESIRecord synRecord : synonyms) {

            Synonym synonym = new Synonym();

            TaxonName taxonName = new TaxonName();

            String resName = synRecord.getScientificname();
            taxonName.setFullName(resName + " " + synRecord.getAuthority());

            taxonName.setCanonicalName(resName);

            String sourceString = synRecord.getCitation(); // concatenation of sec. reference and url
            ParsedCitation parsed = parsePesiCitation(sourceString);
            synonym.setAccordingTo(parsed.accordingTo);

            if(parsed.sourceTaxonUrl != null){
                Source source = new Source();
                source.setUrl(parsed.sourceTaxonUrl);
                source.setTitle(parsed.accordingTo);
                synonym.getSources().add(source);
            }

            taxonName.setRank(synRecord.getRank());
            taxonName.setAuthorship(synRecord.getAuthority());

            synonym.setTaxonName(taxonName);
            synonym.setTaxonomicStatus(synRecord.getStatus());

            synonym.setUrl(synRecord.getUrl());

            tnrResponse.getSynonym().add(synonym);
        }
    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecords(name, false);
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
                }
            }

        }  catch (RemoteException e) {
            logger.error("Error in getGUID method in PESINameService", e);
            throw new DRFChecklistException("Error in getGUID method in PESINameService");
        }

    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecords(name, true);
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
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
        String name = query.getRequest().getName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecordsByVernacular(name);
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
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
        String name = query.getRequest().getName();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecordsByVernacular("%" + name + "%");
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest());
                    query.getResponse().add(tnrResponse);
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
    private Response tnrResponseFromRecord(PESINameServicePortType pesinspt, PESIRecord record, Query.Request request) throws RemoteException {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        SearchMode searchMode = SearchMode.valueOf(request.getSearchMode());

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
            if(request.isAddSynonymy() &&  records != null && records.length > 0) {
                generateSynonyms(records,tnrResponse);
            }
        }

        return tnrResponse;
    }

    class ParsedCitation {

        String accordingTo = null;
        String sourceTaxonUrl = null;
    }
}


