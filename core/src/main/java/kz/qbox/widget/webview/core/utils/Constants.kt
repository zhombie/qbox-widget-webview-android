package kz.qbox.widget.webview.core.utils

import android.webkit.WebView

internal object Constants {

    val URL_SCHEMES = arrayOf(
        WebView.SCHEME_TEL,
        WebView.SCHEME_MAILTO,
        WebView.SCHEME_GEO,
        "sms:",
        "smsto:",
        "mms:",
        "mmsto:"
    )

//     val SHORTEN_LINKS = arrayOf(
//        "t.me",
//        "telegram.me",
//        "telegram.dog",
//        "vk.com",
//        "vk.cc",
//        "fb.me",
//        "facebook.com",
//        "fb.com"
//    )

    val FILE_EXTENSIONS = arrayOf(
        ".dot",
        ".rtf",
        ".bmp",
        ".html",
        ".csv",
        ".xml",
        ".txt",
        ".pdf",

        ".doc",
        ".docx",
        ".ppt",
        ".pptx",
        ".xls",
        ".xlsx",

        ".rar",
        ".zip",
    )

}