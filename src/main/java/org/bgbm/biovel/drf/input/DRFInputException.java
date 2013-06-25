package org.bgbm.biovel.drf.input;

public class DRFInputException extends Exception {
	
	public DRFInputException(String mesg) {
		super(mesg);
	}

	public DRFInputException(Exception e) {
		super(e);
	}
	
	public DRFInputException(Error e) {
		super(e);
	}
}
