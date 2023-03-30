package kz.qbox.widget.webview.core.models

enum class AppEvent {
    PIP_ENTER,
    PIP_EXIT;

    companion object {
        fun of(value: String): AppEvent? {
            try {
                return AppEvent.valueOf(value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}