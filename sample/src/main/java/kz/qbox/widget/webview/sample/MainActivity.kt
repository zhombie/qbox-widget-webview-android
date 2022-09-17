package kz.qbox.widget.webview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kz.qbox.widget.webview.core.WebViewActivity
import kz.qbox.widget.webview.core.Widget

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Widget.isLoggingEnabled = true

        startActivity(
            WebViewActivity.newIntent(
                context = this,
                url = BuildConfig.WIDGET_URL
            )
        )
    }

}