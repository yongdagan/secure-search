package com.gmail.yongdagan.secure_search.service;

import java.security.spec.InvalidKeySpecException;

import com.gmail.yongdagan.secure_search.persist.dao.AccountDAO;
import com.gmail.yongdagan.secure_search.persist.dao.PersistException;
import com.gmail.yongdagan.secure_search.persist.dataobject.Account;
import com.gmail.yongdagan.secure_search.persist.dataobject.CryptoUtil;

public class AccountManagerImpl implements AccountManager {
	
	private AccountDAO accountDAO;

	public void setAccountDAO(AccountDAO accountDAO) {
		this.accountDAO = accountDAO;
	}
	
	@Override
	public Account register(String username, String password)
			throws ServiceException {
		Account account = null;
		try {
			account = accountDAO.getAccountByUsername(username);
			if(account != null) {
				return null;
			}
			account = new Account();
			account.setUsername(username);
			account.setPassword(generateRealPassword(password));
			account.setCapacity(0L);
			accountDAO.addAccount(account);
		} catch (Exception e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return account;
	}

	@Override
	public Account login(String username, String password)
			throws ServiceException {
		Account account = null;
		try {
			account = accountDAO.getAccountByUsername(username);
			if(account == null || !checkPassword(account.getPassword(), password)) {
				return null;
			}
		} catch (Exception e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return account;
	}

	@Override
	public boolean updateInfo(Account account, String oldPassword, String newPassword, String addKey) throws ServiceException {
		try {
			if(!checkPassword(account.getPassword(), oldPassword)) {
				return false;
			}
		} catch (InvalidKeySpecException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		if(!newPassword.equals("")) {
			try {
				account.setPassword(generateRealPassword(newPassword));
			} catch (InvalidKeySpecException e) {
				throw new ServiceException(this.getClass().getName() + e);
			}
		}
		if(!addKey.equals("")) {
			account.setAddKey(addKey);
		}
		try {
			accountDAO.updateAccount(account);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return true;
	}
	
	private String generateRealPassword(String password) throws InvalidKeySpecException {
		// add salt
		byte[] salt = CryptoUtil.initSalt();
		byte[] tmp = CryptoUtil.hmacPasswordSalt(password.toCharArray(), salt);
		return CryptoUtil.encodeBASE64(tmp) + " " + CryptoUtil.encodeBASE64(salt);
	}
	
	private boolean checkPassword(String oldPassword, String password) throws InvalidKeySpecException {
		String[] tmp = oldPassword.split(" ");
		byte[] oldHmac = CryptoUtil.decodeBASE64(tmp[0]);
		byte[] salt = CryptoUtil.decodeBASE64(tmp[1]);
		
		// check new hmac
		byte[] newHmac = CryptoUtil.hmacPasswordSalt(password.toCharArray(), salt);
		for(int i = 0; i < oldHmac.length; i ++) {
			if(oldHmac[i] != newHmac[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateCapacity(Account account, Long capacity)
			throws ServiceException {
		account.setCapacity(capacity);
		try {
			accountDAO.updateAccount(account);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
	}

}
