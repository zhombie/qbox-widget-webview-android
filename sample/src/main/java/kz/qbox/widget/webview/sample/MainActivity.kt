package kz.qbox.widget.webview.sample

import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.CallState
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.Language
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.sample.model.Params
import java.util.Date

class MainActivity : AppCompatActivity(), Widget.Listener {

    companion object {
        private const val DEFAULT_DOMAIN = "test.kz"
    }

    private val projectTextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialTextView>(R.id.projectTextView)
    }

    private val projectSwitchButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.projectSwitchButton)
    }

    private val topicTextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialTextView>(R.id.topicTextView)
    }

    private val topicEditButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.topicEditButton)
    }

    private val destinationTextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialTextView>(R.id.destinationTextView)
    }

    private val destinationEditButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.destinationEditButton)
    }

    private val phoneNumberTextView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialTextView>(R.id.phoneNumberTextView)
    }

    private val phoneNumberEditButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.phoneNumberEditButton)
    }

    private val launchButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<MaterialButton>(R.id.launchButton)
    }

    private var selectedTopic: String = BuildConfig.CALL_TOPIC
        set(value) {
            Log.d("QBox", "Set selectedTopic -> value: $value")
            field = value
            topicTextView.text = value
        }

    private var inputPhoneNumber: String = BuildConfig.CALL_PHONE_NUMBER
        set(value) {
            Log.d("QBox", "Set inputPhoneNumber -> value: $value")
            field = value
            phoneNumberTextView.text = value
        }

    private var destination: String = BuildConfig.CALL_DESTINATION
        set(value) {
            Log.d("QBox", "Set destination -> value: $value")
            field = value
            destinationTextView.text = value
        }

    private val projects = parseProjects()

    private var selectedProject: Pair<String, Params> = projects.entries.last().toPair()
        set(value) {
            Log.d("QBox", "Set selectedProject -> value: $value")
            field = value
            projectTextView.text = value.first
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectedProject = projects.entries.last().toPair()
        selectedTopic = BuildConfig.CALL_TOPIC
        inputPhoneNumber = BuildConfig.CALL_PHONE_NUMBER
        destination = BuildConfig.CALL_DESTINATION

        setupProjectSwitchButton()
        setupTopicEditButton()
        setupPhoneNumberEditButton()
        setupDestinationEditButton()

        launchButton.setOnClickListener {
            launchWidget()
        }
    }

    private fun setupProjectSwitchButton() {
        projectSwitchButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setItems(projects.keys.toTypedArray()) { dialog: DialogInterface, position: Int ->
                    dialog.dismiss()

                    val item = projects.entries.elementAt(position).toPair()

                    val key = item.first
                    val value = projects[key]

                    selectedProject = if (value == null) {
                        projects.entries.first().toPair()
                    } else {
                        key to value
                    }
                }
                .show()
        }
    }

    private fun setupTopicEditButton() {
        topicEditButton.setOnClickListener {
            val editText = TextInputEditText(this)
            AlertDialog.Builder(this)
                .setTitle("Topic")
                .setView(editText)
                .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                    selectedTopic = editText.text.toString()
                }
                .show()
        }
    }

    private fun setupPhoneNumberEditButton() {
        phoneNumberEditButton.setOnClickListener {
            val editText = TextInputEditText(this)
            AlertDialog.Builder(this)
                .setTitle("Phone number")
                .setView(editText)
                .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                    inputPhoneNumber = editText.text.toString()
                }
                .show()
        }
    }

    private fun setupDestinationEditButton() {
        destinationEditButton.setOnClickListener {
            val editText = TextInputEditText(this)
            AlertDialog.Builder(this)
                .setTitle("Destination")
                .setView(editText)
                .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                    destination = editText.text.toString()
                }
                .show()
        }
    }

    private fun launchWidget() {
        val key = selectedProject.first
        val params = selectedProject.second

        val (url, call) = params.url to params.call

        Log.d(MainActivity::class.java.simpleName, "launchWidget() -> key: $key, params: $params")

        val flavor = key.split(":")[1]

        val exampleCustomer = User(
            firstName = "Shaken",
            lastName = "Aimanov",
            patronymic = "Kenzhetaiuly",
            birthdate = Date(SystemClock.currentThreadTimeMillis()),
            iin = "140215100000",
            phoneNumber = "77771234567",
            dynamicAttrs = DynamicAttrs("foo" to "bar")
        )

        Widget.isLoggingEnabled = true

        when (flavor) {
            "full-suite" -> {
                Widget.Builder.FullSuite(this)
                    .setLoggingEnabled(true)
                    .setUrl(url)
                    .setLanguage(Language.KAZAKH)
                    .setUser(exampleCustomer)
//                    .setCustomActivity(SampleActivity::class.java)
                    .setListener(this)
                    .launch()
            }
            "audio-call" -> {
                Widget.Builder.AudioCall(this)
                    .setLoggingEnabled(true)
                    .setUrl(url)
                    .setLanguage(Language.KAZAKH)
                    .apply {
                        if (call != null) {
                            val copy = call.copy(
                                phoneNumber = inputPhoneNumber,
                                destination = destination
                            )
                            setCall(call = copy)
                        }
                    }
                    .setUser(exampleCustomer)
//                    .setCustomActivity(SampleActivity::class.java)
                    .setListener(this)
                    .launch()
            }
            "video-call" -> {
                Widget.Builder.VideoCall(this)
                    .setLoggingEnabled(true)
                    .setUrl(url)
                    .setLanguage(Language.KAZAKH)
                    .apply {
                        if (call != null) {
                            val copy = call.copy(
                                topic = selectedTopic
                            )
                            setCall(call = copy)
                        }
                    }
                    .setUser(exampleCustomer)
//                    .setCustomActivity(SampleActivity::class.java)
                    .setListener(this)
                    .launch()
            }
        }
    }

    private fun parseProjects(): Map<String, Params> {
        val paramsMap = mutableMapOf<String, Params>()
        BuildConfig.CALL_ROUTES.split(",").forEach { pair ->
            val (baseUrl, flavor, title, url) = pair.split("*")
            paramsMap["$baseUrl:$flavor:$title"] = Params(
                title = title,
                url = url,
                call = Call(
                    domain = DEFAULT_DOMAIN,
                    type = Call.Type.VIDEO,
                    topic = selectedTopic,
                    phoneNumber = inputPhoneNumber,
                    destination = destination,
                    dynamicAttrs = DynamicAttrs("request_id" to "123456")
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