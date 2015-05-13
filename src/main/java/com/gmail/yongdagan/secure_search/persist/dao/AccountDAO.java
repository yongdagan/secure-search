package com.gmail.yongdagan.secure_search.persist.dao;

import com.gmail.yongdagan.secure_search.persist.dataobject.Account;

public interface AccountDAO {
	
	public Account getAccountById(Long id) throws PersistException;
	public Account getAccountByUsername(String username) throws PersistException;
	public Long addAccount(Account account) throws PersistException;
	public int updateAccount(Account account) throws PersistException;
	public int deleteAccount(Long id) throws PersistException;
	
}
