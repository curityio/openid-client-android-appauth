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

package io.curity.identityserver.client.views;

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

    var subject = ""
    var authenticationDescription = ""

    fun processTokens() {

        val idToken = ApplicationStateManager.tokenResponse?.idToken ?: return

        try {
            val jwtClaims = readIdTokenClaims(idToken)

            val greeting = events.getString(R.string.authenticated_greeting)
            val descriptionPart1 = events.getString(R.string.authn_description1)
            val descriptionPart2 = events.getString(R.string.authn_description2)
            val time = jwtClaims.getNumericDateClaimValue("auth_time")
            val acr = jwtClaims.getClaimValueAsString("acr")

            subject = "$greeting ${jwtClaims.subject}"
            authenticationDescription = "$descriptionPart1 $time $descriptionPart2 $acr"

        } catch(ex: ApplicationException) {
            events.handleError(ex)
        }
    }

    fun refreshAccessToken() {

        val refreshToken = ApplicationStateManager.tokenResponse?.refreshToken ?: return

        CoroutineScope(Dispatchers.IO).launch {

            val model = this@AuthenticatedFragmentViewModel
            try {

                val response = model.appauth.refreshAccessToken(
                    refreshToken,
                    ApplicationStateManager.serverConfiguration,
                    ApplicationStateManager.registrationResponse
                )

                withContext(Dispatchers.Main) {
                    ApplicationStateManager.tokenResponse = response
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    model.events.handleError(ex)
                }
            }
        }
    }

    fun startLogout() {

        val intent = appauth.getEndSessionRedirectIntent(
            ApplicationStateManager.serverConfiguration,
            ApplicationStateManager.registrationResponse,
            ApplicationStateManager.tokenResponse?.idToken,
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
