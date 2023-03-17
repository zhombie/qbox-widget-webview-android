package kz.qbox.widget.webview.sample

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.*
import kz.qbox.widget.webview.sample.model.Params
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_DOMAIN = "test.kz"
    }

    private val button by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.button)
    }

    private val paramsMap = mutableMapOf<String, Params>()

    private lateinit var selected: Pair<String, Params>

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.WIDGET_TITLES.size == BuildConfig.WIDGET_LINKS.size) {
            BuildConfig.WIDGET_TITLES.forEachIndexed { index, item ->
                paramsMap[item] = Params(
                    title = item,
                    url = BuildConfig.WIDGET_LINKS[index],
                    call = if (index <= 3) null else Call(
                        domain = DEFAULT_DOMAIN,
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                )
            }
            selected = paramsMap.entries.first().toPair()
        }

        with(button) {
            text = "Launch: " + selected.first
            setOnClickListener {
                launchWidget(selected.second)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun launchWidget(params: Params) {
        val calendar = Calendar.getInstance()
        calendar.set(1970, 1, 1)

        if (params.call == null) {
            Widget.Builder.FullSuite(this)
                .setLoggingEnabled(true)
                .setUrl(params.url)
                .setLanguage(Language.RUSSIAN)
                .launch()
        } else {
            Widget.Builder.VideoCall(this)
                .setLoggingEnabled(true)
                .setUrl(params.url)
                .setCall(call = params.call)
                .setLanguage(Language.RUSSIAN)
                .launch()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        paramsMap.keys.forEach { menu?.add(it) }
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        button?.text = "Launch: " + item.title

        val key = item.title
        selected = if (key == null) {
            paramsMap.entries.first().toPair()
        } else {
            val value = paramsMap[key]
            if (value == null) {
                paramsMap.entries.first().toPair()
            } else {
                key.toString() to value
            }
        }

        return super.onOptionsItemSelected(item)
    }

}