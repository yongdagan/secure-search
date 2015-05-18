package com.gmail.yongdagan.secure_search.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;

public interface IndexManager {
	
	public void parseIndexFile(Long accountId, InputStream indexStream, Path accountPath) throws ServiceException;
	public List<Doc> rankSearch(Long accountId, String addKey, String termNames, String trapdoors) throws ServiceException;
	public List<Doc> booleanSearch(Long accountId, String termNames, String trapdoors) throws ServiceException;
	public List<Doc> getAccountFiles(Long accountId, Integer page) throws ServiceException;
	public long getDocNumByAccountId(Long accountId) throws ServiceException;
	public boolean hasDoc(Long accountId, Long docId) throws ServiceException;
	public int deleteDoc(Long accountId, Long docId) throws ServiceException;
	public void addDoc(Doc doc) throws ServiceException;
	public void clearIndexAndFiles(Long accountId, Path accountPath) throws ServiceException;
}
