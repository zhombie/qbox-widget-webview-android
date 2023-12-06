package kz.qbox.widget.webview.core.sdk

import android.content.Intent
import android.os.Build

internal object IntentCompat {

    inline fun <reified T : Enum<T>> getEnum(
        intent: Intent,
        name: String
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(name) as? T
        }
    }

    inline fun <reified T : java.io.Serializable> getSerializable(
        intent: Intent,
        name: String
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(name) as? T
        }
    }

}