package kz.qbox.widget.webview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kz.qbox.widget.webview.core.WebViewActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(
            WebViewActivity.newIntent(
                this,
                "https://kenes.vlx.kz/widget/external/enis?topic=enotary_warranty"
            )
        )
    }

}