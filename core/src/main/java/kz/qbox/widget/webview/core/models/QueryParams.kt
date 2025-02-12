package kz.qbox.widget.webview.core.models

import org.json.JSONObject

data class QueryParams constructor(
    val value: Map<String, String>
) : Base() {

    constructor(vararg pairs: Pair<String, String>) : this(mapOf(*pairs))

    override fun toJSONObject(): JSONObject {
        try {
            return JSONObject(value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return JSONObject()
    }

}