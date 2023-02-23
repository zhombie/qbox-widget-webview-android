package kz.qbox.widget.webview.sample

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
import kz.qbox.widget.webview.sample.model.WidgetModel
import java.util.*

class MainActivity : AppCompatActivity() {

    private var widgetModel: WidgetModel = WidgetModel(
        title = BuildConfig.FIRST_WIDGET_TITLE,
        url = BuildConfig.FIRST_WIDGET_URL,
        call = Call(
            domain = "test.kz",
            type = Call.Type.VIDEO,
            topic = BuildConfig.CALL_TOPIC
        )
    )

    private val widgetTitles = listOf(
        BuildConfig.FIRST_WIDGET_TITLE,
        BuildConfig.SECOND_WIDGET_TITLE,
        BuildConfig.THIRD_WIDGET_TITLE,
        BuildConfig.FOURTH_WIDGET_TITLE,
        BuildConfig.FIFTH_WIDGET_TITLE,
        BuildConfig.SIXTH_WIDGET_TITLE,
        BuildConfig.SEVENTH_WIDGET_TITLE,
        BuildConfig.EIGHTH_WIDGET_TITLE
    )

    private var widgetButton: MaterialButton? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        widgetButton = findViewById(R.id.button)
        widgetButton?.text = BuildConfig.FIRST_WIDGET_TITLE
        widgetButton?.setOnClickListener {
            launchWidget(widgetModel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun launchWidget(widget: WidgetModel) {
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
        widgetTitles.forEach { item ->
            menu?.add(item)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        widgetButton?.text = item.title
        widgetModel = when (item.title) {
            BuildConfig.FIRST_WIDGET_TITLE -> {
                WidgetModel(
                    title = BuildConfig.FIRST_WIDGET_TITLE,
                    url = BuildConfig.FIRST_WIDGET_URL,
                    call = Call(
                        domain = "test.kz",
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                )
            }
            BuildConfig.SECOND_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.SECOND_WIDGET_TITLE,
                    url = BuildConfig.SECOND_WIDGET_URL,
                    call = Call(
                        domain = "test.kz",
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                )
            BuildConfig.THIRD_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.THIRD_WIDGET_TITLE,
                    url = BuildConfig.THIRD_WIDGET_URL,
                    call = Call(
                        domain = "test.kz",
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                )
            BuildConfig.FOURTH_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.FOURTH_WIDGET_TITLE,
                    url = BuildConfig.FOURTH_WIDGET_URL,
                    call = Call(
                        domain = "test.kz",
                        type = Call.Type.VIDEO,
                        topic = BuildConfig.CALL_TOPIC
                    )
                )
            BuildConfig.FIFTH_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.FIFTH_WIDGET_TITLE,
                    url = BuildConfig.FIFTH_WIDGET_URL,
                )
            BuildConfig.SIXTH_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.SIXTH_WIDGET_TITLE,
                    url = BuildConfig.SIXTH_WIDGET_URL,
                )
            BuildConfig.SEVENTH_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.SEVENTH_WIDGET_TITLE,
                    url = BuildConfig.SEVENTH_WIDGET_URL,
                )
            BuildConfig.EIGHTH_WIDGET_TITLE ->
                WidgetModel(
                    title = BuildConfig.EIGHTH_WIDGET_TITLE,
                    url = BuildConfig.EIGHTH_WIDGET_URL,
                )
            else -> WidgetModel(
                title = BuildConfig.EIGHTH_WIDGET_TITLE,
                url = BuildConfig.EIGHTH_WIDGET_URL,
            )
        }
        return super.onOptionsItemSelected(item)
    }

}