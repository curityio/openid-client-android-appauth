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

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.ErrorActivity.Companion.handleError
import io.curity.identityserver.client.config.ApplicationConfig
import io.curity.identityserver.client.error.ApplicationException
import io.curity.identityserver.client.error.GENERIC_ERROR
import io.curity.identityserver.client.error.IllegalApplicationStateException
import io.curity.identityserver.client.error.ServerCommunicationException
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration.fetchFromIssuer

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_AUTHORIZATION_INTENT = 100
    }

    private lateinit var authorizationService: AuthorizationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        try {
            findViewById<Button>(R.id.loginButton).setOnClickListener {
                performAuthorizationRequest()
            }

            fetchFromIssuer(ApplicationConfig.issuer) { config, ex ->
                handleConfigurationRetrievalResult(config, ex)
                if (!isRegistered()) {
                    registerClient()
                }
            }

            authorizationService = AuthorizationService(this)
        } catch (exception: ApplicationException) {
            handleError(this, exception.errorTitle, exception.errorDescription ?: GENERIC_ERROR)
        }
    }

    private fun registerClient() {
        val nonTemplatizedRequest =
            RegistrationRequest.Builder(ApplicationStateManager.serverConfiguration,
                    listOf(ApplicationConfig.redirectUri))
                .setGrantTypeValues(listOf(GrantTypeValues.AUTHORIZATION_CODE))
                .setAdditionalParameters(mapOf("scope" to ApplicationConfig.scope))
                .build()

        authorizationService.performRegistrationRequest(nonTemplatizedRequest,
            handleRegistrationResponse())
    }

    private fun isRegistered(): Boolean {
        return ApplicationStateManager.isRegistered()
    }

    private fun handleRegistrationResponse(): (RegistrationResponse?, AuthorizationException?) -> Unit {
        return { registrationResponse, authorizationException ->
            when {
                registrationResponse != null -> {
                    ApplicationStateManager.registrationResponse = registrationResponse
                }
                else -> throw ServerCommunicationException("Failed to register",
                    authorizationException?.errorDescription)
            }

        }
    }

    private fun performAuthorizationRequest() {
        val request = buildAuthorizationRequest()
        authorizationService.performAuthorizationRequest(
            request,
            PendingIntent.getActivity(this, REQUEST_CODE_AUTHORIZATION_INTENT,
                Intent(this, WaitingActivity::class.java), 0))
    }

    private fun handleConfigurationRetrievalResult(config: AuthorizationServiceConfiguration?,
                                                   ex: AuthorizationException?) {
        if (ex != null || config == null) {
            Log.e(TAG, "Failed to retrieve discovery document")
            throw ServerCommunicationException("Failed to fetch server configuration",
                ex?.errorDescription)
        }

        if (config.registrationEndpoint == null) {
            throw ServerCommunicationException("Invalid server configuration",
                "Server discovery doc did not contain a registration endpoint")
        }
        Log.i(TAG, "Discovery document retrieved")
        Log.d(TAG, config.toJsonString())
        ApplicationStateManager.serverConfiguration = config
    }

    private fun buildAuthorizationRequest(): AuthorizationRequest {
        val clientId = ApplicationStateManager.clientId
            ?: throw IllegalApplicationStateException("No client id in configuration")

        return AuthorizationRequest.Builder(ApplicationStateManager.serverConfiguration, clientId,
                ResponseTypeValues.CODE,
                ApplicationConfig.redirectUri)
            .setScopes(ApplicationConfig.scope)
            .build()
    }

}
