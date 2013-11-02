package com.mixshare.rapid_evolution.ui.widgets.common.rating;

import com.mixshare.rapid_evolution.music.rating.Rating;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QWidget;

public class StarEditor extends QWidget 
{
    private StarRating starRating;

    public StarEditor(QWidget parent, StarRating rating) {
        super(parent);

        starRating = rating;
        setMouseTracking(true);
        setAutoFillBackground(true);
    }

    @Override
    public QSize sizeHint() {
        return starRating.sizeHint();
    }

    @Override
    public void paintEvent(QPaintEvent event) {
        QPainter painter = new QPainter(this);
        starRating.paint(painter, rect(), palette(), StarRating.ReadWrite);
    }

    @Override
    public void mouseMoveEvent(QMouseEvent event)  {
        int star = starAtPosition(event.x());
        if ((star != starRating.getRating().getRatingStars()) && (star >= 0)) {
            starRating.setRating(Rating.getRating(star * 20));
            update();
        }
    }

    public int starAtPosition(int x) {
        int star = (x / (starRating.sizeHint().width()
                        / starRating.getMaxRating())) + 1;
        if (star < 0 || star > starRating.getMaxRating())
            return -1;
        return star;
    }

    public void setStarRating(StarRating rating) {
        starRating = rating;
    }

    public StarRating starRating() {
        return starRating;
    }
    
}