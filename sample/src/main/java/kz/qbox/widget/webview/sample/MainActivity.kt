package kz.qbox.widget.webview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.Language
import kz.qbox.widget.webview.core.models.User

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchWidget()

        findViewById<MaterialButton>(R.id.button).setOnClickListener {
            launchWidget()
        }
    }

    private fun launchWidget() {
        Widget.Builder.FullSuite(this)
            .setLoggingEnabled(true)
            .setUrl(BuildConfig.WIDGET_URL)
            .setLanguage(Language.RUSSIAN)
            .setCall(
                Call(
                    domain = "dev.test.kz",
                    type = Call.Type.VIDEO,
                    topic = "general"
                )
            )
            .setUser(
                User(
                    firstName = "First name",
                    lastName = "Last name",
                    iin = "901020304050",
                    phoneNumber = "77771234567"
                )
            )
            .launch()
    }

}