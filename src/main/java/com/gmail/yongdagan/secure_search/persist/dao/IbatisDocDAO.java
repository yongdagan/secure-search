package com.gmail.yongdagan.secure_search.persist.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;
import com.ibatis.sqlmap.client.SqlMapClient;

public class IbatisDocDAO implements DocDAO {
	
	private SqlMapClient sqlMapClient;
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	@Override
	public Doc getDoc(Long docId, Long accountId) throws PersistException {
		Doc doc = null;
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("docId", docId);
			map.put("accountId", accountId);
			
			List<Doc> docs = sqlMapClient.queryForList("Doc.getDoc", map);
			if(!docs.isEmpty()) {
				doc = docs.get(0);
			}
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return doc;
	}
	
	@Override
	public List<Doc> getDocsByAccountId(Long accountId, Long offset) throws PersistException {
		List<Doc> docs = null;
		try {
			HashMap<String, Long> map = new HashMap<String, Long>();
			map.put("accountId", accountId);
			map.put("offset", offset);
			docs = sqlMapClient.queryForList("Doc.getDocsByAccountId", map);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return docs;
	}

	@Override
	public long getDocNumByAccountId(Long accountId) throws PersistException {
		long n = 0;
		try {
			Long tmp = (Long) sqlMapClient.queryForObject("Doc.getDocNumByAccountId", accountId);
			if(tmp != null) {
				n = tmp.longValue();
			}
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public Long addDoc(Doc doc) throws PersistException {
		Long n = -1L;
		try {
			n = (Long)sqlMapClient.insert("Doc.addDoc", doc);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public int updateDoc(Doc doc) throws PersistException {
		int n = 0;
		try {
			n = sqlMapClient.update("Doc.updateDoc", doc);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public int deleteDoc(Long docId, Long accountId) throws PersistException {
		int n = 0;
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("docId", docId);
			map.put("accountId", accountId);
			
			n = sqlMapClient.delete("Doc.deleteDoc", map);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}

	@Override
	public int deleteDocsByAccountId(Long accountId) throws PersistException {
		int n = 0;
		try {
			n = sqlMapClient.delete("Doc.deleteDocsByAccountId", accountId);
		} catch (SQLException e) {
			throw new PersistException(this.getClass().getName() + ":" + e.getMessage());
		}
		return n;
	}
	
}
