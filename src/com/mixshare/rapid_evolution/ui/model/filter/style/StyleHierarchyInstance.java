package com.mixshare.rapid_evolution.ui.model.filter.style;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

/**
 * This class is used so that "breaks, funky" will show up as "funky" under "breaks" and "breaks" under "funky"
 */
public class StyleHierarchyInstance extends FilterHierarchyInstance {

    static private final long serialVersionUID = 0L;    	
	
    static private Logger log = Logger.getLogger(StyleHierarchyInstance.class);    
	
	public StyleHierarchyInstance(HierarchicalRecord record, TreeHierarchyInstance parentInstance) {
		super(record, parentInstance);
	}
	
	/**
	 * This method strips away the parent style names from the current style instance in the hierarchy...
	 */
	protected String calculateName() {
		StyleRecord style = (StyleRecord)record;
    	Vector<String> parentStyleNames = new Vector<String>();
    	TreeHierarchyInstance thisParentInstance = parentInstance;
    	while (thisParentInstance != null) {
    		String parentStyleName = thisParentInstance.getName();
    		parentStyleNames.add(parentStyleName);
    		thisParentInstance = thisParentInstance.getParentInstance();
    	}
    	String styleName = style.getStyleName();
    	for (String parentStyleName : parentStyleNames) {
			String parentReference = parentStyleName + ", ";
			int index = styleName.toLowerCase().indexOf(parentReference.toLowerCase());
			if (index < 0) {
				parentReference = ", " + parentStyleName;
				index = styleName.toLowerCase().indexOf(parentReference.toLowerCase());
			}
			if (index >= 0) {
				styleName = styleName.substring(0, index) + styleName.substring(index + parentReference.length());
			}
    	}	    	
    	return styleName;
	}
	
}
