package org.xunyss.ideax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class TicketHandler extends AbstractHandler {
	
	private static final String URI_OBTAIN_TICKET = "/rpc/obtainTicket.action";
	private static final String URI_RELEASE_TICKET = "/rpc/releaseTicket.action";
	
	private boolean autostop = true;
	
	public TicketHandler(boolean autostop) {
		this.autostop = autostop;
	}
	
	/**
	 * 
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		Log.debug("REQUEST >>");
		Log.debug(request.getRequestURI());
		Log.debug(request.getQueryString());
		
		if (response.isCommitted() || baseRequest.isHandled()) {
			return;
		}
		
		baseRequest.setHandled(true);
		
		HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod());
		
		if (requestMethod == HttpMethod.GET && target.equals(URI_OBTAIN_TICKET)) {
			handleObtain(request, response);
		}
		else if (requestMethod == HttpMethod.GET && target.equals(URI_RELEASE_TICKET)) {
			handleRelease(request, response);
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Bad Request");
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void handleObtain(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
//		String buildDate		= request.getParameter("buildDate");
//		String clientVersion	= request.getParameter("clientVersion");
//		String hostName			= request.getParameter("hostName");
//		String machineId		= request.getParameter("machineId");
//		String productCode		= request.getParameter("productCode");
//		String productFamilyId	= request.getParameter("productFamilyId");
		String salt				= request.getParameter("salt");
//		String secure			= request.getParameter("secure");
		String userName			= request.getParameter("userName");
//		String version			= request.getParameter("version");
//		String versionNumber	= request.getParameter("versionNumber");
		
		final int prolongation_period = 607875500;
		final String responseXml = "<ObtainTicketResponse>"
				+ "<message></message>"
				+ "<prolongationPeriod>" + prolongation_period + "</prolongationPeriod>"
				+ "<responseCode>OK</responseCode>"
				+ "<salt>" + salt + "</salt>"
				+ "<ticketId>1</ticketId>"
				+ "<ticketProperties>licensee=" + userName + "\tlicenseType=0\t</ticketProperties>"
				+ "</ObtainTicketResponse>";
		
		LicenseSigner licenseSigner = LicenseSigner.getInstance();
		String signature = licenseSigner.signMessage(responseXml);
		
		responseOK(response,
				String.format("<!-- %s -->\n%s", signature, responseXml));
		
		/*
		 * 
		 */
		if (autostop) {
			stopServer();
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void handleRelease(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
//		String clientVersion	= request.getParameter("clientVersion");
//		String hostName			= request.getParameter("hostName");
//		String machineId		= request.getParameter("machineId");
//		String productCode		= request.getParameter("productCode");
//		String productFamilyId	= request.getParameter("productFamilyId");
//		String salt				= request.getParameter("salt");
//		String secure			= request.getParameter("secure");
//		String ticketId			= request.getParameter("ticketId");
//		String userName			= request.getParameter("userName");
		
		// i don't know.. T.T
		final String responseXml = "<ReleaseTicketResponse>"
				+ "<responseCode>OK</responseCode>"
				+ "</ReleaseTicketResponse>";
				
		responseOK(response,
				responseXml);
	}
	
	/**
	 * 
	 * @param response
	 * @param body
	 * @throws IOException
	 * @throws ServletException
	 */
	private void responseOK(HttpServletResponse response, String body) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/xml; charset=utf-8");
		response.getWriter().write(body);
	}
	
	
	/**
	 * 
	 */
	private void stopServer() {
		final long serverStopTimeout = 10000L;
		final long serverStopDelay = 3000L;
		getServer().setStopTimeout(serverStopTimeout);
		
		new Thread() {
			@Override
			public void run() {
				try {
					Log.info("stop license server");
					
					Thread.sleep(serverStopDelay);
					getServer().stop();
				}
				catch (Exception e) {
					Log.error("fail to stop license server", e);
				}
			}
		}.start();
	}
}
