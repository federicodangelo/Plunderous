package com.fangelo.plunderous.client.ui.dialog.confirm

import com.fangelo.libraries.ui.Dialog
import com.fangelo.libraries.ui.DialogResult

class ConfirmDialog(title: String, text: String) : Dialog(title) {

    init {

        text(text)

        button("Yes", DialogResult.Yes)
        button("No", DialogResult.No)
    }
}
