package io.xunyss.ideax.lcs;

import org.eclipse.jetty.server.Server;

public interface TKHandleListener {
	
	void handled(Server server);
	
	
//	private void stopServer(final Server server) {
//		final long serverStopTimeout = 10_000L;
//		final long serverStopDelay = 3_000L;
//		server.setStopTimeout(serverStopTimeout);
//		
//		new Thread() {
//			@Override
//			public void run() {
//				try {
//					Log.info("stop lcs..");
//					
//					Thread.sleep(serverStopDelay);
//					server.stop();
//				}
//				catch (Exception e) {
//					Log.error("fail to stop lcs", e);
//				}
//			}
//		}.start();
//	}
}
