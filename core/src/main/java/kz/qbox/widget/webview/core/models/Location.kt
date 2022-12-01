package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject

data class Location constructor(
    val latitude: Double,
    val longitude: Double
) : Base() {

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("lat", latitude)
            jsonObject.put("lon", longitude)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}