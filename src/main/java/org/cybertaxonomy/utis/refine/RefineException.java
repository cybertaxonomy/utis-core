package org.cybertaxonomy.utis.refine;

public class RefineException extends Exception {

	public RefineException(Exception e) {
		super(e);
	}

	public RefineException(Error e) {
		super(e);
	}
	
	public RefineException(String msg) {
		super(msg);
	}
}
