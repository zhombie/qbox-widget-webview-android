package kz.qbox.widget.webview.sample

import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kz.qbox.widget.webview.core.Widget
import kz.qbox.widget.webview.core.models.*
import java.util.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchWidget()

        findViewById<MaterialButton>(R.id.button).setOnClickListener {
            launchWidget()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun launchWidget() {
        val calendar = Calendar.getInstance()
        calendar.set(1970, 1, 1)

        Widget.Builder.VideoCall(this)
            .setLoggingEnabled(true)
            .setUrl(BuildConfig.WIDGET_URL)
            .setLanguage(Language.RUSSIAN)
            .setCall(
                Call(
                    domain = "test.kz",
                    type = Call.Type.VIDEO,
                    topic = "dev",
                )
            )
            .setUser(
                User(
                    id = 1
                )
            )
            .launch()
    }

}