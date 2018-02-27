package io.xunyss.ideax;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import io.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
@Deprecated
public class AppLauncher {
	
	private static final String DIR_COMPANY = "Je" + "tB" + "ra" + "in" + "s";
	private static final String DIR_PRODUCT = "In" + "te" + "ll" + "iJ";
	
	private static final String ENV_PROGRAMFILES = "ProgramFiles";
	private static final String ENV_PROGRAMFILES_X86 = "ProgramFiles(x86)";
	
	public static void exec(String appPath) {
		String command = appPath != null && appPath.length() > 0 ? appPath : findApp();
		
		if (command != null && command.length() > 0) {
			if (new File(command).isFile()) {
				Log.info("exec application: " + command);
				
				try {
					Process process = Runtime.getRuntime().exec(command);
					process.getInputStream().close();
					process.getOutputStream().close();
				}
				catch (IOException ioe) {
					Log.error("fail to execute application", ioe);
				}
			}
			else {
				Log.error("wrong path: " + command);
			}
		}
		else {
			Log.error("cannot find application");
		}
	}
	
	protected static String findApp() {
		String appPath = findApp(System.getenv(ENV_PROGRAMFILES));
		
		if (appPath == null) {
			appPath = findApp(System.getenv(ENV_PROGRAMFILES_X86));
		}
		
		return appPath;
	}
	
	private static String findApp(String programfiles) {
		File companyDir = new File(programfiles + File.separator + DIR_COMPANY);
		if (companyDir.isDirectory()) {
			
			// code: java 1.8 SPEC
//			File[] productDirs = companyDir.listFiles(pathname -> {
//				String productDir = pathname.getAbsolutePath();
//				String shortname;
//				
//				int idx = productDir.lastIndexOf(File.separator);
//				shortname = productDir.substring(idx + 1);
//				
//				return pathname.isDirectory()
//						&& shortname.startsWith(DIR_PRODUCT);
//			});
			
			// code: under java 1.8 SPEC
			File[] productDirs = companyDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					String productDir = pathname.getAbsolutePath();
					String shortname;
					
					int idx = productDir.lastIndexOf(File.separator);
					shortname = productDir.substring(idx + 1);
					
					return pathname.isDirectory()
							&& shortname.startsWith(DIR_PRODUCT);
				}
			});
			
			if (productDirs.length > 0) {
				return productDirs[0].getAbsolutePath()
						+ File.separator + "bin"
						+ File.separator + findExe();
			}
		}
		
		return null;
	}
	
	private static String findExe() {
		if (System.getenv(ENV_PROGRAMFILES_X86) != null) {
			return "id" + "ea" + "64" + ".e" + "xe";
		}
		return "id" + "ea" + ".e" + "xe";
	}
}
