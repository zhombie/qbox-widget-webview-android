package kz.qbox.widget.webview.sample

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.*
import kz.qbox.widget.webview.sample.model.Params
import java.util.*

class MainActivity : AppCompatActivity(), Widget.Listener {

    companion object {
        private const val DEFAULT_DOMAIN = "test.kz"
    }

    private val textView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialTextView>(R.id.textView)
    }

    private val switchButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.switchButton)
    }

    private val launchButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.launchButton)
    }

    private val paramsMap = parseParams()

    private var selected: Pair<String, Params> = paramsMap.entries.first().toPair()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setText(selected.first)

        switchButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setItems(paramsMap.keys.toTypedArray()) { dialog: DialogInterface, position: Int ->
                    dialog.dismiss()

                    val item = paramsMap.entries.elementAt(position).toPair()

                    setText(item.first)

                    val key = item.first
                    val value = paramsMap[key]
                    selected = if (value == null) {
                        paramsMap.entries.first().toPair()
                    } else {
                        key to value
                    }
                }
                .show()
        }

        launchButton.setOnClickListener {
            launchWidget(selected.second)
        }
    }

    private fun setText(value: String) {
        textView.text = value
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
                .setCustomActivity(SampleActivity::class.java)
                .setListener(this)
                .launch()
        } else {
            Widget.Builder.VideoCall(this)
                .setLoggingEnabled(true)
                .setUrl(params.url)
                .setLanguage(Language.KAZAKH)
                .setCall(call = params.call)
                .setUser(exampleCustomer)
                .setCustomActivity(SampleActivity::class.java)
                .setListener(this)
                .launch()
        }
    }

    private fun parseParams(): Map<String, Params> {
        val paramsMap = mutableMapOf<String, Params>()
        BuildConfig.CALL_ROUTES.split(",").forEach { pair ->
            val (title, url) = pair.split("*")
            paramsMap[title] = Params(
                title = title,
                url = url,
                call = Call(
                    domain = DEFAULT_DOMAIN,
                    type = Call.Type.VIDEO,
                    topic = BuildConfig.CALL_TOPIC,
//                    location = Location(
//                        latitude = 51.14721,
//                        longitude = 71.39069,
//                    ),
                )
            )
        }
        return paramsMap
    }

    /**
     * [Widget.Listener] implementation
     */
    override fun onCallState(state: CallState) {
        Log.d(MainActivity::class.java.simpleName, "onCallState() -> state: $state")
    }

}