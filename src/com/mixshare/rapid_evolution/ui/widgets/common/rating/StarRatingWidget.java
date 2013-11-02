package com.mixshare.rapid_evolution.ui.widgets.common.rating;

import com.mixshare.rapid_evolution.music.rating.Rating;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QListView;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStandardItemModel;

public class StarRatingWidget extends QListView {
	
	static public final int RATING_HEIGHT = 25;
	static public final int RATING_WIDTH = 105;
	
	private StarRatingChangedListener changeListener;
	
	public StarRatingWidget(StarRatingChangedListener changeListener, Rating rating) {
		super();
		this.changeListener = changeListener;
		
    	QSizePolicy viewSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	setSizePolicy(viewSizePolicy);
    	setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
    	setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);	    	
        setModel(new QStandardItemModel());
        setItemDelegate(new StarRatingDelegate(this, changeListener));
        model().insertColumn(0);
        model().insertRow(0);	        
        setMaximumSize(RATING_WIDTH, RATING_HEIGHT);		
        setAutoFillBackground(true);		
        model().setData(model().index(0,0), new StarRating(rating));			
	}
	
	public Rating getRating() {
		StarRating rating = (StarRating)model().data(model().index(0, 0));
		return rating.getRating();
	}
	
}
