package io.curity.identityserver.client

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.ErrorActivity.Companion.GENERIC_ERROR
import io.curity.identityserver.client.ErrorActivity.Companion.handleError
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import org.jose4j.jwt.consumer.JwtConsumerBuilder

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
                authorizationService.performTokenRequest(response.createTokenExchangeRequest(),
                    handleTokenResponse())
            }
            error != null -> {
                Log.e(TAG, "Got an error from authorization request: {}", error)
                handleError(this, "Authorization request failed", error.errorDescription)
            }
            else -> {
                Log.e(TAG, "Got neither response or error in authorization callback")
                handleError(this, GENERIC_ERROR, "No response in authorization callback")
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
                exception != null -> { // authorization failed, check ex for more details
                    Log.e(TAG, "Got error response")
                    handleError(this, "Token request failed", exception.errorDescription)
                }
                else -> {
                    handleError(this, ErrorActivity.GENERIC_ERROR, null)
                }
            }
        }
    }

}
