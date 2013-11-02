package com.mixshare.rapid_evolution.util.io.writers;

import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.xml.sax.helpers.AttributesImpl;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.util.StringUtil;

public class XMLLineWriter extends AbstractLineWriter {

    private static Logger log = Logger.getLogger(XMLLineWriter.class);
    
    private String filename;
    private FileOutputStream outputstream;
    private AttributesImpl atts;
    private TransformerHandler hd;
    
    public XMLLineWriter(String _filename) {
        filename = _filename;
        try {
            outputstream = new FileOutputStream(filename);
            StreamResult streamResult = new StreamResult(outputstream);
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            // SAX2.0 ContentHandler.
            hd = tf.newTransformerHandler();
            Transformer serializer = hd.getTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "users.dtd");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            hd.setResult(streamResult);
            hd.startDocument();
            atts = new AttributesImpl();
            hd.startElement("", "", "lines", atts);
        } catch (Exception e) {
            log.error("XMLTextFileWriter(): error", e);
        }
    }

    public void writeLine(boolean value) { writeLine(String.valueOf(value)); }
    public void writeLine(int value) { writeLine(String.valueOf(value)); }
    public void writeLine(long value) { writeLine(String.valueOf(value)); }
    public void writeLine(byte value) { writeLine(String.valueOf(value)); }
    public void writeLine(float value) { writeLine(String.valueOf(value)); }
    public void writeLine(double value) { writeLine(String.valueOf(value)); }
    public void writeLine(short value) { writeLine(String.valueOf(value)); }
    
    public void writeLine(String text) {
        writeLine(text, null);
    }

    public void writeLine(Object line, String annotation) {
        try {
        	String text = StringUtil.cleanString(String.valueOf(line));
            atts.clear();
            if ((annotation != null) && RE3Properties.getBoolean("write_line_annotations")) {
            	atts.addAttribute("", "", "hint", "CDATA", annotation);
            }
            hd.startElement("", "", "l", atts);
            hd.characters(text.toCharArray(), 0, text.length());
            hd.endElement("", "", "l");
        } catch (Exception e) {
            log.error("writeLine(): error", e);
        }
    }

    public void close() {
        try {
            hd.endElement("", "", "lines");
            hd.endDocument();        	
            outputstream.close();
        } catch (Exception e) {
            log.error("close(): error", e);
        }
    }
    
    static public void main(String[] args) {
    	try {
	    	RapidEvolution3.loadLog4J();
	    	XMLLineWriter writer = new XMLLineWriter("c:/temp/test.xml");
	    	writer.writeLine("test1");
	    	writer.writeLine("test2");
	    	writer.writeLine("test3");
	    	writer.close();
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }
	
}
