/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.util.EnumSet;

import org.cybertaxonomy.utis.checklist.worms.AphiaNameServiceLocator;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;

/**
 * @author a.kohlbecker
 * @since Jul 15, 2019
 *
 */
public class DiatomBaseClient extends WoRMSClient {

    public static final String ID = "diatombase";
    public static final String LABEL = "DiatomBase";
    public static final String URL = "http://www.diatombase.org/";
    public static final String DATA_AGR_URL = "";

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike,
            SearchMode.vernacularNameExact,
            SearchMode.vernacularNameLike,
            SearchMode.findByIdentifier
            );

    public static final EnumSet<SearchMode> SCIENTIFICNAME_SEARCH_MODES = EnumSet.of(
            SearchMode.scientificNameExact,
            SearchMode.scientificNameLike
            );

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.of(
            ClassificationAction.higherClassification,
            ClassificationAction.taxonomicChildren
            );



    public DiatomBaseClient() {
        super();

    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL, getSearchModes());
        return checklistInfo;
    }

    @Override
    protected AphiaNameServiceLocator aphiaServiceLocator() {
        AphiaNameServiceLocator aphiansl = new AphiaNameServiceLocator();
        // for WoRMS this is "http://www.marinespecies.org/aphia.php?p=soap"
        // the DiatomBase taxa are served through a different end point:
        aphiansl.setAphiaNameServicePortEndpointAddress("http://www.diatombase.org/aphia.php?p=soap");
        return aphiansl;
    }

}
