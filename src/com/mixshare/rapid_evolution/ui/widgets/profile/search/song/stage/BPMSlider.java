package com.mixshare.rapid_evolution.ui.widgets.profile.search.song.stage;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class BPMSlider extends QSlider {

    static private Logger log = Logger.getLogger(BPMSlider.class);
	
    ////////////
    // FIELDS //
    ////////////
    
	private double currentShift;
	private int currentScale;
	private QLineEdit bpmEdit;
	private QVBoxLayout labelLayout;
	private Vector<QWidget> currentLabels;
	private StageWidget stageWidget;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public BPMSlider(QLineEdit bpmEdit, QVBoxLayout labelLayout, StageWidget stageWidget) {
		super();
		this.bpmEdit = bpmEdit;
		this.labelLayout = labelLayout;
		this.stageWidget = stageWidget;
		sliderMoved.connect(this, "bpmChanged(Integer)");		
		sliderReleased.connect(this, "bpmSliderReleased()");
		setTickPosition(QSlider.TickPosition.TicksAbove);
		setBpmShift(0.0f);	
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public double getCurrentShift() { return currentShift; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setBpmShift(float floatValue) {
		setBpmShift(floatValue, true);
	}
	public void setBpmShift(float floatValue, boolean triggerStage) {
		currentShift = floatValue;
		int value = -(int)Math.round(floatValue * 100);
		setValue(value);
		setBpmText();
		if (triggerStage) {
			stageWidget.updateKey();
			stageWidget.setBpmShift(floatValue);
		}
	}
	
	private void setBpmText() {
		try {
			double bpmShift = currentShift; 
			bpmShift = Math.round(bpmShift*100.0) / 100.0; // round it	
			if (bpmShift > 0.0) {
				bpmEdit.setText("+" + bpmShift + "%");
			} else if (bpmShift < 0.0) {
				bpmEdit.setText(bpmShift + "%");
			} else {
				bpmEdit.setText("0%");
			}
		} catch (Exception e) {
			log.error("setBpmText(): error", e);
		}		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected void bpmChanged(Integer sliderVal) {
		currentShift = -((double)sliderVal) / 100;
		setBpmText();
		stageWidget.updateKey();
		stageWidget.setBpmShift((float)currentShift);
	}
	
	protected void bpmSliderReleased() {
		stageWidget.keyBpmChangeEvent();
	}
	
	public void updateBpmSliderRanges(String value) {	
		if (currentLabels != null) {
			for (QWidget widget : currentLabels) {
				labelLayout.removeWidget(widget);
				widget.close();
			}
		}
		Vector<QWidget> newLabels = new Vector<QWidget>();
        if (value.equals("4%")) {
        	currentScale = 4;        	
        	setRange(-400, 400);
        	setTickInterval(50);
        	setSingleStep(25);
            //bpmslider.setMajorTickSpacing(100);
            //bpmslider.setMinorTickSpacing(50);
        	newLabels.add(new QLabel("-4"));
        	newLabels.add(new QLabel("-3"));
        	newLabels.add(new QLabel("-2"));
        	newLabels.add(new QLabel("-1"));
        	newLabels.add(new QLabel("0"));
        	newLabels.add(new QLabel("+1"));
        	newLabels.add(new QLabel("+2"));
        	newLabels.add(new QLabel("+3"));
        	newLabels.add(new QLabel("+4"));            	
        } else if (value.equals("8%")) {
        	currentScale = 8;
        	setRange(-800, 800);
        	setTickInterval(100);
        	setSingleStep(50);            	
            //bpmslider.setMajorTickSpacing(200);
            //bpmslider.setMinorTickSpacing(100);
        	newLabels.add(new QLabel("-8"));
        	newLabels.add(new QLabel("-6"));
        	newLabels.add(new QLabel("-4"));
        	newLabels.add(new QLabel("-2"));
        	newLabels.add(new QLabel("0"));
        	newLabels.add(new QLabel("+2"));
        	newLabels.add(new QLabel("+4"));
        	newLabels.add(new QLabel("+6"));
        	newLabels.add(new QLabel("+8"));
        } else if (value.equals("10%")) {
        	currentScale = 10;
        	setRange(-1000, 1000);
        	setTickInterval(100);
        	setSingleStep(50);            	            	            	
            //bpmslider.setMajorTickSpacing(200);
            //bpmslider.setMinorTickSpacing(100);
        	newLabels.add(new QLabel("-10"));
        	newLabels.add(new QLabel("-8"));
        	newLabels.add(new QLabel("-6"));
        	newLabels.add(new QLabel("-4"));
        	newLabels.add(new QLabel("-2"));
        	newLabels.add(new QLabel("0"));
        	newLabels.add(new QLabel("+2"));
        	newLabels.add(new QLabel("+4"));
        	newLabels.add(new QLabel("+6"));
        	newLabels.add(new QLabel("+8"));
        	newLabels.add(new QLabel("+10"));        	
        } else if (value.equals("20%")) {
        	currentScale = 20;
        	setRange(-2000, 2000);

        	setTickInterval(200);
        	setSingleStep(100);            	            	            	
            //bpmslider.setMajorTickSpacing(400);
            //bpmslider.setMinorTickSpacing(200);
        	newLabels.add(new QLabel("-20"));
        	newLabels.add(new QLabel("-16"));
        	newLabels.add(new QLabel("-12"));
        	newLabels.add(new QLabel("-8"));
        	newLabels.add(new QLabel("-4"));
        	newLabels.add(new QLabel("0"));
        	newLabels.add(new QLabel("+4"));
        	newLabels.add(new QLabel("+8"));
        	newLabels.add(new QLabel("+12"));
        	newLabels.add(new QLabel("+16"));
        	newLabels.add(new QLabel("+20"));          	
        } else if (value.equals("50%")) {
        	currentScale = 50;
        	setRange(-5000, 5000);
        	setTickInterval(500);
        	setSingleStep(250);            	            	            	            	
            //bpmslider.setMajorTickSpacing(1000);
            //bpmslider.setMinorTickSpacing(500);
        	newLabels.add(new QLabel("-50"));
        	newLabels.add(new QLabel("-40"));
        	newLabels.add(new QLabel("-30"));
        	newLabels.add(new QLabel("-20"));
        	newLabels.add(new QLabel("-10"));
        	newLabels.add(new QLabel("0"));
        	newLabels.add(new QLabel("+10"));
        	newLabels.add(new QLabel("+20"));
        	newLabels.add(new QLabel("+30"));
        	newLabels.add(new QLabel("+40"));
        	newLabels.add(new QLabel("+50"));        	
        }  		
        if (currentShift < -currentScale) {
        	currentShift = -currentScale;
        	setBpmText();
        }
        if (currentShift > currentScale) {
        	currentShift = currentScale;
        	setBpmText();
        }
        for (QWidget widget : newLabels) {
        	labelLayout.addWidget(widget);
        	labelLayout.setAlignment(widget, AlignmentFlag.AlignRight);
        }
        currentLabels = newLabels;
	}	
	
}
