package kz.qbox.widget.webview.core.ui.presentation

import android.app.DownloadManager
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebView.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import kz.qbox.widget.webview.core.R
import kz.qbox.widget.webview.core.models.*
import kz.qbox.widget.webview.core.utils.*
import java.util.*

private val TAG = WebViewActivity::class.java.simpleName

class WebViewActivity : AppCompatActivity() {

    companion object {
        fun newIntent(
            context: Context,
            flavor: Flavor,
            url: String,
            language: String?,
            call: Call?,
            user: User?,
            dynamicAttrs: DynamicAttrs?,
        ): Intent = Intent(context, WebViewActivity::class.java)
            .putExtra("flavor", flavor)
            .putExtra("url", url)
            .putExtra("language", language)
            .putExtra("call", call)
            .putExtra("user", user)
            .putExtra("dynamic_attrs", dynamicAttrs)
    }

    private var toolbar: Toolbar? = null
    private var contentView: LinearLayout? = null
    private var fragmentContainerView: FragmentContainerView? = null

    /**
     * [DownloadManager] download ids list (which has downloading status)
     */

    private val language by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("language") ?: Locale.getDefault().language
    }

    private val flavor by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getEnum<Flavor>(intent, "flavor") ?: throw IllegalStateException()
    }

    private val url by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("url") ?: throw IllegalStateException()
    }

    private val call by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<Call>(intent, "call")
    }

    private val user by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<User>(intent, "user")
    }

    private val dynamicAttrs by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<DynamicAttrs>(intent, "dynamic_attrs")
    }

    private var callback: Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qbox_widget_activity_webview)

        toolbar = findViewById(R.id.toolbar)
        fragmentContainerView = findViewById(R.id.fragmentContainerView)
        contentView = findViewById(R.id.contentView)

        setupFragmentContainer()
        setupActionBar()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        callback?.onBackPressed { super.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.qbox_widget_webview, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reload -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.qbox_widget_alert_title_reload)
                    .setMessage(R.string.qbox_widget_alert_message_reload)
                    .setNegativeButton(R.string.qbox_widget_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.qbox_widget_reload) { dialog, _ ->
                        dialog.dismiss()
                        callback?.onReload()
                    }
                    .show()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onUserLeaveHint() {
        callback?.onUserLeaveHint { super.onUserLeaveHint() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        if (isInPictureInPictureMode) {
            contentView?.visibility = View.GONE
        } else {
            contentView?.visibility = View.VISIBLE
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    private fun setupActionBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        setupActionBar(toolbar, isBackButtonEnabled = true) {
            onBackPressed()
        }
    }

    private fun setupFragmentContainer() {
        val fragment = WebViewFragment.newInstance(flavor, url, language, call, user, dynamicAttrs)
        callback = fragment.callbackInstance

        supportFragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            add(R.id.fragmentContainerView, fragment)
            commit()
        }
    }

}