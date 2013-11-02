package com.mixshare.rapid_evolution.ui.dialogs.filter;

import com.mixshare.rapid_evolution.ui.util.Translations;
import com.trolltech.qt.gui.QCompleter;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QWidget;

public class AddFilterDialog extends QDialog {

	////////////
	// FIELDS //
	////////////
	
    private Ui_AddFilterDialog ui = new Ui_AddFilterDialog();
    private String typeDescription;
    private QCompleter completer;

    //////////////////
    // CONSTRUCTORS //    
    //////////////////
    
    public AddFilterDialog(String typeDescription) {
    	this.typeDescription = typeDescription;
        init();
    }
    public AddFilterDialog(String typeDescription, QCompleter completer) {
    	this.typeDescription = typeDescription;
    	this.completer = completer;
        init();
    }

    public AddFilterDialog(QWidget parent, String typeDescription) {
        super(parent);
        this.typeDescription = typeDescription;
        init();        
    }
    public AddFilterDialog(QWidget parent, String typeDescription, QCompleter completer) {
        super(parent);
        this.typeDescription = typeDescription;
        this.completer = completer;
        init();        
    }
    
    private void init() {
    	ui.setupUi(this);
    	ui.label.setText(typeDescription + " " + Translations.get("add_filter_text_suffix"));
    	setWindowTitle(Translations.get("add_filter_window_title_prefix") + " " + typeDescription);
    	setFixedSize(size()); // disallows resizing (couldn't figure out how to do that from the GUI editor)
    	ui.filterName.setFocus();    	
    	if (completer != null)
    		ui.filterName.setCompleter(completer);    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getFilterName() {
    	return ui.filterName.text();
    }
    
    /////////////
    // SETTERS //
    /////////////
    
    public void setTitle(String title) { setWindowTitle(title); }
    
    public void setLabel(String label) { ui.label.setText(label); }
    
    public void setFilterName(String name) {
    	ui.filterName.setText(name);
    }
    
}
