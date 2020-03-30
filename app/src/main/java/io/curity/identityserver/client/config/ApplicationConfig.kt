package io.curity.identityserver.client.config

import android.net.Uri

object ApplicationConfig {
    val redirectUri: Uri = Uri.parse("io.curity.client:/callback")
    const val scope = "openid profile"
    val issuer: Uri = Uri.parse("https://dlindau.ngrok.io/~")
}