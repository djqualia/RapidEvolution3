package com.mixshare.rapid_evolution.ui.widgets.profile.details.search;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.ui.dialogs.fields.TextFieldInputDialog;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;

abstract public class SearchDetailsTableView extends DetailsTableView {

	////////////
	// FIELDS //
	////////////
	
	protected QAction createBooleanField;
    protected QAction createTextField;

    /////////////////
    // CONSTRUCTOR //
    /////////////////
	
	public SearchDetailsTableView(CommonDetailsModelManager modelManager) {
		super(modelManager);
		
        createBooleanField = new QAction("Add Flag", this);
        createBooleanField.triggered.connect(this, "addFlag()");		
        createBooleanField.setIcon(new QIcon(RE3Properties.getProperty("menu_create_flag_icon")));
        
        createTextField = new QAction("Add Text Field", this);
        createTextField.triggered.connect(this, "addTextField()");		
        createTextField.setIcon(new QIcon(RE3Properties.getProperty("menu_create_field_icon")));
        
        addAction(createBooleanField);
        addAction(createTextField);		
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public SearchIndex getSearchIndex() { return (SearchIndex)getIndex(); }
	
	////////////
	// EVENTS //
	////////////
	
	protected void addFlag() { 
    	TextFieldInputDialog textFieldInputDialog = new TextFieldInputDialog(Translations.get("details_add_flag_window_title"), Translations.get("details_add_flag_input_text"), "", null);
    	if (textFieldInputDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		String flagName = textFieldInputDialog.getFilterName();
    		if ((flagName != null) && (flagName.length() > 0) && (getSearchIndex().getUserDataType(flagName) == null)) {
    			getSearchIndex().addUserDataType(flagName, UserDataType.TYPE_BOOLEAN_FLAG);
    			setupPersistentEditors();
    		}
    	}		
	}

	protected void addTextField() {
    	TextFieldInputDialog textFieldInputDialog = new TextFieldInputDialog(Translations.get("details_add_text_field_window_title"), Translations.get("details_add_text_field_input_text"), "", null);
    	if (textFieldInputDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		String fieldName = textFieldInputDialog.getFilterName();
    		if ((fieldName != null) && (fieldName.length() > 0) && (getSearchIndex().getUserDataType(fieldName) == null)) {
    			getSearchIndex().addUserDataType(fieldName, UserDataType.TYPE_TEXT_FIELD);
    			setupPersistentEditors();
    		}
    	}				
	}
	
}
