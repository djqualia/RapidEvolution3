package com.mixshare.rapid_evolution.data.search.parameters.search;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class UserDataTypeWeight implements Serializable {

    static private Logger log = Logger.getLogger(UserDataTypeWeight.class);
	
    static private final long serialVersionUID = 0L;    		
	
	private UserDataType userDataType;
	private Object userDataValue;
	private float weight;
	
	public UserDataTypeWeight(UserDataType userDataType, Object userDataValue, float weight) {
		this.userDataType = userDataType;
		this.userDataValue = userDataValue;
		this.weight = weight;
	}
	public UserDataTypeWeight(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		userDataType = new UserDataType(lineReader);
		int userType = Integer.parseInt(lineReader.getNextLine());
		if (userType == 1)
			userDataValue = lineReader.getNextLine();
		else if (userType == 2)
			userDataValue = Integer.parseInt(lineReader.getNextLine());
		else if (userType == 3)
			userDataValue = Float.parseFloat(lineReader.getNextLine());
		weight = Float.parseFloat(lineReader.getNextLine());
	}
		
	public UserDataType getUserDataType() { return userDataType; }
	public Object getUserDataValue() { return userDataValue; }
	public float getWeight() { return weight; }

	public void setUserDataValue(Object userDataValue) { this.userDataValue = userDataValue; }
	public void setUserDataType(UserDataType userDataType) { this.userDataType = userDataType; }
	public void setWeight(float weight) { this.weight = weight; }
	
	public String toString() { 
		return userDataType.toString() + "=" + userDataValue + "x" + weight;
	}

	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		userDataType.write(writer);
		if (userDataValue instanceof String) {
			writer.writeLine(1);
			writer.writeLine((String)userDataValue);
		} else if (userDataValue instanceof Integer) {
			writer.writeLine(2);
			writer.writeLine((Integer)userDataValue);			
		} else if (userDataValue instanceof Float) {
			writer.writeLine(3);
			writer.writeLine((Float)userDataValue);			
		} else {
			log.warn("write(): unknown user data type=" + userDataValue.getClass());
			writer.writeLine(-1);
		}
		writer.writeLine(weight);
	}
	
}
