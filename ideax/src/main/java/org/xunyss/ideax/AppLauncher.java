package org.xunyss.ideax;

import java.io.File;
import java.io.IOException;

import org.xunyss.ideax.log.Log;

/**
 * 
 * @author XUNYSS
 */
public class AppLauncher {
	
	private static final String DIR_JETBRAINS = "JetBrains";
	private static final String DIR_INTELLIJ = "IntelliJ";
	
	private static final String ENV_PROGRAMFILES = "ProgramFiles";
	private static final String ENV_PROGRAMFILES_X86 = "ProgramFiles(x86)";
	
	public static void exec(String appPath) throws IOException {
		String command = appPath != null && appPath.length() > 0 ? appPath : findApp();
		
		if (command != null && command.length() > 0) {
			if (new File(command).isFile()) {
				Log.info("exec Intellij-IDEA : " + command);
				
				try {
					Process process = Runtime.getRuntime().exec(command);
					process.getInputStream().close();
					process.getOutputStream().close();
				}
				catch (IOException ioe) {
					Log.error("fail to execute Intellij-IDEA application", ioe);
				}
			}
			else {
				Log.error("wrong path: " + command);
			}
		}
		else {
			Log.error("cannot find Intellij-IDEA application");
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
		String jetbrains = programfiles + File.separator + DIR_JETBRAINS;
		
		File jbroot = new File(jetbrains);
		if (jbroot.isDirectory()) {
			File[] intellijdirs = jbroot.listFiles(pathname -> {
				String jbdir = pathname.getAbsolutePath();
				String shortname;
				
				int idx = jbdir.lastIndexOf(File.separator);
				shortname = jbdir.substring(idx + 1);
				
				return pathname.isDirectory()
						&& shortname.startsWith(DIR_INTELLIJ);
			});
			
			if (intellijdirs.length > 0) {
				return intellijdirs[0].getAbsolutePath()
						+ File.separator + "bin"
						+ File.separator + findExe();
			}
		}
		
		return null;
	}
	
	private static String findExe() {
		if (System.getenv(ENV_PROGRAMFILES_X86) != null) {
			return "idea64.exe";
		}
		return "idea.exe";
	}
}
