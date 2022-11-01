package kz.qbox.widget.webview.core.ui.components

import android.app.Activity
import android.app.AlertDialog
import kz.qbox.widget.webview.core.R

class DownloadingProgressView constructor(private val activity: Activity) {
    private var dialog: AlertDialog? = null

    fun showProgress(fileName: String){
        val builder = AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.info_files_download_started))
            .setMessage(activity.getString(R.string.label_files_download_in_progress, fileName))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        val layoutInflater = activity.layoutInflater
        builder.setView(layoutInflater.inflate(R.layout.progress_bar_layout, null))
        dialog = builder.create()
        dialog?.show()
    }

    fun dismissProgressBar(){
        dialog?.dismiss()
    }
}