package kz.qbox.widget.webview.sample.model

import kz.qbox.widget.webview.core.models.Call

data class Params(
    val title: String,
    val url: String,
    val call: Call? = null,
)