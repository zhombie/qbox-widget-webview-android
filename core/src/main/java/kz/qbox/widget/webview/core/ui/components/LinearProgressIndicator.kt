package kz.qbox.widget.webview.core.ui.components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import kz.qbox.widget.webview.core.R

class LinearProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val progressBar: ProgressBar
    private val leftSideProgress: TextView
    private val rightSideProgress: TextView

    init {
        val view = inflate(context, R.layout.qbox_widget_linear_progress_view, this)

        progressBar = view.findViewById(R.id.progressBar)
        leftSideProgress = view.findViewById(R.id.leftSideProgress)
        rightSideProgress = view.findViewById(R.id.rightSideProgress)
    }

    fun setProgress(progress: Int) {
        leftSideProgress.text = "$progress%"
        rightSideProgress.text = "$progress/100"
        progressBar.progress = progress
    }


}