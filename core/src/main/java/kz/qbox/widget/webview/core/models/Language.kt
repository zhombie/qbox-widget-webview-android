package kz.qbox.widget.webview.core.models

enum class Language constructor(val code: String) {
    ENGLISH("en"),
    KAZAKH("kk"),
    RUSSIAN("ru");

    override fun toString(): String = code
}