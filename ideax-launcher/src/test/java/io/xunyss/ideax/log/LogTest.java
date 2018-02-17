package io.xunyss.ideax.log;

import org.junit.Ignore;
import org.junit.Test;

import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class LogTest {
	
	@Ignore
	@Test
	public void logging() {
		Log.debug("this is debug message");
		Log.info("this is info message");
		Log.error("this is error message");
	}
}
