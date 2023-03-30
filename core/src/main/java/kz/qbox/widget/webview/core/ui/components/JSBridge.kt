package kz.qbox.widget.webview.core.ui.components

import android.webkit.JavascriptInterface
import findEnumByName
import kz.qbox.widget.webview.core.models.*
import kz.qbox.widget.webview.core.utils.encode

class JSBridge constructor(
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
    fun onCallState(state: String) {
        findEnumByName<CallState>(state)?.let {
            listener?.onCallState(it)
        }
    }

    @JavascriptInterface
    fun onLanguageSet(language: String): Boolean =
        listener?.onLanguageSet(language) == true

    @JavascriptInterface
    fun onClose(): Boolean =
        listener?.onClose() == true

    fun dispose() {
        listener = null
    }

    interface Listener {
        fun onCallState(state: CallState)
        fun onLanguageSet(language: String): Boolean
        fun onClose(): Boolean
    }

}