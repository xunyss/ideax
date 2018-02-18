package io.xunyss.ideax.gk;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.xunyss.commons.io.FileUtils;
import io.xunyss.commons.io.IOUtils;
import io.xunyss.commons.lang.JarClassLoader;
import io.xunyss.commons.lang.ZipUtils;
import io.xunyss.commons.net.HttpDownloader;
import io.xunyss.openssl.OpenSSL;

/**
 * GK.
 *
 * @author XUNYSS
 */
public class GK {
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		//------------------------------------------------------------------------------------------
		// java -jar gk.jar -s(show url)
		// java -jar gk.jar -d(download)
		// java -jar gk.jar -z(zip path) "/data/lcs-installer.zip"
		//------------------------------------------------------------------------------------------
		
		boolean down = false;
		String zip = null;
		
		for (int idx = 0; idx < args.length; idx++) {
			if ("-s".equals(args[idx])) {
				Log.out(Consts.downUrl);
				Log.out();
				return;
			}
			else if ("-d".equals(args[idx])) {
				down = true;
				break;
			}
			else if ("-z".equals(args[idx])) {
				if (idx == args.length - 1) {
					usage();
					return;
				}
				zip = args[idx + 1];
				break;
			}
		}
		
		if (down == false && zip == null) {
			usage();
			return;
		}
		
		// start GK
		new GK().run(down, zip);
	}
	
	private static final void usage() {
		Log.out("Invalid arguments");
		Log.out("Usage: GK -sdz");
		Log.out();
	}
	
	
	//==============================================================================================
	
	private File workingDir = new File("./temp_work");
	
	private String lcsZip = null;
	private String modules = null;
	private String privateExponent = null;
	private File generatedkeyFile = null;
	private String pemString = null;
	
	
	private GK() throws IOException {
		// 0. set working directory
		FileUtils.makeDirectory(workingDir);
	}
	
	private void run(boolean down, String zip) {
		try {
			// 1. download "lcs-installer.zip"
			if (down) {
				download();
			}
			
			// 2. unzip "lcs-installer.zip"
			lcsZip = (zip != null ? zip : lcsZip);
			unzip();
			
			// 3. get modulus, private exponent
			extract();
			
			// 4. generate private key
			generateKey();
			
			// 5. convert private key
			convertKey();
			
			// 6. create "ideax.pem"
			createPem();
		}
		catch (Exception ex) {
			Log.err("Failed to create key file 'ideax.pem'", ex);
		}
		finally {
			// 7. remove temp working directory
			try {
				FileUtils.deleteDirectory(workingDir);
			}
			catch (IOException ex) {
				Log.err("Failed to delete temporary directory", ex);
			}
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void download() throws IOException {
		Log.out("Download 'lcs-installer.zip'");
		Log.out();
		
		HttpDownloader httpDownloader = new HttpDownloader();
		httpDownloader.setDownloadPath(workingDir);
		lcsZip = httpDownloader.download(Consts.downUrl);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void unzip() throws IOException {
		Log.out("Unzip 'lcs-installer.zip'");
		Log.out();
		
		ZipUtils.unzip(lcsZip, workingDir);
	}
	
	/**
	 * 
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private void extract() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException {
		Class<?> clazz;
		try (JarClassLoader jarClassLoader = new JarClassLoader(new File(workingDir, Consts.srvJar))) {
			// version 1
//			clazz = jarClassLoader.loadClass(Consts.fpkkClsV1);
			// version 2
			clazz = jarClassLoader.loadClass(Consts.fpkkClsV2);
		}
		
		// version 1
//		Field fieldModulus = clazz.getDeclaredField("MODULUS");
//		Field fielPprivateExponent = clazz.getDeclaredField("PRIVATE_EXPONENT");
		// version 2
		Field fieldModulus = clazz.getDeclaredField("d");
		Field fielPprivateExponent = clazz.getDeclaredField("a");
		
		fieldModulus.setAccessible(true);
		fielPprivateExponent.setAccessible(true);
		modules = (String) fieldModulus.get(clazz);
		privateExponent = (String) fielPprivateExponent.get(clazz);
		
		Log.out("Extract MODULUS: " + modules);
		Log.out("Extract PRIVATE_EXPONENT: " + privateExponent);
		Log.out();
	}
	
	/**
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	private void generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		KeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(modules), new BigInteger(privateExponent));
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		String generatedKey;
		StringWriter stringWriter = new StringWriter();
		PemWriter pemWriter = new PemWriter(stringWriter);
		try {
			pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", privateKey.getEncoded()));
		}
		finally {
			IOUtils.closeQuietly(pemWriter);
			generatedKey = stringWriter.toString();
		}
		
		generatedkeyFile = new File(workingDir, "ideax.temp.pem");
		FileUtils.copy(generatedKey, generatedkeyFile);
		
		Log.out("Generated private key:\n" + generatedKey);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void convertKey() throws IOException  {
		OpenSSL openssl = new OpenSSL();
		openssl.exec("rsa", "-in", generatedkeyFile.getPath(), "-modulus");
		pemString = openssl.getOutput();
		
		Log.out("Converted private key:\n" + pemString);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void createPem() throws IOException {
		int startPos = pemString.indexOf("-----BEGIN RSA PRIVATE KEY-----");
		pemString = pemString.substring(startPos);
		FileUtils.copy(pemString, new File("ideax.pem"));
		
		Log.out("Key file 'ideax.pem' is created");
		Log.out();
	}
}
