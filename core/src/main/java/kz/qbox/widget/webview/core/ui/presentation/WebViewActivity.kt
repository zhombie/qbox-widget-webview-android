package kz.qbox.widget.webview.core.ui.presentation

import android.Manifest
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebView.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import kz.garage.image.preview.ImagePreviewDialogFragment
import kz.qbox.widget.webview.core.Logger
import kz.qbox.widget.webview.core.R
import kz.qbox.widget.webview.core.device.Device
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.Flavor
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.core.multimedia.preview.VideoPreviewDialogFragment
import kz.qbox.widget.webview.core.multimedia.receiver.DownloadStateReceiver
import kz.qbox.widget.webview.core.multimedia.selection.GetContentDelegate
import kz.qbox.widget.webview.core.multimedia.selection.GetContentResultContract
import kz.qbox.widget.webview.core.multimedia.selection.MimeType
import kz.qbox.widget.webview.core.multimedia.selection.StorageAccessFrameworkInteractor
import kz.qbox.widget.webview.core.ui.components.DownloadingProgressView
import kz.qbox.widget.webview.core.ui.components.JSBridge
import kz.qbox.widget.webview.core.ui.components.ProgressView
import kz.qbox.widget.webview.core.ui.components.WebView
import kz.qbox.widget.webview.core.utils.IntentCompat
import kz.qbox.widget.webview.core.utils.PermissionRequestMapper
import kz.qbox.widget.webview.core.utils.setupActionBar
import java.io.File
import java.util.Locale

class WebViewActivity : AppCompatActivity(), WebView.Listener, JSBridge.Listener {

    companion object {
        private val TAG = WebViewActivity::class.java.simpleName

        private val URL_SCHEMES = arrayOf(
            SCHEME_TEL,
            SCHEME_MAILTO,
            SCHEME_GEO,
            "sms:",
            "smsto:",
            "mms:",
            "mmsto:"
        )

//        private val SHORTEN_LINKS = arrayOf(
//            "t.me",
//            "telegram.me",
//            "telegram.dog",
//            "vk.com",
//            "vk.cc",
//            "fb.me",
//            "facebook.com",
//            "fb.com"
//        )

        private val FILE_EXTENSIONS = arrayOf(
            ".dot",
            ".pptx",
            ".rtf",
            ".xlsx",
            ".xls",
            ".bmp",
            ".csv",
            ".html",
            ".xml",
            ".txt",
            ".pdf",
            ".doc",
            ".zip",
            ".rar",
            ".docx",
        )

        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private val STORAGE_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun newIntent(
            context: Context,
            flavor: Flavor,
            url: String,
            language: String?,
            call: Call?,
            user: User?
        ): Intent {
            return Intent(context, WebViewActivity::class.java)
                .putExtra("flavor", flavor)
                .putExtra("url", url)
                .putExtra("language", language)
                .putExtra("call", call)
                .putExtra("user", user)
        }
    }

    private var appBarLayout: AppBarLayout? = null
    private var toolbar: MaterialToolbar? = null
    private var webView: WebView? = null
    private var progressView: ProgressView? = null
    private var downloadingProgressBar: DownloadingProgressView? = null

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

    private val requestedPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Logger.debug(TAG, "requestedPermissionsLauncher() -> permissions: $permissions")

            webView?.setPermissionRequestResult(
                PermissionRequestMapper.fromAndroidToWebClient(permissions)
            )

