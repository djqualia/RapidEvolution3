package com.mixshare.rapid_evolution.ui.dialogs.fields;

import com.trolltech.qt.gui.QCompleter;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QWidget;

public class TextFieldInputDialog extends QDialog {

	////////////
	// FIELDS //
	////////////
	
    private Ui_TextFieldInputDialog ui = new Ui_TextFieldInputDialog();
    private String windowTitle;
    private String inputString;
    private String initialValue;
    private QCompleter completer;
    
    //////////////////
    // CONSTRUCTORS //    
    //////////////////
    
    public TextFieldInputDialog(String windowTitle, String inputString, String initialValue, QCompleter completer) {
    	this.windowTitle = windowTitle;
    	this.inputString = inputString;
    	this.initialValue = initialValue;
    	this.completer = completer;
        init();
    }

    public TextFieldInputDialog(QWidget parent, String windowTitle, String inputString, String initialValue, QCompleter completer) {
        super(parent);
    	this.windowTitle = windowTitle;
        this.inputString = inputString;
        this.initialValue = initialValue;
    	this.completer = completer;
        init();        
    }
    
    private void init() {
    	ui.setupUi(this);
    	ui.label.setText(inputString + ":");
    	ui.fieldValue.setText(initialValue);
    	setWindowTitle(windowTitle);
    	if (completer != null)
    		ui.fieldValue.setCompleter(completer);
    	setFixedSize(size()); // disallows resizing (couldn't figure out how to do that from the GUI editor)
    	ui.fieldValue.setFocus();    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getFilterName() {
    	return ui.fieldValue.text();
    }
        
}
