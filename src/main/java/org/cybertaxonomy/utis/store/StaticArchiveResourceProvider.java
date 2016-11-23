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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;

/**
 * @author a.kohlbecker
 * @date Nov 10, 2016
 *
 */
public class StaticArchiveResourceProvider implements ResourceProvider {

    private List<URI> resources = new ArrayList<>();

    public StaticArchiveResourceProvider(String ... resources) throws URISyntaxException{
        for(String r : resources){
            this.resources.add(new URI(r));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<URI> getResources(Date lastUpdated) throws DRFChecklistException {

        return Collections.unmodifiableList(resources);
    }

}
