package kz.qbox.widget.webview.core.models

import org.json.JSONObject

interface JSONObjectable {
    fun toJSONObject(): JSONObject
}