package com.mixshare.rapid_evolution.util.debug;


public class StackTraceLogger {
		
	static public String getStackTrace() {
		StringBuffer result = new StringBuffer();
		Throwable t = new Throwable();
		StackTraceElement[] es = t.getStackTrace();
		for (int i = 1; i < es.length; i++) {
		   StackTraceElement e = es[i];
		   result.append("\n");
		   result.append("\tin class: ");
		   result.append(e.getClassName());
		   result.append("\tin source file: ");
		   result.append(e.getFileName());
		   result.append("\tin method: ");
		   result.append(e.getMethodName());
		   result.append("\tat line: ");
		   result.append(e.getLineNumber());
		   if (e.isNativeMethod())
			   result.append(" native");
		}		
		return result.toString();
	}
	
}
