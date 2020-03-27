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
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_AUTHORIZATION_INTENT = 100
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
            authorizationService.performAuthorizationRequest(
                request,
                PendingIntent.getActivity(this, REQUEST_CODE_AUTHORIZATION_INTENT,
                    Intent(this, WaitingActivity::class.java), 0))
        }

        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse("https://dlindau.ngrok.io/~")) { config, ex ->
            handleConfigurationRetrievalResult(config, ex)
        }

        authorizationService = AuthorizationService(this)
    }

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

}
