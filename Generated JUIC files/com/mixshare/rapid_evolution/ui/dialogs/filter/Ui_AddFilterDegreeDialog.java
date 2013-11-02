/********************************************************************************
** Form generated from reading ui file 'AddFilterDegreeDialog.jui'
**
** Created: Mon Feb 8 22:20:23 2010
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package com.mixshare.rapid_evolution.ui.dialogs.filter;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_AddFilterDegreeDialog implements com.trolltech.qt.QUiForm<QDialog>
{
    public QWidget horizontalLayoutWidget;
    public QHBoxLayout horizontalLayout;
    public QLabel label;
    public QLineEdit filterName;
    public QLabel label_2;
    public QSlider horizontalSlider;
    public QWidget horizontalLayoutWidget_2;
    public QHBoxLayout horizontalLayout_2;
    public QPushButton moreButton;
    public QPushButton okButton;
    public QPushButton cancelButton;

    public Ui_AddFilterDegreeDialog() { super(); }

    public void setupUi(QDialog AddFilterDegreeDialog)
    {
        AddFilterDegreeDialog.setObjectName("AddFilterDegreeDialog");
        AddFilterDegreeDialog.resize(new QSize(551, 130).expandedTo(AddFilterDegreeDialog.minimumSizeHint()));
        QSizePolicy sizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy.setHorizontalStretch((byte)0);
        sizePolicy.setVerticalStretch((byte)0);
        sizePolicy.setHeightForWidth(AddFilterDegreeDialog.sizePolicy().hasHeightForWidth());
        AddFilterDegreeDialog.setSizePolicy(sizePolicy);
        horizontalLayoutWidget = new QWidget(AddFilterDegreeDialog);
        horizontalLayoutWidget.setObjectName("horizontalLayoutWidget");
        horizontalLayoutWidget.setGeometry(new QRect(20, 20, 511, 41));
        horizontalLayout = new QHBoxLayout(horizontalLayoutWidget);
        horizontalLayout.setObjectName("horizontalLayout");
        label = new QLabel(horizontalLayoutWidget);
        label.setObjectName("label");

        horizontalLayout.addWidget(label);

        filterName = new QLineEdit(horizontalLayoutWidget);
        filterName.setObjectName("filterName");

        horizontalLayout.addWidget(filterName);

        label_2 = new QLabel(horizontalLayoutWidget);
        label_2.setObjectName("label_2");

        horizontalLayout.addWidget(label_2);

        horizontalSlider = new QSlider(horizontalLayoutWidget);
        horizontalSlider.setObjectName("horizontalSlider");
        horizontalSlider.setOrientation(com.trolltech.qt.core.Qt.Orientation.Horizontal);

        horizontalLayout.addWidget(horizontalSlider);

        horizontalLayoutWidget_2 = new QWidget(AddFilterDegreeDialog);
        horizontalLayoutWidget_2.setObjectName("horizontalLayoutWidget_2");
        horizontalLayoutWidget_2.setGeometry(new QRect(110, 70, 331, 51));
        horizontalLayout_2 = new QHBoxLayout(horizontalLayoutWidget_2);
        horizontalLayout_2.setSpacing(6);
        horizontalLayout_2.setObjectName("horizontalLayout_2");
        moreButton = new QPushButton(horizontalLayoutWidget_2);
        moreButton.setObjectName("moreButton");
        moreButton.setMaximumSize(new QSize(100, 16777215));

        horizontalLayout_2.addWidget(moreButton);

        okButton = new QPushButton(horizontalLayoutWidget_2);
        okButton.setObjectName("okButton");
        okButton.setMaximumSize(new QSize(100, 16777215));

        horizontalLayout_2.addWidget(okButton);

        cancelButton = new QPushButton(horizontalLayoutWidget_2);
        cancelButton.setObjectName("cancelButton");
        cancelButton.setMaximumSize(new QSize(100, 16777215));

        horizontalLayout_2.addWidget(cancelButton);

        retranslateUi(AddFilterDegreeDialog);
        cancelButton.clicked.connect(AddFilterDegreeDialog, "close()");
        okButton.clicked.connect(AddFilterDegreeDialog, "accept()");

        AddFilterDegreeDialog.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog AddFilterDegreeDialog)
    {
        AddFilterDegreeDialog.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDegreeDialog", "Add FilterDegree", null));
        label.setText(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDegreeDialog", "FilterDegree Name:", null));
        filterName.setText("");
        label_2.setText(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDegreeDialog", "Degree:", null));
        moreButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDegreeDialog", "More", null));
        okButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDegreeDialog", "OK", null));
        cancelButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDegreeDialog", "Cancel", null));
    } // retranslateUi

}

