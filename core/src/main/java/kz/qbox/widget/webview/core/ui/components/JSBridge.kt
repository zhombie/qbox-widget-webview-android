package kz.qbox.widget.webview.core.ui.components

import android.webkit.JavascriptInterface
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
    fun onClose(): Boolean =
        listener?.onClose() == true

    @JavascriptInterface
    fun onChangeLanguage(language: String): Boolean =
        listener?.onChangeLanguage(language) == true

//    @JavascriptInterface
//    fun onJavaScriptLogging(message: String) {
//        listener?.onLogMessageReceived(message)
//    }

    @JavascriptInterface
    fun onLifecycleState(state: String) {
        Lifecycle.State.of(state)?.let {
            listener?.onLifecycleState(it)
        }
    }

    fun dispose() {
        listener = null
    }

    interface Listener {
        //        fun onLogMessageReceived(message: String)
        fun onLifecycleState(state: Lifecycle.State)
        fun onChangeLanguage(language: String): Boolean
        fun onClose(): Boolean
    }

}