package org.xunyss.ideax;

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
import org.xunyss.commons.util.IOUtils;
import org.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class LicenseSigner {
	
	/**
	 * 
	 */
	private static LicenseSigner instance = new LicenseSigner();
	
	/**
	 * 
	 * @return
	 */
	public static LicenseSigner getInstance() {
		return instance;
	}
	
	private static final String PEM_RESOURCE_PATH = "key/ideax.pem";
	private static final String SECURITY_PROVIDER = "BC";
	private static final String SIGN_ALGORITHM = "MD5WithRSA";
	
	
	private Signature ideaSignature = null;
	
	/**
	 * 
	 */
	private LicenseSigner() {
		try {
			init();
		}
		catch (Exception e) {
			Log.error("fail to License-Signer initializing", e);
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
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
	
	/**
	 * get ideax.pem file
	 * @return
	 * @throws IOException
	 */
	private Object loadPrivateKeyPem() throws IOException {
		InputStream pemInput = null;
		PEMParser pemParser = null;
		
		try {
			File ideaxPem = new File("./" + PEM_RESOURCE_PATH);
			if (ideaxPem.isFile()) {
				pemInput = new FileInputStream(ideaxPem);
			}
			else {
				pemInput = ClassLoader.getSystemResourceAsStream(PEM_RESOURCE_PATH);
				if (pemInput == null) {
					pemInput = Thread.currentThread().getContextClassLoader()
							.getResourceAsStream(PEM_RESOURCE_PATH);
				}
				if (pemInput == null) {
					pemInput = getClass().getClassLoader()
							.getResourceAsStream(PEM_RESOURCE_PATH);
				}
			}
			
			if (pemInput == null) {
				throw new IOException("pem resource not found : " + PEM_RESOURCE_PATH);
			}
			
			pemParser = new PEMParser(new InputStreamReader(pemInput));
			Object pem = pemParser.readObject();
			
			return pem;
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
				throw new IllegalStateException("License-Signer is not initialized");
			}
			
			StringBuilder signed = new StringBuilder();
			
			ideaSignature.update(message.getBytes("utf-8"));
			byte[] signData = ideaSignature.sign();		// do reset
			
			int signLen = signData.length;
			for (int at = 0; at < signLen; at++) {
				signed.append(digitChar(signData[at] >> 4 & 0xF));
				signed.append(digitChar(signData[at] & 0xF));
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
