package kz.qbox.widget.webview.core.system

import android.app.ActivityManager
import android.content.Context
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

inline val Context.activityManager: ActivityManager?
    get() = getSystemServiceCompat(ActivityManager::class.java)

inline val Context.audioManager: AudioManager?
    get() = getSystemServiceCompat(AudioManager::class.java)

inline val Context.telephonyManager: TelephonyManager?
    get() = getSystemServiceCompat(TelephonyManager::class.java)

inline val Context.wifiManager: WifiManager?
    get() = getSystemServiceCompat(WifiManager::class.java)

inline fun <reified T> Context.getSystemServiceCompat(serviceClass: Class<T>): T? =
    ContextCompat.getSystemService(this, serviceClass)