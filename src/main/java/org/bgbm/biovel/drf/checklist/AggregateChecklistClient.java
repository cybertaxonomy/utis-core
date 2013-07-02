package org.bgbm.biovel.drf.checklist;


public abstract class AggregateChecklistClient extends BaseChecklistClient {
	
	public AggregateChecklistClient() {		
		super();		
	}	
		
	public AggregateChecklistClient(String checklistInfoJson) throws DRFChecklistException {	
		super(checklistInfoJson);			
	}	
	
}
