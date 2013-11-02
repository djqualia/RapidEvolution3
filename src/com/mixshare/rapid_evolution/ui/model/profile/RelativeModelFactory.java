package com.mixshare.rapid_evolution.ui.model.profile;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * This class returns the various types of model managers used when viewing profile tabs.  It will first
 * check to see if an instance of the model already exists, so the user's column/sorting preferences can be
 * preserved for each type of model manager.
 */
public class RelativeModelFactory implements Serializable {

	static private Logger log = Logger.getLogger(RelativeModelFactory.class);	
    static private final long serialVersionUID = 0L;    
	
	////////////
	// FIELDS //
	////////////
	
	private Map<Class<?>, ModelManagerInterface> relativeModelMap = new HashMap<Class<?>, ModelManagerInterface>();
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public RelativeModelFactory() { }
	public RelativeModelFactory(LineReader lineReader) {
		try {
			int version = Integer.parseInt(lineReader.getNextLine());
			int size = Integer.parseInt(lineReader.getNextLine());
			for (int i = 0; i < size; ++i) {
				String className = lineReader.getNextLine();
				className = StringUtil.replace(className, "com.mixshare.rapid_evolution.ui.model.profile.search.EventModelManager", "com.mixshare.rapid_evolution.ui.model.profile.search.ProfileEventModelManager");
				int spaceIndex = className.indexOf(" ");
				if (spaceIndex > 0)
					className = className.substring(spaceIndex + 1);
				Class<?> modelClass = Class.forName(className);
				if (modelClass != null) {
					if (log.isTraceEnabled())
						log.trace("RelativeModelFactory(): processing model class=" + className);
					Constructor<?> constructor = modelClass.getConstructor(LineReader.class);
					if (constructor != null) {
						ModelManagerInterface modelManager = (ModelManagerInterface)constructor.newInstance(lineReader);
						relativeModelMap.put(modelClass, modelManager);
					} else {
						log.warn("RelativeModelFactory(): expected constructor not found for class=" + className);
					}
				} else {
					log.warn("RelativeModelFactory(): class not found=" + className);
				}
			}
		} catch (Exception e) {
			log.error("RelativeModelFactory(): error", e);
		}
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public ModelManagerInterface getRelativeModelManager(Class<?> classObject) {
		ModelManagerInterface result = null;
		try {
			result = relativeModelMap.get(classObject);
			if (result == null) {
				result = (ModelManagerInterface)classObject.newInstance();
				result.initColumns();
				relativeModelMap.put(classObject, result);
			}
		} catch (Exception e) {
			log.error("getRelativeModelManager(): error", e);
		}
		return result;
	}

	public Map<Class<?>, ModelManagerInterface> getRelativeModelMap() {
		return relativeModelMap;
	}

	public void setRelativeModelMap(
			Map<Class<?>, ModelManagerInterface> relativeModelMap) {
		this.relativeModelMap = relativeModelMap;
	}
	
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(relativeModelMap.size());
		for (Entry<Class<?>, ModelManagerInterface> entry : relativeModelMap.entrySet()) {
			writer.writeLine(entry.getKey().toString());
			entry.getValue().write(writer);
		}
	}
	
}
