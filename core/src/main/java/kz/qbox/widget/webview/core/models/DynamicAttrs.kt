package kz.qbox.widget.webview.core.models

import org.json.JSONObject

data class DynamicAttrs constructor(
    val value: Map<String, Any>
) : Base() {

    constructor(vararg pairs: Pair<String, Any>) : this(mapOf(*pairs))

    override fun toJSONObject(): JSONObject {
        try {
            return JSONObject(value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return JSONObject()
    }

}