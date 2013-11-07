package com.mixshare.rapid_evolution.ui.widgets.filter.options.release;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
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

public class ReleaseSearchOptionsUI extends AbstractSearchOptionsUI {

	static private Logger log = Logger.getLogger(ReleaseSearchOptionsUI.class);
    
	static public ReleaseSearchOptionsUI instance = null;
    
	////////////
	// FIELDS //
	////////////

	private QLineEdit artistField;
	private QLineEdit titleField;
	private QLineEdit labelField;
	private QLineEdit stylesField;
	private QLineEdit tagsField;	
	private QComboBox minBeatIntensityChoices;
	private QComboBox maxBeatIntensityChoices;
	private QLineEdit minReleasedField;
	private QLineEdit maxReleasedField;
	private QLineEdit minRatingField;
	private QLineEdit maxDaysSinceAdded;
	private QCheckBox includeUnrated;
	private QCheckBox excludeRated;
	private QCheckBox showDisabled;
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ReleaseSearchOptionsUI() {
    	instance = this;
    	
    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	setSizePolicy(searchWidgetSizePolicy);    	  

    	QGridLayout searchLayout = new QGridLayout(this);
    	//searchLayout.setMargin(5);    	

    	QLabel artistLabel = new QLabel();
    	artistLabel.setText(Translations.get("release_filter_artist"));    	
    	artistField = new QLineEdit();
    	artistField.setFixedWidth(90);
    	artistField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(artistLabel, 0, 0, 1, 1);
    	searchLayout.addWidget(artistField, 0, 1, 1, 2);

    	QLabel titleLabel = new QLabel();
    	titleLabel.setText(Translations.get("release_filter_title"));    	
    	titleField = new QLineEdit();
    	titleField.setFixedWidth(90);
    	titleField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(titleLabel, 1, 0, 1, 1);
    	searchLayout.addWidget(titleField, 1, 1, 1, 2);
    	
    	QLabel labelLabel = new QLabel();
    	labelLabel.setText(Translations.get("release_filter_label"));    	
    	labelField = new QLineEdit();
    	labelField.setFixedWidth(90);
    	labelField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(labelLabel, 2, 0, 1, 1);
    	searchLayout.addWidget(labelField, 2, 1, 1, 2);
    	
    	QLabel stylesLabel = new QLabel();
    	stylesLabel.setText(Translations.get("search_filter_styles"));    	
    	stylesField = new QLineEdit();
    	stylesField.setFixedWidth(90);
    	stylesField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(stylesLabel, 3, 0, 1, 1);
    	searchLayout.addWidget(stylesField, 3, 1, 1, 2);    	

    	QLabel tagsLabel = new QLabel();
    	tagsLabel.setText(Translations.get("search_filter_tags"));    	
    	tagsField = new QLineEdit();
    	tagsField.setFixedWidth(90);
    	tagsField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(tagsLabel, 4, 0, 1, 1);
    	searchLayout.addWidget(tagsField, 4, 1, 1, 2);       	
    	
    	QLabel minBeatIntensityLabel = new QLabel();
    	minBeatIntensityLabel.setText(Translations.get("release_filter_min_beat_intensity"));    	
    	minBeatIntensityChoices = new QComboBox();
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_low"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_low"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_medium"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_high"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_high"));
    	minBeatIntensityChoices.setCurrentIndex(0);
    	minBeatIntensityChoices.currentIndexChanged.connect(this, "beatIntensityChanged(Integer)");
    	minBeatIntensityChoices.setFixedWidth(80);
    	searchLayout.addWidget(minBeatIntensityLabel, 5, 0, 1, 1);
    	searchLayout.addWidget(minBeatIntensityChoices, 5, 1, 1, 2);

    	QLabel maxBeatIntensityLabel = new QLabel();
    	maxBeatIntensityLabel.setText(Translations.get("release_filter_max_beat_intensity"));    	
    	maxBeatIntensityChoices = new QComboBox();
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_low"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_low"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_medium"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_high"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_high"));
    	maxBeatIntensityChoices.setCurrentIndex(4);
    	maxBeatIntensityChoices.currentIndexChanged.connect(this, "beatIntensityChanged(Integer)");
    	maxBeatIntensityChoices.setFixedWidth(80);
    	searchLayout.addWidget(maxBeatIntensityLabel, 6, 0, 1, 1);
    	searchLayout.addWidget(maxBeatIntensityChoices, 6, 1, 1, 2);
    	
    	QLabel minReleasedLabel = new QLabel();
    	minReleasedLabel.setText(Translations.get("release_filter_min_year_released"));    	
    	minReleasedField = new QLineEdit();
    	minReleasedField.setFixedWidth(50);
    	minReleasedField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(minReleasedLabel, 7, 0, 1, 1);
    	searchLayout.addWidget(minReleasedField, 7, 1, 1, 2);

    	QLabel maxReleasedLabel = new QLabel();
    	maxReleasedLabel.setText(Translations.get("release_filter_max_year_released"));
    	maxReleasedField = new QLineEdit();
    	maxReleasedField.setFixedWidth(50);
    	maxReleasedField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(maxReleasedLabel, 8, 0, 1, 1);
    	searchLayout.addWidget(maxReleasedField, 8, 1, 1, 2);
    	
    	QLabel minRating = new QLabel();
    	minRating.setText(Translations.get("release_filter_min_rating"));    	
    	QLabel minRatingSuffix = new QLabel();
    	minRatingSuffix.setText(Translations.get("release_filter_min_rating_suffix"));    	
    	minRatingField = new QLineEdit();
    	minRatingField.setFixedWidth(50);
    	minRatingField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(minRating, 9, 0, 1, 1);
    	searchLayout.addWidget(minRatingField, 9, 1, 1, 1);
    	searchLayout.addWidget(minRatingSuffix, 9, 2, 1, 1);    	
    	
    	QLabel maxDaysSinceAddedLabel = new QLabel();
    	maxDaysSinceAddedLabel.setText(Translations.get("song_filter_max_days_since_added"));    	
    	QLabel maxDaysSinceAddedSuffix = new QLabel();
    	maxDaysSinceAddedSuffix.setText(Translations.get("song_filter_max_days_since_added_suffix"));    	
    	maxDaysSinceAdded = new QLineEdit();
    	maxDaysSinceAdded.setFixedWidth(50);
    	maxDaysSinceAdded.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(maxDaysSinceAddedLabel, 10, 0, 1, 1);
    	searchLayout.addWidget(maxDaysSinceAdded, 10, 1, 1, 1);
    	searchLayout.addWidget(maxDaysSinceAddedSuffix, 10, 2, 1, 1);
    	
    	includeUnrated = new QCheckBox();
    	includeUnrated.setText(Translations.get("release_filter_include_unrated"));
    	includeUnrated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(includeUnrated, 11, 0, 1, 2);
    	
    	excludeRated = new QCheckBox();
    	excludeRated.setText(Translations.get("release_filter_exclude_rated"));
    	excludeRated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(excludeRated, 12, 0, 1, 2);
    	
    	showDisabled = new QCheckBox();
    	showDisabled.setText(Translations.get("release_filter_show_disabled"));
    	showDisabled.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(showDisabled, 13, 0, 1, 2);    	
    	
    	QPushButton updateButton = new QPushButton();
    	updateButton.setText(Translations.get("filter_search_options_update"));
    	updateButton.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(updateButton, 14, 0, 1, 3);
    	
    	for (int i = 0; i < 15; ++i)
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
    	titleField.setText("");
    	labelField.setText("");
    	minRatingField.setText("");
    	maxDaysSinceAdded.setText("");
    	minBeatIntensityChoices.setCurrentIndex(0);
    	maxBeatIntensityChoices.setCurrentIndex(4);
    	minReleasedField.setText("");
    	maxReleasedField.setText("");
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
    	ReleaseSearchParameters releaseParameters = SearchWidgetUI.instance.getReleaseSearchParameters();
    	int numFilters = update(releaseParameters);
    	releaseParameters = SearchWidgetUI.instance.getRecommendedReleaseSearchParameters();
    	update(releaseParameters);
    	if (updateTable) {
    		SearchWidgetUI.instance.updateFilter();
    		CentralWidgetUI.instance.setNumReleaseFilters(numFilters);
    	}    	
    }
    public int update(SearchParameters searchParameters) {
    	ReleaseSearchParameters releaseParameters = (ReleaseSearchParameters)searchParameters;
    	int numFilters = 0;
    	if (artistField.text().length() > 0) {
    		releaseParameters.setArtistSearchText(artistField.text());
    		++numFilters;
    	} else 
    		releaseParameters.setArtistSearchText("");
    	if (titleField.text().length() > 0) {
    		releaseParameters.setTitleSearchText(titleField.text());
    		++numFilters;
    	} else 
    		releaseParameters.setTitleSearchText("");
    	if (labelField.text().length() > 0) {
    		releaseParameters.setLabelSearchText(labelField.text());
    		++numFilters;
    	} else 
    		releaseParameters.setLabelSearchText("");
    	if (stylesField.text().length() > 0) {
    		releaseParameters.setStyleSearchText(stylesField.text());
    		++numFilters;
    	} else 
    		releaseParameters.setStyleSearchText("");    	
    	if (tagsField.text().length() > 0) {
    		releaseParameters.setTagSearchText(tagsField.text());
    		++numFilters;
    	} else 
    		releaseParameters.setTagSearchText("");    	    	
    	if (minBeatIntensityChoices.currentIndex() != 0) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (minBeatIntensityChoices.currentIndex() == 4)
    			releaseParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 3)
    			releaseParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 2)
    			releaseParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 1)
    			releaseParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		releaseParameters.setMinBeatIntensity(null);
    	}
    	if (maxBeatIntensityChoices.currentIndex() != 4) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (maxBeatIntensityChoices.currentIndex() == 3)
    			releaseParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 2)
    			releaseParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 1)
    			releaseParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 0)
    			releaseParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		releaseParameters.setMaxBeatIntensity(null);
    	}    	
    	if (minReleasedField.text().length() > 0) {
    		try {
    			releaseParameters.setMinYearReleased(Short.parseShort(minReleasedField.text()));
				++numFilters;
    		} catch (Exception e) {    		
				releaseParameters.setMinYearReleased((short)0);
			}
    	} else {
    		releaseParameters.setMinYearReleased((short)0);
    	}
    	if (maxReleasedField.text().length() > 0) {
    		try {
    			releaseParameters.setMaxYearReleased(Short.parseShort(maxReleasedField.text()));
				++numFilters;
    		} catch (Exception e) {    		
				releaseParameters.setMaxYearReleased((short)0);
			}
    	} else {
    		releaseParameters.setMaxYearReleased((short)0);
    	}
    	if (!minRatingField.text().equals("")) {
    		try {
    			releaseParameters.setMinRating(Rating.getRating(Integer.parseInt(minRatingField.text())));
    			++numFilters;
    		} catch (Exception e) {
    			releaseParameters.setMinRating(null);
    		}
    	} else {
    		releaseParameters.setMinRating(null);
    	}
    	if (!maxDaysSinceAdded.text().equals("")) {
    		try {
    			releaseParameters.setMaxDaysSinceLastAdded(Short.parseShort(maxDaysSinceAdded.text()));
    			++numFilters;
    		} catch (Exception e) {
    			releaseParameters.setMaxDaysSinceLastAdded((short)0);
    		}
    	} else {
    		releaseParameters.setMaxDaysSinceLastAdded((short)0);
    	}    	
    	if (includeUnrated.isChecked()) {
    		releaseParameters.setIncludeUnrated(true);
    		++numFilters;
    	} else {
    		releaseParameters.setIncludeUnrated(false);
    	}
    	if (excludeRated.isChecked()) {
    		releaseParameters.setExcludeRated(true);
    		++numFilters;
    	} else {
    		releaseParameters.setExcludeRated(false);
    	}    
    	if (showDisabled.isChecked()) {
    		releaseParameters.setShowDisabled(true);
    		++numFilters;
    	} else {
    		releaseParameters.setShowDisabled(false);
    	}    	
    	currentCount = numFilters;
    	return numFilters;
    }
	
    
}
