package kz.qbox.widget.webview.core.ui.components

import android.webkit.JavascriptInterface
import findEnumByName
import kz.qbox.widget.webview.core.Logger
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.CallState
import kz.qbox.widget.webview.core.models.Device
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.User
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
        Logger.debug("JSBridge", "onCallState() -> state: $state")
        findEnumByName<CallState>(state)?.let {
            listener?.onCallState(it)
        }
    }

    @JavascriptInterface
    fun onLanguageSet(language: String): Boolean =
        listener?.onLanguageSet(language) == true

    @JavascriptInterface
    fun getAvailableAudioOutputDevices(): String =
        listener?.getAvailableAudioOutputDevices()?.joinToString(separator = ":") ?: ""

    @JavascriptInterface
    fun getSelectedAudioOutputDevice(): String =
        listener?.getSelectedAudioOutputDevice() ?: ""

    @JavascriptInterface
    fun onSelectAudioOutputDevice(name: String) =
        listener?.onSelectAudioOutputDevice(name) == true

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
        fun getAvailableAudioOutputDevices(): List<String>
        fun getSelectedAudioOutputDevice(): String?
        fun onSelectAudioOutputDevice(name: String): Boolean
    }

}