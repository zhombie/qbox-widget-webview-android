package kz.qbox.widget.webview.core.models

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

data class User constructor(
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null,
    val iin: String? = null,
    val phoneNumber: String? = null
) : JSONObjectable, Serializable {

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("first_name", firstName)
            jsonObject.put("last_name", lastName)
            jsonObject.put("patronymic", patronymic)
            jsonObject.put("iin", iin)
            jsonObject.put("phone_number", phoneNumber)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}