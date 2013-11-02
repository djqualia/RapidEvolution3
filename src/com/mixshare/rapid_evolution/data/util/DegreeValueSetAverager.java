package com.mixshare.rapid_evolution.data.util;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.DataConstants;

public class DegreeValueSetAverager implements DataConstants {

	////////////
	// FIELDS //
	////////////
	
	private Vector<DegreeValue> degrees = new Vector<DegreeValue>();
	private float totalWeight = 0.0f;
	
	/////////////
	// GETTERS //
	/////////////
	
	public int getNumEntries() { return degrees.size(); }
	
	public Vector<DegreeValue> getDegrees() {
		Vector<DegreeValue> copy = new Vector<DegreeValue>(degrees.size());
		for (int d = 0; d < degrees.size(); ++d) {
			DegreeValue value = degrees.get(d);
			copy.add(new DegreeValue(value.getObject(), value.getPercentage() / totalWeight, DATA_SOURCE_COMPUTED));
		}
		java.util.Collections.sort(copy);
		return copy;		
	}
	
	public Vector<DegreeValue> getNormalizedDegrees() {
		Vector<DegreeValue> copy = new Vector<DegreeValue>(degrees.size());
		for (int d = 0; d < degrees.size(); ++d) {
			DegreeValue value = degrees.get(d);
			copy.add(new DegreeValue(value.getObject(), value.getPercentage() / totalWeight, DATA_SOURCE_COMPUTED));
		}
		java.util.Collections.sort(copy);		
		if (copy.size() > 0) {
			// the first entry is guaranteed to be the greatest			
			float maxValue = copy.get(0).getPercentage();
			for (DegreeValue degree : copy)
				degree.setPercentage(degree.getPercentage() / maxValue);			
		}
		return copy;		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void addDegreeValueSet(Vector<DegreeValue> degreeValues, float weight) {
		for (int d = 0; d < degreeValues.size(); ++d) {
			DegreeValue value = degreeValues.get(d);
			boolean found = false;
			int i = 0;
			while ((i < degrees.size()) && !found) {
				DegreeValue existingValue = (DegreeValue)degrees.get(i);
				if (existingValue.getObject() != null) {
					Object existingObject = existingValue.getObject();
					Object valueObject = value.getObject();
					boolean equals = (existingObject instanceof String) ? ((String)existingObject).equalsIgnoreCase((String)valueObject) : existingObject.equals(valueObject);
					if (equals) {
						found = true;
						existingValue.setPercentage(existingValue.getPercentage() + weight * value.getPercentage());
					}
				}
				++i;
			}
			if (!found) {
				degrees.add(new DegreeValue(value.getObject(), weight * value.getPercentage(), DATA_SOURCE_COMPUTED));
			}
		}	
		totalWeight += weight;
	}	
	
}
