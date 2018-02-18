package io.xunyss.ideax.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import io.xunyss.commons.io.IOUtils;

/**
 * 
 * @author XUNYSS
 */
public class SimpleFileDecoder extends InputStream {
	
	private InputStream inputStream;
	
	
	public SimpleFileDecoder(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public int read() throws IOException {
		int bs, be, loopc = 0;
		while (true) {
			loopc++;
			bs = inputStream.read();
			if (bs == IOUtils.EOF) {
				return bs;
			}
			else if (bs == ' ' || bs == '\r' || bs == '\n') {
				continue;
			}
			
			if (loopc > 4) {
				// InputStream#read(byte b[], int off, int len) throws IOException
				// 메소드에서 IOException 먹어 버림
			//	throw new IOException("Invalid encoded stream");
				
				// TODO: 적절한 Exception 객체 사용할 것
				throw new UnsupportedEncodingException("Invalid encoded stream");
			}
			loopc = 0;
			break;
		}
		be = inputStream.read();
		String sb = String.format("%c%c", bs, be);
		return Integer.parseInt(sb, 16) & 0xff ^ 0xff;
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}
}
