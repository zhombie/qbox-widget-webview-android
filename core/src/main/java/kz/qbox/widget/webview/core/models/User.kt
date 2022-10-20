package kz.qbox.widget.webview.core.models

import java.io.Serializable

data class User constructor(
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null,
    val iin: String? = null,
    val phoneNumber: String? = null
) : Serializable