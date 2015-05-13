package com.gmail.yongdagan.secure_search.persist.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.gmail.yongdagan.secure_search.persist.dataobject.Term;
import com.ibatis.sqlmap.client.SqlMapClient;

public class IbatisTermDAO implements TermDAO {
	
	private SqlMapClient sqlMapClient;
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	@Override
	public Term getTermById(Long id) throws PersistException {
		Term term = null;
		try {
			List<Term> terms = sqlMapClient.queryForList("Term.getTermById", id);
			if(!terms.isEmpty()) {
				term = terms.get(0);
			}
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return term;
	}

	@Override
	public List<Term> getTerms(Long accountId, String name) throws PersistException {
		List<Term> terms = null;
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("accountId", accountId);
			map.put("name", name);
			
			terms = sqlMapClient.queryForList("Term.getTerm", map);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return terms;
	}

	@Override
	public Long addTerm(Term term) throws PersistException {
		Long id = -1L;
		try {
			id = (Long) sqlMapClient.insert("Term.addTerm", term);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return id;
	}

	@Override
	public int deleteTerm(Long id) throws PersistException {
		int n = 0;
		try {
			n = sqlMapClient.delete("Term.deleteTerm", id);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public int deleteTermsByAccountId(Long accountId) throws PersistException {
		int n = 0;
		try {
			n = sqlMapClient.delete("Term.deleteTermsByAccountId", accountId);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public void addTermList(List<Term> terms) throws PersistException {
		try {
			sqlMapClient.insert("Term.addTermList", terms);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
	}

}
