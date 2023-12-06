package kz.qbox.widget.webview.sample

import android.app.DownloadManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.CallState
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.Flavor
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.core.ui.presentation.Listener
import kz.qbox.widget.webview.core.ui.presentation.WebViewFragment
import kz.qbox.widget.webview.sample.utils.IntentCompat
import java.util.Locale

class SampleActivity : AppCompatActivity(), Widget.Listener {

    private var contentView: LinearLayout? = null
    private var fragmentContainerView: FragmentContainerView? = null
    private var sampleButton: Button? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        fragmentContainerView = findViewById(R.id.fragmentContainerView)
        contentView = findViewById(R.id.contentView)
        sampleButton = findViewById(R.id.sampleButton)

//        getFragment()?.onBackPressed {
//            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    getFragment()?.onBackPressed { onBackPressedDispatcher.onBackPressed() }
//                }
//            })
//        }

        setupFragmentContainer()
        setupReloadButton()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        getFragment()?.onBackPressed { super.onBackPressed() }
    }

    override fun onUserLeaveHint() {
        getFragment()?.onUserLeaveHint { super.onUserLeaveHint() }
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
        val fragment = WebViewFragment.newInstance(
            flavor = flavor,
            url = url,
            language = language,
            call = call,
            user = user,
            dynamicAttrs = dynamicAttrs
        )
        fragment.setListener(this)
        supportFragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            add(R.id.fragmentContainerView, fragment)
            commit()
        }
    }

    private fun getFragment(): Listener? {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        if (fragment is Listener) {
            return fragment
        }
        return null
    }

    private fun setupReloadButton() {
        sampleButton?.setOnClickListener {
            Toast.makeText(this, "Hello, World!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * [Widget.Listener] implementation
     */
    override fun onCallState(state: CallState) {
        Log.d(SampleActivity::class.java.simpleName, "onCallState() -> state: $state")
    }
}