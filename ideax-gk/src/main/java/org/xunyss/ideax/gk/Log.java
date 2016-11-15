package org.xunyss.ideax.gk;

/**
 * 
 * @author XUNYSS
 */
public class Log {
	
	static void out() {
		System.out.println();
	}
	
	static void out(String msg) {
		System.out.println("GK" + msg);
	}
	
	static void err(String msg, Throwable t) {
		System.err.println("GK" + msg + ": ");
		t.printStackTrace();
	}
}
