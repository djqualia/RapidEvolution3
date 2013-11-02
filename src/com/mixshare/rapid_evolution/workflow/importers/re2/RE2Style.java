package com.mixshare.rapid_evolution.workflow.importers.re2;

import java.util.HashMap;
import java.util.Iterator;

public class RE2Style {

	static public int ROOT_STYLE_ID = -1;
	
	private String name;
	private int styleId;
	private int sIndex;
	private String description;
	private boolean categoryOnly;
	private HashMap<Integer, Object> child_style_ids = new HashMap<Integer, Object>();
	private HashMap<Integer, Object> parent_style_ids = new HashMap<Integer, Object>();
	
	public String toString() {
		return name + ", " + sIndex + "->" + styleId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isCategoryOnly() {
		return categoryOnly;
	}
	public void setCategoryOnly(boolean categoryOnly) {
		this.categoryOnly = categoryOnly;
	}
	public void addParentStyle(int styleid) {
		parent_style_ids.put(new Integer(styleid), null);
	}
	public Iterator<Integer> getParentStyleIter() {
		return parent_style_ids.keySet().iterator();
	}
	public void addChildStyle(int styleid) {
		child_style_ids.put(new Integer(styleid), null); 
	}
	public Iterator<Integer> getChildStyleIter() {
		return child_style_ids.keySet().iterator();
	}
	public int getStyleId() {
		return styleId;
	}
	public void setStyleId(int styleId) {
		this.styleId = styleId;
	}
	public int getSIndex() {
		return sIndex;
	}
	public void setSIndex(int index) {
		sIndex = index;
	}  
	
	
}
