package org.cybertaxonomy.utis.checklist;


import java.util.UUID;

import org.apache.http.HttpHost;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;

public class BgbmEditClient extends AbstractCdmServerClient {

    public static final String ID = "bgbm-cdm-server";
    public static final String LABEL = "Name catalogues served by the BGBM CDM Server";
    public static final String DOC_URL = "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html";
    public static final String COPYRIGHT_URL = "http://cybertaxonomy.eu/cdmlib/license.html";

    private static final HttpHost HTTP_HOST = new HttpHost("api.cybertaxonomy.org", 80);
    private static final String SERVER_PATH_PREFIX = "/";

    // edit-test
    // private static final String SERVER_PATH_PREFIX = "/cdmserver/";
    // private static final HttpHost HTTP_HOST = new HttpHost("test.e-taxonomy.eu", 80);

    // localhost
//    private static final String SERVER_PATH_PREFIX = "/";
//    private static final HttpHost HTTP_HOST = new HttpHost("localhost", 8080);



    public BgbmEditClient() {
        super(HTTP_HOST, SERVER_PATH_PREFIX);
    }

    public BgbmEditClient(String checklistInfoJson) throws DRFChecklistException {
        super(HTTP_HOST, SERVER_PATH_PREFIX, checklistInfoJson);
    }


    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {

        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,DOC_URL,COPYRIGHT_URL, getSearchModes());

        ServiceProviderInfo col = new ServiceProviderInfo("col",
                "Catalogue Of Life (EDIT - name catalogue end point)",
                "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "http://www.catalogueoflife.org/col/info/copyright", ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        col.setDefaultClassificationId("29d4011f-a6dd-4081-beb8-559ba6b84a6b");
        col.getSupportedActions().addAll(SEARCH_MODES);
        col.getSupportedActions().addAll(CLASSIFICATION_ACTION);
        checklistInfo.addSubChecklist(col);

        ServiceProviderInfo euromed = new ServiceProviderInfo("euromed",
                "Euro+Med",
                "http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html",
                "© Botanic Garden and Botanical Museum Berlin-Dahlem 2006", ServiceProviderInfo.DEFAULT_SEARCH_MODE);
        euromed.setDefaultClassificationId("314a68f9-8449-495a-91c2-92fde8bcf344");
        euromed.getSupportedActions().addAll(SEARCH_MODES);
        euromed.getSupportedActions().addAll(CLASSIFICATION_ACTION);
        checklistInfo.addSubChecklist(euromed);

        return checklistInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String generateTaxonURL(UUID taxonUUID, ServiceProviderInfo subChecklist) {
        return HTTP_HOST.toString() + "/" + subChecklist.getId() + " /taxon/" + taxonUUID.toString();
    }





}
