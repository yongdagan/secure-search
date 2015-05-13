package com.gmail.yongdagan.secure_search.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gmail.yongdagan.secure_search.persist.dao.TermDAO;
import com.gmail.yongdagan.secure_search.persist.dataobject.Term;

public class TestIbatisTermDAO {
	
	TermDAO termDAO;
	Long id;
	
	@Before
	public void testAddTerm() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("ss-persist.xml");
		termDAO = (TermDAO) context.getBean("termDAO");
		
		Term term = new Term();
		term.setAccountId(1L);
		term.setName("abc");
		term.setTrapdoor("trapdoor".getBytes());
		term.setDocIds("docIds".getBytes());
		term.setScores("scores".getBytes());
		termDAO.addTerm(term);
		id = term.getId();
	}
	
	@After
	public void testDeleteTerm() throws Exception {
		termDAO.deleteTerm(id);
	}
	
	@Test
	public void testGetTermById() throws Exception {
		Term term = termDAO.getTermById(id);
		assertEquals(1L, term.getAccountId().longValue());
		assertEquals("abc", term.getName());
		assertArrayEquals("trapdoor".getBytes(), term.getTrapdoor());
		assertArrayEquals("docIds".getBytes(), term.getDocIds());
		assertArrayEquals("scores".getBytes(), term.getScores());
		
		assertNull(termDAO.getTermById(55L));
	}
	
	@Test
	public void testGetTerms() throws Exception {
		List<Term> terms = termDAO.getTerms(1L, "abc");
		Term term = terms.get(0);
		assertEquals(id, term.getId());
		assertArrayEquals("trapdoor".getBytes(), term.getTrapdoor());
		assertArrayEquals("docIds".getBytes(), term.getDocIds());
		assertArrayEquals("scores".getBytes(), term.getScores());
		
		assertTrue(termDAO.getTerms(55L, "asd").isEmpty());
	}
	
	@Test
	public void testDeleteTermsOfAccountId() throws Exception {
		Term term = new Term();
		term.setAccountId(2L);
		term.setName("abc");
		term.setTrapdoor("trapdoor".getBytes());
		term.setDocIds("docIds".getBytes());
		term.setScores("scores".getBytes());
		Long tmp = term.getId();
		
		termDAO.deleteTermsByAccountId(2L);
		assertNull(termDAO.getTermById(tmp));
		assertNotNull(termDAO.getTermById(id));
	}
	
//	@Test
	public void testAddTermList() throws Exception {
		Date start = new Date();
		List<Term> terms = new ArrayList<Term>();
		for(int i = 0; i < 10000; i ++) {
			Term term = new Term();
			term.setAccountId(3L);
			term.setName("asdf");
			term.setTrapdoor("asd".getBytes());
			term.setDocIds("ada".getBytes());
			term.setScores("asadda".getBytes());
			terms.add(term);
		}
		termDAO.addTermList(terms);
		Date end = new Date();
		System.out.println(end.getTime() - start.getTime());
		
		start = new Date();
		for(int i = 0; i < 10000; i ++) {
			Term term = new Term();
			term.setAccountId(3L);
			term.setName("asdf");
			term.setTrapdoor("asd".getBytes());
			term.setDocIds("ada".getBytes());
			term.setScores("asadda".getBytes());
			termDAO.addTerm(term);
		}
		end = new Date();
		System.out.println(end.getTime() - start.getTime());
	}

}
