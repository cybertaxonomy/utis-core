package org.cybertaxonomy.utis.checklist;


import java.util.EnumSet;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhycobankClient extends AbstractCdmServerClient {

    private static final Logger logger = LoggerFactory.getLogger(PhycobankClient.class);

    public static final String ID = "bgbm-phycobank";
    public static final String LABEL = "Phycobank";
    public static final String DOC_URL = "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html";
    public static final String COPYRIGHT_URL = "http://cybertaxonomy.eu/cdmlib/license.html";

    // phycobank-production
    // private static final String SERVER_PATH_PREFIX = "/";
    // private static final HttpHost HTTP_HOST = new HttpHost("api.phycobank.org", 80);

    // edit-test
    // private static final String SERVER_PATH_PREFIX = "/cdmserver/";
    // private static final HttpHost HTTP_HOST = new HttpHost("test.e-taxonomy.eu", 80);

    // localhost
    private static final String SERVER_PATH_PREFIX = "/";
    private static final HttpHost HTTP_HOST = new HttpHost("localhost", 8080);


    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.noneOf(ClassificationAction.class);

    public PhycobankClient() {
        super(HTTP_HOST, SERVER_PATH_PREFIX);
    }

    public PhycobankClient(String checklistInfoJson) throws DRFChecklistException {
        super(HTTP_HOST, SERVER_PATH_PREFIX, checklistInfoJson);
    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {

        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,DOC_URL,COPYRIGHT_URL, getSearchModes());

        ServiceProviderInfo phycobank = new ServiceProviderInfo(
                "phycobank", // id
                "PhycoBank", // label
                "http://www.phycobank.org", // "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html"
                "http://www.catalogueoflife.org/col/info/copyright",
                ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        //phycobank.setDefaultClassificationId("----- NA ----"); // no suitable default classification in phycobank
        phycobank.getSupportedActions().addAll(SEARCH_MODES);
        phycobank.getSupportedActions().addAll(CLASSIFICATION_ACTION);
        checklistInfo.addSubChecklist(phycobank);

        return checklistInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String generateTaxonURL(UUID taxonUUID, ServiceProviderInfo subChecklist) {
        return "https://www.phycobank.org/cdm_dataportal/taxon/" + taxonUUID.toString();
    }

}
