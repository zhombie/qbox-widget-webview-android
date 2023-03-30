package kz.qbox.widget.webview.core.models

enum class AppState {
    START,
    STOP;

    companion object {
        fun of(value: String): AppState? {
            try {
                return AppState.valueOf(value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}