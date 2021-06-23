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

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import androidx.databinding.BaseObservable
import io.curity.identityserver.client.AppAuthHandler
import io.curity.identityserver.client.ApplicationStateManager
import io.curity.identityserver.client.configuration.ApplicationConfig
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.errors.InvalidIdTokenException
import io.curity.identityserver.client.views.error.ErrorFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder

class AuthenticatedFragmentViewModel(
    private val events: AuthenticatedFragmentEvents,
    private val appauth: AppAuthHandler,
    private val error: ErrorFragmentViewModel) : BaseObservable() {

    var subject: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null

    fun processTokens() {

        val idToken = ApplicationStateManager.idToken ?: return

        try {
            val jwtClaims = readIdTokenClaims(idToken)
            subject = "Subject: ${jwtClaims.subject}"
            accessToken = "Access Token: ${ApplicationStateManager.tokenResponse?.accessToken}"
            refreshToken = "Refresh Token: ${ApplicationStateManager.tokenResponse?.refreshToken}"
            notifyChange()

        } catch(ex: ApplicationException) {
            error.setErrorDetails(ex)
        }
    }

    fun refreshAccessToken() {

        val refreshToken = ApplicationStateManager.tokenResponse?.refreshToken ?: return

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = this@AuthenticatedFragmentViewModel.appauth.refreshAccessToken(
                    refreshToken,
                    ApplicationStateManager.serverConfiguration!!,
                    ApplicationStateManager.registrationResponse!!
                )

                withContext(Dispatchers.Main) {
                    ApplicationStateManager.tokenResponse = response
                    if (response == null) {
                        events.onEndSession()
                    }
                    processTokens()
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    error.setErrorDetails(ex)
                }
            }
        }
    }

    fun startLogout() {

        val intent = appauth.getEndSessionRedirectIntent(
            ApplicationStateManager.serverConfiguration!!,
            ApplicationStateManager.registrationResponse!!,
            ApplicationStateManager.idToken,
            ApplicationConfig.postLogoutRedirectUri)

        events.startLogoutRedirect(intent)
    }

    fun endLogout(data: Intent) {

        try {
            appauth.handleEndSessionResponse(AuthorizationException.fromIntent(data))
            ApplicationStateManager.tokenResponse = null
            events.onEndSession()

        } catch (ex: ApplicationException) {
            error.setErrorDetails(ex)
        }
    }

    private fun readIdTokenClaims(idToken: String): JwtClaims {

        val jwtConsumer = JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setRequireSubject()
            .setAllowedClockSkewInSeconds(30)
            .setExpectedIssuer(ApplicationStateManager.serverConfiguration?.discoveryDoc?.issuer)
            .setExpectedAudience(ApplicationStateManager.registrationResponse?.clientId)
            .build()

        try {
            return jwtConsumer.processToClaims(idToken)
        } catch (e: InvalidJwtException) {
            Log.e(ContentValues.TAG, "${e.message}")
            throw InvalidIdTokenException("Failed to parse ID Token")
        }
    }
}
