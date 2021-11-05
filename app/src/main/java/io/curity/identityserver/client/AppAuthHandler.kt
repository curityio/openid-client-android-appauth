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

package io.curity.identityserver.client

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration.fetchFromIssuer
import io.curity.identityserver.client.configuration.ApplicationConfig
import io.curity.identityserver.client.errors.GENERIC_ERROR
import io.curity.identityserver.client.errors.ServerCommunicationException

/*
 * Manage AppAuth integration in one class in order to reduce code in the rest of the app
 */
class AppAuthHandler(private val config: ApplicationConfig, val context: Context) {

    private var authService = AuthorizationService(context)

    /*
     * Get OpenID Connect endpoints
     */
    suspend fun fetchMetadata(): AuthorizationServiceConfiguration {

        return suspendCoroutine { continuation ->

            fetchFromIssuer(this.config.getIssuerUri()) { metadata, ex ->

                when {
                    metadata != null -> {
                        Log.i(ContentValues.TAG, "Metadata retrieved successfully")
                        Log.d(ContentValues.TAG, metadata.toJsonString())
                        continuation.resume(metadata)
                    }
                    else -> {
                        val error = createAuthorizationError("Metadata Download Error", ex)
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Trigger a redirect with standard parameters
     * acr_values could be sent as an extra parameter, to control the authentication method
     */
    fun getAuthorizationRedirectIntent(metadata: AuthorizationServiceConfiguration): Intent {

        // Use acr_values to select a particular authentication method at runtime
        val extraParams = mutableMapOf<String, String>()
        //extraParams.put("acr_values", "urn:se:curity:authentication:html-form:Username-Password")

        val request = AuthorizationRequest.Builder(
            metadata,
            this.config.clientID,
            ResponseTypeValues.CODE,
            this.config.getRedirectUri())
            .setScopes(this.config.scope)
            .setAdditionalParameters(extraParams)
            .build()

        return authService.getAuthorizationRequestIntent(request)
    }

    /*
     * Handle the authorization response, including the user closing the Chrome Custom Tab
     */
    fun handleAuthorizationResponse(
        response: AuthorizationResponse?,
        ex: AuthorizationException?): AuthorizationResponse {

        if (response == null) {
            throw createAuthorizationError("Authorization Request Error", ex)
        }

        Log.i(ContentValues.TAG, "Authorization response received successfully")
        Log.d(ContentValues.TAG, "CODE: ${response.authorizationCode}, STATE: ${response.state}")
        return response
    }

    /*
     * Handle the authorization code grant request to get tokens
     */
    suspend fun redeemCodeForTokens(authResponse: AuthorizationResponse): TokenResponse? {

        return suspendCoroutine { continuation ->

            val extraParams = mutableMapOf<String, String>()
            val tokenRequest = authResponse.createTokenExchangeRequest(extraParams)

            authService.performTokenRequest(tokenRequest) { tokenResponse, ex ->

                when {
                    tokenResponse != null -> {
                        Log.i(ContentValues.TAG, "Authorization code grant response received successfully")
                        Log.d(ContentValues.TAG, "AT: ${tokenResponse.accessToken}, RT: ${tokenResponse.refreshToken}, IDT: ${tokenResponse.idToken}" )
                        continuation.resume(tokenResponse)
                    }
                    else -> {
                        val error = createAuthorizationError("Authorization Response Error", ex)
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Try to refresh an access token and return null when the refresh token expires
     */
    suspend fun refreshAccessToken(
        metadata: AuthorizationServiceConfiguration,
        refreshToken: String): TokenResponse? {

        return suspendCoroutine { continuation ->

            val extraParams = mutableMapOf<String, String>()
            val tokenRequest = TokenRequest.Builder(metadata, this.config.clientID)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .setAdditionalParameters(extraParams)
                .build()

            authService.performTokenRequest(tokenRequest) { tokenResponse, ex ->

                when {
                    tokenResponse != null -> {
                        Log.i(ContentValues.TAG, "Refresh token grant response received successfully")
                        Log.d(ContentValues.TAG, "AT: ${tokenResponse.accessToken}, RT: ${tokenResponse.refreshToken}, IDT: ${tokenResponse.idToken}" )
                        continuation.resume(tokenResponse)
                    }
                    else -> {

                        if (ex != null &&
                            ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
                            ex.code == AuthorizationException.TokenRequestErrors.INVALID_GRANT.code
                        ) {
                            Log.i(ContentValues.TAG, "Refresh token expired and the user must re-authenticate")
                            continuation.resume(null)

                        } else {

                            val error = createAuthorizationError("Token Refresh Error", ex)
                            continuation.resumeWithException(error)
                        }
                    }
                }
            }
        }
    }

    /*
     * Do an OpenID Connect end session redirect and remove the SSO cookie
     */
    fun getEndSessionRedirectIntent(metadata: AuthorizationServiceConfiguration,
                                    idToken: String?): Intent {

        val extraParams = mutableMapOf<String, String>()
        val request = EndSessionRequest.Builder(metadata)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(this.config.getPostLogoutRedirectUri())
            .setAdditionalParameters(extraParams)
            .build()

        return authService.getEndSessionRequestIntent(request)
    }

    /*
     * Finalize after receiving an end session response
     */
    fun handleEndSessionResponse(ex: AuthorizationException?) {

        when {
            ex != null -> {
                throw createAuthorizationError("End Session Request Error", ex)
            }
        }
    }

    /*
     * Clean up AppAuth resources on exit
     */
    fun dispose() {
        this.authService.dispose()
    }

    /*
     * Process standard OAuth error / error_description fields and also AppAuth error identifiers
     */
    private fun createAuthorizationError(title: String, ex: AuthorizationException?): ServerCommunicationException {

        val parts = mutableListOf<String>()

        if (ex?.type != null) {
            parts.add("(${ex.type} / ${ex.code})")
        }

        if (ex?.error != null) {
            parts.add(ex.error!!)
        }

        val description: String = if (ex?.errorDescription != null) {
            ex.errorDescription!!
        } else {
            GENERIC_ERROR
        }
        parts.add(description)

        val fullDescription = parts.joinToString(" : ")
        Log.e(ContentValues.TAG, fullDescription)
        return ServerCommunicationException(title, fullDescription)
    }
}