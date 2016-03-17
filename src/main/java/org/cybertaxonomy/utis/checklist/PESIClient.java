package org.cybertaxonomy.utis.checklist;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import org.apache.http.HttpHost;
import org.cybertaxonomy.utis.checklist.pesi.PESINameServiceLocator;
import org.cybertaxonomy.utis.checklist.pesi.PESINameServicePortType;
import org.cybertaxonomy.utis.checklist.pesi.PESIRecord;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.SoapClient;
import org.cybertaxonomy.utis.tnr.msg.HigherClassificationElement;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.Source;
import org.cybertaxonomy.utis.tnr.msg.Synonym;
import org.cybertaxonomy.utis.tnr.msg.Taxon;
import org.cybertaxonomy.utis.tnr.msg.TaxonBase;
import org.cybertaxonomy.utis.tnr.msg.TaxonName;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.IdentifierUtils;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PESIClient extends BaseChecklistClient<SoapClient> {

    private static final Logger logger = LoggerFactory.getLogger(PESIClient.class);

    private static final HttpHost HTTP_HOST = new HttpHost("http://www.eu-nomen.eu",80);
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
            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.of(
            ClassificationAction.higherClassification
            );

    public static final EnumSet<SearchMode> SCIENTIFICNAME_SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike
            );


    public PESIClient() {
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

    private Taxon generateTaxon(PESIRecord taxonRecord, boolean addClassification) {


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

        String lsid = taxonRecord.getValid_guid();
        taxon.setIdentifier(lsid);

        String secReference  = addSources(taxonRecord.getGUID(), taxon);
        taxon.setAccordingTo(secReference);

        if(addClassification) {
            String[] rankNames = new String[] {"Genus", "Family", "Order", "_class", "Phylum", "Kingdom"};
            Method getRankLevelMethod = null;
            for(String rankName : rankNames) {
                try {
                    getRankLevelMethod = taxonRecord.getClass().getDeclaredMethod("get" + rankName, (Class<?>[])null);
                    String higherTaxonName = getRankLevelMethod.invoke(taxonRecord).toString();
                    HigherClassificationElement hce = new HigherClassificationElement();
                    hce.setScientificName(higherTaxonName);
                    if(rankName.equals("_class")) {
                        rankName  = "Class";
                    }
                    hce.setRank(rankName);
                    taxon.getHigherClassification().add(hce);
                } catch(NullPointerException e) {
                    // IGNORE, just try the next rank

                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return taxon;
    }

    private void generateSynonyms(PESIRecord[] synonyms, Response tnrResponse) {

        for(PESIRecord synRecord : synonyms) {

            Synonym synonym = new Synonym();

            TaxonName taxonName = new TaxonName();

            String resName = synRecord.getScientificname();
            taxonName.setScientificName(resName + " " + synRecord.getAuthority());

            taxonName.setCanonicalName(resName);

            if(logger.isDebugEnabled()) {
                logger.debug("source citation is " + synRecord.getCitation());
            }

            taxonName.setRank(synRecord.getRank());
            taxonName.setAuthorship(synRecord.getAuthority());

            synonym.setTaxonName(taxonName);
            synonym.setTaxonomicStatus(synRecord.getStatus());

            synonym.setUrl(synRecord.getUrl());

            String secReference = addSources(synRecord.getGUID(), synonym);
            synonym.setAccordingTo(secReference);

            tnrResponse.getSynonym().add(synonym);
        }
    }

    /**
     *
     * @param synRecord
     * @param taxonBase
     *
     * @return
     *    A string containing the creator property of the
     *   first source returned by the PESI web service. This citation actually is the secReference!
     */
    private String addSources(String guid, TaxonBase taxonBase) {
        /*
         * The source citation info is concatenated into one string which is split in this example into the 3 main sections:
         *
         * 1.] "Marhold, K. (2011): Caryophyllaceae. – In: Euro+Med Plantbase - the information resource for Euro-Mediterranean plant diversity. "
         * 2.] "Cerastium vulgatum subsp. caespitosum (Asch.) Dostál"
         * 3.] "http://ww2.bgbm.org/euroPlusMed/PTaxonDetail.asp?UUID=9002CE99-A6A3-4BF3-9EBD-8C197E440752"
         *
         * 1. = source citation = sec reference of synonym
         * 2. = name in source = synonym
         * 3. = Uri to the name in source = uri to the synonym
         */
        String secReference = null;
        PESINameServiceLocator pesins = new PESINameServiceLocator();
        try {
            PESINameServicePortType pesinspt = getPESINameService(pesins);

            org.cybertaxonomy.utis.checklist.pesi.Source[] pesiSources = pesinspt.getPESISourcesByGUID(guid);
            if(logger.isDebugEnabled()) {
                logger.debug(pesiSources.length + " sources found for taxon (" + taxonBase.getTaxonName().getScientificName() + ") GUID " + guid);
            }
            for(org.cybertaxonomy.utis.checklist.pesi.Source sourceRecord : pesiSources) {
                Source source = new Source();
                if(sourceRecord.getTitle() == null && sourceRecord.getBibliographicCitation() == null) {
                    logger.error("A source should at least have title or  bibliographicCitation " + guid);
                }
                if(sourceRecord.getType().equals("nameAccordingTo")) {
                    secReference = sourceRecord.getTitle();
                    continue;
                }
                source.setTitle(sourceRecord.getBibliographicCitation()); // source citation
                source.setIdentifier(sourceRecord.getIdentifier()); // often contains the URI to the source, in case of marine species (see 'Salmo'), other wise it is null
                taxonBase.getSources().add(source);
            }
        } catch (DRFChecklistException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return secReference;
    }

    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();

        PESINameServiceLocator pesins = new PESINameServiceLocator();
        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecords(name, false);
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest(), false);
                    query.getResponse().add(tnrResponse);
                }
            }

        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecords method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecords method in PESINameService");
        }

    }

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();

        PESINameServiceLocator pesins = new PESINameServiceLocator();
        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecords(name, true);
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest(), false);
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
        String name = query.getRequest().getQueryString();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecordsByVernacular(name);
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest(), false);
                    query.getResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecordsByVernacular method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecordsByVernacular method in PESINameService");
        }

    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord[] records = pesinspt.getPESIRecordsByVernacular("%" + name + "%");
            if(records != null){
                for (PESIRecord record : records) {
                    Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest(), false);
                    query.getResponse().add(tnrResponse);
                }
            }
        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecordsByVernacular method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecordsByVernacular method in PESINameService");
        }

    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {

        _findByIdenifier(tnrMsg, false);
    }

    /**
     * @param tnrMsg
     * @param addClassification TODO
     * @throws DRFChecklistException
     */
    private void _findByIdenifier(TnrMsg tnrMsg, boolean addClassification) throws DRFChecklistException {
        Query query = singleQueryFrom(tnrMsg);
        String name = query.getRequest().getQueryString();
        PESINameServiceLocator pesins = new PESINameServiceLocator();

        PESINameServicePortType pesinspt = getPESINameService(pesins);

        try {
            PESIRecord record = pesinspt.getPESIRecordByGUID(name);
            if(record != null){
                Response tnrResponse = tnrResponseFromRecord(pesinspt, record, query.getRequest(), addClassification);
                query.getResponse().add(tnrResponse);
            }

        }  catch (RemoteException e) {
            logger.error("Error in getPESIRecordByGUID method in PESINameService", e);
            throw new DRFChecklistException("Error in getPESIRecordByGUID method in PESINameService");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("taxonomicChildren mode not supported by " + this.getClass().getSimpleName());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {

        _findByIdenifier(tnrMsg, true);
    }

    /**
     * @param pesinspt
     * @param record
     * @param addClassification TODO
     * @param searchMode TODO
     * @throws RemoteException
     */
    private Response tnrResponseFromRecord(PESINameServicePortType pesinspt, PESIRecord record, Query.Request request, boolean addClassification) throws RemoteException {

        Response tnrResponse = TnrMsgUtils.tnrResponseFor(getServiceProviderInfo());

        SearchMode searchMode = SearchMode.valueOf(request.getSearchMode());

        String taxonGUID = record.getValid_guid();
        if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
            tnrResponse.setMatchingNameString(record.getScientificname());
        }

        if(taxonGUID != null){

            // case when accepted name
            if(record.getGUID() != null && record.getGUID().equals(taxonGUID)) {
                Taxon taxon = generateTaxon(record, addClassification);
                tnrResponse.setTaxon(taxon);
                if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                    tnrResponse.setMatchingNameType(NameType.TAXON);
                }
            } else {
                // case when synonym
                PESIRecord taxonRecord = pesinspt.getPESIRecordByGUID(taxonGUID);
                Taxon taxon = generateTaxon(taxonRecord, false);
                tnrResponse.setTaxon(taxon);
                if(SCIENTIFICNAME_SEARCH_MODES.contains(searchMode)){
                    tnrResponse.setMatchingNameType(NameType.SYNONYM);
                }
            }

            // FIXME check for isAddSynonymy prior doing the request
            PESIRecord[] records = pesinspt.getPESISynonymsByGUID(taxonGUID);
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

    @Override
    public boolean isSupportedIdentifier(String value) {
        return IdentifierUtils.checkLSID(value) || IdentifierUtils.checkUUID(value);
    }

}


