package io.xunyss.ideax;

import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.lang.StringUtils;
import io.xunyss.ideax.lcs.LCServer;
import io.xunyss.ideax.lcs.TKHandleListener;
import io.xunyss.ideax.log.Log;
import io.xunyss.localtunnel.LocalTunnel;
import io.xunyss.localtunnel.LocalTunnelClient;
import io.xunyss.localtunnel.monitor.MonitoringListener;

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
	
	private LocalTunnel localTunnel;
	private LCServer lcServer;
	private boolean handleLocalTunnel = false;
	private boolean handleLCServer = false;
	
	private void run(final int port, final boolean serverMode, final String executable) throws Exception {
		// 프로세스 종료시 / Control + C 종료시 수행
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Log.info("LCS is stopped.");
			}
		});
		
		if (serverMode) {
			runServer(port);
		}
		else {
			runExec(port, executable);
		}
	}
	
	private void runServer(int port) throws Exception {
		lcServer = new LCServer(port, true);
		lcServer.start();
		
		Log.info("LCS address: http://<hostname>:" + port);
		Log.info("LCS is ready..");
		lcServer.join();
	}
	
	private void runExec(int port, String executable) throws Exception {
		MonitoringListener ltListener = new MonitoringListener() {
			@Override
			public void onExecuteProxyTask(long threadId) {
			}
			@Override
			public void onConnectRemote(int activeTaskCount) {
			}
			@Override
			public void onDisconnectRemote(int activeTaskCount) {
			}
			@Override
			public void onErrorRemote(int activeTaskCount) {
			}
			@Override
			public void onConnectLocal(int activeTaskCount) {
			}
			@Override
			public void onDisconnectLocal(int activeTaskCount) {
				handleLocalTunnel = true;
				stopForExec();
			}
			@Override
			public void onErrorLocal(int activeTaskCount) {
			}
		};
		
		TKHandleListener lcListener = new TKHandleListener() {
			@Override
			public void handled() {
				handleLCServer = true;
				stopForExec();
			}
		};
		
		//------------------------------------------------------------------------------------------
		// 1. localTunnel
		localTunnel = LocalTunnelClient.getDefault().create(port);
		localTunnel.setMonitoringListener(ltListener);
		localTunnel.setMaxActive(2);
		localTunnel.open("xunysslcs");
		localTunnel.start();
		
		Log.info("Tunnel started.");
		
		//------------------------------------------------------------------------------------------
		// 2. jetty
		lcServer = new LCServer(port, false, lcListener);
		lcServer.start();
		
		Log.info("LCS address: " + localTunnel.getRemoteDetails().getUrl());
		Log.info("LCS is ready..");
		
		//------------------------------------------------------------------------------------------
		// 3. executable
		if (StringUtils.isNotEmpty(executable)) {
			Log.info("Start application..");
			new ProcessExecutor().execute(executable);
		}
	}
	
	private void stopForExec() {
		if (handleLocalTunnel && handleLCServer) {
			localTunnel.stop();
			lcServer.stop();
		}
	}
}
