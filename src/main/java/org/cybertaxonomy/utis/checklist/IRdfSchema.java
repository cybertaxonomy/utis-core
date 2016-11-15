/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

/**
 * @author a.kohlbecker
 * @date Nov 14, 2016
 *
 */
public interface IRdfSchema {

    public String schemaUri();

    public String abbreviation();

    public String property(String name);

}
