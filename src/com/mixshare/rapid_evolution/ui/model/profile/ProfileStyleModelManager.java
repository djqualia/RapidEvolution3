package com.mixshare.rapid_evolution.ui.model.profile;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ProfileStyleModelManager extends StyleModelManager {

    static private Logger log = Logger.getLogger(ProfileStyleModelManager.class);	
    static private final long serialVersionUID = 0L;    

    static public StaticTypeColumn[] ALL_COLUMNS = {		
    	COLUMN_STYLE_NAME.getInstance(true),
    	COLUMN_DEGREE.getInstance(true),
    	COLUMN_SOURCE.getInstance(false)
    };    
	
    ////////////
    // FIELDS //
    ////////////
    
    transient protected SearchProfile relativeProfile;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ProfileStyleModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("relativeProfile")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public ProfileStyleModelManager() {	}
	public ProfileStyleModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS)
			sourceColumns.add(column);	
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	
	public String getTypeDescription() { return "Profile Style"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getSourceData(): columnId=" + columnId + ", record=" + record);		
		StyleRecord styleRecord = (StyleRecord)record;
		// can insert model specific columns here
		if (columnId == COLUMN_DEGREE.getColumnId()) {	
			if (relativeProfile != null)
				return Accuracy.getAccuracy((int)(relativeProfile.getMaxSourceStyleDegree(styleRecord) * 100.0f));
			return Accuracy.getAccuracy(0);
		} else if (columnId == COLUMN_SOURCE.getColumnId()) {
			if (relativeProfile != null)
				return DataConstantsHelper.getDataSourceDescription(relativeProfile.getMaxSourceStyleSource(styleRecord));
			return DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_UNKNOWN);
 		}
		return super.getSourceData(columnId, record);
	}
		
	/////////////
	// SETTERS //
	/////////////
		
	public void setRelativeProfile(SearchProfile relativeProfile) {
		this.relativeProfile = relativeProfile;
		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported())
			load();
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	public void updateInstance(TreeHierarchyInstance treeInstance) { updateInstance(treeInstance, false, true); }

	public void refresh() {
		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported())
			load();
		else
			super.refresh();		
	}
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version;
	}
	
	private void load() {
		Vector<SearchResult> styles = new Vector<SearchResult>(relativeProfile.getNumSourceStyles());
		for (DegreeValue degree : relativeProfile.getSourceStyleDegrees()) {
			StyleRecord style = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(degree.getName()));
			if (style != null)
				styles.add(new SearchResult(style, degree.getPercentage()));
		}
		loadData(styles, null);		
	}
	
}
