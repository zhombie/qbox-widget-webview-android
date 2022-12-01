package kz.qbox.widget.webview.core.ui.components

import android.webkit.JavascriptInterface
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.Device
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.core.utils.encode

internal class JSBridge constructor(
    private val device: Device,
    private val call: Call?,
    private val user: User?,
    private val dynamicAttrs: DynamicAttrs?,
    private var listener: Listener? = null
) {

    @JavascriptInterface
    fun getDevice(): String = device.encode()

    @JavascriptInterface
    fun getCall(): String? = call?.encode()

    @JavascriptInterface
    fun getUser(): String? = user?.encode()

    @JavascriptInterface
    fun getExtra(): String? = dynamicAttrs?.encode()

    @JavascriptInterface
    fun onClose(): Boolean =
        listener?.onClose() == true

    @JavascriptInterface
    fun onChangeLanguage(language: String): Boolean =
        listener?.onChangeLanguage(language) == true

    fun dispose() {
        listener = null
    }

    interface Listener {
        fun onChangeLanguage(language: String): Boolean
        fun onClose(): Boolean
    }

}