package com.mixshare.rapid_evolution.util.launchers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mixshare.rapid_evolution.util.StringUtil;

public class WindowsLauncher {
	
	static private String[] invalidHeapStrings = new String[] { "Could not reserve enough space for object heap", "Invalid maximum heap size" };
	
	static public void main(String args[]) {
        try {
        	String bestJavaPath = getJavaPath("C:\\Program Files (x86)\\Java");
        	if (bestJavaPath == null)
        		bestJavaPath = getJavaPath("C:\\Program Files\\Java");
        	       
        	boolean launch64Bit = false;
        	if (!isJava64bit(bestJavaPath)) {
	            String command[] = new String[5];
	            command[0] = "cmd";
	            command[1] = "/c";
	            command[2] = "re3.bat";
	            command[3] = bestJavaPath;
	        	command[4] = "lib\\qt\\win32\\qtjambi-win32-msvc2005-4.5.2_01.jar";
	        	System.out.println("Attempting to launch 32 bit mode with JVM=" + bestJavaPath);
	            Process proc = Runtime.getRuntime().exec(command);
	            
	            // any error message?
	            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
	            // any output?
	            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
	            // kick them off
	            errorGobbler.start();
	            outputGobbler.start();                                    
	            Thread.sleep(2500);
	            	            
	            if (isInvalidMemory(errorGobbler.getOutput()) || isInvalidMemory(outputGobbler.getOutput())) {
	            	launch64Bit = true;	            
	            	System.out.println("Could not launch with 32-bit JVM, trying 64-bit...");
	            }
	            
        	} else {
        		launch64Bit = true;
        	}
        	
        	if (launch64Bit) {
        		bestJavaPath = getJavaPath("C:\\Program Files\\Java");
        		if (isJava64bit(bestJavaPath)) {
		            String command[] = new String[5];
		            command[0] = "cmd";
		            command[1] = "/c";
		            command[2] = "re3.bat";
		            command[3] = bestJavaPath;
		        	command[4] = "lib\\qt\\win64\\qtjambi-win64-msvc2005x64-4.5.2_01.jar";
		        	System.out.println("Attempting to launch 64 bit mode with JVM=" + bestJavaPath);
		        	Process proc = Runtime.getRuntime().exec(command);
		        	
		            // any error message?
		            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
		            // any output?
		            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
		            // kick them off
		            errorGobbler.start();
		            outputGobbler.start();                                    
		            Thread.sleep(2500);

		            if (isInvalidMemory(errorGobbler.getOutput()) || isInvalidMemory(outputGobbler.getOutput())) {
		            	System.out.println("Could not launch with 64-bit JVM (not enough memory?), attempting to launch with lower values");		            	
		            	launchWithLessRAM(bestJavaPath, "lib\\qt\\win64\\qtjambi-win64-msvc2005x64-4.5.2_01.jar", "64");
		            }
		        	
        		} else {        			
        			System.out.println("No 64 bit JVM found, attempting to launch with lesser RAM values using 32 bit JVM");        			
        			launchWithLessRAM(bestJavaPath, "lib\\qt\\win32\\qtjambi-win32-msvc2005-4.5.2_01.jar", "32");
        		}
        	}
            
        }
        catch(Exception e) {
            System.out.println("An error occurred starting RE3: " + e);
        }
    }
	
