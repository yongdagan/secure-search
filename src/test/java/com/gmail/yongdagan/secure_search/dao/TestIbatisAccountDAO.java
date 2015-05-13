package com.gmail.yongdagan.secure_search.dao;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gmail.yongdagan.secure_search.persist.dao.AccountDAO;
import com.gmail.yongdagan.secure_search.persist.dataobject.Account;

public class TestIbatisAccountDAO {
	
	AccountDAO accountDAO;
	Long id;
	
	@Before
	public void testAddAccount() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("ss-persist.xml");
		accountDAO = (AccountDAO) context.getBean("accountDAO");
		
		Account account = new Account();
		account.setUsername("abc");
		account.setPassword("123");
		account.setAddKey("key");
		account.setCapacity(0L);
		accountDAO.addAccount(account);
		id = account.getId();
	}
	
	@Test
	public void testGetAccountById() throws Exception {
		Account account = accountDAO.getAccountById(id);
		assertEquals("abc", account.getUsername());
		assertEquals("123", account.getPassword());
		assertEquals("key", account.getAddKey());
	
		assertNull(accountDAO.getAccountById(3L));
	}
	
	@Test
	public void testGetAccountByUsername() throws Exception {
		Account account = accountDAO.getAccountByUsername("abc");
		assertEquals(id, account.getId());
		assertEquals("123", account.getPassword());
		assertEquals("key", account.getAddKey());
	
		assertNull(accountDAO.getAccountByUsername("dsf"));
	}
	
	@Test
	public void testUpdateAccount() throws Exception {
		Account account = accountDAO.getAccountById(id);
		assertEquals("123", account.getPassword());
		account.setPassword("321");
		accountDAO.updateAccount(account);
		account = accountDAO.getAccountById(id);
		assertEquals("321", account.getPassword());
	}
	
	@After
	public void testDeleteAccount() throws Exception {
		accountDAO.deleteAccount(id);
	}

}
