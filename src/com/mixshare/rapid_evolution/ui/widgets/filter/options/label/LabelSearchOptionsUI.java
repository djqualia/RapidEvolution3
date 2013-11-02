package com.mixshare.rapid_evolution.ui.widgets.filter.options.label;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.AbstractSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSpacerItem;

public class LabelSearchOptionsUI extends AbstractSearchOptionsUI {

	static private Logger log = Logger.getLogger(LabelSearchOptionsUI.class);
    
	static public LabelSearchOptionsUI instance = null;
    
	////////////
	// FIELDS //
	////////////

	private QLineEdit artistField;
	private QLineEdit labelField;
	private QLineEdit stylesField;
	private QLineEdit tagsField;	
	private QComboBox minBeatIntensityChoices;
	private QComboBox maxBeatIntensityChoices;
	private QLineEdit minRatingField;
	private QLineEdit maxDaysSinceAdded;	
	private QCheckBox includeUnrated;
	private QCheckBox excludeRated;
	private QCheckBox showDisabled;
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public LabelSearchOptionsUI() {
    	instance = this;
    	
    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	setSizePolicy(searchWidgetSizePolicy);    	  

    	QGridLayout searchLayout = new QGridLayout(this);
    	//searchLayout.setMargin(5);    	
    	
    	QLabel labelLabel = new QLabel();
    	labelLabel.setText(Translations.get("label_filter_label"));    	
    	labelField = new QLineEdit();
    	labelField.setFixedWidth(90);
    	labelField.returnPressed.connect(this, "updateFilter()");    	
    	searchLayout.addWidget(labelLabel, 0, 0, 1, 1);
    	searchLayout.addWidget(labelField, 0, 1, 1, 2);    	
    	    	
    	QLabel artistLabel = new QLabel();
    	artistLabel.setText(Translations.get("label_filter_artist"));    	
    	artistField = new QLineEdit();
    	artistField.setFixedWidth(90);
    	artistField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(artistLabel, 1, 0, 1, 1);
    	searchLayout.addWidget(artistField, 1, 1, 1, 2);    	
    	    	
    	QLabel stylesLabel = new QLabel();
    	stylesLabel.setText(Translations.get("search_filter_styles"));    	
    	stylesField = new QLineEdit();
    	stylesField.setFixedWidth(90);
    	stylesField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(stylesLabel, 2, 0, 1, 1);
    	searchLayout.addWidget(stylesField, 2, 1, 1, 2);    	

    	QLabel tagsLabel = new QLabel();
    	tagsLabel.setText(Translations.get("search_filter_tags"));    	
    	tagsField = new QLineEdit();
    	tagsField.setFixedWidth(90);
    	tagsField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(tagsLabel, 3, 0, 1, 1);
    	searchLayout.addWidget(tagsField, 3, 1, 1, 2);   
    	
    	QLabel minBeatIntensityLabel = new QLabel();
    	minBeatIntensityLabel.setText(Translations.get("label_filter_min_beat_intensity"));    	
    	minBeatIntensityChoices = new QComboBox();
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_low"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_low"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_medium"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_high"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_high"));
    	minBeatIntensityChoices.setCurrentIndex(0);
    	minBeatIntensityChoices.currentIndexChanged.connect(this, "beatIntensityChanged(Integer)");
    	minBeatIntensityChoices.setFixedWidth(80);
    	searchLayout.addWidget(minBeatIntensityLabel, 4, 0, 1, 1);
    	searchLayout.addWidget(minBeatIntensityChoices, 4, 1, 1, 2);    	

    	QLabel maxBeatIntensityLabel = new QLabel();
    	maxBeatIntensityLabel.setText(Translations.get("label_filter_max_beat_intensity"));    	
    	maxBeatIntensityChoices = new QComboBox();
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_low"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_low"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_medium"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_high"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_high"));
    	maxBeatIntensityChoices.setCurrentIndex(4);
    	maxBeatIntensityChoices.currentIndexChanged.connect(this, "beatIntensityChanged(Integer)");
    	maxBeatIntensityChoices.setFixedWidth(80);
    	searchLayout.addWidget(maxBeatIntensityLabel, 5, 0, 1, 1);
    	searchLayout.addWidget(maxBeatIntensityChoices, 5, 1, 1, 2);    	
    	
    	QLabel minRating = new QLabel();
    	minRating.setText(Translations.get("label_filter_min_rating"));    	
    	QLabel minRatingSuffix = new QLabel();
    	minRatingSuffix.setText(Translations.get("label_filter_min_rating_suffix"));    	
    	minRatingField = new QLineEdit();
    	minRatingField.setFixedWidth(50);
    	minRatingField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(minRating, 6, 0, 1, 1);
    	searchLayout.addWidget(minRatingField, 6, 1, 1, 1);    	
    	searchLayout.addWidget(minRatingSuffix, 6, 2, 1, 1);    	

    	QLabel maxDaysSinceAddedLabel = new QLabel();
    	maxDaysSinceAddedLabel.setText(Translations.get("song_filter_max_days_since_added"));    	
    	QLabel maxDaysSinceAddedSuffix = new QLabel();
    	maxDaysSinceAddedSuffix.setText(Translations.get("song_filter_max_days_since_added_suffix"));    	
    	maxDaysSinceAdded = new QLineEdit();
    	maxDaysSinceAdded.setFixedWidth(50);
    	maxDaysSinceAdded.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(maxDaysSinceAddedLabel, 7, 0, 1, 1);
    	searchLayout.addWidget(maxDaysSinceAdded, 7, 1, 1, 1);
    	searchLayout.addWidget(maxDaysSinceAddedSuffix, 7, 2, 1, 1);
    	
    	includeUnrated = new QCheckBox();
    	includeUnrated.setText(Translations.get("label_filter_include_unrated"));
    	includeUnrated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(includeUnrated, 8, 0, 1, 2);
    	
    	excludeRated = new QCheckBox();
    	excludeRated.setText(Translations.get("label_filter_exclude_rated"));
    	excludeRated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(excludeRated, 9, 0, 1, 2);
    
    	showDisabled = new QCheckBox();
    	showDisabled.setText(Translations.get("label_filter_show_disabled"));
    	showDisabled.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(showDisabled, 10, 0, 1, 2);
    	
    	QPushButton updateButton = new QPushButton();
    	updateButton.setText(Translations.get("filter_search_options_update"));
    	updateButton.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(updateButton, 11, 0, 1, 3);

    	for (int i = 0; i < 12; ++i)
    		searchLayout.setRowStretch(i, 0);    	
    	searchLayout.addItem(new QSpacerItem(0, 0, QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Expanding));
    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    
    /////////////
    // METHODS //
    /////////////
    
    public void clearFilters() {
    	labelField.setText("");
    	artistField.setText("");
    	minRatingField.setText("");
    	maxDaysSinceAdded.setText("");
    	minBeatIntensityChoices.setCurrentIndex(0);
    	maxBeatIntensityChoices.setCurrentIndex(4);     	
    	includeUnrated.setChecked(false);
    	excludeRated.setChecked(false);
    	showDisabled.setChecked(false);
    	stylesField.setText("");
    	tagsField.setText("");
    	update(null, false);
    }
    
    protected void beatIntensityChanged(Integer value) { updateFilter(); }
    protected void updateFilter() { update(false, true); }        
    protected void update(Boolean checked) { update(checked, true); }
    protected void update(Boolean checked, boolean updateTable) {    	
    	LabelSearchParameters labelParameters = SearchWidgetUI.instance.getLabelSearchParameters();
    	int numFilters = update(labelParameters);
    	labelParameters = SearchWidgetUI.instance.getRecommendedLabelSearchParameters();
    	update(labelParameters);
    	if (updateTable) {
    		SearchWidgetUI.instance.updateFilter();
    		CentralWidgetUI.instance.setNumLabelFilters(numFilters);
    	}    	
    }
    public int update(SearchParameters searchParameters) {
    	LabelSearchParameters labelParameters = (LabelSearchParameters)searchParameters;
    	int numFilters = 0;
    	if (labelField.text().length() > 0) {
    		labelParameters.setLabelSearchText(labelField.text());
    		++numFilters;
    	} else 
    		labelParameters.setLabelSearchText(""); 	    	
    	if (artistField.text().length() > 0) {
    		labelParameters.setArtistSearchText(artistField.text());
    		++numFilters;
    	} else 
    		labelParameters.setArtistSearchText("");
    	if (stylesField.text().length() > 0) {
    		labelParameters.setStyleSearchText(stylesField.text());
    		++numFilters;
    	} else 
    		labelParameters.setStyleSearchText("");    	
    	if (tagsField.text().length() > 0) {
    		labelParameters.setTagSearchText(tagsField.text());
    		++numFilters;
    	} else 
    		labelParameters.setTagSearchText("");    	
    	
    	if (minBeatIntensityChoices.currentIndex() != 0) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (minBeatIntensityChoices.currentIndex() == 4)
    			labelParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 3)
    			labelParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 2)
    			labelParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 1)
    			labelParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		labelParameters.setMinBeatIntensity(null);
    	}
    	if (maxBeatIntensityChoices.currentIndex() != 4) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (maxBeatIntensityChoices.currentIndex() == 3)
    			labelParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 2)
    			labelParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 1)
    			labelParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 0)
    			labelParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		labelParameters.setMaxBeatIntensity(null);
    	}    	
    	if (!minRatingField.text().equals("")) {
    		try {
    			labelParameters.setMinRating(Rating.getRating(Integer.parseInt(minRatingField.text())));
    			++numFilters;
    		} catch (Exception e) {
    			labelParameters.setMinRating(null);
    		}
    	} else {
    		labelParameters.setMinRating(null);
    	}
    	if (!maxDaysSinceAdded.text().equals("")) {
    		try {
    			labelParameters.setMaxDaysSinceLastAdded(Short.parseShort(maxDaysSinceAdded.text()));
    			++numFilters;
    		} catch (Exception e) {
    			labelParameters.setMaxDaysSinceLastAdded((short)0);
    		}
    	} else {
    		labelParameters.setMaxDaysSinceLastAdded((short)0);
    	}    	
    	if (includeUnrated.isChecked()) {
    		labelParameters.setIncludeUnrated(true);
    		++numFilters;
    	} else {
    		labelParameters.setIncludeUnrated(false);
    	}
    	if (excludeRated.isChecked()) {
    		labelParameters.setExcludeRated(true);
    		++numFilters;
    	} else {
    		labelParameters.setExcludeRated(false);
    	}
    	if (showDisabled.isChecked()) {
    		labelParameters.setShowDisabled(true);
    		++numFilters;
    	} else {
    		labelParameters.setShowDisabled(false);
    	}    	
    	currentCount = numFilters;
    	return numFilters;
    }
	
}
