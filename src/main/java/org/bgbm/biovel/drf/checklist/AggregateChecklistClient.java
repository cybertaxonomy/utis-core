package org.bgbm.biovel.drf.checklist;

import org.bgbm.biovel.drf.rest.ServiceProviderInfo;


public abstract class AggregateChecklistClient extends BaseChecklistClient {
	
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
