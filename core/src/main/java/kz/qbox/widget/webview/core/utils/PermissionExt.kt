@file:Suppress("NOTHING_TO_INLINE")

package kz.qbox.widget.webview.core.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

internal inline fun Activity.isPermissionGranted(permission: String): Boolean =
    checkPermissionCompat(permission, PackageManager.PERMISSION_GRANTED)

internal inline fun Activity.isPermissionDenied(permission: String): Boolean =
    checkPermissionCompat(permission, PackageManager.PERMISSION_DENIED)

internal inline fun Activity.checkPermissionCompat(permission: String, result: Int): Boolean =
    ActivityCompat.checkSelfPermission(this, permission) == result
