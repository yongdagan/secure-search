package com.gmail.yongdagan.secure_search.service;

import com.gmail.yongdagan.secure_search.persist.dataobject.Account;

public interface AccountManager {
	public Account register(String username, String password) throws ServiceException;
	public Account login(String username, String password) throws ServiceException;
	public boolean updateInfo(Account account, String oldPassword, String newPassword, String addKey) throws ServiceException;
	public void updateCapacity(Account account, Long capacity) throws ServiceException;
}
