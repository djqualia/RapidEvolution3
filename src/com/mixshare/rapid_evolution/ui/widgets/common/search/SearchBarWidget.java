package com.mixshare.rapid_evolution.ui.widgets.common.search;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QWidget;

public class SearchBarWidget extends QWidget {

	////////////
	// FIELDS //
	////////////
	
	private QPushButton searchButton;
    private QLineEdit filterText;
    private QPushButton clearTextButton;
	private QComboBox filterCombo;
    private QPushButton configureColumnsButton;
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public SearchBarWidget(boolean showCombo, int marginSize) {
    	filterText = new QLineEdit();
    	clearTextButton = new QPushButton();
    	clearTextButton.setIcon(new QIcon(RE3Properties.getProperty("search_text_clear_icon")));
    	clearTextButton.setVisible(false);
    	clearTextButton.clicked.connect(this, "clearTextClicked(Boolean)");
    	filterCombo = new QComboBox();
    	if (!showCombo)
    		filterCombo.setVisible(false);
    	configureColumnsButton = new QPushButton(); 
        configureColumnsButton.setToolTip(Translations.get("column_selection_tooltip"));
        QIcon configureColumnsIcon = new QIcon(RE3Properties.getProperty("configure_columns_icon"));
        configureColumnsButton.setIcon(configureColumnsIcon);    
        QIcon searchIcon = new QIcon(RE3Properties.getProperty("search_icon"));
        searchButton = new QPushButton();        
        searchButton.setMaximumWidth(RE3Properties.getInt("search_bar_icon_max_width"));
        searchButton.setEnabled(false);
        searchButton.setFlat(true);              
        searchButton.setIcon(searchIcon);        
        
        QHBoxLayout textSectionLayout = new QHBoxLayout();
        textSectionLayout.setSpacing(0);
        textSectionLayout.setMargin(0);
        textSectionLayout.addWidget(filterText);
        textSectionLayout.addWidget(clearTextButton);        
        
        QHBoxLayout searchFilterInputLayout = new QHBoxLayout();
        searchFilterInputLayout.setSpacing(4);
        searchFilterInputLayout.setMargin(marginSize);
        setLayout(searchFilterInputLayout);
        
        filterText.textChanged.connect(this, "searchTextChanged(String)");
        
        searchFilterInputLayout.addWidget(searchButton);
        searchFilterInputLayout.addLayout(textSectionLayout);
        searchFilterInputLayout.addWidget(filterCombo);
        searchFilterInputLayout.addWidget(configureColumnsButton);        
    }
    
    /////////////
    // GETTERS //
    /////////////

    public QLineEdit getFilterText() {
		return filterText;
	}

	public QComboBox getFilterCombo() {
		return filterCombo;
	}

	public QPushButton getConfigureColumnsButton() {
		return configureColumnsButton;
	}

	/////////////
	// SETTERS //
	/////////////
    
	public void setFilterText(QLineEdit filterText) {
		this.filterText = filterText;
	}

	public void setFilterCombo(QComboBox filterCombo) {
		this.filterCombo = filterCombo;
	}

	public void setConfigureColumnsButton(QPushButton configureColumnsButton) {
		this.configureColumnsButton = configureColumnsButton;
	}
	
	////////////
	// EVENTS //
	////////////
	
	protected void searchTextChanged(String value) {
		if (value.length() > 0) {
			clearTextButton.setVisible(true);
		} else {
			clearTextButton.setVisible(false);
		}
	}
	
	protected void clearTextClicked(Boolean checked) {
		filterText.setText("");
	}
	
}
