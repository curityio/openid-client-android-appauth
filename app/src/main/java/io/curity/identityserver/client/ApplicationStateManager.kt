package io.curity.identityserver.client

import io.curity.identityserver.client.error.IllegalApplicationStateException
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationResponse

object ApplicationStateManager {
    private lateinit var authState: AuthState

    var clientId: String? = null

    var serverConfiguration: AuthorizationServiceConfiguration
        get() {
            return authState.authorizationServiceConfiguration
                ?: throw IllegalApplicationStateException("Configuration not set")
        }
        set(configuration) {
            authState = AuthState(configuration)
        }

    var registrationResponse: RegistrationResponse
        get() {
            return authState.lastRegistrationResponse
                ?: throw IllegalApplicationStateException("Not registered")
        }
        set(registrationResponse) {
            authState.update(registrationResponse)
            clientId = registrationResponse.clientId
        }

    fun isRegistered(): Boolean {
        return authState.lastRegistrationResponse != null
    }
}