package kz.qbox.widget.webview.core.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kz.qbox.widget.webview.core.R

fun AppCompatActivity.showError(url: String): AlertDialog =
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.qbox_widget_attention))
        .setMessage(getString(R.string.qbox_widget_alert_message_error_occurred))
        .setPositiveButton(getString(R.string.qbox_widget_copy)) { dialog, _ ->
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("url", url))
            dialog.dismiss()
        }
        .setNegativeButton(getString(R.string.qbox_widget_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
