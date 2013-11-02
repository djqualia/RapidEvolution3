/********************************************************************************
** Form generated from reading ui file 'SelectColumnsDialog.jui'
**
** Created: Tue Nov 3 12:45:50 2009
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package com.mixshare.rapid_evolution.ui.dialogs.columns;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_SelectColumnsDialog implements com.trolltech.qt.QUiForm<QDialog>
{
    public QDialogButtonBox buttonBox;
    public QTreeView columnTreeView;
    public QWidget horizontalLayoutWidget;
    public QHBoxLayout horizontalLayout;
    public QPushButton searchButton;
    public QLineEdit columnSearchText;

    public Ui_SelectColumnsDialog() { super(); }

    public void setupUi(QDialog SelectColumnsDialog)
    {
        SelectColumnsDialog.setObjectName("SelectColumnsDialog");
        SelectColumnsDialog.resize(new QSize(440, 530).expandedTo(SelectColumnsDialog.minimumSizeHint()));
        QSizePolicy sizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy.setHorizontalStretch((byte)0);
        sizePolicy.setVerticalStretch((byte)0);
        sizePolicy.setHeightForWidth(SelectColumnsDialog.sizePolicy().hasHeightForWidth());
        SelectColumnsDialog.setSizePolicy(sizePolicy);
        buttonBox = new QDialogButtonBox(SelectColumnsDialog);
        buttonBox.setObjectName("buttonBox");
        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
        buttonBox.setCenterButtons(true);
        columnTreeView = new QTreeView(SelectColumnsDialog);
        columnTreeView.setObjectName("columnTreeView");
        columnTreeView.setGeometry(new QRect(20, 50, 401, 421));
        horizontalLayoutWidget = new QWidget(SelectColumnsDialog);
        horizontalLayoutWidget.setObjectName("horizontalLayoutWidget");
        horizontalLayoutWidget.setGeometry(new QRect(20, 10, 401, 41));
        horizontalLayout = new QHBoxLayout(horizontalLayoutWidget);
        horizontalLayout.setSpacing(4);
        horizontalLayout.setObjectName("horizontalLayout");
        horizontalLayout.setSizeConstraint(com.trolltech.qt.gui.QLayout.SizeConstraint.SetDefaultConstraint);
        searchButton = new QPushButton(horizontalLayoutWidget);
        searchButton.setObjectName("searchButton");
        searchButton.setEnabled(false);

        horizontalLayout.addWidget(searchButton);

        columnSearchText = new QLineEdit(horizontalLayoutWidget);
        columnSearchText.setObjectName("columnSearchText");

        horizontalLayout.addWidget(columnSearchText);

        retranslateUi(SelectColumnsDialog);
        buttonBox.clicked.connect(SelectColumnsDialog, "accept()");

        SelectColumnsDialog.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog SelectColumnsDialog)
    {
        SelectColumnsDialog.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("SelectColumnsDialog", "Column Selection", null));
        searchButton.setText("");
    } // retranslateUi

}

