package com.mixshare.rapid_evolution.ui.dialogs.mixout;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRatingChangedListener;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRatingWidget;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSpacerItem;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class MixoutDialog extends QDialog implements StarRatingChangedListener, DataConstants {

	static private Logger log = Logger.getLogger(MixoutDialog.class);
	
	static public final byte MODE_ADD = 0;
	static public final byte MODE_EDIT = 1;
	
	private MixoutProfile mixoutProfile;
	private QLineEdit fromLine;
	private QLineEdit toLine;
	private QTextEdit comments;
	private QComboBox type;
	private QLineEdit bpmDiff;
	private StarRatingWidget rating;
    
    public MixoutDialog(QWidget parent, MixoutProfile mixoutProfile, byte mode) {
        super(parent);
        this.mixoutProfile = mixoutProfile;
        init(mode);
    }

    private void init(byte mode) {
    	try {    		
    		if (mode == MODE_ADD)
    			setWindowTitle(Translations.get("mixout_dialog_add_title"));
    		else
    			setWindowTitle(Translations.get("mixout_dialog_edit_title"));
	        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));

	    	QSizePolicy trailWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	trailWidgetSizePolicy.setVerticalStretch((byte)1);
	    	setSizePolicy(trailWidgetSizePolicy);    	    	    	
	    	
	    	QVBoxLayout verticalLayout = new QVBoxLayout(this);    	
	    	verticalLayout.setMargin(10);    	    	
	    	
	    	QHBoxLayout fromLayout = new QHBoxLayout();
	    	QLabel fromLabel = new QLabel(Translations.get("mixout_dialog_from_text"));
	    	fromLayout.addWidget(fromLabel);
	    	fromLine = new QLineEdit();
	    	fromLine.setText(mixoutProfile.getFromSong().toString());
	    	fromLine.setReadOnly(true);
	    	fromLine.setFocusPolicy(Qt.FocusPolicy.NoFocus);
	    	fromLayout.addWidget(fromLine);
	    	fromLayout.setStretch(0, 0);
	    	fromLayout.setStretch(1, 1);
	    	fromLayout.setAlignment(fromLabel, Qt.AlignmentFlag.AlignHCenter);

	    	QHBoxLayout toLayout = new QHBoxLayout();
	    	QLabel toLabel = new QLabel(Translations.get("mixout_dialog_to_text"));
	    	toLayout.addWidget(toLabel);
	    	toLine = new QLineEdit();
	    	toLine.setText(mixoutProfile.getToSong().toString());
	    	toLine.setReadOnly(true);
	    	toLine.setFocusPolicy(Qt.FocusPolicy.NoFocus);
	    	toLayout.addWidget(toLine);
	    	toLayout.setStretch(0, 0);
	    	toLayout.setStretch(1, 1);
	    	toLayout.setAlignment(toLabel, Qt.AlignmentFlag.AlignHCenter);
	    	
	    	QHBoxLayout commentsLayout = new QHBoxLayout();
	    	QLabel commentsLabel = new QLabel(Translations.get("mixout_dialog_comments_text"));
	    	commentsLayout.addWidget(commentsLabel);
	    	comments = new QTextEdit();
	    	comments.setText(mixoutProfile.getComments());	    	
	    	commentsLayout.addWidget(comments);
	    	commentsLayout.setStretch(0, 0);
	    	commentsLayout.setStretch(1, 1);
	    	commentsLayout.setAlignment(commentsLabel, Qt.AlignmentFlag.AlignHCenter);
	    	
	    	QHBoxLayout ratingLayout = new QHBoxLayout();
	    	QLabel ratingLabel = new QLabel(Translations.get("mixout_dialog_rating_text"));
	    	ratingLayout.addWidget(ratingLabel);
	    	rating = new StarRatingWidget(this, mixoutProfile.getRating());
	    	ratingLayout.addWidget(rating);
	    	ratingLayout.addSpacerItem(new QSpacerItem(0, 0, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding));
	    	ratingLayout.setStretch(0, 0);
	    	ratingLayout.setStretch(1, 1);
	    	ratingLayout.setAlignment(ratingLabel, Qt.AlignmentFlag.AlignHCenter);
	    		    		    	
	    	QHBoxLayout bpmDiffLayout = new QHBoxLayout();
	    	QLabel bpmDiffLabel = new QLabel(Translations.get("mixout_dialog_bpmdiff_text"));
	    	bpmDiffLayout.addWidget(bpmDiffLabel);
	    	bpmDiff = new QLineEdit();
	    	bpmDiff.setMaximumWidth(50);
	    	double bpmShift = mixoutProfile.getBpmDiff();
	    	bpmShift = Math.round(bpmShift*100.0) / 100.0; // round it	
	    	if (bpmShift > 0)
	    		bpmDiff.setText("+" + String.valueOf(bpmShift));
	    	else
	    		bpmDiff.setText(String.valueOf(bpmShift));	    	
	    	bpmDiffLayout.addWidget(bpmDiff);
	    	bpmDiffLayout.addSpacerItem(new QSpacerItem(0, 0, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding));
	    	bpmDiffLayout.setStretch(0, 0);
	    	bpmDiffLayout.setStretch(1, 1);
	    	bpmDiffLayout.setAlignment(bpmDiffLabel, Qt.AlignmentFlag.AlignHCenter);
	    	
	    	QHBoxLayout typeLayout = new QHBoxLayout();
	    	QLabel typeLabel = new QLabel(Translations.get("mixout_dialog_type_text"));
	    	typeLayout.addWidget(typeLabel);
	    	type = new QComboBox();
	    	type.addItem(Translations.get("mixout_transition_text"), MixoutRecord.TYPE_TRANSITION);
	    	type.addItem(Translations.get("mixout_addon_text"), MixoutRecord.TYPE_ADDON);
	    	type.setMaximumWidth(100);
	    	if (mixoutProfile.getType() == MixoutRecord.TYPE_TRANSITION)
	    		type.setCurrentIndex(0);
	    	else if (mixoutProfile.getType() == MixoutRecord.TYPE_ADDON)
	    		type.setCurrentIndex(1);
	    	typeLayout.addWidget(type);
	    	typeLayout.addSpacerItem(new QSpacerItem(0, 0, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding));
	    	typeLayout.setStretch(0, 0);
	    	typeLayout.setStretch(1, 1);
	    	typeLayout.setAlignment(typeLabel, Qt.AlignmentFlag.AlignHCenter);
	    	
	    	int labelWidth = 60;
	    	fromLabel.setMaximumWidth(labelWidth);
	    	fromLabel.setMinimumWidth(labelWidth);
	    	toLabel.setMaximumWidth(labelWidth);
	    	toLabel.setMinimumWidth(labelWidth);
	    	commentsLabel.setMaximumWidth(labelWidth);
	    	commentsLabel.setMinimumWidth(labelWidth);
	    	ratingLabel.setMaximumWidth(labelWidth);
	    	ratingLabel.setMinimumWidth(labelWidth);
	    	bpmDiffLabel.setMaximumWidth(labelWidth);
	    	bpmDiffLabel.setMinimumWidth(labelWidth);
	    	typeLabel.setMaximumWidth(labelWidth);
	    	typeLabel.setMinimumWidth(labelWidth);
	    	
	        verticalLayout.addLayout(fromLayout);
	        verticalLayout.addLayout(toLayout);
	        verticalLayout.addLayout(commentsLayout);
	        verticalLayout.addLayout(ratingLayout);
	        verticalLayout.addLayout(bpmDiffLayout);
	        verticalLayout.addLayout(typeLayout);
	        
	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "closeMixouts()");	        
	        verticalLayout.addWidget(buttonBox);	        
	        
	        QWidgetUtil.setWidgetSize(this, "mixout_dialog", 650, 200);
	        QWidgetUtil.setWidgetPosition(this, "mixout_dialog");
	        	        	        	        
    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }
    
    public void ratingChanged(Rating newRating) {

    }
    
    protected void closeMixouts() {
    	this.close();
    	try {
	    	mixoutProfile.setComments(comments.toPlainText());
	    	try { mixoutProfile.setBpmDiff(Float.parseFloat(bpmDiff.text())); } catch (NumberFormatException nfe) { }
	    	if (type.currentIndex() == 0)
	    		mixoutProfile.setType(MixoutRecord.TYPE_TRANSITION);
	    	else if (type.currentIndex() == 1)
	    		mixoutProfile.setType(MixoutRecord.TYPE_ADDON);
	    	mixoutProfile.setRating(rating.getRating(), DATA_SOURCE_USER);
	    	mixoutProfile.save();
	    	mixoutProfile.getFromSong().update();
    	} catch (Exception e) {
    		log.error("closeMixouts(): error", e);
    	}
    }
    
    protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty("mixout_dialog_width", String.valueOf(re.size().width()));
    	UIProperties.setProperty("mixout_dialog_height", String.valueOf(re.size().height()));
    }
    
    protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty("mixout_dialog_x", String.valueOf(me.pos().x()));
    	UIProperties.setProperty("mixout_dialog_y", String.valueOf(me.pos().y()));
    }

}
