package kz.qbox.widget.webview.core.ui.components

import android.webkit.JavascriptInterface
import kz.qbox.widget.webview.core.Logger
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

    @JavascriptInterface
    fun onLifecycleState(state: String) {
        Logger.debug(
            "onLifecycleState()",
            "${Lifecycle.State.FINISHED}, ${Lifecycle.State.STARTED}"
        )
        when (state) {
            "start" -> Lifecycle.State.of("STARTED")?.let {
                listener?.onLifecycleState(it)
            }
            "finish" -> Lifecycle.State.of("FINISHED")?.let {
                listener?.onLifecycleState(it)
            }
        }
    }

    fun dispose() {
        listener = null
    }

    interface Listener {
        fun onLifecycleState(state: Lifecycle.State)
        fun onChangeLanguage(language: String): Boolean
        fun onClose(): Boolean
    }

}