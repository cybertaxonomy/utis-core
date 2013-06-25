package org.bgbm.biovel.drf.utils;


public class TnrMsgException extends Exception{

	public TnrMsgException(Exception e) {
		super(e);
	}

	public TnrMsgException(Error e) {
		super(e);
	}
	

}
