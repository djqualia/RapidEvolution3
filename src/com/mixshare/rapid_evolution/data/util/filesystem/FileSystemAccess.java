package com.mixshare.rapid_evolution.data.util.filesystem;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;

public class FileSystemAccess {

    static private Logger log = Logger.getLogger(FileSystemAccess.class);
	
	static private FileSystemInterface impl;
	
	static {
		try {
			impl = (FileSystemInterface)Class.forName(RE3Properties.getProperty("file_system_implementation")).newInstance();
		} catch (Exception e) {
			log.error("FileSystemInterface(): couldn't load implementation=" + RE3Properties.getProperty("file_system_implementation"), e);
		}
	}
	
	static public FileSystemInterface getFileSystem() {
		return impl;
	}
	
}
