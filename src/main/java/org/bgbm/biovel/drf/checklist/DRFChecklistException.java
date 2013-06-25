package org.bgbm.biovel.drf.checklist;


public class DRFChecklistException extends Exception {

	public DRFChecklistException(Exception e) {
		super(e);
	}

	public DRFChecklistException(String mesg) {
		super(mesg);
	}

}
