package com.mixshare.rapid_evolution.ui.dialogs.options;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.tree.TreeItemModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QStandardItem;

public class OptionsModelManager extends TreeModelManager {

	static private Logger log = Logger.getLogger(OptionsModelManager.class);
    static private final long serialVersionUID = 0L;    	
    
    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_SETTING_NAME.getInstance(true),
		COLUMN_SETTING_VALUE.getInstance(true),
		COLUMN_SETTING_ACTION.getInstance(true),
		COLUMN_SETTING_DESCRIPTION.getInstance(true),
		COLUMN_SETTING_ID.getInstance(false),
		COLUMN_SETTING_TYPE.getInstance(false)
    };
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public OptionsModelManager() {
		setPrimarySortColumn(COLUMN_SETTING_NAME.getColumnId());
		initColumns();
	}
	public OptionsModelManager(LineReader lineReader) {
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
	
	public int getIdColumn() {
		int i = 0;
		for (Column column : sourceColumns) {
			if (column.getColumnId() == COLUMN_SETTING_ID.getColumnId())
				return i;
			++i;			
		}
		return -1;
	}
	public int getTypeColumn() {
		int i = 0;
		for (Column column : sourceColumns) {
			if (column.getColumnId() == COLUMN_SETTING_TYPE.getColumnId())
				return i;
			++i;			
		}
		return -1;
	}
	public int getActionColumn() {
		int i = 0;
		for (Column column : sourceColumns) {
			if (column.getColumnId() == COLUMN_SETTING_ACTION.getColumnId())
				return i;
			++i;			
		}
		return -1;
	}
	public int getNameColumn() {
		int i = 0;
		for (Column column : sourceColumns) {
			if (column.getColumnId() == COLUMN_SETTING_NAME.getColumnId())
				return i;
			++i;			
		}
		return -1;
	}
	public int getValueColumn() {
		int i = 0;
		for (Column column : sourceColumns) {
			if (column.getColumnId() == COLUMN_SETTING_VALUE.getColumnId())
				return i;
			++i;			
		}
		return -1;
	}
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	public String getTypeDescription() { return Translations.get("settings_model_type_text"); }
	
	public OptionsProxyModel getOptionsProxyModel() { return (OptionsProxyModel)getProxyModel(); }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);
		SongRecord songRecord = (SongRecord)record;
		if (columnId == COLUMN_SETTING_NAME.getColumnId())
			return songRecord.toString();
		if (columnId == COLUMN_SETTING_VALUE.getColumnId())
			return new SmartString(songRecord.getArtistsDescription());
		if (columnId == COLUMN_SETTING_ACTION.getColumnId())
			return new SmartString(songRecord.getReleases());
		if (columnId == COLUMN_SETTING_DESCRIPTION.getColumnId())
			return new SmartString(songRecord.getReleaseTitle());		
		if (columnId == COLUMN_SETTING_ID.getColumnId())
			return new SmartString(songRecord.getTitle());
		if (columnId == COLUMN_SETTING_TYPE.getColumnId())
			return new SmartString(songRecord.getRemix());

		return null;
	}	
		
	/////////////
	// METHODS //
	/////////////
	
	protected void createSourceModel(QObject parent) {
		model = new TreeItemModel(0, getNumColumns(), parent, this);		
		loadTree();
	}
	
	protected void loadTree() {
		try {
			initViewColumns();
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = null;
	        try {
	            document = builder.parse(new File("settings.xml").toURI().toString());
	        } catch (java.io.FileNotFoundException fnfe) {
	        	if (log.isDebugEnabled())
	        		log.debug("execute(): settings.xml not found");
	            return;
	        }
	        Element elem = document.getDocumentElement();
	        float version = Float.parseFloat(elem.getAttribute("version"));
	        NodeList rootnodes = elem.getChildNodes();
	        parseXML(rootnodes, null);
		} catch (Exception e) {	
			log.error("loadTree(): error", e);
		}
	}	
	
    private void parseXML(NodeList nodeList, QStandardItem parent) {
        for (int nodeIter = 0; nodeIter < nodeList.getLength(); ++nodeIter) {
            Node node = nodeList.item(nodeIter);
            if (node.getNodeName().equalsIgnoreCase("category")) {
            	String id = node.getAttributes().getNamedItem("id").getTextContent();
            	String key = "settings_" + id + "_description";
            	
	        	QStandardItem settingColumn = new QStandardItem();
	        	settingColumn.setText(Translations.get("settings_" + id + "_title"));
	        	        	
	        	QStandardItem valueColumn = new QStandardItem();
	        	
	        	QStandardItem actionColumn = new QStandardItem();
	        	
	        	QStandardItem descriptionColumn = new QStandardItem();
	        	descriptionColumn.setText(Translations.get("settings_" + id + "_description"));

	        	QStandardItem idColumn = new QStandardItem();
	        	idColumn.setText(id);

	        	QStandardItem typeColumn = new QStandardItem();
	        	typeColumn.setText("");
	        	
	        	ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(6);
	        	newRow.add(settingColumn);
	        	newRow.add(valueColumn);
	        	newRow.add(actionColumn);
	        	newRow.add(descriptionColumn);
	        	newRow.add(idColumn);
	        	newRow.add(typeColumn);
	        	
	        	if (parent == null)
	        		((TreeItemModel)model).appendRow(newRow);
	        	else
	        		parent.appendRow(newRow);
	        	
	        	parseXML(node.getChildNodes(), settingColumn);
	        			        	
            } else if (node.getNodeName().equalsIgnoreCase("setting")) {
            	String id = node.getAttributes().getNamedItem("id").getTextContent();
            	String type = node.getAttributes().getNamedItem("type").getTextContent();

	        	QStandardItem settingColumn = new QStandardItem();
	        	settingColumn.setText(Translations.get("settings_" + id + "_title"));
	        	        	
	        	QStandardItem valueColumn = new QStandardItem();
	        	if (Translations.has(id + "_" + RE3Properties.getProperty(id)))
	        		valueColumn.setText(Translations.get(id + "_" + RE3Properties.getProperty(id)));
	        	else
	        		valueColumn.setText(RE3Properties.getProperty(id));
	        	
	        	QStandardItem actionColumn = new QStandardItem();
	        	
	        	QStandardItem descriptionColumn = new QStandardItem();
	        	descriptionColumn.setText(Translations.get("settings_" + id + "_description"));

	        	QStandardItem idColumn = new QStandardItem();
	        	idColumn.setText(id);

	        	QStandardItem typeColumn = new QStandardItem();
	        	typeColumn.setText(type);
	        	
	        	ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(6);
	        	newRow.add(settingColumn);
	        	newRow.add(valueColumn);
	        	newRow.add(actionColumn);
	        	newRow.add(descriptionColumn);
	        	newRow.add(idColumn);
	        	newRow.add(typeColumn);

	        	if (parent == null)
	        		((TreeItemModel)model).appendRow(newRow);
	        	else
	        		parent.appendRow(newRow);	        	
            }
        }    	
    }	
	
	/**
	 * Don't call directly (in a Java thread), call remove(...)
	 */		
	public void resetModel() {
		((TreeItemModel)model).clear();
		loadTree();
	}		
	
	public void refresh() {
		resetModel();
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version;
	}
	
}
