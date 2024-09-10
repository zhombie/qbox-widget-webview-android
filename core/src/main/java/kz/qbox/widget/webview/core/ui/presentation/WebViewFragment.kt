package kz.qbox.widget.webview.core.ui.presentation

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import androidx.core.os.HandlerCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import kz.garage.image.preview.ImagePreviewDialogFragment
import kz.qbox.widget.webview.core.Logger
import kz.qbox.widget.webview.core.R
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.device.Provider
import kz.qbox.widget.webview.core.models.AppEvent
import kz.qbox.widget.webview.core.models.AppState
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.CallState
import kz.qbox.widget.webview.core.models.Device
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.Flavor
import kz.qbox.widget.webview.core.models.UI
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.core.multimedia.receiver.DownloadStateReceiver
import kz.qbox.widget.webview.core.multimedia.selection.GetContentDelegate
import kz.qbox.widget.webview.core.multimedia.selection.GetContentResultContract
import kz.qbox.widget.webview.core.multimedia.selection.MimeType
import kz.qbox.widget.webview.core.multimedia.selection.StorageAccessFrameworkInteractor
import kz.qbox.widget.webview.core.sdk.ActivityCompat
import kz.qbox.widget.webview.core.sdk.BundleCompat
import kz.qbox.widget.webview.core.ui.components.JSBridge
import kz.qbox.widget.webview.core.ui.components.ProgressView
import kz.qbox.widget.webview.core.ui.components.WebView
import kz.qbox.widget.webview.core.ui.dialogs.DownloadProgressDialog
import kz.qbox.widget.webview.core.ui.dialogs.showError
import kz.qbox.widget.webview.core.utils.Constants
import kz.qbox.widget.webview.core.utils.LOCATION_PERMISSIONS
import kz.qbox.widget.webview.core.utils.PermissionRequestMapper
import kz.qbox.widget.webview.core.utils.STORAGE_PERMISSIONS
import kz.qbox.widget.webview.core.utils.audioManager
import kz.qbox.widget.webview.core.utils.clipboardManager
import kz.qbox.widget.webview.core.utils.downloadManager
import kz.qbox.widget.webview.core.utils.getIntOrDefault
import kz.qbox.widget.webview.core.utils.isPermissionGranted
import kz.qbox.widget.webview.core.utils.isStoragePermissionsGranted
import kz.qbox.widget.webview.core.utils.locationManager
import kz.qbox.widget.webview.core.utils.showImagePreview
import org.json.JSONObject
import java.io.File
import java.util.Locale

