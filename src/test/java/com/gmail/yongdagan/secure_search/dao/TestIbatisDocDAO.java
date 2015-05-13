package com.gmail.yongdagan.secure_search.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gmail.yongdagan.secure_search.persist.dao.DocDAO;
import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;

public class TestIbatisDocDAO {
	
	DocDAO docDAO;
	
	@Before
	public void testAddDoc() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("ss-persist.xml");
		docDAO = (DocDAO) context.getBean("docDAO");
		
		Doc doc = new Doc();
		doc.setDocId(1L);;
		doc.setAccountId(2L);
		doc.setName("name");
		docDAO.addDoc(doc);
	}
	
	@After
	public void testDeleteDoc() throws Exception {
		docDAO.deleteDoc(1L, 2L);
	}
	
	@Test
	public void testGetDoc() throws Exception {
		Doc doc = docDAO.getDoc(1L, 2L);
		assertEquals("name", doc.getName());
		
		doc = docDAO.getDoc(333L, 4L);
		assertNull(doc);
	}
	
	@Test
	public void testGetDocsByAccountId() throws Exception {
		Doc doc = new Doc();
		doc.setDocId(2L);
		doc.setAccountId(2L);
		doc.setName("name");
		docDAO.addDoc(doc);
		
		List<Doc> docs = docDAO.getDocsByAccountId(2L);
		assertEquals(2, docs.size());
		
		docDAO.deleteDoc(2L, 2L);
		
		docs = docDAO.getDocsByAccountId(4555L);
		assertTrue(docs.isEmpty());
	}
	
	@Test
	public void testGetDocNumByAccountId() throws Exception {
		assertEquals(1, docDAO.getDocNumByAccountId(2L));
	}
	
	@Test
	public void testUpdateDoc() throws Exception {
		Doc doc = docDAO.getDoc(1L, 2L);
		assertEquals("name", doc.getName());
		doc.setName("a");
		docDAO.updateDoc(doc);
		doc = docDAO.getDoc(1L, 2L);
		assertEquals("a", doc.getName());
	}
	
	@Test
	public void testDeleteDocsByAccountId() throws Exception {
		Doc doc = new Doc();
		doc.setDocId(1L);
		doc.setAccountId(1L);
		doc.setName("name");
		docDAO.addDoc(doc);
		
		docDAO.deleteDocsByAccountId(1L);
		assertNull(docDAO.getDoc(1L, 1L));
		assertNotNull(docDAO.getDoc(1L, 2L));
	}
	

}
