package kz.qbox.widget.webview.core.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import kz.qbox.widget.webview.core.R
import kz.qbox.widget.webview.core.ui.components.LinearProgressIndicator

internal class DownloadProgressDialog constructor(
    context: Context,
    cancelable: Boolean = true,
    cancelListener: DialogInterface.OnCancelListener? = null,
    params: Params
) : AlertDialog(context, cancelable, cancelListener) {

    data class Params constructor(
        val fileName: String
    )

    private val progressView by lazy(LazyThreadSafetyMode.NONE) {
        LinearProgressIndicator(context)
    }

    var progress: Double = 0.0
        set(value) {
            field = value
            progressView.setProgress(value.toInt())
        }

    init {
        setTitle(R.string.qbox_widget_info_files_download_started)
        setMessage(
            context.getString(
                R.string.qbox_widget_label_files_download_in_progress,
                params.fileName
            )
        )
        setView(progressView)
    }

}
