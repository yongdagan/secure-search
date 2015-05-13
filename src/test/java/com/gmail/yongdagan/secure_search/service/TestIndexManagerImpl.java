package com.gmail.yongdagan.secure_search.service;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gmail.yongdagan.secure_search.persist.dataobject.CryptoUtil;
import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;

public class TestIndexManagerImpl {
	
	private IndexManager indexManager;
	
	@Before
	public void prepare() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("ss-persist.xml", "ss-service.xml");
		indexManager = (IndexManager) context.getBean("indexManager");
	}
	
//	@Test
	public void testParseIndexFile() throws Exception {
		Path indexFile = Paths.get("/home/yongdagan/xx/indexFile.txt");
		Integer accountId = 1;
//		indexManager.parseIndexFile(accountId, new FileInputStream(indexFile.toFile()));
	}
	
//	@Test
	public void testRankSearch() throws Exception {
		String addKey = "13789568824645175100312096832245786408022756076796917313109672580799099102001";
		byte[] hmac1 = CryptoUtil.decodeBASE64("tdpTFyMtyhTLPmt8ddhEzU8nKJBoYoxPkezb7mFGkpocw6vdGfF0iT+FnGvMjY0caqWUje5ajf+4b0CyVCpc9w==");
		byte[] hamc2 = CryptoUtil.decodeBASE64("zfNJm2/1qd567s0ouinkU6S+Ep2m5sHG6CnUNIGsfq5EIn7iXmPuLqDb9/VDONCw2j5a5JlMViWiiCqNf2XjTA==");
		List<Doc> docs = indexManager.rankSearch(1L, addKey,
				CryptoUtil.encodeBASE64(CryptoUtil.generateHMAC(hmac1, "beautiful".getBytes())),
				CryptoUtil.encodeBASE64(CryptoUtil.generateHMAC(hamc2, "beautiful".getBytes())));
		for(Doc doc : docs) {
			System.out.println(doc.getId() + " " + doc.getName());
		}
	}
	
}
