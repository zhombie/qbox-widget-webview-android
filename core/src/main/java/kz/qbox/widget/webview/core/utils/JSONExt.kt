package kz.qbox.widget.webview.core.utils

import kz.qbox.widget.webview.core.models.JSONObjectable

fun JSONObjectable.encode(): String =
    toJSONObject().toString(4)