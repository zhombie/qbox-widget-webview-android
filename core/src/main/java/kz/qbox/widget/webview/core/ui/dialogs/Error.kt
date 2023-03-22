package kz.qbox.widget.webview.core.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kz.qbox.widget.webview.core.R

fun AppCompatActivity.showError(url: String): AlertDialog {
    val linkMessage = TextView(this).apply {
        setPadding(65, 0, 65, 0)
        setTextColor(Color.BLACK)
        textSize = 15f
        isClickable = true
        movementMethod = LinkMovementMethod.getInstance()
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml("<a href='$url'>$url</a>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml("<a href='$url'>$url</a>")
        }
    }

    return AlertDialog.Builder(this)
        .setTitle(getString(R.string.qbox_widget_attention))
        .setMessage(getString(R.string.qbox_widget_alert_message_error_occurred))
        .setView(linkMessage)
        .setPositiveButton(getString(R.string.qbox_widget_copy)) { dialog, _ ->
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(ClipData.newPlainText("url", url))
            dialog.dismiss()
        }
        .setNegativeButton(getString(R.string.qbox_widget_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
}
