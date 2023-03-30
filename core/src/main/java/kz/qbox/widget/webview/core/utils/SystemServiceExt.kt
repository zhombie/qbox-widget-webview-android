package kz.qbox.widget.webview.core.utils

import android.app.ActivityManager
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.location.LocationManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

internal inline val Context.activityManager: ActivityManager?
    get() = getSystemServiceCompat(ActivityManager::class.java)

internal inline val Context.audioManager: AudioManager?
    get() = getSystemServiceCompat(AudioManager::class.java)

internal inline val Context.clipboardManager: ClipboardManager?
    get() = getSystemServiceCompat(ClipboardManager::class.java)

internal inline val Context.downloadManager: DownloadManager?
    get() = getSystemServiceCompat(DownloadManager::class.java)

internal inline val Context.locationManager: LocationManager?
    get() = getSystemServiceCompat(LocationManager::class.java)

internal inline val Context.telephonyManager: TelephonyManager?
    get() = getSystemServiceCompat(TelephonyManager::class.java)

internal inline val Context.wifiManager: WifiManager?
    get() = getSystemServiceCompat(WifiManager::class.java)

internal inline fun <reified T> Context.getSystemServiceCompat(serviceClass: Class<T>): T? =
    ContextCompat.getSystemService(this, serviceClass)