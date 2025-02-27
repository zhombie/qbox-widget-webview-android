package kz.qbox.widget.webview.core.models

import org.json.JSONObject

data class QueryParams constructor(
    val value: Map<String, String>
) : Base() {

    constructor(vararg pairs: Pair<String, String>) : this(mapOf(*pairs))

    @Suppress("unused")
    companion object {
        const val PRIMARY_COLOR = "primary_color"
        const val HEAD_TITLE = "head_title"
        const val MAIN_ICON_URL = "main_icon_url"
        const val MAIN_ICON_WIDTH = "main_icon_width"
        const val MAIN_TITLE = "main_title"
        const val MAIN_DESCRIPTION = "main_description"
        const val READINESS_CHECK_TEXT = "readiness_check_text"
        const val READINESS_RETRY_BUTTON_TEXT = "readiness_retry_button_text"
        const val READINESS_CLOSE_BUTTON = "readiness_close_button"
        const val START_CALL_BUTTON_TEXT = "start_call_button_text"
        const val FOOTER_TEXT = "footer_text"
        const val DEVICE_TYPE = "device_type"
        const val CALL_MEDIA = "call_media"
        const val REQUEST_CALL_FEEDBACK = "request_call_feedback"
        const val END_SCREEN_MAIN_PAGE_BUTTON = "end_screen_main_page_button"
        const val END_SCREEN_NEW_CALL_BUTTON = "end_screen_new_call_button"
    }

    override fun toJSONObject(): JSONObject {
        try {
            return JSONObject(value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return JSONObject()
    }

}