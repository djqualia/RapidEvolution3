package com.mixshare.rapid_evolution.util;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.io.StreamGobbler;

public class CommandRunner {

    static private Logger log = Logger.getLogger(StringUtil.class);   
	
    ////////////
    // FIELDS //
    ////////////
    
    private int exitVal;
	private String output;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommandRunner(String command) {
		try {			
            Process proc = Runtime.getRuntime().exec(command);
            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), log);
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), log);
            // kick them off
            errorGobbler.start();
            outputGobbler.start();                                    
            // any error???
            exitVal = proc.waitFor();	
            String errorOutput = errorGobbler.getOutput();
            String normalOutput = outputGobbler.getOutput();            
            output = normalOutput;
            if (errorOutput.length() > normalOutput.length())
            	output = errorOutput;
		} catch (Exception e) {
			log.error("CommandRunner(): error", e);
		}
	}	

	public CommandRunner(String[] command) {
		try {			
            Process proc = Runtime.getRuntime().exec(command);
            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), log);
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), log);
            // kick them off
            errorGobbler.start();
            outputGobbler.start();                                    
            // any error???
            exitVal = proc.waitFor();	
            String errorOutput = errorGobbler.getOutput();
            String normalOutput = outputGobbler.getOutput();            
            output = normalOutput;
            if (errorOutput.length() > normalOutput.length())
            	output = errorOutput;
		} catch (Exception e) {
			log.error("CommandRunner(): error", e);
		}
	}	

	/////////////
	// GETTERS //
	/////////////
	
	public int getExitVal() { return exitVal; }
	public String getOutput() { return output; }
	
}
