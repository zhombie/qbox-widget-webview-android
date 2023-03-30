package kz.qbox.widget.webview.core.utils

import android.database.Cursor

fun Cursor.getIntOrDefault(columnName: String, default: Int): Int {
    val columnIndex = getColumnIndex(columnName)
    if (columnIndex < 0) return default
    return getInt(columnIndex)
}
