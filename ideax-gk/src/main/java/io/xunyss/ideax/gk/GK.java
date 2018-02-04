package io.xunyss.ideax.gk;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.xunyss.commons.io.IOUtils;
import io.xunyss.commons.lang.ZipUtils;
import io.xunyss.commons.net.HttpDownloader;
import io.xunyss.commons.openssl.OpenSSL;
import io.xunyss.commons.reflect.JarClassLoader;

/**
 * GK.
 *
 * <p>usages
 * <ul>
 *   <li>java -jar ideax-gk.jar</li>
 *   <li>java -jar ideax-gk.jar http 160.61.190.143 18283</li>
 * </ul>
 *
 * @author XUNYSS
 */
public class GK {
	
	public static void main(String[] args) throws Exception {
		
		// 0. set working directory
		File workingDir = new File("./temp_work");
		
		try {
			// 1. download "lcs-installer.zip"
//			Log.out("download 'lcs-installer.zip'");
//			Log.out();
//
			// 2. unzip "lcs-installer.zip"
			Log.out("extract 'lcs-installer.zip'");
//			ZipUtils.unzip(downloadedFile, workingDir);
			ZipUtils.unzip("D:\\xdev\\works\\intellij-projects\\io.xunyss\\temp_work\\license-server-installer.zip", workingDir);
			
			// 3. get MODULUS, PRIVATE_EXPONENT
			Class<?> clazz = JarClassLoader.loadClass(new File(workingDir, Consts.srvJar), Consts.fpkkCls);
			Field fieldModulus = clazz.getDeclaredField("MODULUS");
			Field fielPprivateExponent = clazz.getDeclaredField("PRIVATE_EXPONENT");
			fieldModulus.setAccessible(true);
			fielPprivateExponent.setAccessible(true);
			String modules = (String) fieldModulus.get(clazz);
			String privateExponent = (String) fielPprivateExponent.get(clazz);
			Log.out("extract MODULUS: " + modules);
			Log.out("extract PRIVATE_EXPONENT: " + privateExponent);
			Log.out();
			
			// 4. generate private key
			String generatedKey = generatePrivateKey(modules, privateExponent);
			File generatedkeyFile = new File(workingDir, "ideax.temp.pem");
			IOUtils.copy(generatedKey, generatedkeyFile);
			Log.out("generated private key:\n" + generatedKey);
			
			// 5. convert private key
			OpenSSL openssl = new OpenSSL();
			openssl.exec("rsa", "-in", generatedkeyFile.getPath(), "-modulus");
			String pemStr = openssl.getOutput();
			Log.out("converted private key:\n" + pemStr);
			
			// 6. create "ideax.pem"
			int startPos = pemStr.indexOf("-----BEGIN RSA PRIVATE KEY-----");
			pemStr = pemStr.substring(startPos);
			IOUtils.copy(pemStr, new File("ideax.pem"));
			Log.out("'ideax.pem' is created");
		}
		catch (Exception ex) {
			Log.err("fail to create key file 'ideax.pem'", ex);
		}
		finally {
			// 7. remove temp working directory
//			FileUtils.deleteDirectory(workingDir);
		}
	}
	
	private String download() {
		HttpDownloader downloader = new HttpDownloader();
		if (args.length > 2) {
			downloader.setProxy(args[0], args[1], Integer.parseInt(args[2]));
		}
		String downloadURL = Consts.downUrl;
		String downloadedFile = downloader.download(downloadURL);		
	}
	
	private static String generatePrivateKey(String modules, String privateExponent) throws Exception {
		KeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(modules), new BigInteger(privateExponent));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		StringWriter stringWriter = new StringWriter();
		PemWriter pemWriter = new PemWriter(stringWriter);
		
		try {
			pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", privateKey.getEncoded()));
		}
		finally {
			IOUtils.closeQuietly(pemWriter);
		}

		return stringWriter.toString();
	}
}
