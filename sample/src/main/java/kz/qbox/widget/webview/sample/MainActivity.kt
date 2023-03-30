package kz.qbox.widget.webview.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
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

    private val paramsMap = parseParams()

    private var selected: Pair<String, Params> = paramsMap.entries.first().toPair()

    @SuppressLint("SetTextI18n")
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

    private fun launchWidget(params: Params) {
        val exampleCustomer = User(
            firstName = "Shaken",
            lastName = "Aimanov",
            patronymic = "Kenzhetaiuly",
            birthdate = Date(SystemClock.currentThreadTimeMillis()),
            iin = "140215100000",
            phoneNumber = "77771234567",
            dynamicAttrs = DynamicAttrs("foo" to "bar")
        )

        if (params.call == null) {
            Widget.Builder.FullSuite(this)
                .setLoggingEnabled(true)
                .setUrl(params.url)
                .setLanguage(Language.KAZAKH)
                .setUser(exampleCustomer)
                .launch()
        } else {
            Widget.Builder.VideoCall(this)
                .setLoggingEnabled(true)
                .setUrl(params.url)
                .setLanguage(Language.KAZAKH)
                .setCall(call = params.call)
                .setUser(exampleCustomer)
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

    private fun parseParams(): Map<String, Params> {
        val paramsMap = mutableMapOf<String, Params>()
        BuildConfig.CALL_ROUTES.split(",").forEachIndexed { index, pair ->
            val (title, url) = pair.split("*")
            paramsMap[title] = Params(
                title = title,
                url = url,
                call = if (index <= 3) {
                    null
                } else {
                    Call(
                        domain = DEFAULT_DOMAIN,
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                }
            )
        }
        return paramsMap
    }

}