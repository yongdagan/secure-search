package com.gmail.yongdagan.secure_search.persist.dataobject;

public class Term {
	
	private Long id;
	private Long accountId;
	private String name;
	private byte[] trapdoor;
	private byte[] docIds;
	private byte[] scores;
	
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
	public byte[] getTrapdoor() {
		return trapdoor;
	}
	public void setTrapdoor(byte[] trapdoor) {
		this.trapdoor = trapdoor;
	}
	public byte[] getDocIds() {
		return docIds;
	}
	public void setDocIds(byte[] docIds) {
		this.docIds = docIds;
	}
	public byte[] getScores() {
		return scores;
	}
	public void setScores(byte[] scores) {
		this.scores = scores;
	}
}
