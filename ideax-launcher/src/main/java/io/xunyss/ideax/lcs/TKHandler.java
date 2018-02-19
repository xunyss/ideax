package io.xunyss.ideax.lcs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class TKHandler extends AbstractHandler {
	
	private static final String URI_OBTAIN_TK =
			"/r" + "pc" + "/o" + "bt" + "ai" + "nT" + "ic" + "ke" + "t." + "ac" + "ti" + "on";
	private static final String URI_RELEASE_TK =
			"/r" + "pc" + "/r" + "el" + "ea" + "se" + "Ti" + "ck" + "et" + ".a" + "ct" + "io" + "n";
	
	private TKHandleListener listener;
	
	public TKHandler(TKHandleListener listener) {
		this.listener = listener;
	}
	
	/**
	 * 
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (response.isCommitted() || baseRequest.isHandled()) {
			return;
		}
		
		// enable to handle
		if (HttpMethod.valueOf(request.getMethod()) == HttpMethod.GET && enableHandle(target)) {
			
			// set true handled flag
			baseRequest.setHandled(true);
			
			Log.debug("REQUEST << " + request.getRemoteAddr());
			Log.debug(request.getRequestURI());
			Log.debug(request.getQueryString());
			
			switch (target) {
				case URI_OBTAIN_TK:
					handleObtain(request, response);
					break;
				case URI_RELEASE_TK:
					handleRelease(request, response);
					break;
			}
			
			// notify to listener
			if (listener != null) {
				listener.handled(getServer());
			}
		}
		
		// other request
		// 아무것도 안하고 그냥 return; --> server 에게 맡김
//		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//		response.getWriter().write("Bad Request");
	}
	
	/**
	 * 
	 * @param target
	 * @return
	 */
	private boolean enableHandle(String target) {
		return	target.equals(URI_OBTAIN_TK) ||
				target.equals(URI_RELEASE_TK)
				;
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
		
//		String r01 = request.getParameter("buil" + "dDat" + "e");
//		String r02 = request.getParameter("clie" + "ntVe" + "rsio" + "n");
//		String r03 = request.getParameter("host" + "Name");
//		String r04 = request.getParameter("mach" + "ineI" + "d");
//		String r05 = request.getParameter("prod" + "uctC" + "ode");
//		String r06 = request.getParameter("prod" + "uctF" + "amil" + "yId");
		String r07 = request.getParameter("salt");
//		String r08 = request.getParameter("secu" + "re");
		String r09 = request.getParameter("user" + "Name");
//		String r10 = request.getParameter("vers" + "ion");
//		String r11 = request.getParameter("vers" + "ionN" + "umbe" + "r");
		
		final int prolongation_period = 607_875_500;
		final String responseXml = ""
				+ "<Obt" + "ainT" + "icke" + "tRes" + "pons" + "e>"
				+ "<mes" + "sage" + "></m" + "essa" + "ge>"
				+ "<pro" + "long" + "atio" + "nPer" + "iod>" + prolongation_period + "</pr" + "olon" + "gati" + "onPe" + "riod" + ">"
				+ "<res" + "pons" + "eCod" + "e>OK" + "</re" + "spon" + "seCo" + "de>"
				+ "<sal" + "t>" + r07 + "</sa" + "lt>"
				+ "<tic" + "ketI" + "d>1<" + "/tic" + "ketI" + "d>"
				+ "<tic" + "ketP" + "rope" + "rtie" + "s>li" + "cens" + "ee=" + r09 + "\tlic" + "ense" + "Type" + "=0\t<" + "/tic" + "ketP" + "rope" + "rtie" + "s>"
				+ "</Ob" + "tain" + "Tick" + "etRe" + "spon" + "se>";
		
		LCSigner licenseSigner = LCSigner.getInstance();
		String signature = licenseSigner.signMessage(responseXml);
		
		responseOK(response,
				String.format("<!--" + " %s " + "-->\n%s", signature, responseXml));
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
		
//		String r1 = request.getParameter("clie" + "ntVe" + "rsio" + "n");
//		String r2 = request.getParameter("host" + "Name");
//		String r3 = request.getParameter("mach" + "ineI" + "d");
//		String r4 = request.getParameter("prod" + "uctC" + "ode");
//		String r5 = request.getParameter("prod" + "uctF" + "amil" + "yId");
//		String r6 = request.getParameter("salt");
//		String r7 = request.getParameter("secu" + "re");
//		String r8 = request.getParameter("tick" + "etId");
//		String r9 = request.getParameter("user" + "Name");
		
		// I don't know.. T.T
		final String responseXml = ""
				+ "<Rel" + "ease" + "Tick" + "etRe" + "spon" + "se>"
				+ "<res" + "pons" + "eCod" + "e>OK" + "</re" + "spon" + "seCo" + "de>"
				+ "</Re" + "leas" + "eTic" + "ketR" + "espo" + "nse>";
				
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
		response.getWriter().flush();
	}
}
