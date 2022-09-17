package kz.qbox.widget.webview.core

import android.Manifest.permission.*
import android.webkit.PermissionRequest

object PermissionRequestMapper {

    fun fromAndroidToWebClient(permissions: Map<String, Boolean>): List<String> {
        val resources = mutableListOf<String>()
        if (RECORD_AUDIO in permissions.keys || MODIFY_AUDIO_SETTINGS in permissions.keys) {
            resources.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
        }
        if (CAMERA in permissions.keys) {
            resources.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        }
        return resources
    }

    fun fromWebClientToAndroid(resources: Array<String>): List<String> {
        val permissions = mutableListOf<String>()
        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in resources) {
            permissions.add(RECORD_AUDIO)
            permissions.add(MODIFY_AUDIO_SETTINGS)
        }
        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in resources) {
            permissions.add(CAMERA)
        }
        return permissions
    }

}