package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.io.Serializable;

public class DiscogsReleaseLabelInstance implements Comparable<DiscogsReleaseLabelInstance>, Serializable {

    static private final long serialVersionUID = 0L;    
    
    private String name; // label name
    private String catno; // catalog #
    
    public DiscogsReleaseLabelInstance() { }
    public DiscogsReleaseLabelInstance(String name, String catno) {
        this.name = name;
        this.catno = catno;
    } 
	
    public String getName() { return name; }
    public String getCatalogId() { return catno; }
	public String getCatno() { return catno; }

	public void setCatno(String catno) { this.catno = catno; }
	public void setName(String name) { this.name = name; }
	
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(name);
        if ((catno != null) && (catno.length() > 0)) {
            result.append(" (");
            result.append(catno);
            result.append(")");
        }
        return result.toString();
    }
    
	public boolean equals(Object o) {
		if (o instanceof DiscogsReleaseLabelInstance) {
			DiscogsReleaseLabelInstance oP = (DiscogsReleaseLabelInstance)o;
			if (!oP.name.equals(name))
				return false;
			if (!oP.catno.equals(catno))			
				return false;
			return true;
		}
		return false;
	}     

    public int hashCode() {
        return toString().hashCode();
    }
    
    public int compareTo(DiscogsReleaseLabelInstance o) {
        return toString().compareToIgnoreCase(o.toString());        
    }
    
}