	static private void launchWithLessRAM(String javaPath, String qtLib, String mode) {
		try {
	    	FileReader inputstream = new FileReader("re3.bat");
	    	BufferedReader inputbuffer = new BufferedReader(inputstream);
	    	inputbuffer.readLine(); // echo off
	    	String re3Command = inputbuffer.readLine();
	    	inputbuffer.close();
	    	inputstream.close();
	    	
	    	String prefix = "-mx";
	    	String suffix = "m";
	    	int startIndex = re3Command.indexOf(prefix);
	    	if (startIndex >= 0) {
	    		int endIndex = re3Command.indexOf(suffix, startIndex);
	    		if (endIndex >= 0) {
	    			int maxMemory = Integer.parseInt(re3Command.substring(startIndex + prefix.length(), endIndex));
	    			System.out.println("Initial max memory set to=" + maxMemory);
	    			
	    			int decrement = 100;
	    			boolean failed = true;
	    			while ((failed) && (maxMemory > 100)) {
	    				maxMemory -= decrement;
	    				String newCommand = re3Command.substring(0, startIndex) + prefix + maxMemory + re3Command.substring(endIndex);
	    				newCommand = StringUtil.replace(newCommand, "%1", javaPath);
	    				newCommand = StringUtil.replace(newCommand, "%2", qtLib);
	    				newCommand = "cmd /c " + newCommand;
	    				
	    				System.out.println("Attempting to launch " + mode + " bit mode with JVM=" + javaPath + ", qtLib=" + qtLib + ", max memory=" + maxMemory);
	    				System.out.println("\t[DEBUG] command used: " + newCommand);	    				
	    				Process proc = Runtime.getRuntime().exec(newCommand);
			        	
			            // any error message?
	    				StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
			            // any output?
	    				StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
			            // kick them off
			            errorGobbler.start();
			            outputGobbler.start();                                    
			            Thread.sleep(200);
	
			            if (!isInvalidMemory(errorGobbler.getOutput()) && !isInvalidMemory(outputGobbler.getOutput()))
			            	failed = false;
	    			}
	    		}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    static private String getJavaPath(String directory) {
    	try {
    		System.out.println("Looking for the best JVM in=" + directory);
    		File javaDir = new File(directory);
    		File[] files = javaDir.listFiles();
    		String bestJavaPath = null;
    		String bestJavaVersion = null;
    		for (File file : files) {
    			if (file.isDirectory()) {
    				try {
	    				String javaPath = file.getAbsolutePath() + "\\bin\\java";
	    				System.out.println("Found Java Path=" + javaPath);
	    				String[] command = new String[] { "cmd", "/c", javaPath, "-version" };
	    				Process process = Runtime.getRuntime().exec(command);
	    				VersionProcessor outputGobbler = new VersionProcessor(process.getErrorStream());
	    				VersionProcessor outputGobbler2 = new VersionProcessor(process.getInputStream());
	    				outputGobbler.start();
	    				outputGobbler2.start();
	    				while (outputGobbler.getJavaVersion() == null)
	    					Thread.sleep(5);
	    				String javaVersion = outputGobbler.getJavaVersion();
	    				if (javaVersion.equals("UNKNOWN")) {
	        				while (outputGobbler2.getJavaVersion() == null)
	        					Thread.sleep(5);
	        				javaVersion = outputGobbler2.getJavaVersion();
	    				}
	    				System.out.println("\tJava Version=" + javaVersion);
	    				if (javaVersion.startsWith("1.6") || javaVersion.startsWith("1.5") || javaVersion.startsWith("1.7")) {
	    					if ((bestJavaPath == null) || (bestJavaVersion.compareTo(javaVersion) < 0)) {
	    						bestJavaPath = javaPath;
	    						bestJavaVersion = javaVersion;
	    					}
	    				}
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		if (bestJavaPath != null) {
        		System.out.println("Best Java Path=" + bestJavaPath);
        		System.out.println("Best Java Version=" + bestJavaVersion);    			 
    			return bestJavaPath;
    		}
    	} catch (Exception e) {
    		e.printStackTrace();    		
    	}
    	return "java"; // utilize default
    }
	
    static private class VersionProcessor extends Thread {        
        private InputStream is;
        private String javaVersion;        
        public VersionProcessor(InputStream is) {
            this.is = is;
        }        
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                	line = line.toLowerCase();
                	if (line.indexOf("java version") >= 0)
                		javaVersion = line.substring(14, line.length() - 1);
                	//System.out.println(line);
                }
            } catch (IOException ioe) {
            	ioe.printStackTrace();
            }
            if (javaVersion == null)
            	javaVersion = "UNKNOWN";
        }        
        public String getJavaVersion() { return javaVersion; }
    }    

    static private boolean isJava64bit(String javaPath) {
    	boolean is64bit = false;
    	if (javaPath == null)
    		return false;
    	try {
			 String[] command = new String[] { "cmd", "/c", javaPath, "-version" };
			 Process process = Runtime.getRuntime().exec(command);
			 ArchitectureProcessor outputGobbler = new ArchitectureProcessor(process.getErrorStream());
			 ArchitectureProcessor outputGobbler2 = new ArchitectureProcessor(process.getInputStream());
			 outputGobbler.start();
			 outputGobbler2.start();
			 while (!outputGobbler.isDone())
				 Thread.sleep(5);
			 if (outputGobbler.isValid())
				 is64bit = outputGobbler.is64bit();
			 else {
				 while (!outputGobbler2.isDone())
					 Thread.sleep(5);
				 if (outputGobbler2.isValid())
					 is64bit = outputGobbler2.is64bit();
			 }
    	} catch (Exception e) {
    		e.printStackTrace();    		
    	}
    	if (is64bit)
    		System.out.println("64-bit Java Detected (" + javaPath + ")");
    	else
    		System.out.println("32-bit Java Detected (" + javaPath + ")");
    	return is64bit;
    }

    static private class ArchitectureProcessor extends Thread {        
        private InputStream is;
        private boolean is64bit = false;     
        private boolean done = false;
        private boolean valid = false;
        public ArchitectureProcessor(InputStream is) {
            this.is = is;
        }        
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                	line = line.toLowerCase();
                	if (line.indexOf("64-bit") >= 0)
                		is64bit = true;
                	valid = true;
                	//System.out.println(line);
                }
            } catch (IOException ioe) {
            	ioe.printStackTrace();
            }
            done = true;
        }
        public boolean isValid() { return valid; }
        public boolean isDone() { return done; }
        public boolean is64bit() { return is64bit; }
    }    
    
    static private class StreamGobbler extends Thread {        
        private InputStream is;
        private StringBuffer output = new StringBuffer();        
        public StreamGobbler(InputStream is) { this.is = is; }        
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                	if (output.length() > 0)
                		output.append("\n");
                	output.append(line);
                }
                br.close();
                isr.close();
            } catch (IOException ioe) {
        		ioe.printStackTrace();
            }
        }        
        public String getOutput() { return output.toString(); }        
    }
    
    static private boolean isInvalidMemory(String output) {
    	for (String invalidHeapString : invalidHeapStrings)
    		if (output.indexOf(invalidHeapString) >= 0)
    			return true;
    	return false;
    }
    
}
