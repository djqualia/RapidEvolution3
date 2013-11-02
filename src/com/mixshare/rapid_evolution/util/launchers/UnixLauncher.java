package com.mixshare.rapid_evolution.util.launchers;

public class UnixLauncher {
	
    public static void main(String args[]) {
        try {
            String command[] = new String[1];
            command[0] = "re3.sh";
            Runtime.getRuntime().exec(command);
        }
        catch(Exception e) {
            System.out.println("An error occurred starting RE3: " + e);
        }
    }
	
}
