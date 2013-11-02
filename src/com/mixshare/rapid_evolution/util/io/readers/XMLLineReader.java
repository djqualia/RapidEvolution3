package com.mixshare.rapid_evolution.util.io.readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class XMLLineReader extends DefaultHandler implements LineReader {

    static private Logger log = Logger.getLogger(XMLLineReader.class);

    private boolean isDone = false;
    private final LinkedList<String> nextLines = new LinkedList<String>();
    private final StringBuffer currentLine = new StringBuffer();
    private boolean inLine = false;
    private final Semaphore linkedListSem = new Semaphore(1);
    private int linesRead = 0;

    public XMLLineReader(String filename) throws FileNotFoundException {
    	setFilename(filename, false);
    }
    public XMLLineReader(String filename, boolean useSeparateThread) throws FileNotFoundException {
    	setFilename(filename, useSeparateThread);
    }

    public void setFilename(String filename, boolean useSeparateThread) throws FileNotFoundException {
        try {
            if (filename != null) {
                File file = new File(filename);
                if (file.exists()) {
                	if (useSeparateThread)
                		new ParseThread(this, filename).start();
                	else {
	                	SAXParserFactory factory = SAXParserFactory.newInstance();
	                    SAXParser saxParser = factory.newSAXParser();
	                    saxParser.parse(new File(filename), this);
                	}
                    if (log.isTraceEnabled())
                    	log.trace("setFilename(): parsing filename=" + filename);
                }
            }
        } catch (SAXParseException spe) {
        	if (log.isDebugEnabled())
        		log.debug("setFilename(): sax parse exception" + spe);
        	throw new FileNotFoundException();
        } catch (FileNotFoundException fnfe) {
        	isDone = true;
        	throw fnfe;
        } catch (Exception e) {
            log.error("setFilename(): error Exception", e);
            isDone = true;
        }
    }

    @Override
	public String getNextLine() {
    	if (isDone && (nextLines.size() == 0))
    		return null;
    	while (!isDone && (nextLines.size() == 0)) {
			if (log.isTraceEnabled())
				log.trace("getNextLine(): buffer empty, waiting...");
    		try { Thread.sleep(1); } catch (Exception e) { }
    	}
    	try {
    		linkedListSem.acquire();
    		String result = nextLines.remove(0);
    		++linesRead;
    		if (linesRead == ((linesRead >> 10) << 10))
    			if (log.isTraceEnabled())
    				log.trace("getNextLine(): # lines read=" + linesRead);
    		return result;
    	} catch (Exception e) { } finally {
    		linkedListSem.release();
    	}
    	return null;
    }

    @Override
	public void close() {
    	isDone = true;
    }

    static public void main(String[] args) {
    	try {
    		RapidEvolution3.loadLog4J();
    		XMLLineReader lineReader = new XMLLineReader("c:/temp/test.xml");
    		String line = lineReader.getNextLine();
    		while (line != null) {
    			log.info("line=" + line);
    			line = lineReader.getNextLine();
    		}
    		lineReader.close();
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }

    // HANDLER BASE:

    @Override
	public void startDocument() { }
    @Override
	public void endDocument() {
    	isDone = true;
    	if (log.isTraceEnabled())
    		log.trace("endDocument(): is done");
    }

    @Override
	public void startElement(String uri, String localName, String qname, Attributes attr) {
    	if (isDone)
    		return;
    	if (qname.equals("l") || qname.equals("line"))
    		inLine = true;
    }

    @Override
	public void endElement(String uri, String localName, String qname) {
    	if (isDone)
    		return;
    	if (inLine) {
    		try {
    			linkedListSem.acquire();
    			nextLines.add(currentLine.toString());
    		} catch (Exception e) { } finally {
    			linkedListSem.release();
    		}
    		currentLine.delete(0, currentLine.length());
    		inLine = false;
    	}
    }

    @Override
	public void characters(char[] ch, int start, int length) {
    	if (isDone)
    		return;
    	if (inLine)
    		currentLine.append(new String(ch, start, length));
    }

    @Override
	public void ignorableWhitespace(char[] ch, int start, int length) { }

    @Override
	public void startPrefixMapping(String prefix, String uri) { }

    @Override
	public void endPrefixMapping(String prefix) { }

    @Override
	public void warning(SAXParseException spe) {
    	if (log.isDebugEnabled())
    		log.debug("warning(): warning", spe);
    }

    @Override
	public void fatalError(SAXParseException spe) throws SAXException {
    	log.error("fatalError(): error", spe);
    	isDone = true;
        throw spe;
    }

    private class ParseThread extends Thread {
    	private final DefaultHandler handler;
    	private final String filename;
    	public ParseThread(DefaultHandler handler, String filename) {
    		this.handler = handler;
    		this.filename = filename;
    	}
    	@Override
		public void run() {
    		try {
	        	SAXParserFactory factory = SAXParserFactory.newInstance();
	            SAXParser saxParser = factory.newSAXParser();
	            saxParser.parse(new File(filename), handler);
            } catch (FileNotFoundException fnfe) {
            	isDone = true;
    		} catch (Exception e) {
    			log.error("run(): error", e);
    		}
    	}
    }

}
