package com.gmail.yongdagan.secure_search.persist.dao;

import java.util.List;

import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;

public interface DocDAO {

	public Doc getDoc(Long docId, Long accountId) throws PersistException;
	public List<Doc> getDocsByAccountId(Long accountId, Long offset) throws PersistException;
	public long getDocNumByAccountId(Long accountId) throws PersistException;
	public Long addDoc(Doc doc) throws PersistException;
	public int updateDoc(Doc doc) throws PersistException;
	public int deleteDoc(Long docId, Long accountId) throws PersistException;
	public int deleteDocsByAccountId(Long accountId) throws PersistException;
	
}
