package io.xunyss.ideax.lcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import io.xunyss.commons.io.IOUtils;
import io.xunyss.ideax.codec.SimpleFileDecoder;
import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class LCSigner {
	
	/**
	 * 
	 */
	private static LCSigner instance = new LCSigner();
	
	/**
	 * 
	 * @return
	 */
	public static LCSigner getInstance() {
		return instance;
	}
	
	private static final String PEM_RESOURCE_PATH = "ke" + "y/" + "id" + "ea" + "x." + "pe" + "m";
	private static final String SECURITY_PROVIDER = "BC";
	private static final String SIGN_ALGORITHM = "MD5WithRSA";
	
	private boolean initialized = false;
	
	private Signature ideaSignature = null;
	
	/**
	 * 
	 */
	private LCSigner() {
		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	void init() throws Exception {
		if (!initialized) {
			/*
			 * add BouncyCastleProvider
			 */
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			
			/*
			 * get primary key
			 */
			PEMKeyPair pemKeyPair = (PEMKeyPair) loadPrivateKeyPem();
			JcaPEMKeyConverter pemKeyconverter = new JcaPEMKeyConverter().setProvider(SECURITY_PROVIDER);
			KeyPair keypair = pemKeyconverter.getKeyPair(pemKeyPair);
			PrivateKey privateKey = keypair.getPrivate();
			
			/*
			 * initialize signer signature
			 */
			ideaSignature = Signature.getInstance(SIGN_ALGORITHM, SECURITY_PROVIDER);
			ideaSignature.initSign(privateKey);
		}
	}
	
	/**
	 * get ideax.pem file
	 * @return
	 * @throws IOException
	 */
	private Object loadPrivateKeyPem() throws IOException {
		InputStream pemInput = null;
		PEMParser pemParser = null;
		
		try {
			File ideaxPem = new File(PEM_RESOURCE_PATH);
			if (ideaxPem.isFile()) {
				pemInput = new FileInputStream(ideaxPem);
			}
			/*
			 * 2016.11.17 XUNYSS
			 * disable "embedded resource" > remove "resource PEM file"
			 * 2018.02.18 XUNYSS
			 * "embedded resource" > simple encoding
			 */
			else {
				InputStream pemResourceInput = ClassLoader.getSystemResourceAsStream(PEM_RESOURCE_PATH);
				if (pemResourceInput == null) {
					pemResourceInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(PEM_RESOURCE_PATH);
				}
				if (pemInput == null) {
					pemResourceInput = getClass().getClassLoader().getResourceAsStream(PEM_RESOURCE_PATH);
				}
				
				if (pemResourceInput != null) {
					pemInput = new SimpleFileDecoder(pemResourceInput);
				}
			}
			
			if (pemInput == null) {
				throw new IOException("Fail to initialize: PEM resource not found: " + PEM_RESOURCE_PATH);
			}
			
			pemParser = new PEMParser(new InputStreamReader(pemInput));
			return pemParser.readObject();
		}
		catch (IOException ioe) {
			throw ioe;
		}
		finally {
			IOUtils.closeQuietly(pemInput);
			IOUtils.closeQuietly(pemParser);
		}
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public synchronized String signMessage(String message) {
		try {
			if (ideaSignature == null) {
				throw new IllegalStateException("LCSigner is not initialized");
			}
			
			StringBuilder signed = new StringBuilder();
			
			ideaSignature.update(message.getBytes("utf-8"));
			byte[] signData = ideaSignature.sign();		// do reset
			
			int signLen = signData.length;
			for (int at = 0; at < signLen; at++) {
				signed.append(digitChar(signData[at] >> 4 & 0xf));
				signed.append(digitChar(signData[at] & 0xf));
			}
			
			return signed.toString();
		}
		catch (Exception e) {
			Log.error("fail to sign", e);
			return "[FAIL TO SIGN]";
		}
	}
	
	/**
	 * 
	 * @param digit
	 * @return
	 */
	private final char digitChar(int digit) {
		return (char) (digit < 10 ? digit + 48 : (digit + 97) - '\n');
	}
}
