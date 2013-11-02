package com.mixshare.rapid_evolution.util.sort;

public class SortObjectWrapper implements Comparable<SortObjectWrapper> {

    private Object object;
    private double sort_value;
    
    public SortObjectWrapper(Object object, double sort_value) {
        this.object = object;
        this.sort_value = sort_value;
    }
        
    public Object getObject() { return object; }
    public double getValue() { return sort_value; }
    
    public void setValue(double value) { this.sort_value = value; }

    public String toString() { return object.toString(); }
    
    public int compareTo(SortObjectWrapper sO) {
        if (sort_value < sO.sort_value)
        	return -1;
        else if (sort_value > sO.sort_value)
        	return 1;
        return 0;                
    }    
    
}
