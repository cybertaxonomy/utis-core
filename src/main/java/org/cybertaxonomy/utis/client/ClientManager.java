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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Oct 29, 2015
 *
 */
public class ClientManager {

    protected Logger logger = LoggerFactory.getLogger(ClientManager.class);

    private final Map<Class<? extends AbstractClient<?>>, AbstractClient<?>> clientSingletonBeans = new HashMap<Class<? extends AbstractClient<?>>, AbstractClient<?>>();

    public <T extends AbstractClient<?>> T client(Class<T> clientClass) {

        T instance = null;

        if(!clientSingletonBeans.containsKey(clientClass)){
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

            if(instance.isStatelessClient()) {
                clientSingletonBeans.put(clientClass, instance);
            }
        } else {
            instance = (T) clientSingletonBeans.get(clientClass);
        }

        return instance;
    }

}
