package kz.qbox.widget.webview.core.ui.components

import android.webkit.JavascriptInterface
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.User

internal class JSBridge constructor(
    private val call: Call,
    private val user: User
) {

    @JavascriptInterface
    fun getCall(): String? =
        call.toJSONObject().toString(4)

    @JavascriptInterface
    fun getUser(): String? =
        user.toJSONObject().toString(4)

}