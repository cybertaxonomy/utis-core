// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

import java.util.Date;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;

/**
 * @author a.kohlbecker
 * @date Nov 9, 2016
 *
 */
public interface LastModifiedProvider {

    /**
     * @return
     * @throws DRFChecklistException
     */
    public Date getLastModified() throws DRFChecklistException;

}
