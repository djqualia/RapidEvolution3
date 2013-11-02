package com.mixshare.rapid_evolution.data.user;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class PreferenceMap implements Serializable, DataConstants {

	static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(PreferenceMap.class);
	
	////////////
	// FIELDS //
	////////////
	
	private Map<Integer, Float> preferences;
	private Map<Integer, Float> preferenceWeights;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public PreferenceMap(int initialCapacity) {
		preferences = new LinkedHashMap<Integer, Float>(initialCapacity);
		preferenceWeights = new LinkedHashMap<Integer, Float>(initialCapacity);
	}
	public PreferenceMap() {
		preferences = new LinkedHashMap<Integer, Float>();
		preferenceWeights = new LinkedHashMap<Integer, Float>();
	}
	public PreferenceMap(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		int numPreferences = Integer.parseInt(lineReader.getNextLine());
		preferences = new LinkedHashMap<Integer, Float>(numPreferences);
		for (int i = 0; i < numPreferences; ++i) {
			int key = Integer.parseInt(lineReader.getNextLine());
			float value = Float.parseFloat(lineReader.getNextLine());
			preferences.put(key, value);
		}
		int numPreferenceWeights = Integer.parseInt(lineReader.getNextLine());
		preferenceWeights = new LinkedHashMap<Integer, Float>(numPreferenceWeights);
		for (int i = 0; i < numPreferenceWeights; ++i) {
			int key = Integer.parseInt(lineReader.getNextLine());
			float value = Float.parseFloat(lineReader.getNextLine());
			preferenceWeights.put(key, value);			
		}			
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public float getPreference(Integer id) {
		Float result = preferences.get(id);
		if (result != null)
			return result;
		return 0.0f;
	}
	
	public Vector<DegreeValue> getPreferenceDegrees() {
		Vector<DegreeValue> result = new Vector<DegreeValue>(preferences.size());
		for (Entry<Integer, Float> entry : preferences.entrySet())
			result.add(new DegreeValue(entry.getKey(), entry.getValue(), DATA_SOURCE_COMPUTED));
		return result;
	}
	
	public Map<Integer, Float> getPreferences() { return preferences; }
	public Map<Integer, Float> getPreferenceWeights() { return preferenceWeights; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setPreferenceDegrees(Vector<DegreeValue> degrees) {
		preferences.clear();
		for (DegreeValue degree : degrees) {
			preferences.put((Integer)degree.getObject(), degree.getPercentage());
			preferenceWeights.put((Integer)degree.getObject(), 1.0f);
		}
	}

	public void setPreferences(Map<Integer, Float> preferences) { this.preferences = preferences; }
	public void setPreferenceWeights(Map<Integer, Float> preferenceWeights) { this.preferenceWeights = preferenceWeights; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return preferences.toString(); }
	public String toString(Index index) {
		StringBuffer result = new StringBuffer();
		result.append("{");
		for (Entry<Integer, Float> entry : preferences.entrySet()) {
			if (result.length() > 1)
				result.append(", ");
			Record record = index.getRecord(entry.getKey());
			if (record != null) {
				result.append(record.toString());
				result.append("=");
				result.append(entry.getValue());
			}
		}
		result.append("}");
		return result.toString();
	}

	public void incrementPreference(Integer itemId, float preference) {
		incrementPreference(itemId, preference, 1.0f);
	}
	
	public void incrementPreference(Integer itemId, float preference, float weight) {
		if (!Float.isNaN(preference)) {
			Float existingPreference = preferences.get(itemId);
			if (existingPreference == null)
				existingPreference = preference * weight;
			else
				existingPreference = preference * weight + existingPreference;
			preferences.put(itemId, existingPreference);
			
			Float existingWeight = preferenceWeights.get(itemId);
			if (existingWeight == null)
				existingWeight = weight;
			else
				existingWeight = weight + existingWeight;
			preferenceWeights.put(itemId, existingWeight);
		}
	}	
		
	public void normalize() { normalize(false); }
	public void normalize(boolean useAlpha) {
		int alpha = RE3Properties.getInt("user_preference_degree_alpha");
		float maxPreference = 0.0f;
		Iterator<Float> prefIter = preferences.values().iterator();
		Iterator<Float> countIter = preferenceWeights.values().iterator();
		while (prefIter.hasNext()) {
			float absPreference = Math.abs(prefIter.next());
			float weight = countIter.next();
			absPreference /= weight;
			if (useAlpha) {
				if (weight < alpha)
					absPreference *= weight / alpha;
			}
			if (absPreference > maxPreference)
				maxPreference = absPreference;
		}
		Map<Integer, Float> normalizedPreferences = new LinkedHashMap<Integer, Float>(preferences.size());
		Map<Integer, Float> normalizedPreferenceWeights = new LinkedHashMap<Integer, Float>(preferenceWeights.size());
		if (maxPreference != 0.0f) {
			Iterator<Entry<Integer,Float>> iter = preferences.entrySet().iterator();
			countIter = preferenceWeights.values().iterator();
			while (iter.hasNext()) {
				Entry<Integer,Float> entry = iter.next();
				Integer itemId = entry.getKey();
				float preference = entry.getValue();
				float weight = countIter.next();
				preference /= weight;
				if (useAlpha) {
					if (weight < alpha)
						preference *= weight / alpha;
				}
				preference /= maxPreference;				
				if (Math.abs(preference) > 0.01) {
					normalizedPreferences.put(itemId, preference);
					normalizedPreferenceWeights.put(itemId, weight);
				}
			}
			preferences = normalizedPreferences;
			preferenceWeights = normalizedPreferenceWeights;
		}	
	}
	
	public void write(LineWriter writer) {
		writer.writeLine(1); //version
		writer.writeLine(preferences.size());
		for (Entry<Integer, Float> entry : preferences.entrySet()) {
			writer.writeLine(entry.getKey());
			writer.writeLine(entry.getValue());
		}
		writer.writeLine(preferenceWeights.size());
		for (Entry<Integer, Float> entry : preferenceWeights.entrySet()) {
			writer.writeLine(entry.getKey());
			writer.writeLine(entry.getValue());
		}
	}
			
}
