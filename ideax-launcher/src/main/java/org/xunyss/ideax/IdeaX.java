package org.xunyss.ideax;

import java.net.BindException;

import org.eclipse.jetty.server.Server;
import org.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class IdeaX {
	
	private static final int DEFAULT_PORT = 9797;
	
	/**
	 * <pre>
	 * java -jar ideax.jar
	 * java -jar ideax.jar 9797
	 * java -jar ideax.jar 9797 -exec
	 * java -jar ideax.jar 9797 -exec "C:\Program Files\Company\Product\bin\Application.exe"
	 * java -jar ideax.jar 9797 -server
	 * </pre>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		/*
		 * license server port
		 */
		int port = DEFAULT_PORT;
		
		/*
		 * execute application / shutdown after obtain ticket
		 */
		boolean launch = true;
		String appPath = null;
		
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException nfe) {
				exit();
				return;
			}
			
			if (args.length > 1) {
				if ("-exec".equals(args[1])) {
					if (args.length > 2) {
						appPath = args[2];
					}
				}
				else if ("-server".equals(args[1])) {
					launch = false;
				}
				else {
					exit();
					return;
				}
			}
		}
		
		
		/**
		 * initialize LCSigner
		 */
		Log.info("initialize LCSigner");
		LCSigner.getInstance().init();
		
		/**
		 * start lcs
		 */
		Log.info("start lcs..");
		Server server = new Server(port);
		server.setHandler(new TKHandler(launch));
		try {
			server.start();
		}
		catch (BindException be) {
			server.stop();
			Log.error(be.getMessage());
			return;
		}
		
		/**
		 * execute application
		 */
		if (launch) {
			Log.info("start application..");
			AppLauncher.exec(appPath);
		}
		
		/**
		 * 
		 */
		Log.info("lcs is ready..");
		Log.info("lcs address: http://localhost:" + port);
		server.join();
		
		/**
		 * 
		 */
		Log.info("lcs is stopped");
	}

	/**
	 * 
	 */
	private static void exit() {
		Log.err("program exit: invalid arguments");
	}
}
