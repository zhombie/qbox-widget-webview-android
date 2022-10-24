package kz.qbox.widget.webview.core.multimedia.selection

import android.net.Uri

internal fun interface GetContentDelegate {
    sealed interface Result {
        data class Success constructor(
            val uri: Uri
        ) : Result

        sealed interface Error : Result {
            object Undefined : Error
            object NullableUri : Error
            data class SizeLimitExceeds constructor(
                val maxSize: Int  // in megabytes
            ) : Error
        }
    }

    fun onContentResult(result: Result)
}
