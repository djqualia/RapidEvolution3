package com.mixshare.rapid_evolution.ui.widgets.common.rating;

import java.io.Serializable;

import com.mixshare.rapid_evolution.music.rating.Rating;
import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPolygonF;

public class StarRating implements Comparable<StarRating>, Serializable {
    
    static private final long serialVersionUID = 0L;    	
	
	static public final int ReadOnly = 0, ReadWrite = 1, PaintingFactor = 20;
	static private int maxCount = 5;
	static private QPolygonF starPolygon, diamondPolygon;

    static {
        starPolygon = new QPolygonF();
        for (int i = 0; i < 5; i++) {
            starPolygon.append(
                    new QPointF(0.5 + 0.5 * Math.sin(0.8 * (i + 1) * Math.PI),
                                0.5 - 0.5 * Math.cos(0.8 * (i + 1) * Math.PI)));
        	
        }
        diamondPolygon = new QPolygonF();
        diamondPolygon.append(new QPointF(0.4, 0.5));
        diamondPolygon.append(new QPointF(0.5, 0.4));
        diamondPolygon.append(new QPointF(0.6, 0.5));
        diamondPolygon.append(new QPointF(0.5, 0.6));
        diamondPolygon.append(new QPointF(0.4, 0.5));
    }
	
	////////////
	// FIELDS //
	////////////
	
	private Rating rating;

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public StarRating() { }
    public StarRating(Rating rating) {
        setRating(rating);
    }

    /////////////
    // GETTERS //
    /////////////
    
    public Rating getRating() { return rating; }
    public int getMaxRating() { return maxCount; }
    
    /////////////
    // SETTERS //
    /////////////
    
    public void setRating(Rating rating) { this.rating = rating; }
    
    /////////////
    // METHODS //
    /////////////
    
    public int compareTo(StarRating s) {
		if (rating.getRatingValue() < s.rating.getRatingValue())
			return 1;
		if (rating.getRatingValue() > s.rating.getRatingValue())
			return -1;
    	return 0;
    }

    public void paint(QPainter painter, QRect rect, QPalette palette, int mode) {
    	
        painter.save();

        painter.setRenderHint(QPainter.RenderHint.Antialiasing, true);
        painter.setPen(Qt.PenStyle.NoPen);

        if (mode == ReadWrite)
            painter.setBrush(palette.highlight());
        else
            painter.setBrush(palette.text());

        int yOffset = (rect.height() - PaintingFactor) / 2;
        painter.translate(rect.x(), rect.y() + yOffset);
        painter.scale(PaintingFactor, PaintingFactor);
        
        for (int i = 0; i < maxCount; i++) {
            if (i < rating.getRatingStars())
                painter.drawPolygon(starPolygon, Qt.FillRule.WindingFill);
            else
                painter.drawPolygon(diamondPolygon, Qt.FillRule.WindingFill);
            painter.translate(1.0, 0.0);
        }
        
        painter.restore();
    }

    public QSize sizeHint() {
        return new QSize(PaintingFactor * maxCount, PaintingFactor);
    }

    public String toString() {
    	return String.valueOf(rating.getRatingStars());
    }
    
}