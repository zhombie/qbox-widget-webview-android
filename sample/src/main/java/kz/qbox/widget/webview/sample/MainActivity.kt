package kz.qbox.widget.webview.sample

import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.CallState
import kz.qbox.widget.webview.core.models.DynamicAttrs
import kz.qbox.widget.webview.core.models.Language
import kz.qbox.widget.webview.core.models.QueryParams
import kz.qbox.widget.webview.core.models.UI
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
            Log.d("QBox-MainActivity", "Set selectedTopic -> value: $value")
            field = value
            topicTextView.text = value
        }

    private var inputPhoneNumber: String = BuildConfig.CALL_PHONE_NUMBER
        set(value) {
            Log.d("QBox-MainActivity", "Set inputPhoneNumber -> value: $value")
            field = value
            phoneNumberTextView.text = value
        }

    private var destination: String = BuildConfig.CALL_DESTINATION
        set(value) {
            Log.d("QBox-MainActivity", "Set destination -> value: $value")
            field = value
            destinationTextView.text = value
        }

    private var token: String? = null

    private val projects = parseProjects()

    private var selectedProject: Pair<String, Params> = projects.entries.last().toPair()
        set(value) {
            Log.d("QBox-MainActivity", "Set selectedProject -> value: $value")
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

        lifecycleScope.launch(Dispatchers.IO) {
            val response = HTTPClient.generateToken(
                HTTPClient.GenerateTokenParams(
                    inputPhoneNumber,
                    destination
                )
            )
            Log.d("QBox-MainActivity", "HTTPClient.generateToken() -> response: $response")
            token = response?.token
        }

        launchButton.setOnClickListener {
            launchWidget()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        HTTPClient.close()
    }

    private fun setupProjectSwitchButton() {
        projectSwitchButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Project")
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

        Log.d("QBox-MainActivity", "launchWidget() -> key: $key, params: $params")

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

        when (flavor) {
            "full-suite" -> {
                Widget.Builder.FullSuite(this)
                    .setLoggingEnabled(true)
                    .setUrl(url)
                    .setQueryParams(params.queryParams)
                    .setLanguage(Language.KAZAKH)
                    .setUser(exampleCustomer)
//                    .setCustomActivity(SampleActivity::class.java)
                    .setListener(this)
                    .launch()
            }

            "audio-call" -> {
                val token = token

                if (token.isNullOrBlank()) {
                    return Toast.makeText(this, "token is null or blank!", Toast.LENGTH_SHORT)
                        .show()
                }

                Widget.Builder.AudioCall(this)
                    .setLoggingEnabled(false)
                    .setUrl(url)
                    .setToken(token)
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
                    .setUI(
                        UI(
                            readinessCheckText = "Hello, World!"
                        )
                    )
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
                queryParams = QueryParams(
                    "readiness_check_text" to "Просим подготовить удостоверение личности"
                ),
                call = Call(
                    domain = DEFAULT_DOMAIN,
                    type = Call.Type.VIDEO,
                    topic = selectedTopic,
                    phoneNumber = inputPhoneNumber,
                    destination = destination,
                    dynamicAttrs = DynamicAttrs(
                        "digit" to 1,
                        "foo" to "bar",
                    )
                )
            )
        }
        return paramsMap
    }

    /**
     * [Widget.Listener] implementation
     */
    override fun onCallState(state: CallState) {
        Log.d("QBox-MainActivity", "onCallState() -> state: $state")
    }

}