package kz.qbox.widget.webview.core.utils

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import kz.garage.image.preview.ImagePreviewDialogFragment

fun FragmentActivity.showImagePreview(
    uri: String,
    caption: String? = null
): ImagePreviewDialogFragment {
    return showImagePreview(
        uri = Uri.parse(uri),
        caption = caption
    )
}

fun FragmentActivity.showImagePreview(
    uri: Uri,
    caption: String? = null
): ImagePreviewDialogFragment {
    return ImagePreviewDialogFragment.show(
        fragmentManager = supportFragmentManager,
        uri = uri,
        caption = caption
    )
}
