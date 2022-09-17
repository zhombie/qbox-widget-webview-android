package kz.qbox.widget.webview.sample

import android.app.Application
import kz.garage.image.load.ImageLoader
import kz.garage.image.load.ImageLoaderFactory
import kz.garage.image.load.coil.CoilImageLoader

class SampleApplication : Application(), ImageLoaderFactory {

    override fun getImageLoader(): ImageLoader =
        CoilImageLoader(
            context = this,
            allowHardware = true,
            crossfade = false,
            isLoggingEnabled = false
        )

}