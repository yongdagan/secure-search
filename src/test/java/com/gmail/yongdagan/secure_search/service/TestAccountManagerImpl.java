package com.gmail.yongdagan.secure_search.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gmail.yongdagan.secure_search.persist.dataobject.Account;

public class TestAccountManagerImpl {
	
	private AccountManager accountManager;
	
	@Before
	public void prepare() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("ss-persist.xml", "ss-service.xml");
		accountManager = (AccountManager) context.getBean("accountManager");
	}
	
	@Test
	public void test() throws Exception {
		// register
		assertNotNull(accountManager.register("test1", "123456"));
		assertNull(accountManager.register("test1", "1234afqw56"));
		
		// login
		Account account = accountManager.login("test1", "123456");
		assertNotNull(account);
		account = accountManager.login("test1", "dsfdsfa");
		assertNull(account);
		account = accountManager.login("qweqwe", "fdsaf");
		assertNull(account);
		
		// updateInfo
		account = accountManager.login("test1", "123456");
		assertFalse(accountManager.updateInfo(account, "wqeqw", "dsfds", ""));
		assertTrue(accountManager.updateInfo(account, "123456", "dsfdasf", ""));

		account = accountManager.login("test1", "dsfdasf");
		assertNotNull(account);
		assertEquals("", account.getAddKey());
	}

}
