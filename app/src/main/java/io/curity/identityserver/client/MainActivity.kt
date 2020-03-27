/*
 *  Copyright 2020 Curity AB
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

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.ErrorActivity.Companion.handleError
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*
import org.jose4j.jwt.consumer.JwtConsumerBuilder


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_AUTHORIZATION_INTENT = 100
    }

    private lateinit var serviceConfiguration: AuthorizationServiceConfiguration
    private lateinit var authorizationService: AuthorizationService
    private lateinit var authState: AuthState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val request = buildAuthorizationRequest()
            val authIntent = authorizationService.getAuthorizationRequestIntent(request)
            startActivityForResult(authIntent, REQUEST_CODE_AUTHORIZATION_INTENT)
        }

        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse("https://dlindau.ngrok.io/~")) { config, ex ->
            handleConfigurationRetrievalResult(config, ex)
        }

        authorizationService = AuthorizationService(this)
    }

    @MainThread
    private fun handleConfigurationRetrievalResult(config: AuthorizationServiceConfiguration?,
                                                   ex: AuthorizationException?
    ) {
        if (ex != null || config == null) {
            Log.i(TAG, "Failed to retrieve discovery document")
            return handleError(this, "Failed to fetch server configuration", ex?.errorDescription)
        }

        Log.i(TAG, "Discovery document retrieved")
        Log.d(TAG, config.toJsonString())
        serviceConfiguration = config
        authState = AuthState(serviceConfiguration)
    }

    private fun buildAuthorizationRequest(): AuthorizationRequest {
        val clientId = "app-auth"
        val redirectUri = Uri.parse("io.curity.client:/callback")
        return AuthorizationRequest.Builder(serviceConfiguration, clientId, "code", redirectUri)
            .setScopes("openid profile")
            .setPrompt(AuthorizationRequest.Prompt.LOGIN)
            .build()
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        if (requestCode == REQUEST_CODE_AUTHORIZATION_INTENT && data != null) {
            handleAuthorizationResponse(data)
        }
    }


    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        when {
            response != null -> {
                Log.i(TAG, "Got an authorization response")
            }
            error != null -> {
                Log.e(TAG, "Got an error from authorization request: {}", error)
                return handleError(this, "Authorization request failed", error.errorDescription)
            }
            else -> {
                Log.e(TAG, "Got neither response or error in authorization callback")
                return handleError(this, "No response in authorization callback", null)
            }
        }
        authorizationService.performTokenRequest(response.createTokenExchangeRequest(),
            handleTokenResponse())
    }

    private fun handleTokenResponse(): (TokenResponse?, AuthorizationException?) -> Unit {
        return { tokenResponse, exception ->
            when {
                tokenResponse != null -> {
                    Log.i(TAG, "Got a token response: ${tokenResponse.idToken}")
                    authState.update(tokenResponse, null)
                    viewDataFromIdToken()
                }
                exception != null -> { // authorization failed, check ex for more details
                    Log.e(TAG, "Got error response")
                    throw exception
                }
                else -> {
                    throw RuntimeException("Could not parse data in token response")
                }
            }
        }
    }

    private fun viewDataFromIdToken() {
        val jwtConsumer = JwtConsumerBuilder()
            .setSkipSignatureVerification() // Not required in code flow, since the token is fetched from the server using TLS
            .setRequireSubject()
            .setAllowedClockSkewInSeconds(30)
            .setExpectedIssuer("https://dlindau.ngrok.io/~")
            .setExpectedAudience("app-auth")
            .build()

        val jwtClaims = jwtConsumer.processToClaims(authState.idToken)
        val switchToAuthenticated = Intent(applicationContext, AuthenticatedActivity::class.java)
        switchToAuthenticated.putExtra("id_token_claims", jwtClaims.toJson())
        startActivity(switchToAuthenticated)
    }
}
