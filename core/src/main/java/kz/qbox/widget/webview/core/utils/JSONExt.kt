package kz.qbox.widget.webview.core.utils

import kz.qbox.widget.webview.core.models.JSONObjectable

internal const val DEFAULT_JSON_INDENT_SPACES = 4

fun JSONObjectable.encode(): String =
    toJSONObject().toString(DEFAULT_JSON_INDENT_SPACES)