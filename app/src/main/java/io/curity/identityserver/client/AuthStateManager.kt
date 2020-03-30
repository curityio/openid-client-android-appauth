package io.curity.identityserver.client

import io.curity.identityserver.client.error.IllegalClientStateException
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationResponse

object AuthStateManager {
    private lateinit var authState: AuthState

    var clientId: String? = null

    var configuration: AuthorizationServiceConfiguration
        get() {
            return authState.authorizationServiceConfiguration
                ?: throw IllegalClientStateException("Configuration not set")
        }
        set(configuration) {
            authState = AuthState(configuration)
        }

    var registrationResponse: RegistrationResponse
        get() {
            return authState.lastRegistrationResponse
                ?: throw IllegalClientStateException("Not registered")
        }
        set(registrationResponse) {
            authState.update(registrationResponse)
            clientId = registrationResponse.clientId
        }

    fun isClientRegistered(): Boolean {
        return authState.lastRegistrationResponse != null
    }
}