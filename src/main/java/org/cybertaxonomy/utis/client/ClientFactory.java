// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Oct 29, 2015
 *
 */
public class ClientFactory {

    protected Logger logger = LoggerFactory.getLogger(ClientFactory.class);

    public <T extends AbstractClient<?>> T newClient(Class<T> clientClass) {

        T instance = null;

        logger.debug("Creating new instance of " + clientClass.getName());
        try {
            instance = clientClass.newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return instance;
    }

}
