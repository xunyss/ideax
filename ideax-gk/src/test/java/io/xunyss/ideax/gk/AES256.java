package io.xunyss.ideax.gk;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AES256 {
	
	public static void main(String[] args) throws Exception {
		String plain = "hello world man";
		System.out.println(plain);
		System.out.println();
		
		String cipher = enc(plain);
		System.out.println(cipher);
		System.out.println(dec(cipher));
	}
	
	static SecretKeySpec secretKeySpec;
	static IvParameterSpec ivParameterSpec;
	static {
		byte[] key = "0123456789abcdef0123456789abcdef".getBytes();
		byte[] iv = "0123456789abcdef".getBytes();
		secretKeySpec = new SecretKeySpec(key, "AES");
		ivParameterSpec = new IvParameterSpec(iv);
	}
	
	static String enc(String plain) throws Exception {
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE,
				secretKeySpec,
				ivParameterSpec
		);
		byte[] encb = c.doFinal(plain.getBytes());
		return Base64.encodeBase64String(encb);
	}
	
	static String dec(String cipher) throws Exception {
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE,
				secretKeySpec,
				ivParameterSpec
		);
		byte[] decb = Base64.decodeBase64(cipher);
		return new String(c.doFinal(decb));
	}
}
