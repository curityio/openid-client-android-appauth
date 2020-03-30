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
        authorizationService = AuthorizationService(this)
        handleAuthorizationResponse(intent)
        setContentView(R.layout.activity_waiting)
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        when {
            response != null -> {
                Log.i(TAG, "Got an authorization response")
                val tokenRequest = response.createTokenExchangeRequest(
                    mapOf("client_secret" to AuthStateManager.registrationResponse.clientSecret))
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
                    val authenticatedIntent = Intent(this, AuthenticatedActivity::class.java)
                    authenticatedIntent.putExtra("id_token", tokenResponse.idToken)
                    startActivity(authenticatedIntent)
                }
                else -> {
                    throw ServerCommunicationException("Token request failed",
                        exception?.errorDescription)
                }
            }
        }
    }

}
