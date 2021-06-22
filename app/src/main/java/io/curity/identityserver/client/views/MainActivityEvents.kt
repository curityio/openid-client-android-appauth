package io.curity.identityserver.client.views

import android.content.Intent
import io.curity.identityserver.client.errors.ApplicationException

/*
 * UI actions are triggered via view models which call back to the view via an events interface
 */
interface MainActivityEvents {

    fun startLoginRedirect(intent: Intent)

    fun onLoginSuccess()

    fun startLogoutRedirect(intent: Intent)

    fun onLogoutSuccess()

    fun handleError(ex: ApplicationException)
}