package io.xunyss.ideax.log;

import java.io.PrintStream;
import java.util.Date;

/**
 * 
 * @author XUNYSS
 */
public class Log {
	
	/**
	 * Logging Level
	 */
	private enum Level {
		DEBUG, INFO, ERROR
	}
	
	/**
	 * 
	 */
	private static final String LOG_FORMAT =
			"[%1$5s] %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS.%2$tL %3$s.%4$s(:%5$d): %6$s";
	
	/**
	 * 
	 */
	private static final Level LOG_LEVEL = Level.DEBUG;
	
	
	private static final PrintStream OUT = System.out;
	private static final PrintStream ERR = System.err;
	
	private static final Date datetime = new Date();
	
	
	public static void out(String msg) {
		OUT.println(msg);
	}

	public static void err(String msg) {
		ERR.println(msg);
	}
	
	
	public static void debug(String msg) {
		log(Level.DEBUG, msg, null);
	}
	
	public static void info(String msg) {
		log(Level.INFO, msg, null);
	}
	
	public static void error(String msg) {
		log(Level.ERROR, msg, null);
	}
	
	public static void error(String msg, Throwable thrown) {
		log(Level.ERROR, msg, thrown);
	}
	
	private static void log(Level level, String msg, Throwable thrown) {
		if (level.ordinal() < LOG_LEVEL.ordinal()) {
			return;
		}
		
		datetime.setTime(System.currentTimeMillis());
		
		final int callerDepth = 2;
	//	StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		StackTraceElement[] traces = new Throwable().getStackTrace();
		String className  = traces[callerDepth].getClassName();
		String methodName = traces[callerDepth].getMethodName();
		int lineNumber    = traces[callerDepth].getLineNumber();
		
		PrintStream stream = (level == Level.ERROR ? ERR : OUT);
		
		stream.print(String.format(LOG_FORMAT, level, datetime, className, methodName, lineNumber, msg));
		if (thrown != null) {
			stream.print(": ");
			thrown.printStackTrace(stream);
		}
		else {
			stream.println();
		}
	}
}
