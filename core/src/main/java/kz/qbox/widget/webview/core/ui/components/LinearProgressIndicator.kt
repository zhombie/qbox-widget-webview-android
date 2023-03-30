package kz.qbox.widget.webview.core.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import kz.qbox.widget.webview.core.R

internal class LinearProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val progressBar: ProgressBar
    private val leftSideProgress: AppCompatTextView
    private val rightSideProgress: AppCompatTextView

    init {
        val view = inflate(context, R.layout.qbox_widget_linear_progress_view, this)

        progressBar = view.findViewById(R.id.progressBar)
        leftSideProgress = view.findViewById(R.id.leftSideProgress)
        rightSideProgress = view.findViewById(R.id.rightSideProgress)
    }

    @SuppressLint("SetTextI18n")
    fun setProgress(progress: Int) {
        leftSideProgress.text = "$progress%"
        rightSideProgress.text = "$progress/100"
        progressBar.progress = progress
    }

}