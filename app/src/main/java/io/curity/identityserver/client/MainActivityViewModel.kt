package io.curity.identityserver.client

import android.content.Intent
import androidx.lifecycle.ViewModel
import io.curity.identityserver.client.config.ApplicationConfig
import io.curity.identityserver.client.error.ApplicationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class MainActivityViewModel() : ViewModel() {

    private lateinit var activity: MainActivity
    private lateinit var appauth: AppAuthHandler

    fun initialize(activity: MainActivity) {
        this.activity = activity
        this.appauth = AppAuthHandler(activity)
    }

    /*
     * Lookup metadata and do the dynamic client registration if required
     * Make HTTP requests on a worker thread and then perform updates on the UI thread
     */
    fun registerIfRequired() {

        CoroutineScope(Dispatchers.IO).launch {

            val model = this@MainActivityViewModel
            try {

                val serverConfiguration = appauth.fetchMetadata(ApplicationConfig.issuer)
                if (!ApplicationStateManager.isRegistered()) {
                    val registrationResponse = appauth.registerClient(serverConfiguration)

                    withContext(Dispatchers.Main) {
                        ApplicationStateManager.serverConfiguration = serverConfiguration
                        ApplicationStateManager.registrationResponse = registrationResponse
                    }
                }

            } catch (exception: ApplicationException) {

                withContext(Dispatchers.Main) {
                    model.activity.handleError(exception)
                }
            }
        }
    }

    /*
     * Build the authorization redirect URL and then ask the view to redirect
     */
    fun startLogin() {

        val intent = appauth.getAuthorizationRedirectIntent(
            ApplicationStateManager.serverConfiguration,
            ApplicationStateManager.registrationResponse)

        this.activity.startLoginRedirect(intent)
    }

    /*
     * Redeem the code for tokens and also handle failures or the user cancelling the Chrome Custom Tab
     * Make HTTP requests on a worker thread and then perform updates on the UI thread
     */
    fun endLogin(data: Intent) {

        val model = this@MainActivityViewModel
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val tokenResponse = appauth.handleAuthorizationResponse(
                    AuthorizationResponse.fromIntent(data),
                    AuthorizationException.fromIntent(data),
                    ApplicationStateManager.registrationResponse)

                withContext(Dispatchers.Main) {
                    if (tokenResponse != null) {
                        ApplicationStateManager.tokenResponse = tokenResponse
                        model.activity.onLoginSuccess()
                    }
                }

            } catch (exception: ApplicationException) {

                withContext(Dispatchers.Main) {
                    model.activity.handleError(exception)
                }
            }
        }
    }

    /*
     * Build the end session redirect URL and then ask the view to redirect
     */
    fun startLogout() {

        val intent = appauth.getEndSessionRedirectIntent(
            ApplicationStateManager.serverConfiguration,
            ApplicationStateManager.registrationResponse,
            ApplicationStateManager.tokenResponse?.idToken,
            ApplicationConfig.postLogoutRedirectUri)

        activity.startLogoutRedirect(intent)
    }

    /*
     * Clean up after logging out
     */
    fun endLogout(data: Intent) {
        // appauth.handleAuthorizationResponse()
        activity.onLogoutSuccess()
    }
}