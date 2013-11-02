package com.mixshare.rapid_evolution.ui.widgets.filter.options.song;

import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.tapper.BpmTapper;
import com.mixshare.rapid_evolution.music.bpm.tapper.BpmTapperListener;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
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

public class SongSearchOptionsUI extends AbstractSearchOptionsUI {

	static private Logger log = Logger.getLogger(SongSearchOptionsUI.class);
    
	static public SongSearchOptionsUI instance = null;
    
	////////////
	// FIELDS //
	////////////
	
	private QLineEdit artistField;
	private QLineEdit releaseField;
	private QLineEdit titleField;
	private QLineEdit labelField;
	private QLineEdit stylesField;
	private QLineEdit tagsField;
	private QLineEdit keysField;
	private QLineEdit minBpmField;
	private QLineEdit maxBpmField;
	private QComboBox minBeatIntensityChoices;
	private QComboBox maxBeatIntensityChoices;
	private QLineEdit minDurationField;
	private QLineEdit maxDurationField;
	private QLineEdit minReleasedField;
	private QLineEdit maxReleasedField;
	private QLineEdit minRatingField;
	private QLineEdit maxDaysSinceAdded;
	private QCheckBox showCompatibleOnly;
	private QCheckBox includeUnrated;
	private QCheckBox excludeRated;
	private QCheckBox hasBrokenFileLink;
	private QCheckBox showDisabled;
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public SongSearchOptionsUI() {
    	instance = this;
    	
    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	setSizePolicy(searchWidgetSizePolicy);    	  

    	QGridLayout searchLayout = new QGridLayout(this);
    	//searchLayout.setMargin(5);    	

    	QLabel artistLabel = new QLabel();
    	artistLabel.setText(Translations.get("song_filter_artist"));    	
    	artistField = new QLineEdit();
    	artistField.setFixedWidth(90);
    	artistField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(artistLabel, 0, 0, 1, 1);
    	searchLayout.addWidget(artistField, 0, 1, 1, 2);
    	
    	QLabel releaseLabel = new QLabel();
    	releaseLabel.setText(Translations.get("song_filter_release"));    	
    	releaseField = new QLineEdit();
    	releaseField.setFixedWidth(90);
    	releaseField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(releaseLabel, 1, 0, 1, 1);
    	searchLayout.addWidget(releaseField, 1, 1, 1, 2);
    	
    	QLabel titleLabel = new QLabel();
    	titleLabel.setText(Translations.get("song_filter_title"));    	
    	titleField = new QLineEdit();
    	titleField.setFixedWidth(90);
    	titleField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(titleLabel, 2, 0, 1, 1);
    	searchLayout.addWidget(titleField, 2, 1, 1, 2);
    	
    	QLabel labelLabel = new QLabel();
    	labelLabel.setText(Translations.get("song_filter_label"));    	
    	labelField = new QLineEdit();
    	labelField.setFixedWidth(90);
    	labelField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(labelLabel, 3, 0, 1, 1);
    	searchLayout.addWidget(labelField, 3, 1, 1, 2);

    	QLabel stylesLabel = new QLabel();
    	stylesLabel.setText(Translations.get("search_filter_styles"));    	
    	stylesField = new QLineEdit();
    	stylesField.setFixedWidth(90);
    	stylesField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(stylesLabel, 4, 0, 1, 1);
    	searchLayout.addWidget(stylesField, 4, 1, 1, 2);    	

    	QLabel tagsLabel = new QLabel();
    	tagsLabel.setText(Translations.get("search_filter_tags"));    	
    	tagsField = new QLineEdit();
    	tagsField.setFixedWidth(90);
    	tagsField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(tagsLabel, 5, 0, 1, 1);
    	searchLayout.addWidget(tagsField, 5, 1, 1, 2);        	
    	
    	QLabel keysLabel = new QLabel();
    	keysLabel.setText(Translations.get("song_filter_keys"));
    	keysField = new QLineEdit();
    	keysField.setFixedWidth(90);
    	keysField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(keysLabel, 6, 0, 1, 1);
    	searchLayout.addWidget(keysField, 6, 1, 1, 2);

    	QLabel minBpmLabel = new QLabel();
    	minBpmLabel.setText(Translations.get("song_filter_min_bpm"));    	
    	minBpmField = new QLineEdit();
    	minBpmField.setFixedWidth(50);
    	minBpmField.returnPressed.connect(this, "updateFilter()");
    	QPushButton minBpmTap = new QPushButton();
    	minBpmTap.setText(Translations.get("bpm_tapper_tap_text"));
    	minBpmTap.clicked.connect(new BpmTapper(new MinBpmTapResultProcessor()), "tap(Boolean)");
    	searchLayout.addWidget(minBpmLabel, 7, 0, 1, 1);
    	searchLayout.addWidget(minBpmField, 7, 1, 1, 1);
    	searchLayout.addWidget(minBpmTap, 7, 2, 1, 1);
    	
    	QLabel maxBpmLabel = new QLabel();
    	maxBpmLabel.setText(Translations.get("song_filter_max_bpm"));
    	maxBpmField = new QLineEdit();
    	maxBpmField.setFixedWidth(50);
    	maxBpmField.returnPressed.connect(this, "updateFilter()");
    	QPushButton maxBpmTap = new QPushButton();
    	maxBpmTap.setText(Translations.get("bpm_tapper_tap_text"));
    	maxBpmTap.clicked.connect(new BpmTapper(new MaxBpmTapResultProcessor()), "tap(Boolean)");
    	searchLayout.addWidget(maxBpmLabel, 8, 0, 1, 1);
    	searchLayout.addWidget(maxBpmField, 8, 1, 1, 1);
    	searchLayout.addWidget(maxBpmTap, 8, 2, 1, 1);
    	    	
    	QLabel minBeatIntensityLabel = new QLabel();
    	minBeatIntensityLabel.setText(Translations.get("song_filter_min_beat_intensity"));    	
    	minBeatIntensityChoices = new QComboBox();
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_low"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_low"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_medium"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_high"));
    	minBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_high"));    	
    	minBeatIntensityChoices.setCurrentIndex(0);
    	minBeatIntensityChoices.currentIndexChanged.connect(this, "beatIntensityChanged(Integer)");
    	minBeatIntensityChoices.setFixedWidth(80);
    	searchLayout.addWidget(minBeatIntensityLabel, 9, 0, 1, 1);
    	searchLayout.addWidget(minBeatIntensityChoices, 9, 1, 1, 2);    	
    	
    	QLabel maxBeatIntensityLabel = new QLabel();
    	maxBeatIntensityLabel.setText(Translations.get("song_filter_max_beat_intensity"));    	
    	maxBeatIntensityChoices = new QComboBox();
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_low"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_low"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_medium"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_high"));
    	maxBeatIntensityChoices.addItem(Translations.get("beat_intensity_very_high"));
    	maxBeatIntensityChoices.setCurrentIndex(4);
    	maxBeatIntensityChoices.currentIndexChanged.connect(this, "beatIntensityChanged(Integer)");
    	maxBeatIntensityChoices.setFixedWidth(80);
    	searchLayout.addWidget(maxBeatIntensityLabel, 10, 0, 1, 1);
    	searchLayout.addWidget(maxBeatIntensityChoices, 10, 1, 1, 2);    	
    	
    	QLabel minDurationLabel = new QLabel();
    	minDurationLabel.setText(Translations.get("song_filter_min_duration"));    	
    	minDurationField = new QLineEdit();
    	minDurationField.setFixedWidth(50);
    	minDurationField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(minDurationLabel, 11, 0, 1, 1);
    	searchLayout.addWidget(minDurationField, 11, 1, 1, 2);    	

    	QLabel maxDurationLabel = new QLabel();
    	maxDurationLabel.setText(Translations.get("song_filter_max_duration"));
    	maxDurationField = new QLineEdit();
    	maxDurationField.setFixedWidth(50);
    	maxDurationField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(maxDurationLabel, 12, 0, 1, 1);
    	searchLayout.addWidget(maxDurationField, 12, 1, 1, 2);    	

    	QLabel minReleasedLabel = new QLabel();
    	minReleasedLabel.setText(Translations.get("song_filter_min_year_released"));    	
    	minReleasedField = new QLineEdit();
    	minReleasedField.setFixedWidth(50);
    	minReleasedField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(minReleasedLabel, 13, 0, 1, 1);
    	searchLayout.addWidget(minReleasedField, 13, 1, 1, 2);    	

    	QLabel maxReleasedLabel = new QLabel();
    	maxReleasedLabel.setText(Translations.get("song_filter_max_year_released"));
    	maxReleasedField = new QLineEdit();
    	maxReleasedField.setFixedWidth(50);
    	maxReleasedField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(maxReleasedLabel, 14, 0, 1, 1);
    	searchLayout.addWidget(maxReleasedField, 14, 1, 1, 2);    	
    	
    	QLabel minRating = new QLabel();
    	minRating.setText(Translations.get("song_filter_min_rating"));    	
    	QLabel minRatingSuffix = new QLabel();
    	minRatingSuffix.setText(Translations.get("song_filter_min_rating_suffix"));    	
    	minRatingField = new QLineEdit();
    	minRatingField.setFixedWidth(50);
    	minRatingField.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(minRating, 15, 0, 1, 1);
    	searchLayout.addWidget(minRatingField, 15, 1, 1, 1);    	
    	searchLayout.addWidget(minRatingSuffix, 15, 2, 1, 1);    	

    	QLabel maxDaysSinceAddedLabel = new QLabel();
    	maxDaysSinceAddedLabel.setText(Translations.get("song_filter_max_days_since_added"));    	
    	QLabel maxDaysSinceAddedSuffix = new QLabel();
    	maxDaysSinceAddedSuffix.setText(Translations.get("song_filter_max_days_since_added_suffix"));    	
    	maxDaysSinceAdded = new QLineEdit();
    	maxDaysSinceAdded.setFixedWidth(50);
    	maxDaysSinceAdded.returnPressed.connect(this, "updateFilter()");
    	searchLayout.addWidget(maxDaysSinceAddedLabel, 16, 0, 1, 1);
    	searchLayout.addWidget(maxDaysSinceAdded, 16, 1, 1, 1);
    	searchLayout.addWidget(maxDaysSinceAddedSuffix, 16, 2, 1, 1);
    	
    	showCompatibleOnly = new QCheckBox();
    	showCompatibleOnly.setText(Translations.get("song_filter_compatible_only"));
    	showCompatibleOnly.setToolTip(Translations.get("song_filter_compatible_only_tooltip"));
    	showCompatibleOnly.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(showCompatibleOnly, 17, 0, 1, 2);    
    	
    	includeUnrated = new QCheckBox();
    	includeUnrated.setText(Translations.get("song_filter_include_unrated"));
    	includeUnrated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(includeUnrated, 18, 0, 1, 2);    	
    	
    	excludeRated = new QCheckBox();
    	excludeRated.setText(Translations.get("song_filter_exclude_rated"));
    	excludeRated.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(excludeRated, 19, 0, 1, 2);    	

    	hasBrokenFileLink = new QCheckBox();
    	hasBrokenFileLink.setText(Translations.get("song_filter_has_broken_file_link"));
    	hasBrokenFileLink.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(hasBrokenFileLink, 20, 0, 1, 2);    	
    	
    	showDisabled = new QCheckBox();
    	showDisabled.setText(Translations.get("song_filter_show_disabled"));
    	showDisabled.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(showDisabled, 21, 0, 1, 2);
    	
    	QPushButton updateButton = new QPushButton();
    	updateButton.setText(Translations.get("filter_search_options_update"));
    	updateButton.clicked.connect(this, "update(Boolean)");
    	searchLayout.addWidget(updateButton, 22, 0, 1, 3);    	
    	
    	for (int i = 0; i < 23; ++i)
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
    	releaseField.setText("");
    	titleField.setText("");
    	labelField.setText("");
    	keysField.setText("");
    	minBpmField.setText("");
    	maxBpmField.setText("");
    	minBeatIntensityChoices.setCurrentIndex(0);
    	maxBeatIntensityChoices.setCurrentIndex(4);
    	minDurationField.setText("");
    	maxDurationField.setText("");
    	minReleasedField.setText("");
    	maxReleasedField.setText("");
    	maxDaysSinceAdded.setText("");
    	minRatingField.setText("");
    	includeUnrated.setChecked(false);
    	showCompatibleOnly.setChecked(false);
    	excludeRated.setChecked(false);
    	hasBrokenFileLink.setChecked(false);
    	showDisabled.setChecked(false);
    	stylesField.setText("");
    	tagsField.setText("");
    	update(null, false);
    }
    
    protected void beatIntensityChanged(Integer value) { updateFilter(); }
    protected void updateFilter() { update(false, true); }
    protected void update(Boolean checked) { update(checked, true); }
    protected void update(Boolean checked, boolean updateTable) {
    	SongSearchParameters songParameters = SearchWidgetUI.instance.getSongSearchParameters();
    	int numFilters = update(songParameters);
    	songParameters = SearchWidgetUI.instance.getRecommendedSongSearchParameters();
    	update(songParameters);
    	if (updateTable) {
    		SearchWidgetUI.instance.updateFilter();
    		CentralWidgetUI.instance.setNumSongFilters(numFilters);
    	}    	
    }
    public int update(SearchParameters searchParameters) {
    	SongSearchParameters songParameters = (SongSearchParameters)searchParameters;
    	int numFilters = 0;
    	if (artistField.text().length() > 0) {
    		songParameters.setArtistSearchText(artistField.text());
    		++numFilters;
    	} else 
    		songParameters.setArtistSearchText("");    	
    	if (stylesField.text().length() > 0) {
    		songParameters.setStyleSearchText(stylesField.text());
    		++numFilters;
    	} else 
    		songParameters.setStyleSearchText("");    	
    	if (tagsField.text().length() > 0) {
    		songParameters.setTagSearchText(tagsField.text());
    		++numFilters;
    	} else 
    		songParameters.setTagSearchText("");    	
    	if (releaseField.text().length() > 0) {
    		songParameters.setReleaseSearchText(releaseField.text());
    		++numFilters;
    	} else 
    		songParameters.setReleaseSearchText("");
    	if (titleField.text().length() > 0) {
    		songParameters.setTitleSearchText(titleField.text());
    		++numFilters;
    	} else 
    		songParameters.setTitleSearchText("");
    	if (labelField.text().length() > 0) {
    		songParameters.setLabelSearchText(labelField.text());
    		++numFilters;
    	} else 
    		songParameters.setLabelSearchText("");
    	if (keysField.text().length() > 0) {
    		Vector<Key> keys = new Vector<Key>();
    		StringTokenizer tokenizer = new StringTokenizer(keysField.text(), ",");
    		while (tokenizer.hasMoreTokens()) {
    			Key key = Key.getKey(tokenizer.nextToken());
    			if (key.isValid())
    				keys.add(key);
    		}
    		if (keys.size() > 0) {
    			songParameters.setKeys(keys);
    			++numFilters;    		
    		} else {
    			songParameters.setKeys(null);
    		}
    	} else {
    		songParameters.setKeys(null);
    	}
    	if (minBpmField.text().length() > 0) {
    		try {
    			songParameters.setMinBpm(new Bpm(Float.parseFloat(minBpmField.text())));
    			++numFilters;  
    		} catch (Exception e) {
    			songParameters.setMinBpm(null);
    		}
    	} else {
    		songParameters.setMinBpm(null);
    	}
    	if (maxBpmField.text().length() > 0) {
    		try {
    			songParameters.setMaxBpm(new Bpm(Float.parseFloat(maxBpmField.text())));
    			++numFilters;  
    		} catch (Exception e) {
    			songParameters.setMaxBpm(null);
    		}
    	} else {
    		songParameters.setMaxBpm(null);
    	}    	
    	if (minBeatIntensityChoices.currentIndex() != 0) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (minBeatIntensityChoices.currentIndex() == 4)
    			songParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 3)
    			songParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 2)
    			songParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (minBeatIntensityChoices.currentIndex() == 1)
    			songParameters.setMinBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		songParameters.setMinBeatIntensity(null);
    	}
    	if (maxBeatIntensityChoices.currentIndex() != 4) {
    		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
    		int base = 100 - increment;
    		if (maxBeatIntensityChoices.currentIndex() == 3)
    			songParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 2)
    			songParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 1)
    			songParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		base -= increment;
    		if (maxBeatIntensityChoices.currentIndex() == 0)
    			songParameters.setMaxBeatIntensity(BeatIntensity.getBeatIntensity(base));
    		++numFilters;
    	} else {
    		songParameters.setMaxBeatIntensity(null);
    	}    	
    	if (minDurationField.text().length() > 0) {
			Duration duration = new Duration(minDurationField.text());
			if (duration.isValid()) {
				songParameters.setMinDuration(duration);
				++numFilters;
			} else {
				songParameters.setMinDuration(null);
			}
    	} else {
    		songParameters.setMinDuration(null);
    	}
    	if (maxDurationField.text().length() > 0) {
			Duration duration = new Duration(maxDurationField.text());
			if (duration.isValid()) {
				songParameters.setMaxDuration(duration);
				++numFilters;
			} else {
				songParameters.setMaxDuration(null);
			}
    	} else {
    		songParameters.setMaxDuration(null);
    	}
    	if (minReleasedField.text().length() > 0) {
    		try {
    			songParameters.setMinYearReleased(Short.parseShort(minReleasedField.text()));
				++numFilters;
    		} catch (Exception e) {    		
				songParameters.setMinYearReleased((short)0);
			}
    	} else {
    		songParameters.setMinYearReleased((short)0);
    	}
    	if (maxReleasedField.text().length() > 0) {
    		try {
    			songParameters.setMaxYearReleased(Short.parseShort(maxReleasedField.text()));
				++numFilters;
    		} catch (Exception e) {    		
				songParameters.setMaxYearReleased((short)0);
			}
    	} else {
    		songParameters.setMaxYearReleased((short)0);
    	}
    	if (!minRatingField.text().equals("")) {
    		try {
    			songParameters.setMinRating(Rating.getRating(Integer.parseInt(minRatingField.text())));
    			++numFilters;
    		} catch (Exception e) {
    			songParameters.setMinRating(null);
    		}
    	} else {
    		songParameters.setMinRating(null);
    	}
    	if (!maxDaysSinceAdded.text().equals("")) {
    		try {
    			songParameters.setMaxDaysSinceLastAdded(Short.parseShort(maxDaysSinceAdded.text()));
    			++numFilters;
    		} catch (Exception e) {
    			songParameters.setMaxDaysSinceLastAdded((short)0);
    		}
    	} else {
    		songParameters.setMaxDaysSinceLastAdded((short)0);
    	}
    	if (showCompatibleOnly.isChecked()) {
    		songParameters.setSearchForCompatible(true);
    		++numFilters;
    	} else {
    		songParameters.setSearchForCompatible(false);
    	}
    	if (includeUnrated.isChecked()) {
    		songParameters.setIncludeUnrated(true);
    		++numFilters;
    	} else {
    		songParameters.setIncludeUnrated(false);
    	}
    	if (excludeRated.isChecked()) {
    		songParameters.setExcludeRated(true);
    		++numFilters;
    	} else {
    		songParameters.setExcludeRated(false);
    	}    	
    	if (hasBrokenFileLink.isChecked()) {
    		songParameters.setHasBrokenFileLink(true);
    		++numFilters;
    	} else {
    		songParameters.setHasBrokenFileLink(false);
    	}
    	if (showDisabled.isChecked()) {
    		songParameters.setShowDisabled(true);
    		++numFilters;
    	} else {
    		songParameters.setShowDisabled(false);
    	}
    	currentCount = numFilters;
    	return numFilters;
    }
	
    private class MinBpmTapResultProcessor implements BpmTapperListener {
    	
    	public void finalBpm(double bpm) { }
    	public void setBpm(double bpm) {    		
    		if (log.isTraceEnabled())
    			log.trace("setBpm(); bpm=" + bpm);
    		minBpmField.setText(String.valueOf(bpm));
    	}
    	public void resetBpm() { }    	
    	
    }
    
    private class MaxBpmTapResultProcessor implements BpmTapperListener {
    	
    	public void finalBpm(double bpm) { }
    	public void setBpm(double bpm) {    		
    		if (log.isTraceEnabled())
    			log.trace("setBpm(); bpm=" + bpm);
    		maxBpmField.setText(String.valueOf(bpm));
    	}
    	public void resetBpm() { }    	
    	
    }
    
}
