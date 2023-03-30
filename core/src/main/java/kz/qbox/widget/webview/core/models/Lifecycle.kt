package kz.qbox.widget.webview.core.models

class Lifecycle {
    enum class State() {
        START,
        FINISH;

        companion object {
            fun of(value: String): State? {
                try {
                    return State.valueOf(value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }
}