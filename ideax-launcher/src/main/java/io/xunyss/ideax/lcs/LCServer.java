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
	
	private Server server;
	
	private boolean resourceService;
	private TKHandleListener handleListener;
	
	
	public LCServer(int port, boolean resourceService, TKHandleListener handleListener) throws Exception {
		this.resourceService = resourceService;
		this.handleListener = handleListener;
		
		server = new Server(port);
		initHandlers();
		
		Log.info("Initialize LCSigner");
		LCSigner.getInstance().init();
	}
	
	public LCServer(int port, boolean resourceService) throws Exception {
		this(port, resourceService, null);
	}
	
	private void initHandlers() {
		// TKHandler
		TKHandler tkHandler = new TKHandler(handleListener);
		
		// ResourceHandler
		if (resourceService) {
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
	
	public void start() throws Exception {
		try {
			server.start();
		}
		catch (Exception ex) {
			server.stop();
			throw ex;
		}
	}
	
	public void stop() throws Exception {
		server.stop();
	}
	
	public void join() throws InterruptedException {
		server.join();
	}
}
