package com.gmail.yongdagan.secure_search.persist.dao;

import java.sql.SQLException;
import java.util.List;

import com.gmail.yongdagan.secure_search.persist.dataobject.Account;
import com.ibatis.sqlmap.client.SqlMapClient;

public class IbatisAccountDAO implements AccountDAO {
	
	private SqlMapClient sqlMapClient;
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	@Override
	public Account getAccountById(Long id) throws PersistException {
		Account account = null;
		try {
			List<Account> accounts = sqlMapClient.queryForList("Account.getAccountById", id);
			if(!accounts.isEmpty()) {
				account = accounts.get(0);
			}
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return account;
	}

	@Override
	public Account getAccountByUsername(String username) throws PersistException {
		Account account = null;
		try {
			List<Account> accounts = sqlMapClient.queryForList("Account.getAccountByUsername", username);
			if(!accounts.isEmpty()) {
				account = accounts.get(0);
			}
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return account;
	}

	@Override
	public Long addAccount(Account account) throws PersistException {
		Long id = -1L;
		try {
			id = (Long) sqlMapClient.insert("Account.addAccount", account);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return id;
	}

	@Override
	public int updateAccount(Account account) throws PersistException {
		int n = 0;
		try {
			n = sqlMapClient.update("Account.updateAccount", account);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public int deleteAccount(Long id) throws PersistException {
		int n = 0;
		try {
			n = sqlMapClient.delete("Account.deleteAccount", id);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

}
