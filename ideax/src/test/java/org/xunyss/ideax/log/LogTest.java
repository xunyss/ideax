package org.xunyss.ideax.log;

import org.junit.Test;

/**
 * 
 * @author XUNYSS
 */
public class LogTest {
	
	@Test
	public void logging() {
		Log.debug("this is debug message");
		Log.info("this is info message");
		Log.error("this is error message");
	}
}