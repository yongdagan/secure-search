package com.gmail.yongdagan.secure_search.persist.dataobject;

import java.math.BigInteger;
import java.util.Random;

public class Paillier {

	public static final int BIT_LENGTH = 128;
	private final int CERTAINTY = 64;
	private int bitLength;
	private BigInteger n;
	private BigInteger lambda;
	private BigInteger nsquare;
	private BigInteger g;
	private BigInteger mu;
	
	public String getN() {
		return n.toString();
	}

	public String getLambda() {
		return lambda.toString();
	}

	public String getG() {
		return g.toString();
	}

	public String getMu() {
		return mu.toString();
	}
	
	public String getNsquare() {
		return nsquare.toString();
	}
	
	public Paillier(int bitLength) {
		this.bitLength = bitLength;	
	}
	
	public Paillier(int bitLength, String n, String g, String lambda, String mu) {
		this.bitLength = bitLength;
		this.n = new BigInteger(n);
		this.nsquare = this.n.pow(2);
		this.lambda = new BigInteger(lambda);
		this.mu = new BigInteger(mu);
		this.g = new BigInteger(g);
	}
	
	public Paillier(int bitLength, BigInteger n, BigInteger g, BigInteger lambda, BigInteger mu) {
		this.bitLength = bitLength;
		this.n = n;
		this.nsquare = n.pow(2);
		this.lambda = lambda;
		this.mu = mu;
		this.g = g;
	}
	
	public void generateKeys() {
		// random p, q
		BigInteger p = new BigInteger(bitLength / 2, CERTAINTY, new Random());
		BigInteger q = null;
		do {
			q = new BigInteger(bitLength / 2, CERTAINTY, new Random());
		} while (q.compareTo(p) == 0);
		// n = p*q, nsquare = n*n
		n = p.multiply(q);
		nsquare = n.pow(2);
		// lambda = lcm(p-1, q-1) = (p-1)*(q-1) / gcd(p-1, q-1)
		lambda = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE))
				.divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
		// random g, need gcd(L(g^lambda mod nsquare), n) = 1
		do {
			g = randomZStarNSquare();
		} while(L(g.modPow(lambda, nsquare)).gcd(n).intValue() != 1);
		// mu = L(g^lambda mod nsquare)^(-1) mod n
		mu = L(g.modPow(lambda, nsquare)).modInverse(n);
	}
	
	public byte[] encrypt(byte[] data) throws Exception {
		BigInteger m = new BigInteger(data);
		BigInteger c = encrypt(m);
		return c.toByteArray();
	}
	
	public byte[] decrypt(byte[] data) throws Exception {
		BigInteger c = new BigInteger(data);
		BigInteger m = decrypt(c);
		return m.toByteArray();
	}
	
	public BigInteger encrypt(BigInteger m) throws Exception {
		if(m.compareTo(BigInteger.ZERO) < 0 || m.compareTo(n) >= 0) {
			throw new Exception("Paillier.doEncrypt(BigInteger m): plaintext m is not in Z_n");
		}
		// c = (g^m)*(r^n) mod nsquare
		BigInteger r = randomZStarN();
		return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
	}
	
	public BigInteger decrypt(BigInteger c) throws Exception {
		if(c.compareTo(BigInteger.ZERO) < 0 || c.compareTo(nsquare) >= 0 || c.gcd(nsquare).intValue() != 1) {
			throw new Exception("Paillier.doDecrypt(BigInteger c): ciphertext c is not in Z*_(n^2)");
		}
		// m = L(c^lambda mod nsquare) * mu mod n
		return L(c.modPow(lambda, nsquare)).multiply(mu).mod(n);
	}
	
	
	public static BigInteger add(BigInteger a, BigInteger b, String addKey) {
		BigInteger nsquare = new BigInteger(addKey);
		BigInteger c = a.multiply(b).mod(nsquare);
		return c;
	}
	
	private BigInteger L(BigInteger x) {
		// L(x) = (x-1)/n
		return x.subtract(BigInteger.ONE).divide(n);
	}
	
	private BigInteger randomZStarNSquare() {
		BigInteger r = null;
		do {
			r = new BigInteger(bitLength * 2, new Random());
		} while(r.compareTo(nsquare) >= 0 || r.gcd(nsquare).intValue() != 1);
		return r;
	}
	
	private BigInteger randomZStarN() {
		BigInteger r = null;
		do {
			r = new BigInteger(bitLength, new Random());
		} while(r.compareTo(n) >= 0 || r.gcd(n).intValue() != 1);
		return r;
	}

}
