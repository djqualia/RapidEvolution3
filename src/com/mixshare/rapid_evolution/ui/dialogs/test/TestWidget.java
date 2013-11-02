package com.mixshare.rapid_evolution.ui.dialogs.test;

import com.trolltech.qt.gui.*;

public class TestWidget extends QWidget {

    Ui_TestWidget ui = new Ui_TestWidget();

    public static void main(String[] args) {
        QApplication.initialize(args);

        TestWidget testTestWidget = new TestWidget();
        testTestWidget.show();

        QApplication.exec();
    }

    public TestWidget() {
        ui.setupUi(this);
    }

    public TestWidget(QWidget parent) {
        super(parent);
        ui.setupUi(this);
    }
}
