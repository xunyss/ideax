package io.xunyss.ideax;

import io.xunyss.ideax.lcs.LCServer;
import io.xunyss.ideax.lcs.TKHandleListener;
import io.xunyss.ideax.log.Log;
import io.xunyss.localtunnel.LocalTunnel;
import io.xunyss.localtunnel.LocalTunnelClient;

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
		
		final LCServer lcServer;
		
		if (serverMode) {
			Log.info("Start LCS..");
			
			lcServer = new LCServer(port, true);
			lcServer.start();
			
			Log.info("LCS address: http://<hostname>:" + port);
			Log.info("LCS is ready..");
			lcServer.join();
		}
		else {
			// 1. jetty
			lcServer = new LCServer(port, false, new TKHandleListener() {
				@Override
				public void handled() {
//					try {
//						getLCServer().stop();
//					}
//					catch (Exception ex) {
//						ex.printStackTrace();
//					}
				}
			});
			lcServer.start();
			
			Log.info("LCS address: http://<hostname>:" + port);
			Log.info("LCS is ready..");
			
			// 2. localtunnel
			LocalTunnel localTunnel = LocalTunnelClient.getDefault().create(9797);
			localTunnel.setMonitoringListener(null);
			localTunnel.setMaxActive(2);
			
			localTunnel.open("xunysslcs");
			
			String url = localTunnel.getRemoteDetails().getUrl();
			
			localTunnel.start();
			
			Log.info("URL: " + url);
			
//			// 3. executable
//			if (StringUtils.isNotEmpty(executable)) {
//				Log.info("Start application..");
//				AppLauncher.exec(executable);
//				new ProcessExecutor().execute(executable);
//				// process's streams 을 명시적으로 close 하지 않는 것이 문제가 되는지 확인!
//			}
		}
		
		
		

		
		Log.info("LCS is stopped");
		
		
		
		
		// Control + C 종료시 리소스 반환/정상 종료 처리
	}
}
