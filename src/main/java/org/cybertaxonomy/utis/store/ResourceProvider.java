/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;

/**
 * @author a.kohlbecker
 * @date Nov 9, 2016
 *
 */
public interface ResourceProvider {

    /**
     * @param lastUpdated The date time when the data store was last updated with the resources from the provider
     *
     * @return
     * @throws DRFChecklistException
     */
    public List<URI> getResources(Date lastUpdated) throws DRFChecklistException;

}
