package com.gmail.yongdagan.secure_search.persist.dataobject;

import java.math.BigInteger;

public class Doc {
	
	private Long id;
	private Long docId;
	private Long accountId;
	private String name;
	private BigInteger score;
	
	public void setDocId(Long docId) {
		this.docId = docId;
	}
	public Long getDocId() {
		return docId;
	}
	public BigInteger getScore() {
		return score;
	}
	public void setScore(BigInteger score) {
		this.score = score;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
