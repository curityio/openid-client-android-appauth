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

package io.curity.identityserver.client.views.unauthenticated;

import android.content.Intent
import androidx.databinding.BaseObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse
import io.curity.identityserver.client.AppAuthHandler
import io.curity.identityserver.client.ApplicationStateManager
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.views.error.ErrorFragmentViewModel

class UnauthenticatedFragmentViewModel(
    private val events: UnauthenticatedFragmentEvents,
    private val state: ApplicationStateManager,
    private val appauth: AppAuthHandler,
    val error: ErrorFragmentViewModel) : BaseObservable() {

    /*
     * Build the authorization redirect URL and then ask the view to redirect
     */
    fun startLogin() {

        this.error.clearDetails()
        var metadata = this.state.metadata

        val that = this@UnauthenticatedFragmentViewModel
        CoroutineScope(Dispatchers.IO).launch {
            try {

                // Look up metadata on a worker thread
                if (metadata == null) {
                    metadata = appauth.fetchMetadata()
                }

                // Switch back to the UI thread for the redirect
                withContext(Dispatchers.Main) {

                    that.state.metadata = metadata
                    val intent = appauth.getAuthorizationRedirectIntent(metadata!!)
                    that.events.startLoginRedirect(intent)
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    error.setDetails(ex)
                }
            }
        }
    }

    /*
     * Redeem the code for tokens and also handle failures or the user cancelling the Chrome Custom Tab
     * Make HTTP requests on a worker thread and then perform updates on the UI thread
     */
    fun endLogin(data: Intent) {

        try {

            val authorizationResponse = appauth.handleAuthorizationResponse(
                AuthorizationResponse.fromIntent(data),
                AuthorizationException.fromIntent(data))

            var tokenResponse: TokenResponse?
            val that = this@UnauthenticatedFragmentViewModel
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    tokenResponse = appauth.redeemCodeForTokens(authorizationResponse)

                    withContext(Dispatchers.Main) {
                        that.state.saveTokens(tokenResponse!!)
                        events.onLoggedIn()
                    }

                } catch (ex: ApplicationException) {

                    withContext(Dispatchers.Main) {
                        error.setDetails(ex)
                    }
                }
            }

        } catch (ex: ApplicationException) {
            error.setDetails(ex)
        }
    }
}
