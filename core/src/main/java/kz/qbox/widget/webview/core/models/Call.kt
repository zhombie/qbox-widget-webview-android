package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject

data class Call constructor(
    val domain: String? = null,
    val type: Type,
    val topic: String = "general",
    val location: Location? = null,
    val dynamicAttrs: DynamicAttrs? = null
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
            jsonObject.put("location", location?.toJSONObject())

            dynamicAttrs?.value?.forEach {
                jsonObject.put(it.key, it.value)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}