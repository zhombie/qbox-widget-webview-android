package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject

data class UI constructor(
    val primaryColor: String? = null,
    val headTitle: String? = null,

    val mainIconUrl: String? = null,
    val mainIconWidth: String? = null,
    val mainTitle: String? = null,
    val mainDescription: String? = null,

    val readinessCheckText: String? = null,
    val readinessRetryButtonText: String? = null,

    val startCallButtonText: String? = null,

    val footerText: String? = null,

    val deviceType: String? = null
) : Base() {

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("primary_color", primaryColor)
            jsonObject.put("head_title", headTitle)
            jsonObject.put("main_icon_url", mainIconUrl)
            jsonObject.put("main_icon_width", mainIconWidth)
            jsonObject.put("main_title", mainTitle)
            jsonObject.put("main_description", mainDescription)
            jsonObject.put("readiness_check_text", readinessCheckText)
            jsonObject.put("readiness_retry_button_text", readinessRetryButtonText)
            jsonObject.put("start_call_button_text", startCallButtonText)
            jsonObject.put("footer_text", footerText)
            jsonObject.put("device_type", deviceType)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}