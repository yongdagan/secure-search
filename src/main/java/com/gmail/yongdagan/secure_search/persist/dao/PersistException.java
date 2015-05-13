package com.gmail.yongdagan.secure_search.persist.dao;

public class PersistException extends Exception {

	private static final long serialVersionUID = -6112979464681583510L;
	
	public PersistException(String msg) {
		super(msg);
	}
	
	public PersistException(Throwable e) {
		super(e);
	}

}
