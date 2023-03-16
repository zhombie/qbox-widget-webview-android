package kz.qbox.widget.webview.core.models

class Lifecycle {
    enum class State(val value: String) {
        STARTED("start"),
        FINISHED("finish")
    }
}