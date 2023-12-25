@file:Suppress("NOTHING_TO_INLINE")

package kz.qbox.widget.webview.core.utils

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import androidx.core.content.ContextCompat
import kz.qbox.widget.webview.core.Logger


internal inline fun Context.isPermissionGranted(permission: String): Boolean =
    checkPermissionCompat(permission, PackageManager.PERMISSION_GRANTED)

internal inline fun Context.isPermissionDenied(permission: String): Boolean =
    checkPermissionCompat(permission, PackageManager.PERMISSION_DENIED)

internal inline fun Context.checkPermissionCompat(permission: String, result: Int): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == result

// ---

val LOCATION_PERMISSIONS = arrayOf(
    ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
)

val STORAGE_READ_PERMISSIONS: Array<String>
    get() = when {
        SDK_INT >= UPSIDE_DOWN_CAKE -> arrayOf(
            READ_MEDIA_IMAGES,
            READ_MEDIA_VIDEO,
            READ_MEDIA_VISUAL_USER_SELECTED
        )

        SDK_INT >= TIRAMISU -> arrayOf(
            READ_MEDIA_IMAGES, READ_MEDIA_VIDEO
        )

        else -> arrayOf(
            READ_EXTERNAL_STORAGE
        )
    }

val STORAGE_WRITE_PERMISSIONS: Array<String>
    get() = arrayOf(WRITE_EXTERNAL_STORAGE)

val STORAGE_PERMISSIONS: Array<String>
    get() = STORAGE_READ_PERMISSIONS + STORAGE_WRITE_PERMISSIONS


internal fun Context.isStorageReadPermissionsGranted(): Boolean = when {
    SDK_INT >= TIRAMISU && (isPermissionGranted(READ_MEDIA_IMAGES) || isPermissionGranted(READ_MEDIA_VIDEO)) -> {
        Logger.debug("QBox", "SDK_INT >= TIRAMISU && (isPermissionGranted(READ_MEDIA_IMAGES) || isPermissionGranted(READ_MEDIA_VIDEO))")
        true
        // Full access on Android 13 (API level 33) or higher
    }
    SDK_INT >= UPSIDE_DOWN_CAKE && isPermissionGranted(READ_MEDIA_VISUAL_USER_SELECTED) -> {
        Logger.debug("QBox", "SDK_INT >= UPSIDE_DOWN_CAKE && isPermissionGranted(READ_MEDIA_VISUAL_USER_SELECTED)")
        // Partial access on Android 14 (API level 34) or higher
        true
    }
    else -> {
        Logger.debug("QBox", "isPermissionGranted(READ_EXTERNAL_STORAGE)")
        // Full access up to Android 12 (API level 32)
        isPermissionGranted(READ_EXTERNAL_STORAGE)
    }
}

internal fun Context.isStorageWritePermissionsGranted(): Boolean =
    isPermissionGranted(WRITE_EXTERNAL_STORAGE)

internal fun Context.isStoragePermissionsGranted(): Boolean =
//    isStorageReadPermissionsGranted() && isStorageWritePermissionsGranted()
    isStorageReadPermissionsGranted()
