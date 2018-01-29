package org.xunyss.ideax.gk;

import java.io.ByteArrayOutputStream;
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
import org.xunyss.commons.net.HTTPDownloader;
import org.xunyss.commons.reflect.JarClassLoader;
import org.xunyss.commons.util.ArchiveUtils;

import io.xunyss.commons.io.FileUtils;
import io.xunyss.commons.io.IOUtils;
import io.xunyss.openssl.OpenSSL;

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
			Log.out("download 'lcs-installer.zip'");
			Log.out();
			
			HTTPDownloader downloader = new HTTPDownloader();
			downloader.setDownloadPath(workingDir);
			if (args.length > 2) {
				downloader.setProxy(args[0], args[1], Integer.parseInt(args[2]));
			}
			String downloadURL = Const.downloadURL;
			String downloadedFile = downloader.download(downloadURL);
			
			// 2. unzip "lcs-installer.zip"
			Log.out("extract 'lcs-installer.zip'");
			ArchiveUtils.unzip(downloadedFile, workingDir);

			// 3. get MODULUS, PRIVATE_EXPONENT
			Class<?> clazz = JarClassLoader.loadClass(new File(workingDir, Const.svJar), Const.fpkkCls);
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
			ByteArrayOutputStream pemBytes = new ByteArrayOutputStream();
			OpenSSL openssl = new OpenSSL(pemBytes);
			openssl.execute("rsa", "-in", generatedkeyFile.getPath(), "-modulus");
			String pemStr = pemBytes.toString();
			IOUtils.closeQuietly(pemBytes);
			Log.out("converted private key:\n" + pemStr);
			
			// 6. create "ideax.pem"
			int startPos = pemStr.indexOf("-----BEGIN RSA PRIVATE KEY-----");
			pemStr = pemStr.substring(startPos);
			IOUtils.copy(pemStr, new File("ideax.pem"));
			Log.out("'ideax.pem' is created");
		}
		catch (Exception e) {
			Log.err("fail to create key file 'ideax.pem'", e);
		}
		finally {
			// 7. remove temp working directory
			FileUtils.deleteDirectory(workingDir);
		}
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
	
	
	//----------------------------------------------------------------------------------------------
	
	static class Const {
		
		static byte[] _downloadURL = {0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x64, 0x6f, 0x77, 0x6e, 0x6c, 0x6f, 0x61, 0x64, 0x2e, 0x6a, 0x65, 0x74, 0x62, 0x72, 0x61, 0x69, 0x6e, 0x73, 0x2e, 0x63, 0x6f, 0x6d, 0x2f, 0x6c, 0x63, 0x73, 0x72, 0x76, 0x2f, 0x6c, 0x69, 0x63, 0x65, 0x6e, 0x73, 0x65, 0x2d, 0x73, 0x65, 0x72, 0x76, 0x65, 0x72, 0x2d, 0x69, 0x6e, 0x73, 0x74, 0x61, 0x6c, 0x6c, 0x65, 0x72, 0x2e, 0x7a, 0x69, 0x70};
		static String downloadURL = new String(_downloadURL);
		
		static byte[] _sJar = {0x2f, 0x77, 0x65, 0x62, 0x2f, 0x57, 0x45, 0x42, 0x2d, 0x49, 0x4e, 0x46, 0x2f, 0x6c, 0x69, 0x62, 0x2f, 0x73, 0x65, 0x72, 0x76, 0x65, 0x72, 0x2e, 0x6a, 0x61, 0x72};
		static String svJar = new String(_sJar);
		
		static byte[] _fpkkCls = {0x63, 0x6f, 0x6d, 0x2e, 0x6a, 0x65, 0x74, 0x62, 0x72, 0x61, 0x69, 0x6e, 0x73, 0x2e, 0x6c, 0x73, 0x2e, 0x66, 0x6c, 0x6f, 0x61, 0x74, 0x69, 0x6e, 0x67, 0x2e, 0x46, 0x6c, 0x6f, 0x61, 0x74, 0x69, 0x6e, 0x67, 0x50, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x4b, 0x65, 0x79, 0x73, 0x4b, 0x74};
		static String fpkkCls = new String(_fpkkCls);
	}
}
