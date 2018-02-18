package io.xunyss.ideax;

import java.net.BindException;

import org.eclipse.jetty.server.Server;

import io.xunyss.commons.lang.StringUtils;
import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class IdeaX {
	
	/*
	 * TODO: welcome page service
	 */
	
	/**
	 * 
	 */
	private static final int DEFAULT_PORT = 9797;
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		//------------------------------------------------------------------------------------------
		// java -jar ideax.jar [-port port-number] -server
		// java -jar ideax.jar [-port port-number] -exec <executable>
		//------------------------------------------------------------------------------------------
		
		int port = DEFAULT_PORT;
		boolean serverMode = false;
		String executable = null;
		
		try {
			for (int idx = 0; idx < args.length; idx++) {
				if ("-port".equals(args[idx])) {
					port = Integer.parseInt(args[++idx]);
					continue;
				}
				else if ("-server".equals(args[idx])) {
					serverMode = true;
					break;
				}
				else if ("-exec".equals(args[idx])) {
					executable = args[++idx];
					break;
				}
			}
			
			if (!serverMode && executable == null) {
				throw new IllegalArgumentException();
			}
		}
		catch (Exception ex) {
			usage();
			return;
		}
		
		new IdeaX().run(port, serverMode, executable);
	}
	
	private static void usage() {
		Log.out("Invalid arguments");
		Log.out("Usage: IX [-port port] {-server | -exec <executable>}");
	}
	
	
	//==============================================================================================
	
	private void run(int port, boolean serverMode, String executable) throws Exception {
		
		if (serverMode) {
		}
		else {
			
		}
		
		Log.info("Initialize LCSigner");
		LCSigner.getInstance().init();
		
		Log.info("Start LCS..");
		Server server = new Server(port);
		server.setHandler(new TKHandler(false));
		try {
			server.start();
		}
		catch (BindException ex) {
			server.stop();
			Log.error(ex.getMessage());
			return;
		}		
		
		
		if (StringUtils.isNotEmpty(executable)) {
			Log.info("Start application..");
			AppLauncher.exec(executable);
		}
		
		Log.info("LCS is ready..");
		Log.info("LCS address: http://<hostname>:" + port);
		server.join();
		
		Log.info("LCS is stopped");
	}
}
