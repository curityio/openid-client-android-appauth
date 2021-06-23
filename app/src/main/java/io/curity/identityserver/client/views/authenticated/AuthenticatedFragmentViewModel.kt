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

package io.curity.identityserver.client.views.authenticated;

import android.content.Intent
import androidx.databinding.BaseObservable
import io.curity.identityserver.client.AppAuthHandler
import io.curity.identityserver.client.ApplicationStateManager
import io.curity.identityserver.client.R
import io.curity.identityserver.client.configuration.ApplicationConfig
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.errors.InvalidIdTokenException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder

class AuthenticatedFragmentViewModel(
    private val events: AuthenticatedFragmentEvents,
    private val appauth: AppAuthHandler) : BaseObservable() {

    var subject: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null

    fun processTokens() {

        val idToken = ApplicationStateManager.idToken ?: return

        try {
            val jwtClaims = readIdTokenClaims(idToken)
            subject = "Hello ${jwtClaims.subject}"
            accessToken = "Access Token: ${ApplicationStateManager.tokenResponse?.accessToken}"
            refreshToken = "Refresh Token: ${ApplicationStateManager.tokenResponse?.refreshToken}"
            notifyChange()

        } catch(ex: ApplicationException) {
            events.handleError(ex)
        }
    }

    fun refreshAccessToken() {

        val refreshToken = ApplicationStateManager.tokenResponse?.refreshToken ?: return

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = this@AuthenticatedFragmentViewModel.appauth.refreshAccessToken(
                    refreshToken,
                    ApplicationStateManager.serverConfiguration,
                    ApplicationStateManager.registrationResponse
                )

                withContext(Dispatchers.Main) {
                    ApplicationStateManager.tokenResponse = response
                    processTokens()
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    events.handleError(ex)
                }
            }
        }
    }

    fun startLogout() {

        val intent = appauth.getEndSessionRedirectIntent(
            ApplicationStateManager.serverConfiguration,
            ApplicationStateManager.registrationResponse,
            ApplicationStateManager.idToken,
            ApplicationConfig.postLogoutRedirectUri)

        events.startLogoutRedirect(intent)
    }

    fun endLogout(data: Intent) {

        try {
            appauth.handleEndSessionResponse(
                AuthorizationResponse.fromIntent(data),
                AuthorizationException.fromIntent(data))

            ApplicationStateManager.tokenResponse = null
            events.onLogoutSuccess()

        } catch (ex: ApplicationException) {
            events.handleError(ex)
        }
    }

    private fun readIdTokenClaims(idToken: String): JwtClaims {

        val jwtConsumer = JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setRequireSubject()
            .setAllowedClockSkewInSeconds(30)
            .setExpectedIssuer(ApplicationStateManager.serverConfiguration.discoveryDoc?.issuer)
            .setExpectedAudience(ApplicationStateManager.registrationResponse.clientId)
            .build()

        try {
            return jwtConsumer.processToClaims(idToken)
        } catch (e: InvalidJwtException) {
            throw InvalidIdTokenException(e.message ?: "Failed to parse id token")
        }
    }
}
