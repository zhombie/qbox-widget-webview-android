package kz.qbox.widget.webview.sample

import android.app.DownloadManager
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentContainerView
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.Flavor
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.core.ui.presentation.Callback
import kz.qbox.widget.webview.core.ui.presentation.WebViewFragment
import kz.qbox.widget.webview.sample.utils.IntentCompat
import java.util.Locale

class TestActivity : AppCompatActivity() {

    private var contentView: LinearLayout? = null
    private var fragmentContainerView: FragmentContainerView? = null
    private var reloadButton: Button? = null

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
        setContentView(R.layout.activity_test)

        fragmentContainerView = findViewById(R.id.fragmentContainerView)
        contentView = findViewById(R.id.contentView)
        reloadButton = findViewById(R.id.reload_button)

        setupFragmentContainer()
        setupReloadButton()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        callback?.onBackPressed { super.onBackPressed() }
    }

    override fun onUserLeaveHint() {
        callback?.onUserLeaveHint { super.onUserLeaveHint() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration
    ) {
        if (isInPictureInPictureMode) {
            contentView?.visibility = View.GONE
        } else {
            contentView?.visibility = View.VISIBLE
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
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

    private fun setupReloadButton(){
        reloadButton?.setOnClickListener{
            callback?.onReload()
        }
    }
}