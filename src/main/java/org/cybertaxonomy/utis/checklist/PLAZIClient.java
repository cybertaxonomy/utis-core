// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.util.EnumSet;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.TinkerPopClient;
import org.cybertaxonomy.utis.store.Neo4jStore;
import org.cybertaxonomy.utis.store.Neo4jStoreManager;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;

/**
 * @author a.kohlbecker
 * @date Jun 17, 2016
 *
 */
public class PLAZIClient extends BaseChecklistClient<TinkerPopClient> implements UpdatableStoreInfo {

    public static final String ID = "plazi";
    public static final String LABEL = "PLAZI Treatment Bank";
    public static final String DOC_URL = "http://plazi.org/fusszeilen/info1/legal-disclaimer/";
    public static final String COPYRIGHT_URL = "https://creativecommons.org/publicdomain/zero/1.0/";

    /**
     * check for updates once a day
     */
    private static final int CHECK_UPDATE_MINUTES = 60 * 24;

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
//            SearchMode.scientificNameLike,
//            SearchMode.vernacularNameExact,
//            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.noneOf(ClassificationAction.class);

    private static final String DOWNLOAD_BASE_URL = "http://localhost/download/";

    private static final String TREATMENT_EXPORT_RDF_FILE_URL = DOWNLOAD_BASE_URL + "8A7687E6FFB3FFF6FF1A094FFE236AB9.rdf.gz";

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet getSearchModes() {
        return SEARCH_MODES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet getClassificationActions() {

        return CLASSIFICATION_ACTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupportedIdentifier(String value) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initQueryClient() {
        Neo4jStore neo4jStore = Neo4jStoreManager.provideStoreFor(this);
        queryClient = new TinkerPopClient(neo4jStore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID, LABEL, DOC_URL, COPYRIGHT_URL, getSearchModes());
        return checklistInfo;
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
    public String getTestUrl() {

        return TREATMENT_EXPORT_RDF_FILE_URL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] updatableResources() {
        return new String[] {TREATMENT_EXPORT_RDF_FILE_URL};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int pollIntervalMinutes() {
        return CHECK_UPDATE_MINUTES;
    }

}
