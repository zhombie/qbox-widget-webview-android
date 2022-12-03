package kz.qbox.widget.webview.core.ui.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kz.qbox.widget.webview.core.R

fun AppCompatActivity.showProgress(fileName: String): AlertDialog {
    val dialog = AlertDialog.Builder(this)
        .setTitle(getString(R.string.qbox_widget_info_files_download_started))
        .setMessage(getString(R.string.qbox_widget_label_files_download_in_progress, fileName))
        .setNegativeButton(R.string.qbox_widget_cancel) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
    dialog.show()
    return dialog
}