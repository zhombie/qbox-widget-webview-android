package kz.qbox.widget.webview.core.models

import android.text.format.DateFormat
import org.json.JSONException
import org.json.JSONObject
import java.util.*

data class User constructor(
    val id: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null,
    val birthdate: Date? = null,
    val iin: String? = null,
    val phoneNumber: String? = null,
    val photo: String? = null,
    val dynamicAttrs: DynamicAttrs? = null
) : Base() {

    override fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("user_id", id)
            jsonObject.put("first_name", firstName)
            jsonObject.put("last_name", lastName)
            jsonObject.put("patronymic", patronymic)
            jsonObject.put(
                "birthdate",
                if (birthdate == null) null
                else DateFormat.format("dd.MM.yyyy", birthdate)
            )
            jsonObject.put("iin", iin)
            jsonObject.put("phone", phoneNumber)
            jsonObject.put("photo", photo)

            dynamicAttrs?.value?.forEach {
                jsonObject.put(it.key, it.value)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

}