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

    private val paramsMap = mapOf(
        BuildConfig.FIRST_WIDGET_TITLE to
                Params(
                    title = BuildConfig.FIRST_WIDGET_TITLE,
                    url = BuildConfig.FIRST_WIDGET_URL,
                    call = Call(
                        domain = DEFAULT_DOMAIN,
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                ),
        BuildConfig.SECOND_WIDGET_TITLE to
                Params(
                    title = BuildConfig.SECOND_WIDGET_TITLE,
                    url = BuildConfig.SECOND_WIDGET_URL,
                    call = Call(
                        domain = DEFAULT_DOMAIN,
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                ),
        BuildConfig.THIRD_WIDGET_TITLE to
                Params(
                    title = BuildConfig.THIRD_WIDGET_TITLE,
                    url = BuildConfig.THIRD_WIDGET_URL,
                    call = Call(
                        domain = DEFAULT_DOMAIN,
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                ),
        BuildConfig.FOURTH_WIDGET_TITLE to
                Params(
                    title = BuildConfig.FOURTH_WIDGET_TITLE,
                    url = BuildConfig.FOURTH_WIDGET_URL,
                    call = Call(
                        domain = DEFAULT_DOMAIN,
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                ),
        BuildConfig.FIFTH_WIDGET_TITLE to
                Params(
                    title = BuildConfig.FIFTH_WIDGET_TITLE,
                    url = BuildConfig.FIFTH_WIDGET_URL,
                ),
        BuildConfig.SIXTH_WIDGET_TITLE to
                Params(
                    title = BuildConfig.SIXTH_WIDGET_TITLE,
                    url = BuildConfig.SIXTH_WIDGET_URL,
                ),
        BuildConfig.SEVENTH_WIDGET_TITLE to
                Params(
                    title = BuildConfig.SEVENTH_WIDGET_TITLE,
                    url = BuildConfig.SEVENTH_WIDGET_URL,
                ),
        BuildConfig.EIGHTH_WIDGET_TITLE to
                Params(
                    title = BuildConfig.EIGHTH_WIDGET_TITLE,
                    url = BuildConfig.EIGHTH_WIDGET_URL,
                )
    )

    private var selected: Pair<String, Params> = paramsMap.entries.first().toPair()

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(button) {
            text = "Launch: " + selected.first
            setOnClickListener {
                launchWidget(selected.second)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun launchWidget(widget: Params) {
        val calendar = Calendar.getInstance()
        calendar.set(1970, 1, 1)

        if (widget.call == null) {
            Widget.Builder.FullSuite(this)
                .setLoggingEnabled(true)
                .setUrl(widget.url)
                .setLanguage(Language.RUSSIAN)
                .launch()
        } else {
            Widget.Builder.VideoCall(this)
                .setLoggingEnabled(true)
                .setUrl(widget.url)
                .setCall(call = widget.call)
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