package io.xunyss.ideax.codec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import io.xunyss.commons.io.FileUtils;
import io.xunyss.commons.io.IOUtils;
import io.xunyss.commons.io.ResourceUtils;

/**
 * 
 * @author XUNYSS
 */
public class SimpleFileEncodeTool {
	
	private void encodeFile(String filePath) throws IOException {
		File file = new File(filePath);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		FileUtils.copy(file, bytes);
		IOUtils.closeQuietly(bytes);
		
		byte[] data = bytes.toByteArray();
		int cnt = 0;
		for (int idx = 0; idx < data.length; idx++) {
			System.out.print(String.format("%02x ", 0xff ^ data[idx]));
			if ((++cnt % 32) == 0) {
				System.out.println();
			}
		}
	}
	
	@Test
	public void enc() throws IOException {
		encodeFile("some_file");
	}
	
	@Test
	public void dec() throws IOException {
		// @see LCSigner.java
		final String PEM_RESOURCE_PATH = "ke" + "y/" + "id" + "ea" + "x." + "pe" + "m";
		
		InputStream inputStream = new SimpleFileDecoder(
				ResourceUtils.getResourceAsStream(PEM_RESOURCE_PATH, ClassLoader.getSystemClassLoader()));
		String contents = IOUtils.toString(inputStream);
		System.out.println(contents);
	}
}
