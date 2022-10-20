package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

data class User constructor(
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null,
    val iin: String? = null,
    val phoneNumber: String? = null,
    val device: Device? = null
) : JSONObjectable, Serializable {

    data class Device constructor(
        private val os: String,
        private val osVersion: String,
        private val appVersion: String?,
        private val name: String,
        private val mobileOperator: String?,
        private val battery: Battery
    ) : JSONObjectable {

        data class Battery constructor(
            private val percentage: Double,
            private val isCharging: Boolean,
            private val temperature: Float
        ) : JSONObjectable {

            override fun toJSONObject(): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("percentage", percentage)
                    jsonObject.put("is_charging", isCharging)
                    jsonObject.put("temperature", temperature)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                return jsonObject
            }

        }

        override fun toJSONObject(): JSONObject {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("os", os)
                jsonObject.put("osVersion", osVersion)
                jsonObject.put("appVersion", appVersion)
                jsonObject.put("name", name)
                jsonObject.put("mobileOperator", mobileOperator)
                jsonObject.put("battery", battery.toJSONObject())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return jsonObject
        }

    }

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("first_name", firstName)
            jsonObject.put("last_name", lastName)
            jsonObject.put("patronymic", patronymic)
            jsonObject.put("iin", iin)
            jsonObject.put("phone_number", phoneNumber)
            jsonObject.put("device", device?.toJSONObject())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}