class WebViewFragment internal constructor() : Fragment(),
    WebView.Listener,
    JSBridge.Listener,
    Listener {

    companion object {
        fun newInstance(
            flavor: Flavor,
            url: String,
            token: String?,
            language: String?,
            call: Call?,
            user: User?,
            dynamicAttrs: DynamicAttrs?,
            ui: UI?
        ): WebViewFragment {
            val fragment = WebViewFragment()
            val bundle = Bundle().apply {
                putSerializable("flavor", flavor)
                putString("url", url)
                putString("token", token)
                putString("language", language)
                putSerializable("call", call)
                putSerializable("user", user)
                putSerializable("dynamic_attrs", dynamicAttrs)
                putSerializable("ui", ui)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private val handler: Handler by lazy {
        HandlerCompat.createAsync(Looper.getMainLooper())
    }

    private var webView: WebView? = null
    private var progressView: ProgressView? = null

    private var progressDialog: DownloadProgressDialog? = null

    private var errorDialog: AlertDialog? = null

    private var interactor: StorageAccessFrameworkInteractor? = null

    /**
     * [DownloadManager] download ids list (which has downloading status)
     */
    private var pendingDownloads: MutableList<Pair<Long, String>>? = null

    /**
     * Files that already downloaded by [DownloadManager]
     */
    private var downloadedFiles: MutableList<Pair<String, Uri>>? = null

    private var downloadStateReceiver: DownloadStateReceiver? = null

    private var thread: Thread? = null

    private val requestedPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Logger.debug("QBox-WebViewFragment", "requestedPermissionsLauncher() -> permissions: $permissions")

            webView?.setPermissionRequestResult(
                PermissionRequestMapper.fromAndroidToWebClient(permissions)
            )

            if (permissions.any { !it.value }) {
                showRequestPermissionsAlertDialog()
            }
        }

    private val locationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Logger.debug("QBox-WebViewFragment", "locationPermissionsLauncher() -> permissions: $permissions")

            val isAllPermissionsGranted = permissions.all { it.value }

            webView?.setGeolocationPermissionsShowPromptResult(isAllPermissionsGranted)

            if (!isAllPermissionsGranted) {
                showRequestPermissionsAlertDialog()
            }
        }

    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Logger.debug("QBox-WebViewFragment", "locationSettingsLauncher() -> resultCode: ${result.resultCode}")

            onGeolocationPermissionsShowPrompt()
        }

    private val storagePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Logger.debug("QBox-WebViewFragment", "storagePermissionsLauncher() -> permissions: $permissions")

            if (requireContext().isStoragePermissionsGranted()) {
                onSelectFileRequest()
            } else {
                showRequestPermissionsAlertDialog()
            }
        }

    private val provider: Provider? by lazy { Provider(requireContext()) }

    private val flavor by lazy(LazyThreadSafetyMode.NONE) {
        BundleCompat.getEnum<Flavor>(arguments, "flavor") ?: throw IllegalStateException()
    }

    private val uri by lazy(LazyThreadSafetyMode.NONE) {
        try {
            Uri.parse(arguments?.getString("url"))
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException()
        }
    }

    private val token by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getString("token")
    }

    private val language by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getString("language") ?: Locale.getDefault().language
    }

    private val call by lazy(LazyThreadSafetyMode.NONE) {
        BundleCompat.getSerializable<Call>(arguments, "call")
    }

    private val user by lazy(LazyThreadSafetyMode.NONE) {
        BundleCompat.getSerializable<User>(arguments, "user")
    }

    private val dynamicAttrs by lazy(LazyThreadSafetyMode.NONE) {
        BundleCompat.getSerializable<DynamicAttrs>(arguments, "dynamic_attrs")
    }

    private val ui by lazy(LazyThreadSafetyMode.NONE) {
        BundleCompat.getSerializable<UI>(arguments, "ui")
    }

    private val jsBridge by lazy {
        val provider = provider
        if (provider == null) null else JSBridge(
            device = Device(
                os = provider.os,
                osVersion = provider.osVersion,
                appVersion = provider.versionName,
                name = provider.name,
                mobileOperator = provider.operator,
                battery = Device.Battery(
                    percentage = provider.batteryPercent,
                    isCharging = provider.isPhoneCharging,
                    temperature = provider.batteryTemperature
                )
            ),
            call = call,
            user = user,
            dynamicAttrs = dynamicAttrs,
            ui = ui,
            listener = this
        )
    }

    private var callState: CallState? = null

    private var isDownloading = false

    private val activity: AppCompatActivity
        get() = requireActivity() as AppCompatActivity

    private val audioSwitch by lazy {
        AudioSwitch(
            context = activity.applicationContext,
            loggingEnabled = true,
            audioFocusChangeListener = {
                Logger.debug("QBox-WebViewFragment", "audioFocusChangeListener() -> $it")
            },
            preferredDeviceList = listOf(
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.WiredHeadset::class.java,
                AudioDevice.Earpiece::class.java,
                AudioDevice.Speakerphone::class.java
            )
        )
    }

    private val audioManager by lazy { context?.audioManager }

    fun setListener(listener: Widget.Listener) {
        Widget.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioSwitch.start { audioDevices: List<AudioDevice>, selectedDevice: AudioDevice? ->
            Log.d("QBox-WebViewFragment", "audioSwitch.start() -> audioDevices: $audioDevices")
            Log.d("QBox-WebViewFragment", "audioSwitch.start() -> selectedDevice: $selectedDevice")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.qbox_widget_fragment_web_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        progressView = view.findViewById(R.id.progressView)

        var uri = uri

//        uri = uri.buildUpon()
//            .apply {
//                appendQueryParameter("can_skip_request_call_feedback", "1")
//            }
//            .build()

        when (flavor) {
            Flavor.FULL_SUITE -> {
                uri = uri.buildUpon()
                    .apply {
                        if (!language.isNullOrBlank()) {
                            appendQueryParameter("lang", language)
                        }
                    }
                    .build()
            }

            Flavor.AUDIO_CALL -> {
                uri = uri.buildUpon()
                    .apply {
                        if (!language.isNullOrBlank()) {
                            appendQueryParameter("lang", language)
                        }

                        if (token.isNullOrBlank()) {
                            call?.let {
                                appendQueryParameter("caller", it.phoneNumber)
                                appendQueryParameter("dest", it.destination)
                            }
                        } else {
                            appendQueryParameter("token", token)
                        }

                        Logger.debug("QBox-WebViewFragment", "Widget.isLoggingEnabled: ${Widget.isLoggingEnabled}")

                        if (Widget.isLoggingEnabled) {
                            appendQueryParameter("debug", "1")
                        }
                    }
                    .build()
            }

            Flavor.VIDEO_CALL -> {
                val call = call ?: throw NullPointerException("Call information is not provided!")

                uri = uri.buildUpon()
                    .apply {
                        if (!language.isNullOrBlank()) {
                            appendQueryParameter("lang", language)
                        }

                        appendQueryParameter("topic", call.topic)
                    }
                    .build()
            }
        }

        setupWebView()

        interactor = StorageAccessFrameworkInteractor(activity) { result ->
            when (result) {
                is GetContentDelegate.Result.Success -> {
                    webView?.setFileSelectionPromptResult(result.uri)
                }

                is GetContentDelegate.Result.Error.NullableUri -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.qbox_widget_error_basic,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    webView?.setFileSelectionPromptResult(uri = null)
                }

                is GetContentDelegate.Result.Error.SizeLimitExceeds -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.qbox_widget_error_files_exceeds_limit, result.maxSize),
                        Toast.LENGTH_SHORT
                    ).show()
                    webView?.setFileSelectionPromptResult(uri = null)
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.qbox_widget_error_basic,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    webView?.setFileSelectionPromptResult(uri = null)
                }
            }
        }

        Logger.debug("QBox-WebViewFragment", "uri: $uri")

        webView?.loadUrl(uri.toString())
    }

    override fun onResume() {
        super.onResume()

        Logger.debug("QBox-WebViewFragment", "onResume()")

        try {
            handler.postDelayed({
                Logger.debug("QBox-WebViewFragment", "handler.postDelayed()")
                webView?.setFileSelectionPromptResult(uri = null)
            }, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed(onBack: () -> Unit) {
        val fragments = childFragmentManager.fragments

        val imagePreviewDialogFragments =
            fragments.filterIsInstance<ImagePreviewDialogFragment>()
//        val videoPreviewDialogFragments =
//            fragments.filterIsInstance<VideoPreviewDialogFragment>()

        when {
            imagePreviewDialogFragments.isNotEmpty() -> {
                imagePreviewDialogFragments.forEach {
                    it.dismiss()
                    childFragmentManager.fragments.remove(it)
                }
            }
//            videoPreviewDialogFragments.isNotEmpty() -> {
//                videoPreviewDialogFragments.forEach {
//                    it.dismiss()
//                    supportFragmentManager.fragments.remove(it)
//                }
//            }
            else -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.qbox_widget_alert_title_exit)
                    .setMessage(R.string.qbox_widget_alert_message_exit)
                    .setNegativeButton(R.string.qbox_widget_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.qbox_widget_exit) { dialog, _ ->
                        dialog.dismiss()
                        onBack.invoke()
                    }
                    .show()
            }
        }
    }

    override fun onStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (callState == CallState.START) {
                evaluateJS(JSONObject().apply { put("app_state", AppState.START.toString()) })
            }
        }
        super.onStart()
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (callState == CallState.START) {
                evaluateJS(JSONObject().apply { put("app_state", AppState.STOP.toString()) })
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        audioSwitch.stop()

//        audioManager?.mode = AudioManager.MODE_NORMAL
//        activity.volumeControlStream = AudioManager.STREAM_SYSTEM

        try {
            handler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Widget.listener = null

        jsBridge?.dispose()

        interactor?.dispose()
        interactor = null

        if (downloadStateReceiver != null) {
            try {
                getActivity()?.unregisterReceiver(downloadStateReceiver)
            } catch (_: IllegalArgumentException) {
            }
            downloadStateReceiver = null
        }

        pendingDownloads?.clear()
        pendingDownloads = null

        downloadedFiles?.clear()
        downloadedFiles = null

        isDownloading = false

//        thread?.interrupt()
        thread = null

        super.onDestroy()

        webView?.destroy()
    }

    override fun onUserLeaveHint(onLeave: () -> Unit) {
        Logger.debug("QBox-WebViewFragment", "onUserLeaveHint()")

        if (callState == CallState.START) {
            if (!ActivityCompat.isInPictureInPictureMode(activity)) {
                ActivityCompat.enterPictureInPictureMode(activity)
            }
        }

        onLeave.invoke()
    }

    override fun onReload() {
        Logger.debug("QBox-WebViewFragment", "onReload()")
        webView?.loadUrl("javascript:window.location.reload(true)")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean
    ) {
        Logger.debug("QBox-WebViewFragment", "onPictureInPictureModeChanged() -> $isInPictureInPictureMode")
        if (isInPictureInPictureMode) {
            activity.supportActionBar?.hide()
            evaluateJS(JSONObject().apply { put("app_event", AppEvent.PIP_ENTER.toString()) })
        } else {
            activity.supportActionBar?.show()
            evaluateJS(JSONObject().apply { put("app_event", AppEvent.PIP_EXIT.toString()) })
        }

        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            getActivity()?.finishAndRemoveTask()
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    private fun setupWebView() {
        ActivityCompat.setDarkModeOff(webView)

        webView?.init()
        webView?.setupCookieManager()
        webView?.setMixedContentAllowed(true)
        webView?.setUrlListener { headers, uri ->
            Logger.debug(
                "QBox-WebViewFragment",
                "setUrlListener() -> $headers, $uri, ${uri.scheme}, ${uri.path}, ${uri.encodedPath}, ${uri.authority}"
            )

            return@setUrlListener if (uri.toString().contains("image")) {
                getActivity()?.showImagePreview(uri)
                true
            }
//            else if (uri.toString().contains("video")) {
//                VideoPreviewDialogFragment.show(
//                    fragmentManager = supportFragmentManager,
//                    uri = uri,
//                    caption = uri.toString()
//                )
//                true
//            }
            else resolveUri(uri)
        }

        webView?.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Logger.debug(
                "QBox-WebViewFragment", "onDownloadStart() -> " +
                        "url: $url, " +
                        "userAgent: $userAgent, " +
                        "contentDisposition: $contentDisposition, " +
                        "mimetype: $mimetype, " +
                        "contentLength: $contentLength"
            )

            if (mimetype?.startsWith("image") == true &&
                (url.endsWith("png") ||
                        url.endsWith("jpg") ||
                        url.endsWith("jpeg"))
            ) {
                getActivity()?.showImagePreview(Uri.parse(url))
                return@setDownloadListener
            }
//            else if (mimetype?.startsWith("video") == true &&
//                (url.endsWith("mp4") ||
//                        url.endsWith("avi") ||
//                        url.endsWith("mov") ||
//                        url.endsWith("3gp"))
//            ) {
//                VideoPreviewDialogFragment.show(
//                    fragmentManager = supportFragmentManager,
//                    uri = Uri.parse(url),
//                    caption = null
//                )
//                return@setDownloadListener
//            }

            if (pendingDownloads == null) {
                pendingDownloads = mutableListOf()
            }
            if (url in (pendingDownloads ?: mutableListOf()).map { it.second }) {
                Toast.makeText(
                    requireContext(),
                    R.string.qbox_widget_error_files_download_in_progress,
                    Toast.LENGTH_SHORT
                ).show()
                return@setDownloadListener
            }

            var isLocalFileFoundAndOpened = false
            val found = downloadedFiles?.find { it.first == url }
            if (found != null && !found.second.path.isNullOrBlank()) {
                val file = File(requireNotNull(found.second.path))
                Logger.debug("QBox-WebViewFragment", "file: $file")
                isLocalFileFoundAndOpened = openFile(file, mimetype, url)
            }

            if (isLocalFileFoundAndOpened) return@setDownloadListener

            val status = Environment.getExternalStorageState()
            if (status != Environment.MEDIA_MOUNTED) {
                return@setDownloadListener
            }

            val request = try {
                DownloadManager.Request(Uri.parse(url))
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return@setDownloadListener
            }

            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)

            val publicDirectory = Environment.DIRECTORY_DOWNLOADS
            val deviceDirectory = Environment.getExternalStorageDirectory()
            if (deviceDirectory.freeSpace / 1000000.0 <= 300.0) {
                val linkMessage = TextView(requireContext()).apply {
                    setPadding(65, 0, 65, 0)
                    setTextColor(Color.BLACK)
                    textSize = 15f
                    isClickable = true
                    movementMethod = LinkMovementMethod.getInstance()
                    text = HtmlCompat.fromHtml(
                        "<a href='$url'>$url</a>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.qbox_widget_attention))
                    .setMessage(getString(R.string.qbox_widget_alert_message_not_enough_space))
                    .setView(linkMessage)
                    .setPositiveButton(getString(R.string.qbox_widget_copy)) { dialog, _ ->
                        context?.clipboardManager?.setPrimaryClip(ClipData.newPlainText("url", url))

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.qbox_widget_toast_message_copied_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.qbox_widget_cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()

                return@setDownloadListener
            }

            request.addRequestHeader("User-Agent", userAgent)
            request.allowScanningByMediaScanner()
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)
            request.setDescription(
                getString(
                    R.string.qbox_widget_label_files_download_in_progress,
                    filename
                )
            )
            request.setDestinationInExternalPublicDir(publicDirectory, filename)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.setRequiresCharging(false)
                request.setRequiresDeviceIdle(false)
            }
            request.setTitle(filename)

            downloadFile(request, url, filename)

            if (downloadStateReceiver != null) {
                try {
                    getActivity()?.unregisterReceiver(downloadStateReceiver)
                } catch (_: IllegalArgumentException) {
                }
                downloadStateReceiver = null
            }
            downloadStateReceiver = DownloadStateReceiver { downloadId, uri, mimeType ->
                Logger.debug(
                    "QBox-WebViewFragment",
                    "onFileUriReady() -> " +
                            "downloadId: $downloadId, " +
                            "uri: $uri," +
                            " mimeType: $mimeType"
                )

                pendingDownloads?.removeAll { it.first == downloadId }

                progressDialog?.dismiss()
                progressDialog = null

                val path = uri?.path
                if (!path.isNullOrBlank() && !mimeType.isNullOrBlank()) {
                    if (uri.scheme == "file") {
                        val file = File(path)

                        AlertDialog.Builder(requireContext())
                            .setCancelable(true)
                            .setTitle(R.string.qbox_widget_alert_title_files_download_completed)
                            .setMessage(
                                getString(
                                    R.string.qbox_widget_alert_message_files_download_completed,
                                    file.name
                                )
                            )
                            .setNegativeButton(R.string.qbox_widget_no) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(R.string.qbox_widget_open) { dialog, _ ->
                                dialog.dismiss()

                                // TODO: Handle file open issue
                                openFile(file, mimeType, url)
                            }
                            .show()

                        saveFile(url, uri)
                    }
                }
            }

            ContextCompat.registerReceiver(
                requireContext(),
                downloadStateReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        jsBridge?.let {
            webView?.addJavascriptInterface(it, "JSBridge")
        }

        webView?.setListener(this)
    }

    private fun evaluateJS(jsonObject: JSONObject) {
        webView?.evaluateJavascript("window.postMessage('$jsonObject', '*');", null)
    }

    private fun resolveUri(uri: Uri): Boolean {
        Logger.debug("QBox-WebViewFragment", "resolveUri() -> ${this.uri}, $uri")

        if (this.uri == uri) return false
        if (this.uri.host == uri.host) return false

        Constants.URL_SCHEMES.forEach {
            if (uri.scheme?.let { uriScheme -> it.startsWith(uriScheme) } == true) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    startActivity(intent)
                    return true
                } catch (e: ActivityNotFoundException) {
                    Logger.error("QBox-WebViewFragment", "resolveUri() -> $uri, $e")
                }
            }
        }

//        SHORTEN_LINKS.forEach {
//            if (uri.authority?.equals(it) == true) {
//                val intent = Intent(Intent.ACTION_VIEW).apply {
//                    data = uri
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
//                try {
//                    startActivity(intent)
//                    return true
//                } catch (e: ActivityNotFoundException) {
//                    Logger.debug("QBox-WebViewFragment", "resolveUri() -> $uri, $e")
//                }
//            }
//        }

//        if (Constants.FILE_EXTENSIONS.any { uri.path?.endsWith(it) == true }) return false

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Logger.error("QBox-WebViewFragment", "resolveUri() -> $uri, $e")
            false
        }
    }

    private fun showRequestPermissionsAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.qbox_widget_alert_title_permissions_require)
            .setMessage(R.string.qbox_widget_alert_message_permissions_require)
            .setPositiveButton(R.string.qbox_widget_go_to_settings) { dialog, _ ->
                dialog.dismiss()

                startActivity(
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", context?.packageName, null)
                    }
                )
            }
            .show()
    }

    private fun showGPSDisabledErrorAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.qbox_widget_alert_title_permissions_require_geolocation)
            .setMessage(R.string.qbox_widget_alert_message_permissions_require_geolocation)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()

                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    private fun downloadFile(
        downloadRequest: DownloadManager.Request,
        url: String,
        filename: String
    ) {
        // TODO: Handle DownloadManager absence issue (impossible case, but who knows)
        val downloadManager = context?.downloadManager ?: return

        val id = downloadManager.enqueue(downloadRequest)

        if (pendingDownloads == null) {
            pendingDownloads = mutableListOf()
        }

        val found = pendingDownloads?.indexOfFirst { it.first == id }
        if (found == null || found < 0) {
            pendingDownloads?.add(id to url)
        } else {
            pendingDownloads?.set(found, id to url)
        }

        isDownloading = true

        thread = object : Thread() {
            override fun run() {
                while (isDownloading) {
                    if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                        break
                    }

                    val q = DownloadManager.Query()
                    q.setFilterById(id)
                    val cursor = downloadManager.query(q)
                    if (cursor.moveToFirst()) {
                        when (cursor.getIntOrDefault(DownloadManager.COLUMN_STATUS, 0)) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                isDownloading = false
                            }

                            DownloadManager.STATUS_PAUSED -> {
                                if (cursor.getIntOrDefault(
                                        DownloadManager.COLUMN_REASON,
                                        0
                                    ) != DownloadManager.PAUSED_WAITING_TO_RETRY
                                ) {
                                    isDownloading = false

                                    handler.post {
                                        errorDialog?.dismiss()
                                        errorDialog = null
                                        errorDialog = context?.showError(
                                            R.string.qbox_widget_alert_message_error_occurred,
                                            url
                                        )
                                        errorDialog?.show()
                                    }
                                }
                            }

                            DownloadManager.STATUS_FAILED -> {
                                isDownloading = false

                                handler.post {
                                    errorDialog?.dismiss()
                                    errorDialog = null
                                    errorDialog = context?.showError(
                                        R.string.qbox_widget_alert_message_error_occurred,
                                        url
                                    )
                                    errorDialog?.show()
                                }
                            }

                            else -> {
                                val bytesDownloaded = cursor.getIntOrDefault(
                                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR,
                                    0
                                )
                                val bytesTotal = cursor.getIntOrDefault(
                                    DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
                                    0
                                )
                                val progress = if (bytesTotal > 0) {
                                    (bytesDownloaded * 100.0 / bytesTotal)
                                } else {
                                    0.0
                                }

                                handler.post {
                                    errorDialog?.dismiss()
                                    errorDialog = null

                                    if (progressDialog == null) {
                                        progressDialog = DownloadProgressDialog(
                                            context = requireContext(),
                                            cancelable = true,
                                            cancelListener = null,
                                            params = DownloadProgressDialog.Params(filename)
                                        )
                                        progressDialog?.show()
                                    }
                                    progressDialog?.progress = progress
                                }
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }

        thread?.start()

        Toast.makeText(
            requireContext(),
            R.string.qbox_widget_info_files_download_started,
            Toast.LENGTH_LONG
        )
            .show()
    }

    private fun saveFile(url: String, uri: Uri) {
        if (downloadedFiles == null) {
            downloadedFiles = mutableListOf()
        }
        val found = downloadedFiles?.indexOfFirst { it.first == url }
        if (found == null || found < 0) {
            downloadedFiles?.add(url to uri)
        } else {
            downloadedFiles?.set(found, url to uri)
        }
    }

    private fun openFile(file: File, mimeType: String, url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        val contentUri = try {
            FileProvider.getUriForFile(
                requireContext(),
                "${context?.packageName}.provider",
                file
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()

            errorDialog?.dismiss()
            errorDialog = null
            errorDialog = context?.showError(R.string.qbox_widget_error_files_open_unable, url)
            errorDialog?.show()
            return false
        }

        context?.grantUriPermission(
            context?.packageName,
            contentUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        intent.setDataAndType(contentUri, mimeType)

        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            errorDialog?.dismiss()
            errorDialog = null
            errorDialog = context?.showError(R.string.qbox_widget_error_files_open_unable, url)
            errorDialog?.show()
            false
        }
    }

    /**
     * [JSBridge.Listener] implementation
     */

    override fun onClose(): Boolean {
        getActivity()?.onBackPressed()
        return true
    }

    override fun onLanguageSet(language: String): Boolean {
        return false
    }

    override fun onCallState(state: CallState) {
        Logger.debug("QBox-WebViewFragment", "onCallState() -> state: $state")

        Widget.listener?.onCallState(state)

        callState = state

        if (state == CallState.FINISH) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (getActivity()?.isInPictureInPictureMode == true) {
                    Toast.makeText(
                        requireContext(),
                        R.string.qbox_widget_alert_message_call_finished,
                        Toast.LENGTH_SHORT
                    ).show()

//                    startActivity(
//                        WebViewActivity.newIntent(
//                            context = requireContext(),
//                            flavor = flavor,
//                            url = uri.toString(),
//                            language = language,
//                            call = call,
//                            user = user,
//                            dynamicAttrs = dynamicAttrs
//                        ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                    )

                    getActivity()?.moveTaskToBack(true)
                }
            }
        }
    }

    override fun getAvailableAudioOutputDevices(): List<String> {
        val devices = audioSwitch.availableAudioDevices.map { it.name }
        Log.d("QBox-WebViewFragment", "getAvailableAudioOutputDevices() -> $devices")
        return devices
    }

    override fun getSelectedAudioOutputDevice(): String? {
        return audioSwitch.selectedAudioDevice?.name
    }

    override fun onSelectAudioOutputDevice(name: String): Boolean {
        Log.d("QBox-WebViewFragment", "onSelectAudioOutputDevice -> name: $name")

        audioSwitch.availableAudioDevices.find { it.name == name }?.let {
            Logger.debug("QBox-WebViewFragment", "onSelectAudioOutputDevice() -> $it")

            when (it) {
                is AudioDevice.Earpiece -> {
                    Logger.debug("QBox-WebViewFragment", "onSelectAudioOutputDevice() -> $it")
                    audioManager?.isBluetoothScoOn = false
                    audioManager?.isSpeakerphoneOn = false
                    audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                    activity.volumeControlStream = AudioManager.STREAM_VOICE_CALL
                }

                is AudioDevice.Speakerphone -> {
                    Logger.debug("QBox-WebViewFragment", "onSelectAudioOutputDevice() -> $it")
                    audioManager?.isBluetoothScoOn = false
                    audioManager?.isSpeakerphoneOn = true
                    audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                    activity.volumeControlStream = AudioManager.STREAM_VOICE_CALL
                }

                else -> {
                }
            }

            audioSwitch.selectDevice(it)
            audioSwitch.activate()
        }

        return true
    }

    /**
     * [WebView.Listener] implementation
     */

    override fun onReceivedSSLError(handler: SslErrorHandler?, error: SslError?) {
    }

    override fun onPageLoadProgress(progress: Int) {
        if (progress < 95) {
            progressView?.show()
            progressView?.showTextView()
            progressView?.setText(getString(R.string.qbox_widget_label_widget_loading, progress))
        } else {
            progressView?.hide()
        }
    }

    override fun onSelectFileRequest(): Boolean {
        Logger.debug("QBox-WebViewFragment", "onSelectFileRequest()")

        if (requireContext().isStoragePermissionsGranted()) {
            Logger.debug(
                "QBox-WebViewFragment",
                "Constants.STORAGE_PERMISSIONS.all { activity.isPermissionGranted(it) }"
            )

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.qbox_widget_alert_title_media_selection)
                .setItems(
                    arrayOf(
                        getString(R.string.qbox_widget_content_image),
                        getString(R.string.qbox_widget_content_video),
                        getString(R.string.qbox_widget_content_audio),
                        getString(R.string.qbox_widget_content_document)
                    )
                ) { _, which ->
                    when (which) {
                        0 -> {
                            interactor?.launchSelection(GetContentResultContract.Params(MimeType.IMAGE))
                        }

                        1 -> {
                            interactor?.launchSelection(GetContentResultContract.Params(MimeType.VIDEO))
                        }

                        2 -> {
                            interactor?.launchSelection(GetContentResultContract.Params(MimeType.AUDIO))
                        }

                        3 -> {
                            interactor?.launchSelection(GetContentResultContract.Params(MimeType.DOCUMENT))
                        }
                    }
                }
                .setOnCancelListener {
                    webView?.setFileSelectionPromptResult(uri = null)
                }
                .show()
        } else {
            Logger.debug("QBox-WebViewFragment", "storagePermissionsLauncher.launch(Constants.STORAGE_PERMISSIONS)")

            storagePermissionsLauncher.launch(STORAGE_PERMISSIONS)
        }

        return true
    }

    override fun onPermissionRequest(resources: Array<String>) {
        val permissions = PermissionRequestMapper.fromWebClientToAndroid(resources).toTypedArray()
        Logger.debug("QBox-WebViewFragment", "onPermissionRequest() -> resources: ${resources.contentToString()}")
        Logger.debug(
            "QBox-WebViewFragment",
            "onPermissionRequest() -> permissions: ${permissions.contentToString()}"
        )
        requestedPermissionsLauncher.launch(permissions)
    }

    override fun onPermissionRequestCanceled(resources: Array<String>) {
        Logger.debug(
            "QBox-WebViewFragment",
            "onPermissionRequestCanceled() -> resources: ${resources.contentToString()}"
        )
    }

    override fun onGeolocationPermissionsShowPrompt() {
        Logger.debug("QBox-WebViewFragment", "onGeolocationPermissionsShowPrompt()")
        if (LOCATION_PERMISSIONS.all { requireContext().isPermissionGranted(it) }) {
            val locationManager = context?.locationManager
            if (locationManager == null) {
                showGPSDisabledErrorAlertDialog()
            } else {
                if (LocationManagerCompat.isLocationEnabled(locationManager)) {
                    webView?.setGeolocationPermissionsShowPromptResult(true)
                } else {
                    showGPSDisabledErrorAlertDialog()
                }
            }
        } else {
            locationPermissionsLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    override fun onGeolocationPermissionsHidePrompt() {
        Logger.debug("QBox-WebViewFragment", "onGeolocationPermissionsHidePrompt()")
    }

}

interface Listener {
    fun onUserLeaveHint(onLeave: () -> Unit)
    fun onBackPressed(onBack: () -> Unit)
    fun onReload()
}