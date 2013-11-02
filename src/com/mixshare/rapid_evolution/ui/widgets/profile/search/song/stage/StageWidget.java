package com.mixshare.rapid_evolution.ui.widgets.profile.search.song.stage;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.tapper.BpmTapper;
import com.mixshare.rapid_evolution.music.bpm.tapper.BpmTapperListener;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSpacerItem;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class StageWidget extends QWidget implements BpmTapperListener {

	static private Logger log = Logger.getLogger(StageWidget.class);
	
	////////////
	// FIELDS //
	////////////
	
	private Key currentKey;
	private QLineEdit currentKeyField;
	private Bpm currentBpm;
	private QLineEdit currentBpmField;
	private QCheckBox keyLock;
	private BPMSlider bpmSlider;
	private QLineEdit bpmShift;
	private QComboBox rangeCombo;	
	private QPushButton bpmResetButton;
	private QPushButton bpmTapButton;
	private BpmTapper bpmTapper;
	private QSpacerItem topSpacer;
	private QSpacerItem bottomSpacer;
	private Vector<StageChangeListener> changeListeners = new Vector<StageChangeListener>();
	
	transient private Key lastUpdatedKey;
	transient private Bpm lastUpdatedBpm;
	transient private String lastUpdatedBpmShift;
	transient private float lastBpmRange;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public StageWidget() {
		super();
		
    	QSizePolicy minPolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
		
		QHBoxLayout stageLayout = new QHBoxLayout();
		stageLayout.setMargin(0);
		    	
    	setLayout(stageLayout);
    	
    	QVBoxLayout row1 = new QVBoxLayout();    	
    	row1.setMargin(0);    	

    	QVBoxLayout row2 = new QVBoxLayout();    	
    	row2.setMargin(0);    	
    	
    	QVBoxLayout bpmSliderLabelLayout = new QVBoxLayout();
    	bpmSliderLabelLayout.setMargin(0);
    	
    	topSpacer = new QSpacerItem(0, 10, QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Minimum);
    	bottomSpacer = new QSpacerItem(0, 10, QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Minimum);

    	keyLock = new QCheckBox();
    	keyLock.setText(Translations.get("stage_keylock_text"));
    	if (UIProperties.hasProperty("stage_key_lock"))
    		keyLock.setChecked(UIProperties.getBoolean("stage_key_lock"));
    	keyLock.clicked.connect(this, "keyLockClicked(Boolean)");
    	bpmShift = new QLineEdit();
    	bpmSlider = new BPMSlider(bpmShift, bpmSliderLabelLayout, this);
    	QLabel rangeLabel = new QLabel();
    	rangeLabel.setText(Translations.get("stage_range_text"));
    	rangeLabel.setSizePolicy(minPolicy);
    	QLabel shiftLabel = new QLabel();
    	shiftLabel.setText(Translations.get("stage_shift_text"));
    	shiftLabel.setSizePolicy(minPolicy);
    	rangeCombo = new QComboBox();
    	rangeCombo.addItem("4%");
    	rangeCombo.addItem("8%");
    	rangeCombo.addItem("10%");
    	rangeCombo.addItem("20%");
    	rangeCombo.addItem("50%");
    	rangeCombo.currentIndexChanged.connect(this, "rangeComboChanged(Integer)");
    	if (UIProperties.hasProperty("stage_bpm_range_index")) {
    		int index = UIProperties.getInt("stage_bpm_range_index");
    		rangeCombo.setCurrentIndex(index);
    	} else {
    		rangeCombo.setCurrentIndex(1);
    		//bpmSlider.updateBpmSliderRanges("8%");
    	}
    	setLastBpmRange();
    	
    	bpmResetButton = new QPushButton();
    	bpmResetButton.setText(Translations.get("stage_reset_text"));
    	bpmResetButton.clicked.connect(this, "bpmReset(Boolean)");
    	    	    	    	
    	QHBoxLayout bpmSliderCombo = new QHBoxLayout();
    	bpmSliderCombo.setMargin(0);
    	    	
    	QWidget widget = new QWidget();
    	widget.setLayout(bpmSliderLabelLayout);
    	bpmSliderCombo.addWidget(widget);    	
    	    	
    	QVBoxLayout bpmSliderPadLayout = new QVBoxLayout();
    	bpmSliderPadLayout.addSpacerItem(topSpacer);
    	bpmSliderPadLayout.addWidget(bpmSlider);
    	bpmSliderPadLayout.addSpacerItem(bottomSpacer);
    	
    	bpmSliderCombo.addLayout(bpmSliderPadLayout);
    	bpmSliderCombo.setAlignment(bpmSliderPadLayout, AlignmentFlag.AlignLeft);
    	
    	QHBoxLayout currentKeyLayout = new QHBoxLayout();
    	currentKeyLayout.setMargin(0);
    	currentKeyLayout.addWidget(new QLabel(Translations.get("current_key_text")));
    	currentKeyField = new QLineEdit();
    	currentKeyLayout.addWidget(currentKeyField);
    	
    	QHBoxLayout currentBpmLayout = new QHBoxLayout();
    	currentBpmLayout.setMargin(0);
    	currentBpmLayout.addWidget(new QLabel(Translations.get("current_bpm_text")));
    	currentBpmField = new QLineEdit();
    	currentBpmLayout.addWidget(currentBpmField);
    	
    	bpmTapButton = new QPushButton(Translations.get("current_bpm_tap_text"));
    	bpmTapper = new BpmTapper(this);
    	bpmTapButton.clicked.connect(bpmTapper, "tap(Boolean)");
    	    	
    	row1.addLayout(currentKeyLayout);
    	row1.addWidget(keyLock);
    	row1.addSpacerItem(new QSpacerItem(0, 10, QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Expanding));    	
    	row1.addLayout(currentBpmLayout);
    	row1.addWidget(bpmTapButton);    	
    	row1.addWidget(shiftLabel);
    	row1.addWidget(bpmShift);    	    	
    	row1.addWidget(rangeLabel);
    	row1.addWidget(rangeCombo);    	
    	row1.addWidget(bpmResetButton);

    	row2.addLayout(bpmSliderCombo);

    	stageLayout.addLayout(row1);
    	stageLayout.addLayout(row2);
    	
    	currentKeyField.editingFinished.connect(this, "currentKeyUpdated()");
    	currentKeyField.returnPressed.connect(this, "currentKeyUpdated()");
    	
    	currentBpmField.editingFinished.connect(this, "currentBpmUpdated()");
    	currentBpmField.returnPressed.connect(this, "currentBpmUpdated()");
    	
    	bpmShift.editingFinished.connect(this, "currentBpmShiftUpdated()");
    	bpmShift.returnPressed.connect(this, "currentBpmShiftUpdated()");
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public Key getCurrentKey() {
		if (currentKey == null)
			return Key.NO_KEY;
		return currentKey;
	}
	public Bpm getCurrentBpm() {
		if (currentBpm == null)
			return Bpm.NO_BPM;
		return currentBpm;
	}
	public float getCurrentBpmShift() {
		return (float)bpmSlider.getCurrentShift();
	}
	
	private void setLastBpmRange() {
		int index = rangeCombo.currentIndex();
		if (index == 0)
			lastBpmRange = 4.0f;
		else if (index == 1)
			lastBpmRange = 8.0f;
		else if (index == 2)
			lastBpmRange = 10.0f;
		else if (index == 3)
			lastBpmRange = 20.0f;
		else if (index == 4)
			lastBpmRange = 50.0f;
		lastBpmRange = 8.0f; // default
	}
	
	public float getCurrentBpmRange() {
		return lastBpmRange;
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setCurrentSong(SongProfile song) {
		float endBpm = 0.0f;
		boolean lockSlider = RE3Properties.getBoolean("bpm_slider_lock") || !ProfileWidgetUI.instance.isStageVisible();
		if (!lockSlider)
			endBpm = getCurrentBpm().getBpmValue();
		if (endBpm == 0.0f)
			endBpm = song.getEndBpm();
		if (endBpm == 0.0f)
			endBpm = song.getStartBpm();
		if (endBpm != 0.0f) {
			if (lockSlider)
				endBpm *= (1.0 + getCurrentBpmShift() / 100.0f);
			setBpmActual(endBpm);
		}
		updateKey();
	}
	
	public void setKeyLockNoUpdate(boolean set) {
		keyLock.setChecked(set);
	}
	
	public void setBpmActual(double bpm) {
		SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
		if (currentSong != null) {
			float endBpm = currentSong.getEndBpm();
			if (endBpm == 0.0f)
				endBpm = currentSong.getStartBpm();
			if (endBpm != 0.0f) {
				double difference = Bpm.getBpmDifference(endBpm, bpm);
				if (Math.abs(difference) > getCurrentBpmRange()) {
					difference = 0.0f;
					setBpm(endBpm);
					bpmSlider.setBpmShift((float)difference, false);
					updateKey();
					keyBpmChangeEvent();
					return;
				}
				bpmSlider.setBpmShift((float)difference);				
				keyBpmChangeEvent();
			}
		}
		setBpmLabel(bpm);		
	}
	public void finalBpm(double bpm) { 
		setBpmActual(bpm);
	}
	public void setBpm(double bpm) { // comes from the tapper
		setBpmLabel(bpm);
	}
	
	public void setBpmLabel(double bpm) {
		if (bpm != 0.0) {
			float roundedBpm = (float)Math.round(bpm * 100.0) / 100.0f; // round it			
			currentBpmField.setText(String.valueOf(roundedBpm));
		} else {
			currentBpmField.setText("");
		}
		currentBpm = new Bpm(bpm);
	}
	
	public void setBpmShift(float shift) {
		SongProfile song = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
		if (song != null) {
			float endBpm = song.getEndBpm();
			if (endBpm == 0.0f)
				endBpm = song.getStartBpm();
			if (endBpm != 0.0f) {
				endBpm *= (1.0f + (shift / 100.0f));
				setBpmLabel(endBpm);
			}
		}
	}
	
	public void resetBpm() { // for when tapper resets
		bpmSlider.setBpmShift(0.0f);
	}
	
	public void addChangeListener(StageChangeListener listener) {
		changeListeners.add(listener);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected void rangeComboChanged(Integer index) {
		setLastBpmRange();
		bpmSlider.updateBpmSliderRanges(rangeCombo.itemText(index));
		if (index > 1) {
			topSpacer.changeSize(0, 7);
			bottomSpacer.changeSize(0, 7);
		} else {
			topSpacer.changeSize(0, 10);
			bottomSpacer.changeSize(0, 10);			
		}
		UIProperties.setProperty("stage_bpm_range_index", String.valueOf(index));
		keyBpmChangeEvent();
	}
	
	protected void bpmReset(Boolean checked) { // for when the reset button is clicked
		bpmSlider.setBpmShift(0.0f);
		keyBpmChangeEvent();
	}
	
	protected void keyLockClicked(Boolean checked) {
		updateKey();
		keyBpmChangeEvent();
		UIProperties.setProperty("stage_key_lock", String.valueOf(checked));		
	}
	
	public void updateKey() {
		SongProfile song = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
		if (song != null) {
			Key endKey = song.getEndKey();
			if (!endKey.isValid())
				endKey = song.getStartKey();
			if (endKey.isValid()) {
				if (!keyLock.isChecked()) {
					float bpmShift = getCurrentBpmShift();
					endKey = endKey.getShiftedKeyByBpmDifference(bpmShift);
				}
				currentKey = endKey;
				currentKeyField.setText(endKey.toString());
			}
		}
	}
	
	protected void currentKeyUpdated() {
		if (currentKey.equals(lastUpdatedKey))
			return;
		keyBpmChangeEvent();
		lastUpdatedKey = currentKey;
	}
	
	protected void currentBpmUpdated() {
		try { currentBpm = new Bpm(Float.parseFloat(currentBpmField.text())); } catch (NumberFormatException nfe) { currentBpm = Bpm.NO_BPM; }
		if (currentBpm.equals(lastUpdatedBpm))
			return;
		SongProfile song = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
		if (song != null) {
			float endBpm = song.getEndBpm();
			if (endBpm == 0.0f)
				endBpm = song.getStartBpm();
			if (endBpm != 0.0f) {
				float bpmShift = Bpm.getBpmDifference(endBpm, getCurrentBpm().getBpmValue());
				bpmSlider.setBpmShift(bpmShift);
				keyBpmChangeEvent();
				lastUpdatedBpm = currentBpm;
			}
		}		
	}
	
	protected void currentBpmShiftUpdated() {
		if (bpmShift.text().equals(lastUpdatedBpmShift))
			return;		
		String shiftText = bpmShift.text();
		if (shiftText.endsWith("%"))
			shiftText = shiftText.substring(0, shiftText.length() - 1);
		try {
			float shift = Float.parseFloat(shiftText);
			bpmSlider.setBpmShift(shift);
			keyBpmChangeEvent();
			lastUpdatedBpmShift = bpmShift.text();
		} catch (NumberFormatException nfe) { }
	}

	public void keyBpmChangeEvent() {
		if (log.isDebugEnabled())
			log.debug("keyBpmChangeEvent(): the bpm and/or key have been updated");
		for (StageChangeListener listener : changeListeners)
			listener.stageChanged();
	}
	
}
