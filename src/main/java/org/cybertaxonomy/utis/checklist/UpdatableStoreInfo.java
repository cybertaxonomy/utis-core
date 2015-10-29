// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

/**
 * @author a.kohlbecker
 * @date Oct 29, 2015
 *
 */
public interface UpdatableStoreInfo {

    public String getTestUrl();

    public String[] updatableResources();

    public int pollIntervalMinutes();

}
