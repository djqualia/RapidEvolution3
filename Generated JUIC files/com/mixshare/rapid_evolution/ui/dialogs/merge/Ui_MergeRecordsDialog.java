/********************************************************************************
** Form generated from reading ui file 'MergeRecordsDialog.jui'
**
** Created: Tue Nov 3 12:45:50 2009
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package com.mixshare.rapid_evolution.ui.dialogs.merge;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_MergeRecordsDialog implements com.trolltech.qt.QUiForm<QDialog>
{
    public QDialogButtonBox buttonBox;
    public QLabel label;
    public QTreeView recordsView;

    public Ui_MergeRecordsDialog() { super(); }

    public void setupUi(QDialog MergeRecordsDialog)
    {
        MergeRecordsDialog.setObjectName("MergeRecordsDialog");
        MergeRecordsDialog.resize(new QSize(671, 316).expandedTo(MergeRecordsDialog.minimumSizeHint()));
        buttonBox = new QDialogButtonBox(MergeRecordsDialog);
        buttonBox.setObjectName("buttonBox");
        buttonBox.setGeometry(new QRect(20, 260, 631, 51));
        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Cancel,com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
        buttonBox.setCenterButtons(true);
        label = new QLabel(MergeRecordsDialog);
        label.setObjectName("label");
        label.setGeometry(new QRect(30, 10, 611, 31));
        label.setAlignment(com.trolltech.qt.core.Qt.AlignmentFlag.createQFlags(com.trolltech.qt.core.Qt.AlignmentFlag.AlignCenter));
        recordsView = new QTreeView(MergeRecordsDialog);
        recordsView.setObjectName("recordsView");
        recordsView.setGeometry(new QRect(25, 51, 621, 201));
        retranslateUi(MergeRecordsDialog);
        buttonBox.accepted.connect(MergeRecordsDialog, "accept()");
        buttonBox.rejected.connect(MergeRecordsDialog, "reject()");

        MergeRecordsDialog.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog MergeRecordsDialog)
    {
        MergeRecordsDialog.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MergeRecordsDialog", "Dialog", null));
        label.setText(com.trolltech.qt.core.QCoreApplication.translate("MergeRecordsDialog", "Select the primary item to merge the records into:", null));
    } // retranslateUi

}

