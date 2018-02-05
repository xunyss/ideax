package io.xunyss.ideax.gk;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.xunyss.commons.io.FileUtils;
import io.xunyss.commons.io.IOUtils;
import io.xunyss.commons.lang.JarClassLoader;
import io.xunyss.commons.lang.ZipUtils;
import io.xunyss.commons.net.HttpDownloader;
import io.xunyss.commons.openssl.OpenSSL;

/**
 * GK.
 *
 * <p>usages
 * <ul>
 *   <li>java -jar ideax-gk.jar </li>
 * </ul>
 *
 * @author XUNYSS
 */
public class GK {
	
	public static void main(String[] args) throws Exception {
		GK gk = new GK();
		gk.run();
	}
	
	
	//==============================================================================================
	
	private File workingDir = new File("./temp_work");
	private String lcsZip = null;
	private String modules = null;
	private String privateExponent = null;
	
	private GK() throws IOException {
		// 0. set working directory
		FileUtils.makeDirectory(workingDir);
	}
	
	private void run() {
		try {
			// 1. download "lcs-installer.zip"
			download();
			
			// 2. unzip "lcs-installer.zip"
			unzip();
			
			// 3. get modulus, private exponent
			extract();
			
			// 4. generate private key
			String generatedKey = generatePrivateKey();
			
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
	
	private String download() throws IOException {
		Log.out("download 'lcs-installer.zip'");
		Log.out();
		
		HttpDownloader httpDownloader = new HttpDownloader();
		httpDownloader.setDownloadPath(workingDir);
		return httpDownloader.download(Consts.downUrl);
	}
	
	private void unzip() throws IOException {
		Log.out("unzip 'lcs-installer.zip'");
		Log.out();
		
		ZipUtils.unzip(lcsZip, workingDir);
	}
	
	private void extract() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException {
		Class<?> clazz = JarClassLoader.loadClass(new File(workingDir, Consts.srvJar), Consts.fpkkCls);
		Field fieldModulus = clazz.getDeclaredField("MODULUS");
		Field fielPprivateExponent = clazz.getDeclaredField("PRIVATE_EXPONENT");
		fieldModulus.setAccessible(true);
		fielPprivateExponent.setAccessible(true);
		modules = (String) fieldModulus.get(clazz);
		privateExponent = (String) fielPprivateExponent.get(clazz);
		
		Log.out("extract MODULUS: " + modules);
		Log.out("extract PRIVATE_EXPONENT: " + privateExponent);
		Log.out();
	}
	
	private String generatePrivateKey() throws Exception {
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
