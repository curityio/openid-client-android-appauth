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
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.errors.InvalidIdTokenException
import io.curity.identityserver.client.views.error.ErrorFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.TokenResponse
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import java.lang.ref.WeakReference

class AuthenticatedFragmentViewModel(
    private val events: WeakReference<AuthenticatedFragmentEvents>,
    private val appauth: AppAuthHandler,
    val error: ErrorFragmentViewModel) : BaseObservable() {

    var subject: String = ""
    var accessToken: String = ""
    var refreshToken: String = ""

    var hasRefreshToken = false
    var hasIdToken = false

    fun processTokens() {

        try {

            if (ApplicationStateManager.tokenResponse?.accessToken != null) {
                this.accessToken = ApplicationStateManager.tokenResponse?.accessToken!!
            }

            if (ApplicationStateManager.tokenResponse?.refreshToken != null) {
                this.refreshToken = ApplicationStateManager.tokenResponse?.refreshToken!!
                this.hasRefreshToken = true
            }

            if (ApplicationStateManager.tokenResponse?.idToken != null) {
                this.hasIdToken = true
                val jwtClaims = readIdTokenClaims(ApplicationStateManager.tokenResponse?.idToken!!)
                this.subject = jwtClaims.subject
            }

        } catch(ex: ApplicationException) {
            this.error.setDetails(ex)
        }

        notifyChange()
    }

    fun refreshAccessToken() {

        val metadata = ApplicationStateManager.metadata!!
        val registrationResponse = ApplicationStateManager.registrationResponse!!
        val refreshToken = ApplicationStateManager.tokenResponse!!.refreshToken!!
        var tokenResponse: TokenResponse?
        this.error.clearDetails()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                tokenResponse = this@AuthenticatedFragmentViewModel.appauth.refreshAccessToken(
                    metadata,
                    registrationResponse,
                    refreshToken)

                withContext(Dispatchers.Main) {
                    ApplicationStateManager.tokenResponse = tokenResponse
                    if (tokenResponse == null) {
                        events.get()?.onLoggedOut()
                    }
                    processTokens()
                }

            } catch (ex: ApplicationException) {

                withContext(Dispatchers.Main) {
                    error.setDetails(ex)
                }
            }
        }
    }

    fun startLogout() {

        this.error.clearDetails()
        val intent = appauth.getEndSessionRedirectIntent(
            ApplicationStateManager.metadata!!,
            ApplicationStateManager.registrationResponse!!,
            ApplicationStateManager.idToken)

        this.events.get()?.startLogoutRedirect(intent)
    }

    fun endLogout(data: Intent) {

        try {
            this.appauth.handleEndSessionResponse(AuthorizationException.fromIntent(data))
            ApplicationStateManager.tokenResponse = null
            this.events.get()?.onLoggedOut()

        } catch (ex: ApplicationException) {
            this.error.setDetails(ex)
        }
    }

    private fun readIdTokenClaims(idToken: String): JwtClaims {

        val jwtConsumer = JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setRequireSubject()
            .setAllowedClockSkewInSeconds(30)
            .setExpectedIssuer(ApplicationStateManager.metadata?.discoveryDoc?.issuer)
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
