package kz.qbox.widget.webview.core.models

enum class CallState {
    START,
    FINISH;

    companion object {
        fun of(value: String): CallState? {
            try {
                return CallState.valueOf(value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}