package com.gmail.yongdagan.secure_search.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.gmail.yongdagan.secure_search.persist.dao.DocDAO;
import com.gmail.yongdagan.secure_search.persist.dao.PersistException;
import com.gmail.yongdagan.secure_search.persist.dao.TermDAO;
import com.gmail.yongdagan.secure_search.persist.dataobject.BitArray;
import com.gmail.yongdagan.secure_search.persist.dataobject.CryptoUtil;
import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;
import com.gmail.yongdagan.secure_search.persist.dataobject.Paillier;
import com.gmail.yongdagan.secure_search.persist.dataobject.Term;

public class IndexManagerImpl implements IndexManager {

	private TermDAO termDAO;
	private DocDAO docDAO;
	
	public void setTermDAO(TermDAO termDAO) {
		this.termDAO = termDAO;
	}
	public void setDocDAO(DocDAO docDAO) {
		this.docDAO = docDAO;
	}
	
	@Override
	public void parseIndexFile(Long accountId, InputStream indexStream, Path accountPath) throws ServiceException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream))) {
			String tmp = null;
			// ignore doc
			while(!(tmp = reader.readLine()).equals("")) {
			}
			// read term
			tmp = null;
			List<Term> terms = new ArrayList<Term>();
			while((tmp = reader.readLine()) != null) {
				String[] str = tmp.split(" ");
				Term term = new Term();
				term.setAccountId(accountId);
				term.setName(str[0]);
				term.setTrapdoor(CryptoUtil.decodeBASE64(str[1]));
				term.setDocIds(CryptoUtil.decodeBASE64(str[2]));
				term.setScores(CryptoUtil.decodeBASE64(str[3]));
				terms.add(term);
			}
			termDAO.addTermList(terms);
		} catch (Exception e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
	}
	
	@Override
	public List<Doc> rankSearch(Long accountId, String addKey, String termNames, String trapdoors)
			throws ServiceException {
		String[] termName = termNames.split(",");
		String[] trapdoor = trapdoors.split(",");
		List<Doc> docs = new ArrayList<Doc>();
		try {
			HashMap<Long, BigInteger> docMap = new HashMap<Long, BigInteger>();
			for(int k = 0; k < termName.length; k ++) {
				List<Term> terms = termDAO.getTerms(accountId, termName[k]);
				byte[] hmac = CryptoUtil.decodeBASE64(trapdoor[k]);
				for (Term term : terms) {
					// get term key
					byte[] aesKey = getTermKey(hmac, term.getTrapdoor());
					// get term docIds
					ArrayList<Long> docIds = getTermDocIds(aesKey, term.getDocIds());
//					System.out.println(docIds.size());
					// get term scores
					ArrayList<BigInteger> scores = getTermScores(aesKey, term.getScores());
//					System.out.println(scores.size());
					for (int i = 0; i < docIds.size(); i ++) {
						// get docId, score
						Long docId = docIds.get(i);
						BigInteger score = scores.get(i);
						if (docMap.containsKey(docId)) {
							// add up score
							BigInteger tmp = docMap.get(docId);
							tmp = Paillier.add(tmp, score, addKey);
							docMap.put(docId, tmp);
						} else {
							// add doc
							docMap.put(docId, score);
						}
					}
				}
			}
			// get doc list
			for(Map.Entry<Long, BigInteger> d : docMap.entrySet()) {
				Doc doc = docDAO.getDoc(d.getKey(), accountId);
				if(doc != null) {
					doc.setScore(d.getValue());
					docs.add(doc);
				}
			}
		} catch (Exception e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return docs;
	}
	
	private ArrayList<Long> getTermDocIds(byte[] aesKey, byte[] value) throws Exception {
		BitArray bitArray = new BitArray(CryptoUtil.decryptAES(aesKey, value));
		ArrayList<Long> docIds = new ArrayList<Long>();
		int k = 0;
		Long x = -1L;
		while(bitArray.hasNext()) {
			char c = bitArray.next();
			if(c == '0') {
				StringBuffer buffer = new StringBuffer("1");
				for(int i = 0; i < k; i ++) {
					buffer.append(bitArray.next());
				}
				k = 0;
				if(x == -1L) {
					x = Long.valueOf(buffer.toString(), 2);
					docIds.add(x);	
				} else {
					x += Long.valueOf(buffer.toString(), 2);
					docIds.add(x);
				}
			} else {
				k ++;
			}
		}
		return docIds;
	}
	
	private ArrayList<BigInteger> getTermScores(byte[] aesKey, byte[] value) throws Exception {
		value = CryptoUtil.decryptAES(aesKey, value);
		ArrayList<BigInteger> scores = new ArrayList<BigInteger>();
    	BigInteger tmp = BigInteger.ZERO;
    	int k = 0;
    	for(int i = 0; i < value.length; i ++) {
    		byte b = value[i];
    		tmp = (new BigInteger(Byte.toString((byte) (b & 0x7f)))).shiftLeft(7 * k).or(tmp);
    		k ++;
    		if(b >>> 7 == 0) {
    			scores.add(tmp);
    			tmp = BigInteger.ZERO;
    			k = 0;
    		}
    	}
    	return scores;
	}
	
	@Override
	public List<Doc> booleanSearch(Long accountId, String termNames, String trapdoors)
			throws ServiceException {
		String[] termName = termNames.split(",");
		String[] trapdoor = trapdoors.split(",");
		HashMap<Integer, List<Long>> tmpDocs = new HashMap<Integer, List<Long>>();
		List<Doc> docs = new ArrayList<Doc>();
		
		try {
			// pre get doc list
			for(int i = 0; i < termName.length; i ++) {
				if(!termName[i].equals("|") && !termName[i].equals("&") && !termName[i].equals("-")) {
					List<Term> terms = termDAO.getTerms(accountId, termName[i]);
					byte[] hmac = CryptoUtil.decodeBASE64(trapdoor[i]);
					List<Long> docIds = new ArrayList<Long>();
					for(Term term : terms) {
						// get term key
						byte[] aesKey = getTermKey(hmac, term.getTrapdoor());
						// get term docIds
						ArrayList<Long> list = getTermDocIds(aesKey, term.getDocIds());
						for(int j = 0; j < list.size(); j ++) {
							// get docId, score
							Long docId = list.get(j);
							docIds.add(docId);
						}
					}
					tmpDocs.put(i, docIds);
				}
			}
			// boolean search
			Stack<Integer> istack = new Stack<Integer>();
			int k = -1;
			for(int i = 0; i < termName.length; i ++) {
				if(termName[i].equals("&") || termName[i].equals("|") || termName[i].equals("-")) {
					Integer b = istack.pop();
					Integer a = istack.pop();
					List<Long> bList = tmpDocs.get(b);
					List<Long> aList = tmpDocs.get(a);
					tmpDocs.remove(a);
					tmpDocs.remove(b);
					
					List<Long> cList = null;
					if(termName[i].equals("&")) {
						cList = intersectionList(aList, bList);
					} else if(termName[i].equals("|")) {
						cList = unionList(aList, bList);
					} else {
						cList = differenceList(aList, bList);
					}
					
					tmpDocs.put(k, cList);
					istack.push(k --);
				} else {
					istack.push(i);
				}
			}
			
			List<Long> docIds = tmpDocs.get((k+1));
			for(Long docId : docIds) {
				Doc doc = docDAO.getDoc(docId, accountId);
				if(doc != null) {
					docs.add(doc);
				}
			}
		} catch (Exception e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return docs;
	}
	
	@Override
	public List<Doc> getAccountFiles(Long accountId, Integer page) throws ServiceException {
		List<Doc> docs = new ArrayList<Doc>();
		try {
			docs = docDAO.getDocsByAccountId(accountId, (page - 1) * 10L);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return docs;
	}
	
	@Override
	public boolean hasDoc(Long accountId, Long docId) throws ServiceException {
		Doc doc = null;
		try {
			doc = docDAO.getDoc(docId, accountId);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return doc != null;
	}
	
	@Override
	public int deleteDoc(Long accountId, Long docId) throws ServiceException {
		int n = 0;
		try {
			n = docDAO.deleteDoc(docId, accountId);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
		return n;
	}
	
	private byte[] getTermKey(byte[] hmac, byte[] aesKey) throws Exception {
		// get aes key
		int len;
		if (aesKey.length > hmac.length) {
			len = hmac.length;
		} else {
			len = aesKey.length;
		}
		for (int i = 0; i < len; i++) {
			aesKey[i] ^= hmac[i];
		}
		return aesKey;
	}
	
	/*
	 * set a ^ set b
	 */
	private List<Long> intersectionList(List<Long> a, List<Long> b) {
		List<Long> c = new ArrayList<Long>();
		int i = 0, j = 0, alen = a.size(), blen = b.size();
		while(i < alen && j < blen) {
			Long x = a.get(i), y = b.get(j);
			if(x.equals(y)) {
				c.add(x);
				i ++;
				j ++;
			} else if(x.compareTo(y) > 0) {
				j ++;
			} else {
				i ++;
			}
		}
		return c;
	}
	
	/*
	 * set a V set b
	 */
	private List<Long> unionList(List<Long> a, List<Long> b) {
		List<Long> c = new ArrayList<Long>();
		int i = 0, j = 0, alen = a.size(), blen = b.size();
		while(i < alen && j < blen) {
			Long x = a.get(i), y = b.get(j);
			if(x.equals(y)) {
				c.add(x);
				i ++;
				j ++;
			} else if(x.compareTo(y) > 0) {
				c.add(y);
				j ++;
			} else {
				c.add(x);
				i ++;
			}
		}
		while(i < alen) {
			c.add(a.get(i ++));
		}
		while(j < blen) {
			c.add(b.get(j ++));
		}
		return c;
	}
	
	/*
	 * set a - set b
	 */
	private List<Long> differenceList(List<Long> a, List<Long> b) {
		List<Long> c = new ArrayList<Long>();
		int i = 0, j = 0, alen = a.size(), blen = b.size();
		while(i < alen && j < blen) {
			Long x = a.get(i), y = b.get(j);
			if(x.equals(y)) {
				i ++;
				j ++;
			} else if(x.compareTo(y) > 0) {
				j ++;
			} else {
				c.add(x);
				i ++;
			}
		}
		while(i < alen) {
			c.add(a.get(i ++));
		}
		return c;
	}
	
	@Override
	public void clearIndexAndFiles(Long accountId, Path accountPath) throws ServiceException {
		try {
			termDAO.deleteTermsByAccountId(accountId);
			docDAO.deleteDocsByAccountId(accountId);
			// delete files
			Files.walkFileTree(accountPath, new SimpleFileVisitor<Path>(){
				@Override
    			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					if(!Files.isDirectory(path)) {
						Files.delete(path);
					}
					return FileVisitResult.CONTINUE;
    			}
			});
		} catch (Exception e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
	}
	@Override
	public void addDoc(Doc doc) throws ServiceException {
		try {
			docDAO.addDoc(doc);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
	}
	@Override
	public long getDocNumByAccountId(Long accountId) throws ServiceException {
		try {
			return docDAO.getDocNumByAccountId(accountId);
		} catch (PersistException e) {
			throw new ServiceException(this.getClass().getName() + e);
		}
	}
	
}
