package org.bgbm.biovel.drf.checklist;



import org.bgbm.biovel.drf.utils.JSONUtils;


public abstract class AggregateChecklistClient extends BaseChecklistClient {
	
	public AggregateChecklistClient() {		
		super();
		System.out.println("AggregateChecklistClient");		
	}	
		
	public AggregateChecklistClient(String checklistInfoJson) throws DRFChecklistException {		
		setChecklistInfo(JSONUtils.convertJsonToObject(checklistInfoJson, BaseChecklistClient.ChecklistInfo.class));		
	}	
	
}
