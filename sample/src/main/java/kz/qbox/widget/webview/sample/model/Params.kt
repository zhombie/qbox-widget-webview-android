package kz.qbox.widget.webview.sample.model

import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.QueryParams

data class Params constructor(
    val title: String,
    val url: String,
    val queryParams: QueryParams? = null,
    val call: Call? = null,
)