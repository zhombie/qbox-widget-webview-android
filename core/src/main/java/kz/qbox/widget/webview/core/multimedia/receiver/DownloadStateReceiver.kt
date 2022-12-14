package kz.qbox.widget.webview.core.multimedia.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import kz.qbox.widget.webview.core.Logger

internal class DownloadStateReceiver constructor(
    private val listener: Listener
) : BroadcastReceiver() {

    companion object {
        private val TAG = DownloadStateReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.debug(TAG, "onReceive() -> context: $context, intent: $intent")

        if (context == null) return
        val action = intent?.action
        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId < 0) return

            val downloadManager =
                ContextCompat.getSystemService(context, DownloadManager::class.java) ?: return

            val cursor = downloadManager.query(
                DownloadManager.Query()
                    .setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1))
            )

            Logger.debug(TAG, "onReceive() -> cursor: $cursor")

            if (cursor.moveToFirst()) {
                if (cursor.count > 0) {
                    val status =
                        cursor.getIntOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                    Logger.debug(TAG, "onReceive() -> status: $status")

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val localUri =
                            cursor.getStringOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                        val mimeType =
                            downloadManager.getMimeTypeForDownloadedFile(downloadId)
                        Logger.debug(
                            TAG, "onReceive() -> " +
                                    "downloadId: $downloadId, localUri: $localUri"
                        )
                        listener.onFileUriReady(downloadId, Uri.parse(localUri), mimeType)
                    }
                }
            }

            cursor.close()
        }
    }

    fun interface Listener {
        fun onFileUriReady(downloadId: Long, uri: Uri?, mimeType: String?)
    }

}
