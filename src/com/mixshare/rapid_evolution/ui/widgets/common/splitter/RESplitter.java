package com.mixshare.rapid_evolution.ui.widgets.common.splitter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.trolltech.qt.gui.QSplitter;
import com.trolltech.qt.gui.QSplitterHandle;

public class RESplitter extends QSplitter {

	static private Logger log = Logger.getLogger(RESplitter.class);
	
	private boolean useCustomHandle;
	private String splitterName;
	
	public RESplitter(String splitterName, boolean useCustomHandle) {
		this.splitterName = splitterName;
        splitterMoved.connect(this, "splitterMoved(Integer,Integer)");        
        this.useCustomHandle = useCustomHandle;
        if (useCustomHandle)
        	setHandleWidth(7);
	}
	
	public void splitterMoved(Integer pos, Integer index) {
		if (log.isTraceEnabled())
			log.trace("splitterMoved(): pos=" + pos + ", index=" + index);
		String key = splitterName + "_size";
    	List<Integer> sizes = sizes();
		UIProperties.setProperty(key + "_1", String.valueOf(sizes.get(0)));
		UIProperties.setProperty(key + "_2", String.valueOf(sizes.get(1)));			
	}
	
	
	protected QSplitterHandle createHandle() {
		if (useCustomHandle)
			return new RESplitterHandle(orientation(), this);
		return super.createHandle();

	}
	
	public void restorePosition() {
		String key = splitterName + "_size";
		if (UIProperties.hasProperty(key + "_1")) {
	        ArrayList<Integer> splitterSizes = new ArrayList<Integer>();
        	splitterSizes.add(UIProperties.getInt(key + "_1"));
        	splitterSizes.add(UIProperties.getInt(key + "_2"));        	
			setSizes(splitterSizes);			
		}
	}
	
	public void restorePosition(int a, int b) { restorePosition(a, b, 0); }
	public void restorePosition(int a, int b, int minimum) {
		String key = splitterName + "_size";
		if (UIProperties.hasProperty(key + "_1")) {
	        a = UIProperties.getInt(key + "_1");
	        b = UIProperties.getInt(key + "_2");
		}
        ArrayList<Integer> splitterSizes = new ArrayList<Integer>();
    	splitterSizes.add(a > minimum ? a : minimum);
    	splitterSizes.add(b > minimum ? b : minimum);        	
		setSizes(splitterSizes);		
	}

	public int restoreFirstPosition() {
		String key = splitterName + "_size";
		if (UIProperties.hasProperty(key + "_1")) {
	        ArrayList<Integer> splitterSizes = new ArrayList<Integer>();
	        int size1 = UIProperties.getInt(key + "_1");
	        int size2 = UIProperties.getInt(key + "_2");
        	splitterSizes.add(size1 + size2);
        	splitterSizes.add(0);        	
			setSizes(splitterSizes);
			return size2;
		}
		return 0;
	}
	
}
