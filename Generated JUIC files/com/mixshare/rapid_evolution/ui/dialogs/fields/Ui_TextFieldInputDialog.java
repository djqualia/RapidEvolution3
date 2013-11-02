/********************************************************************************
** Form generated from reading ui file 'DegreeFieldInputDialog.jui'
**
** Created: Fri Jan 29 11:44:39 2010
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package com.mixshare.rapid_evolution.ui.dialogs.fields;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_TextFieldInputDialog implements com.trolltech.qt.QUiForm<QDialog>
{
    public QDialogButtonBox buttonBox;
    public QWidget horizontalLayoutWidget;
    public QHBoxLayout horizontalLayout;
    public QLabel label;
    public QLineEdit fieldValue;

    public Ui_TextFieldInputDialog() { super(); }

    public void setupUi(QDialog TextFieldInputDialog)
    {
        TextFieldInputDialog.setObjectName("TextFieldInputDialog");
        TextFieldInputDialog.resize(new QSize(551, 130).expandedTo(TextFieldInputDialog.minimumSizeHint()));
        QSizePolicy sizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy.setHorizontalStretch((byte)0);
        sizePolicy.setVerticalStretch((byte)0);
        sizePolicy.setHeightForWidth(TextFieldInputDialog.sizePolicy().hasHeightForWidth());
        TextFieldInputDialog.setSizePolicy(sizePolicy);
        buttonBox = new QDialogButtonBox(TextFieldInputDialog);
        buttonBox.setObjectName("buttonBox");
        buttonBox.setGeometry(new QRect(20, 80, 511, 31));
        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Cancel,com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
        buttonBox.setCenterButtons(true);
        horizontalLayoutWidget = new QWidget(TextFieldInputDialog);
        horizontalLayoutWidget.setObjectName("horizontalLayoutWidget");
        horizontalLayoutWidget.setGeometry(new QRect(20, 20, 511, 41));
        horizontalLayout = new QHBoxLayout(horizontalLayoutWidget);
        horizontalLayout.setObjectName("horizontalLayout");
        label = new QLabel(horizontalLayoutWidget);
        label.setObjectName("label");

        horizontalLayout.addWidget(label);

        fieldValue = new QLineEdit(horizontalLayoutWidget);
        fieldValue.setObjectName("fieldValue");

        horizontalLayout.addWidget(fieldValue);

        retranslateUi(TextFieldInputDialog);
        buttonBox.accepted.connect(TextFieldInputDialog, "accept()");
        buttonBox.rejected.connect(TextFieldInputDialog, "reject()");

        TextFieldInputDialog.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog TextFieldInputDialog)
    {
        TextFieldInputDialog.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("TextFieldInputDialog", "Set Field", null));
        label.setText(com.trolltech.qt.core.QCoreApplication.translate("TextFieldInputDialog", "Filter Name:", null));
        fieldValue.setText("");
    } // retranslateUi

}

