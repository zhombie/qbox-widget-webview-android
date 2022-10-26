package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject

data class Call constructor(
    val domain: String? = null,
    val type: Type,
    val topic: String = "general",
    val extra: Map<String, Any>? = null
) : Base() {

    enum class Type constructor(val value: String) {
        TEXT("text"),
        AUDIO("audio"),
        VIDEO("video");

        override fun toString(): String = value
    }

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("domain", domain)
            jsonObject.put("type", type.value)
            jsonObject.put("topic", topic)

            extra?.forEach {
                val (k, v) = it
                jsonObject.put(k, v)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}