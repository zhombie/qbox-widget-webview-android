package kz.qbox.widget.webview.core

import android.content.Context
import android.content.Intent
import kz.qbox.widget.webview.core.models.Call
import kz.qbox.widget.webview.core.models.User
import kz.qbox.widget.webview.core.ui.presentation.WebViewActivity

object Widget {

    var isLoggingEnabled: Boolean = false
        @Synchronized get
        @Synchronized set

    class Builder constructor(private val context: Context) {

        private var isLoggingEnabled: Boolean? = null
        private var url: String? = null
        private var language: String? = null
        private var call: Call? = null
        private var user: User? = null

        fun getLoggingEnabled(): Boolean = isLoggingEnabled ?: false

        fun setLoggingEnabled(isLoggingEnabled: Boolean): Builder {
            this.isLoggingEnabled = isLoggingEnabled
            return this
        }

        fun getUrl(): String? = url

        fun setUrl(url: String): Builder {
            this.url = url
            return this
        }

        fun getLanguage(): String? = language

        fun setLanguage(language: String): Builder {
            this.language = language
            return this
        }

        fun getCall(): Call? = call

        fun setCall(call: Call): Builder {
            this.call = call
            return this
        }

        fun getUser(): User? = user

        fun setUser(user: User): Builder {
            this.user = user
            return this
        }

        fun build(): Intent {
            isLoggingEnabled?.let {
                Widget.isLoggingEnabled = it
            }

            return WebViewActivity.newIntent(
                context = context,
                url = requireNotNull(url) { "Declare url, without it widget won't work!" },
                language = language,
                call = call,
                user = user
            )
        }

        fun launch(): Intent {
            val intent = build()
            context.startActivity(intent)
            return intent
        }
    }


}