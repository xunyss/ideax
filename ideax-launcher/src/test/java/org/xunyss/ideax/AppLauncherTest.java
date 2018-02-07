package org.xunyss.ideax;

import java.net.InetAddress;

import org.junit.Test;

/**
 * 
 * @author XUNYSS
 */
public class AppLauncherTest {

	@Test
	public void findApp() {
		String appPath = AppLauncher.findApp();
		System.out.println(appPath);
		System.out.println(System.getProperty("os.arch"));
	}
	
	@Test
	public void loopback() throws Exception {
		InetAddress ia = InetAddress.getByName("PN-201705188-01");
		System.out.println(ia);
		System.out.println(ia.isLoopbackAddress());
		System.out.println(ia.isMulticastAddress());
		System.out.println(ia.isAnyLocalAddress());
	}
}
