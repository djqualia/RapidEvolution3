package com.mixshare.rapid_evolution.net;

//log4j classes
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;

public abstract class CommandServer extends Thread {

	private static Logger log = Logger.getLogger(CommandServer.class);

	// i/o variables
	private Socket socket = null;
	private OutputStream output_stream = null;
	private InputStream input_stream = null;
	private InputStreamReader input_stream_reader = null;
	private BufferedReader buffered_reader = null;
	protected PrintStream print_stream = null;
	//private ObjectInput object_input = null;
	private boolean stopServing = false;

	public CommandServer(Socket sock) {
		socket = sock;
		try {
			socket.setSoTimeout(RE3Properties.getInt("socket_timeout"));
		} catch (java.lang.NumberFormatException nfe) {
			log.error("CommandServer(): number format error parsing socket_timeout");
		} catch (java.net.SocketException e) {
			log.error("CommandServer(): error setting socket timeout=" + e);
		}
		input_stream = null;
		try {
			input_stream = socket.getInputStream();
			input_stream_reader = new InputStreamReader(input_stream);
			buffered_reader = new BufferedReader(input_stream_reader);
			//object_input = new ObjectInputStream(input_stream);
		} catch (java.io.IOException e) {
			log.error("CommandServer(): error setting up input streams=" + e);
		}
		output_stream = null;
		try {
			output_stream = socket.getOutputStream();
			print_stream = new PrintStream(output_stream);        
		} catch (java.io.IOException e) {
			log.error("CommandServer(): error setting up output streams=" + e);
		}
	}

	public String getIpAddress() {
		if (socket == null) 
			return "";        
		return socket.getInetAddress().toString();
	}
	
	// sends just a string, usually used to piece together an entire line
	public void sendString(String str) {
		if (log.isTraceEnabled()) 
			log.trace("sendString(): sending string: " + str);
		print_stream.print(str);
	}

	// sends an entire line (string + end of line marker)
	public void sendLine(String str) {
		if (log.isTraceEnabled()) 
			log.trace("sendLine(): sending line: " + str);
		//print_stream.println(str);
		print_stream.print(str + "\r\n");
	}

	// sends a line which spans multiple lines
	public void sendComments(String comments) {
		StringTokenizer comment_tokens = new StringTokenizer(comments, "\n");
		int num_comment_lines = comment_tokens.countTokens();
		if (num_comment_lines < 1) {
			sendLine("1");
			sendLine("");
		} else {
			sendLine(String.valueOf(num_comment_lines));
			while (comment_tokens.hasMoreTokens()) {
				String comment_token = comment_tokens.nextToken();
				sendLine(comment_token);
			}
		}        
	}    
 
	// receives an entire line, null if it does not receive one
	public String receiveLine() {
		String return_val = null;
		try {
			return_val = buffered_reader.readLine();        
		} catch (java.net.SocketTimeoutException ste) {
			log.debug("receiveLine(): socket timeout receiving line");
		} catch (java.net.SocketException e) {
			log.debug("receiveLine(): socket exception: " + e);
		} catch (java.io.IOException e) {
			log.debug("receiveLine(): error receiving line=" + e);
		}
		if (log.isTraceEnabled()) 
			log.trace("receiveLine(): received line: " + return_val);
		return return_val;
	}

	public Socket getSocket() { return socket; }

	public void run() {
		if (log.isDebugEnabled()) 
			log.debug("run(): connection started: " + socket.getInetAddress().getHostName());

		try {
			if (authenticate()) {
	            while (isConnected()) {
	                String command = receiveLine();
	                if (command != null)
	                    ProcessCommand(command);
	                else {
	                    stoppedServing();
	                    disconnect();
	                }
	            }
	        }
	
	        stoppedServing();
		} catch (Exception e) {
			log.error("run(): error", e);
		}

		try {
			socket.close();
			print_stream.close();
			output_stream.close();
			buffered_reader.close();
			input_stream_reader.close();
			input_stream.close();
			//object_input.close();            
		} catch (java.io.IOException io) {
			log.error("run(): error closing connection streams", io);
		}
		if (log.isDebugEnabled()) 
			log.debug("run(): connection ended: " + socket.getInetAddress().getHostName());
	}

	public void disconnect() { stopServing = true; }
	public boolean isConnected() { return !stopServing; }

	abstract protected void ProcessCommand(String command);
	abstract protected boolean authenticate();
	abstract protected void stoppedServing();
	
}
