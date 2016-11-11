/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import org.cybertaxonomy.utis.store.LastModifiedProvider;
import org.cybertaxonomy.utis.store.ResourceProvider;

/**
 * @author a.kohlbecker
 * @date Oct 29, 2015
 *
 */
public interface UpdatableStoreInfo {

    public String getTestUrl();

    public int pollIntervalMinutes();

    /**
     * Whether the local store should be updated by a big bulk update
     * which will overwrite the whole store with new data or if new data
     * will be added incrementally by adding new resources
     *
     * @return
     */
    public boolean doIncrementalUpdates();

    /**
     * Implementations can return <code>null</code> or an actual implementation of the @link LastModifiedProvider}.
     * The {@link Neo4jStoreUpdater) will use the fall back LastModifiedProvider method.
     * in case <code>null</code> is returned.
     *
     * @return
     */
    public LastModifiedProvider getLastModifiedProvider();

    public ResourceProvider getResourceProvider();

    /**
     *
     * @return
     */
    public String getInstanceName();


}
