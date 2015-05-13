package com.gmail.yongdagan.secure_search.persist.dao;

import java.util.List;

import com.gmail.yongdagan.secure_search.persist.dataobject.Term;

public interface TermDAO {
	
	public Term getTermById(Long id) throws PersistException;
	public List<Term> getTerms(Long accountId, String name) throws PersistException;
	public Long addTerm(Term term) throws PersistException;
	public void addTermList(List<Term> terms) throws PersistException;
	public int deleteTerm(Long id) throws PersistException;
	public int deleteTermsByAccountId(Long accountId) throws PersistException;

}
