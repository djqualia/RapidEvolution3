package com.mixshare.rapid_evolution.util;

import java.io.File;
import java.io.IOException;

import com.mixshare.rapid_evolution.RE3Properties;

public class OSHelper {

	static public final int UNKNOWN = 0;
	static public final int LINUX = 1;
	static public final int SOLARIS = 2;
	static public final int WINDOWS = 3;
	static public final int MACOS = 4;

	static public boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0);
	}
	static public boolean isWindowsVista() {
		return (System.getProperty("os.name").toLowerCase().indexOf("windows vista") >= 0);
	}
	static public boolean isWindows7() {
		return (System.getProperty("os.name").toLowerCase().indexOf("windows 7") >= 0);
	}
	static public boolean isWindowsXP() {
		return (System.getProperty("os.name").toLowerCase().indexOf("windows xp") >= 0);
	}
	static public boolean isMacOS() {
		return getPlatform() == MACOS;
	}

    public static File getWorkingDirectory() {
    	String dataDirectory = RE3Properties.getProperty("application_data_directory_name");
    	if (dataDirectory == null)
    		return getWorkingDirectory("Rapid Evolution 3 Unit");
    	if (dataDirectory.equalsIgnoreCase("%{RE3_LIBRARY_PATH}")) // dev mode
    		return new File("i:/re3library");
    	dataDirectory = FileUtil.unify(dataDirectory);
    	if (dataDirectory.endsWith("/"))
    		dataDirectory = dataDirectory.substring(0, dataDirectory.length() - 1);
    	return new File(dataDirectory);
    }

    public static File getWorkingDirectory(String applicationName) {
        final String userHome = System.getProperty("user.home", ".");
        final File workingDirectory;
        switch (getPlatform()) {
            case LINUX:
            case SOLARIS:
                workingDirectory = new File(userHome, applicationName + '/');
                break;
            case WINDOWS:
                final String applicationData = System.getenv("APPDATA");
                if (applicationData != null)
                    workingDirectory = new File(applicationData, applicationName + '/');
                else
                    workingDirectory = new File(userHome, applicationName + '/');
                break;
            case MACOS:
                workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
                break;
            default:
                return new File(".");
        }
        if (!workingDirectory.exists())
            if (!workingDirectory.mkdirs())
                throw new RuntimeException("The working directory could not be created: " + workingDirectory);
        return workingDirectory;
    }

    public static String getPlatformAsString() { return System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + ")"; }
    public static int getPlatform() {
	    final String sysName = System.getProperty("os.name").toLowerCase();
	    if (sysName.contains("windows"))
	        return WINDOWS;
	    else if (sysName.contains("mac"))
	        return MACOS;
	    else if (sysName.contains("linux"))
	        return LINUX;
	    else if (sysName.contains("solaris"))
	        return SOLARIS;
	    else
	    	return UNKNOWN;
    }

    static private Boolean useAlternateMusicDirectory = null;
    static public String getMusicDirectory() {
    	if (useAlternateMusicDirectory == null) {
    		initMusicDirectory();
    	}
    	return useAlternateMusicDirectory
    			? RE3Properties.getProperty("alternate_music_directory")
				: RE3Properties.getProperty("root_music_directory");
    }
    static public void initMusicDirectory() {
    	if ((RE3Properties.getProperty("root_music_directory").length() > 0)
    			&& new File(RE3Properties.getProperty("root_music_directory")).exists()) {
			useAlternateMusicDirectory = false;
			return;
    	} else  if ((RE3Properties.getProperty("alternate_music_directory").length() > 0)
    			&& new File(RE3Properties.getProperty("alternate_music_directory")).exists()) {
			useAlternateMusicDirectory = true;
			return;
    	}
    	useAlternateMusicDirectory = false;
    }
    static public String translateMusicDirectory(String filename) {
    	if ((filename == null) || (filename.length() == 0))
    		return filename;
    	String musicDirectory = getMusicDirectory(); // calling this first ensures useAlternateMusicDirectory is set
    	if (!useAlternateMusicDirectory)
    		return filename;
    	File file = new File(filename);
    	File altMusicDir = new File(musicDirectory);
    	File rootDir = new File(RE3Properties.getProperty("root_music_directory"));
    	try {
    		if (file.getCanonicalPath().startsWith(rootDir.getCanonicalPath())) {
    			filename = altMusicDir.getCanonicalPath() + file.getCanonicalPath().substring(rootDir.getCanonicalPath().length());
    		}
    	} catch (IOException e) {
    	}
    	return filename;
    }

    static public void main(String[] args) {
    	System.out.println(getWorkingDirectory());
    }


}
