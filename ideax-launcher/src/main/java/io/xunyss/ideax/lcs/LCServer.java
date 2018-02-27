package io.xunyss.ideax.lcs;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import io.xunyss.commons.io.ResourceUtils;
import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class LCServer {
	
	private static final String LCS_WEB_RESOURCES = "io/xunyss/ideax/lcs/webcontents";
	private static final String[] WELCOME_FILES = {"index.html"};
	
	/**
	 * 
	 */
	private final Server server;
	
	
	/**
	 * 
	 * @param port
	 * @param resourceService
	 * @param handleListener
	 * @throws Exception
	 */
	public LCServer(int port, boolean resourceService, TKHandleListener handleListener) throws Exception {
		if (handleListener != null) {
			handleListener.setLCServer(this);
		}
		server = new Server(port);
		initHandlers(resourceService, handleListener);
		
		Log.debug("Initialize LCSigner");
		LCSigner.getInstance().init();
	}
	
	/**
	 * 
	 * @param port
	 * @param resourceService
	 * @throws Exception
	 */
	public LCServer(int port, boolean resourceService) throws Exception {
		this(port, resourceService, null);
	}
	
	private void initHandlers(boolean resourceService, TKHandleListener handleListener) {
		// TKHandler
		Log.debug("Initialize LCServer TKHandler");
		TKHandler tkHandler = new TKHandler(handleListener);
		
		// ResourceHandler
		if (resourceService) {
			Log.debug("Initialize LCServer ResourceHandler");
			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setWelcomeFiles(WELCOME_FILES);
			resourceHandler.setResourceBase(
					ResourceUtils.getResource(LCS_WEB_RESOURCES, getClass().getClassLoader())
					.toExternalForm()
			);
			
			HandlerList handlerList = new HandlerList();
			handlerList.setHandlers(new Handler[] {tkHandler, resourceHandler});
			server.setHandler(handlerList);
		}
		else {
			server.setHandler(tkHandler);
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		try {
			Log.debug("Staring LCServer");
			server.start();
		}
		catch (Exception ex) {
			server.stop();
			throw ex;
		}
	}
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		Log.debug("Joining LCServer");
		server.join();
	}
	
	/**
	 * 
	 */
	public void stop() {
//		server.stop();
		Log.debug("Safety Stopping LCServer");
		safetyStop();
	}
	
	private void safetyStop() {
		final long serverStopTimeout = 10_000L;
		final long serverStopDelay = 3_000L;
		server.setStopTimeout(serverStopTimeout);
		
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(serverStopDelay);
					server.stop();
				}
				catch (Exception ex) {
					Log.error("Failed to stop LCServer", ex);
				}
			}
		}.start();
	}
}
