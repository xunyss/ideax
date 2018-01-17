package org.xunyss.ideax;

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
}
