package com.mixshare.rapid_evolution.ui.util;

import com.trolltech.qt.gui.QColor;

public class Color {

	private float red;
	private float green;
	private float blue;
	
	public Color(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	public Color(QColor color) {
		this.red = (float)color.redF();
		this.green = (float)color.greenF();
		this.blue = (float)color.blueF();		
	}
	
	public float getRed() {
		return red;
	}
	public void setRed(float red) {
		this.red = red;
	}
	
	public float getGreen() {
		return green;
	}
	public void setGreen(float green) {
		this.green = green;
	}
	
	public float getBlue() {
		return blue;
	}
	public void setBlue(float blue) {
		this.blue = blue;
	}
	
	public QColor getQColor() {
		QColor result = new QColor();
		result.setRedF(red);
		result.setGreenF(green);
		result.setBlueF(blue);
		return result;
	}
	
	public Color getLinearGradient(Color baseColor, float degree) {
		return new Color(getRed() * degree + baseColor.getRed() * (1.0f - degree),
					getGreen() * degree + baseColor.getGreen() * (1.0f - degree),
					getBlue() * degree + baseColor.getBlue() * (1.0f - degree)
				);
	}
	
}