            if (permissions.any { !it.value }) {
                showRequestPermissionsAlertDialog()
            }
        }

    private val locationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Logger.debug(TAG, "locationPermissionsLauncher() -> permissions: $permissions")

            val isAllPermissionsGranted = permissions.all { it.value }

            webView?.setGeolocationPermissionsShowPromptResult(isAllPermissionsGranted)

            if (!isAllPermissionsGranted) {
                showRequestPermissionsAlertDialog()
            }
        }

    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Logger.debug(TAG, "locationSettingsLauncher() -> resultCode: ${result.resultCode}")

            onGeolocationPermissionsShowPrompt()
        }

    private val storagePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Logger.debug(TAG, "storagePermissionsLauncher() -> permissions: $permissions")

            val isAllPermissionsGranted = permissions.all { it.value }

            if (isAllPermissionsGranted) {
                onSelectFileRequest()
            } else {
                showRequestPermissionsAlertDialog()
            }
        }

    private val device: Device by lazy { Device(applicationContext) }

    private val flavor by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getEnum<Flavor>(intent, "flavor")
    }

    private val call by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<Call>(intent, "call")
    }

    private val user by lazy(LazyThreadSafetyMode.NONE) {
        (IntentCompat.getSerializable(intent, "user") ?: User()).copy(
            device = User.Device(
                os = device.os,
                osVersion = device.osVersion,
                appVersion = device.versionName,
                name = device.name,
                mobileOperator = device.operator,
                battery = User.Device.Battery(
                    percentage = device.batteryPercent,
                    isCharging = device.isPhoneCharging,
                    temperature = device.batteryTemperature
                )
            )
        )
    }

    private val jsBridge by lazy {
        JSBridge(call, user, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        appBarLayout = findViewById(R.id.appBarLayout)
        toolbar = findViewById(R.id.toolbar)
        webView = findViewById(R.id.webView)
        progressView = findViewById(R.id.progressView)

        downloadingProgressBar = DownloadingProgressView(this)

        var uri = try {
            Uri.parse(intent.getStringExtra("url"))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: return finish()

        val language = intent.getStringExtra("language") ?: Locale.getDefault().language

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
            else -> {
                throw IllegalStateException()
            }
        }

        setupActionBar()
        setupWebView()

        interactor = StorageAccessFrameworkInteractor(this) { result ->
            when (result) {
                is GetContentDelegate.Result.Success -> {
                    webView?.setFileSelectionPromptResult(result.uri)
                }
                is GetContentDelegate.Result.Error.NullableUri -> {
                    Toast.makeText(this, R.string.error_basic, Toast.LENGTH_SHORT).show()
                    webView?.setFileSelectionPromptResult(uri = null)
                }
                is GetContentDelegate.Result.Error.SizeLimitExceeds -> {
                    Toast.makeText(
                        this,
                        getString(R.string.error_files_exceeds_limit, result.maxSize),
                        Toast.LENGTH_SHORT
                    ).show()
                    webView?.setFileSelectionPromptResult(uri = null)
                }
                else -> {
                    Toast.makeText(this, R.string.error_basic, Toast.LENGTH_SHORT).show()
                    webView?.setFileSelectionPromptResult(uri = null)
                }
            }
        }

        webView?.loadUrl(uri.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragments = supportFragmentManager.fragments

        val imagePreviewDialogFragments =
            fragments.filterIsInstance<ImagePreviewDialogFragment>()
        val videoPreviewDialogFragments =
            fragments.filterIsInstance<VideoPreviewDialogFragment>()

        when {
            imagePreviewDialogFragments.isNotEmpty() -> {
                imagePreviewDialogFragments.forEach {
                    it.dismiss()
                    supportFragmentManager.fragments.remove(it)
                }
            }
            videoPreviewDialogFragments.isNotEmpty() -> {
                videoPreviewDialogFragments.forEach {
                    it.dismiss()
                    supportFragmentManager.fragments.remove(it)
                }
            }
            else -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_exit)
                    .setMessage(R.string.alert_message_exit)
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.exit) { dialog, _ ->
                        dialog.dismiss()
                        super.onBackPressed()
                    }
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reload -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_reload)
                    .setMessage(R.string.alert_message_reload)
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.reload) { dialog, _ ->
                        dialog.dismiss()
                        webView?.loadUrl("javascript:window.location.reload(true)")
                    }
                    .show()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        webView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        webView?.onPause()
    }

    override fun onDestroy() {
        jsBridge.dispose()

        interactor?.dispose()
        interactor = null

        if (downloadStateReceiver != null) {
            try {
                unregisterReceiver(downloadStateReceiver)
            } catch (_: IllegalArgumentException) {
            }
            downloadStateReceiver = null
        }

        pendingDownloads?.clear()
        pendingDownloads = null

        downloadedFiles?.clear()
        downloadedFiles = null

        super.onDestroy()

        webView?.destroy()
    }

    private fun setupActionBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        setupActionBar(toolbar, isBackButtonEnabled = true) {
            onBackPressed()
        }
    }

    private fun setupWebView() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            webView?.settings?.let {
                WebSettingsCompat.setForceDark(
                    it,
                    WebSettingsCompat.FORCE_DARK_OFF
                )
            }
        }

        webView?.init()
        webView?.setupCookieManager()
        webView?.setMixedContentAllowed(true)
        webView?.setUrlListener { headers, uri ->
            Logger.debug(
                TAG,
                "setUrlListener() -> $headers, $uri, ${uri.scheme}, ${uri.path}, ${uri.encodedPath}, ${uri.authority}"
            )

            return@setUrlListener if (uri.toString().contains("image")) {
                ImagePreviewDialogFragment.show(
                    fragmentManager = supportFragmentManager,
                    uri = uri,
                    caption = uri.toString()
                )
                true
            } else if (uri.toString().contains("video")) {
                VideoPreviewDialogFragment.show(
                    fragmentManager = supportFragmentManager,
                    uri = uri,
                    caption = uri.toString()
                )
                true
            } else resolveUri(uri)
        }

        webView?.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Logger.debug(
                TAG, "onDownloadStart() -> " +
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
                ImagePreviewDialogFragment.show(
                    fragmentManager = supportFragmentManager,
                    uri = Uri.parse(url),
                    caption = null
                )
                return@setDownloadListener
            } else if (mimetype?.startsWith("video") == true &&
                (url.endsWith("mp4") ||
                        url.endsWith("avi") ||
                        url.endsWith("mov") ||
                        url.endsWith("3gp"))
            ) {
                VideoPreviewDialogFragment.show(
                    fragmentManager = supportFragmentManager,
                    uri = Uri.parse(url),
                    caption = null
                )
                return@setDownloadListener
            }

            if (pendingDownloads == null) {
                pendingDownloads = mutableListOf()
            }
            if (url in (pendingDownloads ?: mutableListOf()).map { it.second }) {
                Toast.makeText(
                    this,
                    R.string.error_files_download_in_progress,
                    Toast.LENGTH_SHORT
                ).show()
                return@setDownloadListener
            }

            var isLocalFileFoundAndOpened = false
            val found = downloadedFiles?.find { it.first == url }
            if (found != null && !found.second.path.isNullOrBlank()) {
                val file = File(requireNotNull(found.second.path))
                Logger.debug(TAG, "file: $file")
                isLocalFileFoundAndOpened = openFile(file, mimetype)
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

            request.addRequestHeader("User-Agent", userAgent)
            request.allowScanningByMediaScanner()
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)
            request.setDescription(getString(R.string.label_files_download_in_progress, filename))
            request.setDestinationInExternalPublicDir(publicDirectory, filename)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.setRequiresCharging(false)
                request.setRequiresDeviceIdle(false)
            }
            request.setTitle(filename)

            downloadFile(request, url)
            downloadingProgressBar?.showProgress(filename)

            saveFile(
                url,
                getExternalFilesDir(publicDirectory) ?: File(Environment.DIRECTORY_DOWNLOADS),
                filename
            )

            if (downloadStateReceiver != null) {
                try {
                    unregisterReceiver(downloadStateReceiver)
                } catch (_: IllegalArgumentException) {
                }
                downloadStateReceiver = null
            }
            downloadStateReceiver = DownloadStateReceiver { downloadId, uri, mimeType ->
                Logger.debug(
                    TAG,
                    "onFileUriReady() -> " +
                            "downloadId: $downloadId, " +
                            "uri: $uri," +
                            " mimeType: $mimeType"
                )

                pendingDownloads?.removeAll { it.first == downloadId }

                downloadingProgressBar?.dismissProgressBar()

                val path = uri?.path
                if (!path.isNullOrBlank() && !mimeType.isNullOrBlank()) {
                    if (uri.scheme == "file") {
                        val file = File(path)

                        AlertDialog.Builder(this@WebViewActivity)
                            .setCancelable(true)
                            .setTitle(R.string.alert_title_files_download_completed)
                            .setMessage(
                                getString(
                                    R.string.alert_message_files_download_completed,
                                    file.name
                                )
                            )
                            .setNegativeButton(R.string.no) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(R.string.open) { dialog, _ ->
                                dialog.dismiss()
                                openFile(file, mimeType)
                            }
                            .show()
                    }
                }
            }
            registerReceiver(
                downloadStateReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        webView?.addJavascriptInterface(jsBridge, "JSBridge")

        webView?.setListener(this)
    }

    private fun resolveUri(uri: Uri): Boolean {
        URL_SCHEMES.forEach {
            if (uri.scheme?.let { uriScheme -> it.startsWith(uriScheme) } == true) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    startActivity(intent)
                    return true
                } catch (e: ActivityNotFoundException) {
                    Logger.debug(TAG, "resolveUri() -> $uri, $e")
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
//                    Logger.debug(TAG, "resolveUri() -> $uri, $e")
//                }
//            }
//        }

        if (FILE_EXTENSIONS.any { uri.path?.endsWith(it) == true }) return false

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Logger.debug(TAG, "resolveUri() -> $uri, $e")
            false
        }
    }

    private fun showRequestPermissionsAlertDialog() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.alert_title_permissions_require)
            .setMessage(R.string.alert_message_permissions_require)
            .setPositiveButton(R.string.go_to_settings) { dialog, _ ->
                dialog.dismiss()

                startActivity(
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    }
                )
            }
            .show()
    }

    private fun showGPSDisabledErrorAlertDialog() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.alert_title_permissions_require_geolocation)
            .setMessage(R.string.alert_message_permissions_require_geolocation)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()

                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    private fun downloadFile(downloadRequest: DownloadManager.Request, url: String) {
        val downloadManager = ContextCompat.getSystemService(
            applicationContext,
            DownloadManager::class.java
        )
        val id = downloadManager?.enqueue(downloadRequest)
        if (pendingDownloads == null) {
            pendingDownloads = mutableListOf()
        }
        if (id != null) {
            val found = pendingDownloads?.indexOfFirst { it.first == id }
            if (found == null || found < 0) {
                pendingDownloads?.add(id to url)
            } else {
                pendingDownloads?.set(found, id to url)
            }
        }
        Toast.makeText(this, R.string.info_files_download_started, Toast.LENGTH_LONG).show()
    }

    private fun saveFile(url: String, folder: File, filename: String) {
        val uri = Uri.withAppendedPath(Uri.fromFile(folder), filename)
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

    private fun openFile(file: File, mimeType: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        val contentUri = try {
            FileProvider.getUriForFile(
                applicationContext,
                "${packageName}.provider",
                file
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error_files_open_unable, Toast.LENGTH_SHORT).show()
            return false
        }

        grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intent.setDataAndType(contentUri, mimeType)

        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error_files_open_unable, Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * [JSBridge.Listener] implementation
     */

    override fun onClose(): Boolean {
        onBackPressed()
        return true
    }

    override fun onChangeLanguage(language: String) {
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
            progressView?.setText(getString(R.string.label_widget_loading, progress))
        } else {
            progressView?.hide()
        }
    }

    override fun onSelectFileRequest(): Boolean {
        if (STORAGE_PERMISSIONS.all {
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_media_selection)
                .setItems(
                    arrayOf(
                        getString(R.string.content_image),
                        getString(R.string.content_video),
                        getString(R.string.content_audio),
                        getString(R.string.content_document)
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
            storagePermissionsLauncher.launch(STORAGE_PERMISSIONS)
        }

        return true
    }

    override fun onPermissionRequest(resources: Array<String>) {
        val permissions = PermissionRequestMapper.fromWebClientToAndroid(resources).toTypedArray()
        Logger.debug(TAG, "onPermissionRequest() -> resources: ${resources.contentToString()}")
        Logger.debug(TAG, "onPermissionRequest() -> permissions: ${permissions.contentToString()}")
        requestedPermissionsLauncher.launch(permissions)
    }

    override fun onPermissionRequestCanceled(resources: Array<String>) {
        Logger.debug(
            TAG,
            "onPermissionRequestCanceled() -> resources: ${resources.contentToString()}"
        )
    }

    override fun onGeolocationPermissionsShowPrompt() {
        Logger.debug(TAG, "onGeolocationPermissionsShowPrompt()")
        if (LOCATION_PERMISSIONS.all {
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            val locationManager = ContextCompat.getSystemService(this, LocationManager::class.java)
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
        Logger.debug(TAG, "onGeolocationPermissionsHidePrompt()")
    }

}