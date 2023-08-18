package kz.qbox.widget.webview.core.utils

import android.os.Build
import android.os.Bundle

internal object BundleCompat {
    inline fun <reified T : Enum<T>> getEnum(
        arguments: Bundle?,
        name: String
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(name) as? T
        }
    }

    inline fun <reified T : java.io.Serializable> getSerializable(
        arguments: Bundle?,
        name: String
    ): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(name) as? T
        }
    }
}