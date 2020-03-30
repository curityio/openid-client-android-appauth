/*
 *  Copyright 2020. Curity AB
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
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.error.ServerCommunicationException
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse

class WaitingActivity : AppCompatActivity() {

    private lateinit var authorizationService: AuthorizationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)
        authorizationService = AuthorizationService(this)
        handleAuthorizationResponse(intent)
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        when {
            response != null -> {
                Log.i(TAG, "Got an authorization response")
                val tokenRequest = response.createTokenExchangeRequest(
                    mapOf(
                        "client_secret" to ApplicationStateManager.registrationResponse.clientSecret))
                authorizationService.performTokenRequest(tokenRequest, handleTokenResponse())
            }
            else -> {
                throw ServerCommunicationException("Authorization request failed",
                    error?.errorDescription)
            }
        }
    }

    private fun handleTokenResponse(): (TokenResponse?, AuthorizationException?) -> Unit {
        return { tokenResponse, exception ->
            when {
                tokenResponse != null -> {
                    Log.i(TAG, "Got a token response: ${tokenResponse.idToken}")
                    ApplicationStateManager.tokenResponse = tokenResponse
                    startActivity(Intent(this, AuthenticatedActivity::class.java))
                }
                else -> {
                    throw ServerCommunicationException("Token request failed",
                        exception?.errorDescription)
                }
            }
        }
    }

}
