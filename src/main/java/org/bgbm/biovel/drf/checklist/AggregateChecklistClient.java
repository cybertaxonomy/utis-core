package org.bgbm.biovel.drf.checklist;

import org.bgbm.biovel.drf.client.ServiceProviderInfo;
import org.bgbm.biovel.drf.query.IQueryClient;


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
