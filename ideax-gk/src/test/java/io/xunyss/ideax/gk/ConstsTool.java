package io.xunyss.ideax.gk;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author XUNYSS
 */
public class ConstsTool {
	
	@Ignore
	@Test
	public void enc() {
		String str = "hello world";
		byte[] ba = str.getBytes();
		
		for (int i = 0; i < ba.length; i++) {
			System.out.print(String.format("0x%x", ba[i]));
			if (i < ba.length - 1) {
				System.out.print(", ");
			}
		}
	}
	
	@Ignore
	@Test
	public void dec() {
		byte[] ba = {0x63, 0x6f, 0x6d, 0x2e, 0x6a, 0x65, 0x74, 0x62, 0x72, 0x61, 0x69, 0x6e, 0x73, 0x2e, 0x6c, 0x73, 0x2e, 0x66, 0x6c, 0x6f, 0x61, 0x74, 0x69, 0x6e, 0x67, 0x2e, 0x46, 0x6c, 0x6f, 0x61, 0x74, 0x69, 0x6e, 0x67, 0x50, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x4b, 0x65, 0x79, 0x73, 0x4b, 0x74};
		String str = new String(ba);
		System.out.println(str);
	}
	
	@Ignore
	@Test
	public void print() {
		System.out.println(Consts.downUrl);
		System.out.println(Consts.srvJar);
		System.out.println(Consts.fpkkClsV1);
		System.out.println(Consts.fpkkClsV2);
	}
}
