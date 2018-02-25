package io.xunyss.ideax.lcs;

/**
 * 
 * @author XUNYSS
 */
public abstract class TKHandleListener {
	
	/**
	 * 
	 */
	private LCServer lcServer;
	
	
	/**
	 * 
	 * @param lcServer
	 */
	void setLCServer(LCServer lcServer) {
		this.lcServer = lcServer;
	}
	
	/**
	 * 
	 * @return
	 */
	protected LCServer getLCServer() {
		return lcServer;
	}
	
	
	/**
	 * 
	 */
	public abstract void handled();
}
