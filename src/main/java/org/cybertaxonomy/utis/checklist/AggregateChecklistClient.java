package org.cybertaxonomy.utis.checklist;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.IQueryClient;


public abstract class AggregateChecklistClient<QC extends IQueryClient> extends BaseChecklistClient<QC> {

	public AggregateChecklistClient() {
		super();
	}

	public AggregateChecklistClient(String checklistInfoJson) throws DRFChecklistException {
		super(checklistInfoJson);
	}

	public AggregateChecklistClient(ServiceProviderInfo spiInfo) throws DRFChecklistException {
		super(spiInfo);
	}

}
