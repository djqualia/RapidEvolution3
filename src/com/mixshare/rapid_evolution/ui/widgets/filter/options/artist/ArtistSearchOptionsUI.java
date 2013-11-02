package com.mixshare.rapid_evolution.ui.widgets.filter.options.artist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
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

public class ArtistSearchOptionsUI extends AbstractSearchOptionsUI {

	static private Logger log = Logger.getLogger(ArtistSearchOptionsUI.class);
    
	static public ArtistSearchOptionsUI instance = null;
    
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
    
    public ArtistSearchOptionsUI() {
    	instance = this;
    	
    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	setSizePolicy(searchWidgetSizePolicy);    	  

    	QGridLayout searchLayout = new QGridLayout(this);
    	//searchLayout.setMargin(5);    	
    	
    	QLabel artistLabel = new QLabel();
    	artistLabel.setText(Translations.get("artist_filter_artist"));    	
    	artistField = new QLineEdit();
    	artistField.setFixedWidth(90);
    	artistField.returnPressed.connect(this, "updateFilter()");    	
    	searchLayout.addWidget(artistLabel, 0, 0, 1, 1);
    	searchLayout.addWidget(artistField, 0, 1, 1, 2);    	
    	
    	QLabel labelLabel = new QLabel();
    	labelLabel.setText(Translations.get("artist_filter_label"));    	
    	labelField = new QLineEdit();
    	labelField.setFixedWidth(90);
    	labelField.returnPressed.connect(this, "updateFilter()");    	
    	searchLayout.addWidget(labelLabel, 1, 0, 1, 1);
    	searchLayout.addWidget(labelField, 1, 1, 1, 2);    	
    	    	
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
    	minBeatIntensityLabel.setText(Translations.get("artist_filter_min_beat_intensity"));    	
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
    	maxBeatIntensityLabel.setText(Translations.get("artist_filter_max_beat_intensity"));    	
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
    	minRating.setText(Translations.get("artist_filter_min_rating"));    	
    	QLabel minRatingSuffix = new QLabel();
    	minRatingSuffix.setText(Translations.get("artist_filter_min_rating_suffix"));    	
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
    	includeUnrated.setText(Translations.get("artist_filter_include_unrated"));
    	includeUnrated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(includeUnrated, 8, 0, 1, 2);
    	
    	excludeRated = new QCheckBox();
    	excludeRated.setText(Translations.get("artist_filter_exclude_rated"));
    	excludeRated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(excludeRated, 9, 0, 1, 2);
    	
    	showDisabled = new QCheckBox();
    	showDisabled.setText(Translations.get("artist_filter_show_disabled"));
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
    	artistField.setText("");
    	labelField.setText("");
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
    	ArtistSearchParameters artistParameters = SearchWidgetUI.instance.getArtistSearchParameters();
    	int numFilters = update(artistParameters);
    	artistParameters = SearchWidgetUI.instance.getRecommendedArtistSearchParameters();
    	update(artistParameters);
    	if (updateTable) {
    		SearchWidgetUI.instance.updateFilter();
    		CentralWidgetUI.instance.setNumArtistFilters(numFilters);
    	}
    }
    public int update(SearchParameters searchParameters) {
    	ArtistSearchParameters artistParameters = (ArtistSearchParameters)searchParameters;
    	int numFilters = 0;
    	if (artistField.text().length() > 0) {
    		artistParameters.setArtistSearchText(artistField.text());
    		++numFilters;
    	} else 
    		artistParameters.setArtistSearchText("");    	
    	if (labelField.text().length() > 0) {
    		artistParameters.setLabelSearchText(labelField.text());
    		++numFilters;
    	} else 
    		artistParameters.setLabelSearchText("");
    	if (stylesField.text().length() > 0) {
    		artistParameters.setStyleSearchText(stylesField.text());
    		++numFilters;
    	} else 
    		artistParameters.setStyleSearchText("");    	
    	if (tagsField.text().length() > 0) {
    		artistParameters.setTagSearchText(tagsField.text());
    		++numFilters;
    	} else 
    		artistParameters.setTagSearchText("");    	
    	
    	if (minBeatIntensityChoices.currentIndex() != 0) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (minBeatIntensityChoices.currentIndex() == 4)
    			artistParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 3)
    			artistParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 2)
    			artistParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 1)
    			artistParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		artistParameters.setMinBeatIntensity(null);
    	}
    	if (maxBeatIntensityChoices.currentIndex() != 4) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (maxBeatIntensityChoices.currentIndex() == 3)
    			artistParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 2)
    			artistParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 1)
    			artistParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 0)
    			artistParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		artistParameters.setMaxBeatIntensity(null);
    	}    	
    	if (!minRatingField.text().equals("")) {
    		try {
    			artistParameters.setMinRating(Rating.getRating(Integer.parseInt(minRatingField.text())));
    			++numFilters;
    		} catch (Exception e) {
    			artistParameters.setMinRating(null);
    		}
    	} else {
    		artistParameters.setMinRating(null);
    	}
    	if (!maxDaysSinceAdded.text().equals("")) {
    		try {
    			artistParameters.setMaxDaysSinceLastAdded(Short.parseShort(maxDaysSinceAdded.text()));
    			++numFilters;
    		} catch (Exception e) {
    			artistParameters.setMaxDaysSinceLastAdded((short)0);
    		}
    	} else {
    		artistParameters.setMaxDaysSinceLastAdded((short)0);
    	}
    	if (includeUnrated.isChecked()) {
    		artistParameters.setIncludeUnrated(true);
    		++numFilters;
    	} else {
    		artistParameters.setIncludeUnrated(false);
    	}
    	if (excludeRated.isChecked()) {
    		artistParameters.setExcludeRated(true);
    		++numFilters;
    	} else {
    		artistParameters.setExcludeRated(false);
    	}
    	if (showDisabled.isChecked()) {
    		artistParameters.setShowDisabled(true);
    		++numFilters;
    	} else {
    		artistParameters.setShowDisabled(false);
    	}
    	currentCount = numFilters;
    	return numFilters;
    }
	
}
