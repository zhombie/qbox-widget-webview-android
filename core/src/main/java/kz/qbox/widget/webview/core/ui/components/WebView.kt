@file:Suppress("DEPRECATION", "SetJavaScriptEnabled")

package kz.qbox.widget.webview.core.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import kz.qbox.widget.webview.core.Logger
import kz.qbox.widget.webview.core.Widget

class WebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : android.webkit.WebView(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private val TAG = WebView::class.java.simpleName
    }

    data class GeolocationPermissionsShowPrompt constructor(
        val origin: String?,
        val callback: GeolocationPermissions.Callback?
    )

    data class FileSelectionPrompt constructor(
        val fileChooserParams: WebChromeClient.FileChooserParams?,
        val filePathCallback: ValueCallback<Array<Uri>>?
    )

    private var urlListener: UrlListener? = null
    private var listener: Listener? = null

    private var permissionRequest: PermissionRequest? = null
    private var geolocationPermissionsShowPrompt: GeolocationPermissionsShowPrompt? = null
    private var fileSelectionPrompt: FileSelectionPrompt? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        overScrollMode = OVER_SCROLL_ALWAYS

        isSaveEnabled = true

        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowFileAccess = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.blockNetworkImage = false
        settings.blockNetworkLoads = false
        settings.builtInZoomControls = false
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.setGeolocationEnabled(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.javaScriptEnabled = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.loadsImagesAutomatically = true
        settings.loadWithOverviewMode = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.setSupportMultipleWindows(false)
        settings.setSupportZoom(false)
        settings.useWideViewPort = true

        // Enable remote debugging via chrome://inspect
        setWebContentsDebuggingEnabled(true)
    }

    override fun onDetachedFromWindow() {
        urlListener = null
        listener = null

        permissionRequest = null
        geolocationPermissionsShowPrompt = null
        fileSelectionPrompt = null

        super.onDetachedFromWindow()
    }

    fun setUrlListener(urlListener: UrlListener) {
        this.urlListener = urlListener
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun init() {
        webChromeClient = MyWebChromeClient()
        webViewClient = MyWebViewClient()
    }

    fun setupCookieManager() {
        with(CookieManager.getInstance()) {
            Logger.debug(TAG, "CookieManager#hasCookies(): ${hasCookies()}")

            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@WebView, true)
            flush()
        }
    }

    fun setMixedContentAllowed(allowed: Boolean) {
        settings.mixedContentMode = if (allowed) {
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        } else {
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
    }

    fun setPermissionRequestResult(permissions: List<String>) {
        Logger.debug(
            TAG, "setPermissionRequestResult() -> " +
                    "permissions: ${permissions.joinToString(", ")}"
        )
        if (permissions.isEmpty()) {
            permissionRequest?.deny()
        } else {
            Logger.debug(
                TAG, "setPermissionRequestResult() -> " +
                        "$permissionRequest grant permissions: ${permissions.joinToString(", ")}"
            )
            permissionRequest?.grant(permissions.toTypedArray())
        }
        permissionRequest = null
    }

    fun setGeolocationPermissionsShowPromptResult(success: Boolean) {
        Logger.debug(
            TAG, "setGeolocationPermissionsShowPromptResult() -> " +
                    "$success, $geolocationPermissionsShowPrompt"
        )
        if (success) {
            geolocationPermissionsShowPrompt?.callback?.invoke(
                geolocationPermissionsShowPrompt?.origin,
                true,
                false
            )
        } else {
            geolocationPermissionsShowPrompt?.callback?.invoke(
                geolocationPermissionsShowPrompt?.origin,
                false,
                false
            )
        }
        geolocationPermissionsShowPrompt = null
    }

    fun setFileSelectionPromptResult(uri: Uri?) {
        if (uri == null) {
            setFileSelectionPromptResult(uris = null)
        } else {
            setFileSelectionPromptResult(uris = arrayOf(uri))
        }
    }

    fun setFileSelectionPromptResult(uris: Array<Uri>?) {
        Logger.debug(
            TAG,
            "setFileSelectionPromptResult() -> $uris, $fileSelectionPrompt"
        )

        try {
            if (uris.isNullOrEmpty()) {
                fileSelectionPrompt?.filePathCallback?.onReceiveValue(null)
            } else {
                fileSelectionPrompt?.filePathCallback?.onReceiveValue(uris)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fileSelectionPrompt = null
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest?) {
            Logger.debug(
                TAG,
                "onPermissionRequest() -> " +
                        "origin: ${request?.origin}, " +
                        "resources: ${request?.resources?.contentToString()}"
            )

            permissionRequest = request

            if (request != null) {
                listener?.onPermissionRequest(request.resources)
            }
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest?) {
            super.onPermissionRequestCanceled(request)

            Logger.debug(TAG, "onPermissionRequestCanceled() -> request: $request")

            permissionRequest = null

            if (request != null) {
                listener?.onPermissionRequestCanceled(request.resources)
            }
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
        ) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)

//            Logger.debug(
//                TAG,
//                "onGeolocationPermissionsShowPrompt() -> origin: $origin, callback: $callback"
//            )
//
//            geolocationPermissionsShowPrompt = GeolocationPermissionsShowPrompt(
//                origin = origin,
//                callback = callback
//            )
//
//            listener?.onGeolocationPermissionsShowPrompt()
        }

        override fun onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt()

            Logger.debug(TAG, "onGeolocationPermissionsHidePrompt()")

            geolocationPermissionsShowPrompt = null

            listener?.onGeolocationPermissionsHidePrompt()
        }

        override fun onJsAlert(
            view: android.webkit.WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            Logger.debug(
                TAG,
                "onJsAlert() -> " +
                        "url: $url, " +
                        "message: $message, " +
                        "result: $result"
            )
            return super.onJsAlert(view, url, message, result)
        }

        override fun onJsConfirm(
            view: android.webkit.WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            Logger.debug(
                TAG,
                "onJsConfirm() -> " +
                        "url: $url, " +
                        "message: $message, " +
                        "result: $result"
            )
            return super.onJsConfirm(view, url, message, result)
        }

        override fun onJsPrompt(
            view: android.webkit.WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            Logger.debug(
                TAG,
                "onJsPrompt() -> " +
                        "url: $url, " +
                        "message: $message, " +
                        "defaultValue: $defaultValue, " +
                        "result: $result"
            )
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }

        override fun onCreateWindow(
            view: android.webkit.WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }

        override fun onShowFileChooser(
            webView: android.webkit.WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            Logger.debug(
                TAG,
                "onShowFileChooser() -> params: " +
                        "${fileChooserParams?.acceptTypes?.contentToString()}, " +
                        "${fileChooserParams?.filenameHint}, " +
                        "${fileChooserParams?.isCaptureEnabled}, " +
                        "${fileChooserParams?.mode}, " +
                        "${fileChooserParams?.title}," +
                        "${fileChooserParams?.createIntent()}"
            )

            fileSelectionPrompt = FileSelectionPrompt(
                fileChooserParams = fileChooserParams,
                filePathCallback = filePathCallback
            )

            return listener?.onSelectFileRequest() == true
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
            Logger.debug(TAG, "onShowCustomView()")
        }

        override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            listener?.onPageLoadProgress(newProgress)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            return if (Widget.isLoggingEnabled) {
                Logger.debug(TAG, consoleMessage?.message() ?: "Client received console message")
                super.onConsoleMessage(consoleMessage)
            } else {
                false
            }
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onLoadResource(view: android.webkit.WebView?, url: String?) {
            super.onLoadResource(view, url)
            Logger.debug(TAG, "onLoadResource() -> url: $url")
        }

        override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Logger.debug(TAG, "onPageStarted() -> url: $url")
        }

        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
            super.onPageFinished(view, url)
            Logger.debug(TAG, "onPageFinished() -> url: $url")
            listener?.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: android.webkit.WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Logger.debug(
                TAG,
                "shouldOverrideUrlLoading() -> ${request?.requestHeaders}, ${request?.url}"
            )

            return if (request == null) {
                false
            } else {
                if (urlListener?.onLoadUrl(request.requestHeaders, request.url) == true) {
                    true
                } else {
                    super.shouldOverrideUrlLoading(view, request)
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(
            view: android.webkit.WebView?,
            url: String?
        ): Boolean {
            Logger.debug(TAG, "shouldOverrideUrlLoading() -> url: $url")
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onReceivedError(
            view: android.webkit.WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Logger.debug(TAG, "onReceivedError() -> $request, $error")
        }

        override fun onReceivedHttpError(
            view: android.webkit.WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            Logger.debug(TAG, "onReceivedHttpError() -> $request, $errorResponse")
        }

        override fun onReceivedSslError(
            view: android.webkit.WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            Logger.debug(TAG, "onReceivedSslError() -> $handler, $error")
            listener?.onReceivedSSLError(handler, error)
        }
    }

    fun interface UrlListener {
        fun onLoadUrl(headers: Map<String, String>?, uri: Uri): Boolean
    }

    interface Listener {
        fun onReceivedSSLError(handler: SslErrorHandler?, error: SslError?) {}
        fun onPageLoadProgress(progress: Int) {}
        fun onSelectFileRequest(): Boolean { return false }
        fun onPermissionRequest(resources: Array<String>) {}
        fun onPermissionRequestCanceled(resources: Array<String>) {}
        fun onGeolocationPermissionsShowPrompt() {}
        fun onGeolocationPermissionsHidePrompt() {}
        fun onPageFinished(view: android.webkit.WebView?, url: String?) {}
    }

}
