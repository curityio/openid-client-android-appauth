/*
 *  Copyright 2021 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.curity.identityserver.client.views

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import io.curity.identityserver.client.AppAuthHandler
import io.curity.identityserver.client.ApplicationStateManager
import io.curity.identityserver.client.configuration.ApplicationConfig
import io.curity.identityserver.client.errors.ApplicationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

/*
 * Logic triggered from the layout views
 */
class MainActivityViewModel() : ViewModel() {

    lateinit var events: MainActivityEvents
    lateinit var appauth: AppAuthHandler

    fun initialize(context: Context, events: MainActivityEvents) {
        this.appauth = AppAuthHandler(context)
        this.events = events
    }

    /*
     * Startup handling to lookup metadata and do the dynamic client registration if required
     * Make HTTP requests on a worker thread and then perform updates on the UI thread
     */
    fun registerIfRequired() {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val serverConfiguration = appauth.fetchMetadata(ApplicationConfig.issuer)
                if (!ApplicationStateManager.isRegistered()) {
                    val registrationResponse = appauth.registerClient(serverConfiguration)

                    withContext(Dispatchers.Main) {
                        ApplicationStateManager.serverConfiguration = serverConfiguration
                        ApplicationStateManager.registrationResponse = registrationResponse
                    }
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    handleError(ex)
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
            ApplicationStateManager.registrationResponse
        )

        events.startLoginRedirect(intent)
    }

    /*
     * Redeem the code for tokens and also handle failures or the user cancelling the Chrome Custom Tab
     * Make HTTP requests on a worker thread and then perform updates on the UI thread
     */
    fun endLogin(data: Intent) {

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val authorizationResponse = appauth.handleAuthorizationResponse(
                    AuthorizationResponse.fromIntent(data),
                    AuthorizationException.fromIntent(data))

                if (authorizationResponse != null) {

                    val tokenResponse = appauth.redeemCodeForTokens(
                        authorizationResponse,
                        ApplicationStateManager.registrationResponse
                    )

                    withContext(Dispatchers.Main) {
                        if (tokenResponse != null) {
                            ApplicationStateManager.tokenResponse = tokenResponse
                            events.onLoginSuccess()
                        }
                    }
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    handleError(ex)
                }
            }
        }
    }

    /*
     * Build the authorization redirect URL and then ask the view to redirect
     */
    fun startLogout() {

        val intent = appauth.getEndSessionRedirectIntent(
            ApplicationStateManager.serverConfiguration,
            ApplicationStateManager.registrationResponse,
            ApplicationStateManager.tokenResponse?.idToken,
            ApplicationConfig.postLogoutRedirectUri)

        events.startLogoutRedirect(intent)
    }

    /*
     * Clean up after logging out and remove tokens from memory
     */
    fun endLogout(data: Intent) {

        try {
            appauth.handleEndSessionResponse(
                AuthorizationResponse.fromIntent(data),
                AuthorizationException.fromIntent(data))

            ApplicationStateManager.tokenResponse = null
            events.onLogoutSuccess()

        } catch (ex: ApplicationException) {
            handleError(ex)
        }
    }

    /*
     * This is called by fragments
     */
    fun handleError(ex: ApplicationException) {
        events.handleError(ex)
    }
}