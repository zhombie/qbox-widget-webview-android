package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

data class Call constructor(
    val domain: String? = null,
    val type: Type,
    val topic: String
) : JSONObjectable, Serializable {

    enum class Type constructor(val value: String) {
        AUDIO("audio"),
        VIDEO("video");

        override fun toString(): String {
            return value
        }
    }

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("domain", domain)
            jsonObject.put("type", type.value)
            jsonObject.put("topic", topic)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}