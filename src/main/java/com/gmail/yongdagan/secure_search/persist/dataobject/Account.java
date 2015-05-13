package com.gmail.yongdagan.secure_search.persist.dataobject;

public class Account {
	
	private Long id;
	private String username;
	private String password;
	private String addKey;
	private Long capacity;
	
	public Long getCapacity() {
		return capacity;
	}
	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}
	public String getAddKey() {
		return addKey;
	}
	public void setAddKey(String addKey) {
		this.addKey = addKey;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
