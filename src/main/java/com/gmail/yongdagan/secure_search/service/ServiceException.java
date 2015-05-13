package com.gmail.yongdagan.secure_search.service;

public class ServiceException extends Exception {

	private static final long serialVersionUID = -6112979464681583510L;
	
	public ServiceException(String msg) {
		super(msg);
	}
	
	public ServiceException(Throwable e) {
		super(e);
	}

}
