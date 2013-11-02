package com.mixshare.rapid_evolution.ui.model.profile.details;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.ui.model.CommonModelManager;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.util.io.LineReader;

abstract public class AbstractCommonDetailsModelManager extends CommonModelManager {

	public AbstractCommonDetailsModelManager() { super(); }
	public AbstractCommonDetailsModelManager(LineReader lineReader) {
		super(lineReader);
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public void setFieldValue(Column column, Object value);
	abstract public void setFieldValue(Column column, Object value, Profile profile);
		
}
