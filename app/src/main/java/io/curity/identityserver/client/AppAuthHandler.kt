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
import android.net.Uri
import android.util.Log
import io.curity.identityserver.client.configuration.ApplicationConfig
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.errors.GENERIC_ERROR
import io.curity.identityserver.client.errors.ServerCommunicationException
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration.fetchFromIssuer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Manage AppAuth integration in one class in order to reduce code in the rest of the app
 */
class AppAuthHandler(val context: Context) {

    private var authorizationService = AuthorizationService(context)

    /*
     * Get OpenID Connect endpoints and ensure that dynamic client registration is configured
     */
    suspend fun fetchMetadata(issuer: Uri): AuthorizationServiceConfiguration {

        return suspendCoroutine { continuation ->

            fetchFromIssuer(issuer) { config, ex ->

                when {
                    config != null -> {
                        if (config.registrationEndpoint == null) {
                            val error = ApplicationException(
                                "Invalid Configuration Error",
                                "No registration endpoint is configured in the Identity Server"
                            )
                            continuation.resumeWithException(error)
                        }

                        Log.i(ContentValues.TAG, "Discovery document retrieved successfully")
                        Log.d(ContentValues.TAG, config.toJsonString())
                        continuation.resume(config)
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
     * Perform dynamic client registration and then store the response
     */
    suspend fun registerClient(serverConfiguration: AuthorizationServiceConfiguration): RegistrationResponse {

        return suspendCoroutine { continuation ->

            val extraParams = mutableMapOf<String, String>()
            extraParams["scope"] = ApplicationConfig.scope
            extraParams["requires_consent"] = "false"
            extraParams["post_logout_redirect_uris"] = ApplicationConfig.postLogoutRedirectUri.toString()

            val nonTemplatizedRequest =
                RegistrationRequest.Builder(
                    serverConfiguration,
                    listOf(ApplicationConfig.redirectUri)
                )
                    .setGrantTypeValues(listOf(GrantTypeValues.AUTHORIZATION_CODE))
                    .setAdditionalParameters(extraParams)
                    .build()

            val authService = AuthorizationService(context)
            authService.performRegistrationRequest(nonTemplatizedRequest) { registrationResponse, ex ->
                when {
                    registrationResponse != null -> {
                        Log.i(ContentValues.TAG, "Registration data retrieved successfully")
                        Log.d(ContentValues.TAG, "ID: ${registrationResponse.clientId}, Secret: ${registrationResponse.clientSecret}")
                        continuation.resume(registrationResponse)
                    }
                    else -> {
                        val error = createAuthorizationError("Registration Error", ex)
                        continuation.resumeWithException(error)
                    }
                }
            }
        }
    }

    /*
     * Trigger a redirect with standard parameters
     * acr_values can be sent as an extra parameter, to control authentication methods
     */
    fun getAuthorizationRedirectIntent(
        serverConfiguration: AuthorizationServiceConfiguration,
        registrationResponse: RegistrationResponse): Intent {

        // Use acr_values to select a particular authentication method at runtime
        val extraParams = mutableMapOf<String, String>()
        //extraParams.put("acr_values", "urn:se:curity:authentication:html-form:Username-Password")
        extraParams.put("acr_values", "urn:se:curity:authentication:html-form:Username-Passwor2d")

        val request = AuthorizationRequest.Builder(serverConfiguration, registrationResponse.clientId,
            ResponseTypeValues.CODE,
            ApplicationConfig.redirectUri)
            .setScopes(ApplicationConfig.scope)
            .setAdditionalParameters(extraParams)
            .build()

        return authorizationService.getAuthorizationRequestIntent(request)
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
    suspend fun redeemCodeForTokens(
            response: AuthorizationResponse,
            registrationResponse: RegistrationResponse): TokenResponse? {

        return suspendCoroutine { continuation ->

            val extraParams = mapOf("client_secret" to registrationResponse.clientSecret)
            val tokenRequest = response.createTokenExchangeRequest(extraParams)

            val authService = AuthorizationService(context)
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
        refreshToken: String,
        serverConfiguration: AuthorizationServiceConfiguration,
        registrationResponse: RegistrationResponse): TokenResponse? {

        return suspendCoroutine { continuation ->

            val extraParams = mapOf("client_secret" to registrationResponse.clientSecret)
            val tokenRequest = TokenRequest.Builder(serverConfiguration, registrationResponse.clientId)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .setAdditionalParameters(extraParams)
                .build()

            val authService = AuthorizationService(context)
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
    fun getEndSessionRedirectIntent(serverConfiguration: AuthorizationServiceConfiguration,
                                    registrationResponse: RegistrationResponse,
                                    idToken: String?,
                                    postLogoutRedirectUri: Uri): Intent {

        val extraParams = mapOf("client_id" to registrationResponse.clientId)
        val request = EndSessionRequest.Builder(serverConfiguration)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(postLogoutRedirectUri)
            .setAdditionalParameters(extraParams)
            .build()

        return authorizationService.getEndSessionRequestIntent(request)
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