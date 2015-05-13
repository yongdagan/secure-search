package com.gmail.yongdagan.secure_search.persist.dataobject;

import java.util.ArrayList;

public class BitArray {
	
	private ArrayList<Byte> list;		// for push, list(i, j) is the next place
	private byte[] bytes;				// for next
	private int i;
	private int j;
	
	public BitArray() {
		// only push
		list = new ArrayList<Byte>();
		list.add(new Byte((byte) 0));
		i = 0;
		j = 0;
	}
	
	public BitArray(byte[] bytes) {
		// only next
		this.bytes = bytes;
		i = 0;
		j = 0;
	}
	
	public boolean hasNext() {
		return i < bytes.length && j < 8;
	}
	
	public char next() {
		byte b = (byte) (bytes[i] >> j & 1);
		j ++;
		if(j == 8) {
			j = 0;
			i ++;
		}
		return Byte.toString(b).charAt(0);
	}
	
	public void push(char bit) {
		if(bit == '1') {
			Byte tmp = list.get(i);
			tmp = (byte) (tmp | (1 << j));
			list.set(i, tmp);
		}
		j ++;
		if(j == 8) {
			j = 0;
			i ++;
			list.add(new Byte((byte) 0));
		}
	}
	
	public void push(String str) {
		int len = str.length();
		for(int i = 0; i < len; i ++) {
			push(str.charAt(i));
		}
	}
	
	public byte[] toByteArray() {
		int size = 0;
		if(j != 0) {
			// padding 1
			int k = 7 - j;
			while(k >= 0) {
				push('1');
				k --;
			}
		}
		size = list.size() - 1;
		byte[] result = new byte[size];
		for(int i = 0; i < size; i ++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
}
