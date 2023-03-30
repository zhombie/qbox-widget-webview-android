package kz.qbox.widget.webview.core.utils

import android.Manifest
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
        ".pptx",
        ".rtf",
        ".xlsx",
        ".xls",
        ".bmp",
        ".csv",
        ".html",
        ".xml",
        ".txt",
        ".pdf",
        ".doc",
        ".zip",
        ".rar",
        ".docx",
    )

    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

}