package io.xunyss.ideax;

import org.eclipse.jetty.server.Server;

import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.lang.StringUtils;
import io.xunyss.ideax.lcs.LCServer;
import io.xunyss.ideax.lcs.TKHandleListener;
import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class XL {
	
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
				throw new IllegalArgumentException("Invalid arguments");
			}
		}
		catch (Exception ex) {
			usage();
			return;
		}
		
		new XL().run(port, serverMode, executable);
	}
	
	private static void usage() {
		Log.out("Invalid arguments");
		Log.out("Usage: IX [-port port] {-server | -exec <executable>}");
	}
	
	
	//==============================================================================================
	
	private void run(int port, boolean serverMode, String executable) throws Exception {
		
		if (serverMode) {
			Log.info("Start LCS..");
			LCServer lcserver = new LCServer(port, true);
			lcserver.start();
			
			Log.info("LCS address: http://<hostname>:" + port);
			Log.info("LCS is ready..");
			lcserver.join();
		}
		else {
			// 1. jetty
			final LCServer lcserver = new LCServer(port, true, new TKHandleListener() {
				@Override
				public void handled(Server server) {
					try {
						lcserver.stop();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			
			Log.info("LCS address: http://<hostname>:" + port);
			Log.info("LCS is ready..");
			lcserver.join();
			
			lcserver.start();
			// 2. localtunnel
			
			// 3. executable
			if (StringUtils.isNotEmpty(executable)) {
				Log.info("Start application..");
				AppLauncher.exec(executable);
				new ProcessExecutor().execute(executable);
				// process's streams 을 명시적으로 close 하지 않는 것이 문제가 되는지 확인!
			}
		}
		
		
		

		
		Log.info("LCS is stopped");
		
		
		
		
		// Control + C 종료시 리소스 반환/정상 종료 처리
	}
}
