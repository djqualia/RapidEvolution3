/********************************************************************************
** Form generated from reading ui file 'TestWidget.jui'
**
** Created: Thu Dec 3 20:42:22 2009
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package com.mixshare.rapid_evolution.ui.dialogs.test;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_TestWidget implements com.trolltech.qt.QUiForm<QWidget>
{
    public QWidget horizontalLayoutWidget;
    public QHBoxLayout horizontalLayout;
    public QLabel label_2;
    public QSpacerItem horizontalSpacer;
    public QLabel label;

    public Ui_TestWidget() { super(); }

    public void setupUi(QWidget TestWidget)
    {
        TestWidget.setObjectName("TestWidget");
        TestWidget.resize(new QSize(400, 300).expandedTo(TestWidget.minimumSizeHint()));
        horizontalLayoutWidget = new QWidget(TestWidget);
        horizontalLayoutWidget.setObjectName("horizontalLayoutWidget");
        horizontalLayoutWidget.setGeometry(new QRect(39, 219, 331, 31));
        horizontalLayout = new QHBoxLayout(horizontalLayoutWidget);
        horizontalLayout.setObjectName("horizontalLayout");
        label_2 = new QLabel(horizontalLayoutWidget);
        label_2.setObjectName("label_2");

        horizontalLayout.addWidget(label_2);

        horizontalSpacer = new QSpacerItem(40, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        horizontalLayout.addItem(horizontalSpacer);

        label = new QLabel(horizontalLayoutWidget);
        label.setObjectName("label");

        horizontalLayout.addWidget(label);

        retranslateUi(TestWidget);

        TestWidget.connectSlotsByName();
    } // setupUi

    void retranslateUi(QWidget TestWidget)
    {
        TestWidget.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("TestWidget", "Form", null));
        label_2.setText(com.trolltech.qt.core.QCoreApplication.translate("TestWidget", "TextLabel", null));
        label.setText(com.trolltech.qt.core.QCoreApplication.translate("TestWidget", "TextLabel", null));
    } // retranslateUi

}

