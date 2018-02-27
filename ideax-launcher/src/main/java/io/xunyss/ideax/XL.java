package io.xunyss.ideax;

import java.io.IOException;
import java.util.Properties;

import io.xunyss.commons.exec.ExecuteException;
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
	private static final int TUNNEL_MAX_ACTIVE = 2;
	private static final String TUNNEL_SUB_DOMAIN = "xunysslcs";
	
	
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
		Log.out("Usage: XL [-port port] {-server | -exec <executable>}");
	}
	
	
	//==============================================================================================
	
	private LocalTunnel localTunnel;
	private LCServer lcServer;
	private boolean handleLocalTunnel = false;
	private boolean handleLCServer = false;
	
	
	/**
	 * 
	 * @param port
	 * @param serverMode
	 * @param executable
	 * @throws Exception
	 */
	private void run(final int port, final boolean serverMode, final String executable) throws Exception {
		// disable all jetty-logging
		Properties jettyLogProps = new Properties();
		jettyLogProps.setProperty("log.LEVEL", "OFF");
		org.eclipse.jetty.util.log.StdErrLog.setProperties(jettyLogProps);
		
		// set Log-Level
		Log.setLevel(Log.Level.DEBUG);
		
		// 프로세스 종료시 / Control + C 종료시 수행
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Log.info("XL is stopped");
			}
		});
		
		if (serverMode) {
			runServer(port);
		}
		else {
			runExec(port, executable);
		}
	}
	
	/**
	 * 
	 * @param port
	 * @throws Exception
	 */
	private void runServer(int port) throws Exception {
		lcServer = new LCServer(port, true);
		lcServer.start();
		
		Log.info("LCServer Address: http://<hostname>:" + port);
		Log.info("LCServer is ready");
		
//		lcServer.join();
	}
	
	/**
	 * 
	 * @param port
	 * @param executable
	 * @throws Exception
	 */
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
		//------------------------------------------------------------------------------------------
		localTunnel = LocalTunnelClient.getDefault().create(port);
		localTunnel.setMonitoringListener(ltListener);
		localTunnel.setMaxActive(TUNNEL_MAX_ACTIVE);
		
		Log.info("Connecting Local-Tunnel");
		
		try {
			localTunnel.open(TUNNEL_SUB_DOMAIN);
			localTunnel.start();
		}
		catch (IOException ex) {
			Log.error("Failed to connect Local-Tunnel", ex);
		}
		
		Log.info("Local-Tunnel Sub-Domain: " + localTunnel.getRemoteDetails().getSubDomain());
		Log.info("Local-Tunnel is started");
		
		//------------------------------------------------------------------------------------------
		// 2. lcServer
		//------------------------------------------------------------------------------------------
		if (localTunnel.isRunning()) {
			lcServer = new LCServer(port, false, lcListener);
			try {
				lcServer.start();
				
				Log.info("LCServer Address: " + localTunnel.getRemoteDetails().getUrl());
				Log.info("LCServer is ready");
			}
			catch (Exception ex) {
				Log.error("Failed to start LCServer", ex);
				
				// if 'lcServer' fail --> stop 'localTunnel'
				Log.info("Stopping Local-Tunnel");
				localTunnel.stop();
			}
		}
		
		//------------------------------------------------------------------------------------------
		// 3. executable
		//------------------------------------------------------------------------------------------
		if (StringUtils.isNotEmpty(executable)) {
			Log.info("Starting Application");
			Log.info(executable);
			
			try {
				new ProcessExecutor().execute(executable);
			}
			catch (ExecuteException ex) {
				Log.error("Failed to execute Application", ex);
			}
		}
	}
	
	/**
	 * 
	 */
	private void stopForExec() {
		if (handleLocalTunnel && handleLCServer) {
			Log.info("Stopping Local-Tunnel");
			Log.info("Stopping LCServer");
			
			localTunnel.stop();
			lcServer.stop();
		}
	}
}
