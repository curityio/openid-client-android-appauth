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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.ErrorActivity.Companion.handleError
import io.curity.identityserver.client.error.ApplicationException
import io.curity.identityserver.client.error.IllegalApplicationStateException
import io.curity.identityserver.client.error.ServerCommunicationException
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder

class AuthenticatedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticated)

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener(logout())

        try {
            val idToken = intent.getStringExtra("id_token")
                ?: throw IllegalApplicationStateException("No ID token in intent")

            viewDataFromIdToken(idToken)
        } catch (e: ApplicationException) {
            handleError(this, e.errorTitle, e.errorDescription)
        }
    }

    private fun viewDataFromIdToken(idToken: String?) {

        val jwtConsumer = JwtConsumerBuilder()
            .setSkipSignatureVerification() // Not required in code flow, since the token is fetched from the server using TLS
            .setRequireSubject()
            .setAllowedClockSkewInSeconds(30)
            .setExpectedIssuer(ApplicationStateManager.serverConfiguration.discoveryDoc?.issuer)
            .setExpectedAudience(ApplicationStateManager.clientId)
            .build()

        val jwtClaims = try {
            jwtConsumer.processToClaims(idToken)
        } catch (e: InvalidJwtException) {
            throw ServerCommunicationException("Invalid ID Token", e.message)
        }
        val title = findViewById<TextView>(R.id.hello_subject)
        title.text = getString(R.string.hello_subject, jwtClaims.subject)
        val authnDescription = findViewById<TextView>(R.id.authn_description)
        authnDescription.text = getString(R.string.authn_description,
            jwtClaims.getNumericDateClaimValue("auth_time"), jwtClaims.getClaimValueAsString("acr"))
    }


    private fun logout(): (View) -> Unit = {
        val logoutIntent = Intent(applicationContext, MainActivity::class.java)
        startActivity(logoutIntent)
    }
}
