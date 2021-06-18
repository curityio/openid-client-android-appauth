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

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import io.curity.identityserver.client.config.ApplicationConfig
import io.curity.identityserver.client.error.ServerCommunicationException
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration.fetchFromIssuer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/*
 * A code layer to manage AppAuth integration in one place and reduce code in the rest of the app
 * https://nicedoc.io/openid/AppAuth-Android
 */
class AppAuthController(private val context: Context) {

    private val authorizationService: AuthorizationService = AuthorizationService(context)

    /*
     * Get OpenID Connect endpoints and ensure that dynamic client registration is configured
     */
    suspend fun fetchMetadata(issuer: Uri): AuthorizationServiceConfiguration {

        return suspendCoroutine { continuation ->

            fetchFromIssuer(issuer) { config, ex ->

                when {
                    config != null -> {
                        if (config.registrationEndpoint == null) {
                            val error = ServerCommunicationException(
                                "Invalid server configuration",
                                "Server discovery doc did not contain a registration endpoint"
                            )
                            continuation.resumeWithException(error)
                        }

                        Log.i(ContentValues.TAG, "Discovery document retrieved")
                        Log.d(ContentValues.TAG, config.toJsonString())
                        continuation.resume(config)
                    }
                    else -> {
                        Log.e(ContentValues.TAG, "Failed to retrieve discovery document")
                        val error = ServerCommunicationException(
                            "Failed to fetch server configuration",
                            ex?.errorDescription
                        )
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Perform dynamic client registration and then store the response
     */
    suspend fun registerClient(serverConfiguration: AuthorizationServiceConfiguration): RegistrationResponse {

        val nonTemplatizedRequest =
            RegistrationRequest.Builder(
                serverConfiguration,
                listOf(ApplicationConfig.redirectUri)
            )
                .setGrantTypeValues(listOf(GrantTypeValues.AUTHORIZATION_CODE))
                .setAdditionalParameters(mapOf("scope" to ApplicationConfig.scope))
                .build()

        return suspendCoroutine { continuation ->

            authorizationService.performRegistrationRequest(nonTemplatizedRequest) { registrationResponse, authorizationException ->
                when {
                    registrationResponse != null -> {
                        continuation.resume(registrationResponse)
                    }
                    else -> {
                        val error = ServerCommunicationException("Failed to register", authorizationException?.errorDescription)
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Trigger a redirect with standard parameters
     */
    fun startAuthorizationRedirect(
        serverConfiguration: AuthorizationServiceConfiguration,
        registrationResponse: RegistrationResponse,
        pendingIntent: PendingIntent) {

        // Use acr_values to select a particular authentication method at runtime
        val extraParams = mutableMapOf<String, String>()
        //extraParams.put("acr_values", "urn:se:curity:authentication:html-form:Username-Password")

        val request = AuthorizationRequest.Builder(serverConfiguration, registrationResponse.clientId,
            ResponseTypeValues.CODE,
            ApplicationConfig.redirectUri)
            .setScopes(ApplicationConfig.scope)
            .setAdditionalParameters(extraParams)
            .build()

        authorizationService.performAuthorizationRequest(request, pendingIntent)
    }

    /*
     * Handle the response details and then redeem the code for tokens
     */
    suspend fun handleAuthorizationResponse(
        response: AuthorizationResponse?,
        ex: AuthorizationException?,
        registrationResponse: RegistrationResponse): TokenResponse {

        /* TODO: handle login cancelled
            if (ex.type == AuthorizationException.TYPE_GENERAL_ERROR &&
                ex.code.equals(AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code)
        )*/

        if (response == null) {
            throw ServerCommunicationException("Authorization request failed", ex?.errorDescription)
        }

        Log.i(ContentValues.TAG, "Got an authorization response")
        val extraParams = mapOf("client_secret" to registrationResponse.clientSecret)
        val tokenRequest = response.createTokenExchangeRequest(extraParams)

        return suspendCoroutine { continuation ->

            authorizationService.performTokenRequest(tokenRequest) { tokenResponse, ex ->

                when {
                    tokenResponse != null -> {
                        Log.i(ContentValues.TAG, "Got a token response: ${tokenResponse.idToken}")
                        continuation.resume(tokenResponse)
                    }
                    else -> {
                        val error = ServerCommunicationException("Token request failed", ex?.errorDescription)
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Try to refresh an access token
     */
    suspend fun refreshAccessToken(
        refreshToken: String,
        serverConfiguration: AuthorizationServiceConfiguration,
        registrationResponse: RegistrationResponse): TokenResponse {

        val tokenRequest = TokenRequest.Builder(serverConfiguration, registrationResponse.clientId)
            .setGrantType(GrantTypeValues.REFRESH_TOKEN)
            .setRefreshToken(refreshToken)
            .build()

        return suspendCoroutine { continuation ->

            authorizationService.performTokenRequest(tokenRequest) { tokenResponse, ex ->

                when {
                    /* TODO: handle session expired
                        if (ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
                            ex.code.equals(AuthorizationException.TokenRequestErrors.INVALID_GRANT.code)
                    )*/

                    tokenResponse != null -> {
                        Log.i(ContentValues.TAG, "Got a token response: ${tokenResponse.idToken}")
                        continuation.resume(tokenResponse)
                    }
                    else -> {
                        val error = ServerCommunicationException("Token request failed", ex?.errorDescription)
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Do an OpenID Connect end session redirect and remove the SSO cookie
     */
    fun startEndSessionRedirect(serverConfiguration: AuthorizationServiceConfiguration,
                                idToken: String,
                                postLogoutRedirectUri: Uri,
                                pendingIntent: PendingIntent) {

        val request = EndSessionRequest.Builder(serverConfiguration)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(postLogoutRedirectUri)
            .build()

        authorizationService.performEndSessionRequest(request, pendingIntent)
    }
}