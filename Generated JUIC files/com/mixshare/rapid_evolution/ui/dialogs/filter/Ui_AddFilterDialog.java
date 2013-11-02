/********************************************************************************
** Form generated from reading ui file 'AddFilterDialog.jui'
**
** Created: Tue Nov 3 12:45:50 2009
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package com.mixshare.rapid_evolution.ui.dialogs.filter;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_AddFilterDialog implements com.trolltech.qt.QUiForm<QDialog>
{
    public QDialogButtonBox buttonBox;
    public QWidget horizontalLayoutWidget;
    public QHBoxLayout horizontalLayout;
    public QLabel label;
    public QLineEdit filterName;

    public Ui_AddFilterDialog() { super(); }

    public void setupUi(QDialog AddFilterDialog)
    {
        AddFilterDialog.setObjectName("AddFilterDialog");
        AddFilterDialog.resize(new QSize(551, 130).expandedTo(AddFilterDialog.minimumSizeHint()));
        QSizePolicy sizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy.setHorizontalStretch((byte)0);
        sizePolicy.setVerticalStretch((byte)0);
        sizePolicy.setHeightForWidth(AddFilterDialog.sizePolicy().hasHeightForWidth());
        AddFilterDialog.setSizePolicy(sizePolicy);
        buttonBox = new QDialogButtonBox(AddFilterDialog);
        buttonBox.setObjectName("buttonBox");
        buttonBox.setGeometry(new QRect(20, 80, 511, 31));
        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Cancel,com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
        buttonBox.setCenterButtons(true);
        horizontalLayoutWidget = new QWidget(AddFilterDialog);
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

        retranslateUi(AddFilterDialog);
        buttonBox.accepted.connect(AddFilterDialog, "accept()");
        buttonBox.rejected.connect(AddFilterDialog, "reject()");

        AddFilterDialog.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog AddFilterDialog)
    {
        AddFilterDialog.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDialog", "Add Filter", null));
        label.setText(com.trolltech.qt.core.QCoreApplication.translate("AddFilterDialog", "Filter Name:", null));
        filterName.setText("");
    } // retranslateUi

}

