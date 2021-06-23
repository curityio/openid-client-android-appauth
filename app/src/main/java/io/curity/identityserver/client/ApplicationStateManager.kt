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

import io.curity.identityserver.client.errors.IllegalApplicationStateException
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.TokenResponse

/*
 * Wraps the AuthState class from the AppAuth library
 * Some or all of the auth state can be persisted to a secure location such as Encrypted Shared Preferences
 */
object ApplicationStateManager {

    private var authState: AuthState? = null
    var idToken: String? = null

    var serverConfiguration: AuthorizationServiceConfiguration
        get () {
            return authState?.authorizationServiceConfiguration
                ?: throw IllegalApplicationStateException("Configuration not set in authState")
        }
        set (configuration) {
            authState = AuthState(configuration)
        }

    var registrationResponse: RegistrationResponse
        get () {
            return authState?.lastRegistrationResponse
                ?: throw IllegalApplicationStateException("Client not registered in authState")
        }
        set (registrationResponse) {
            authState?.update(registrationResponse)
        }

    var tokenResponse: TokenResponse?
        get () {
            return authState?.lastTokenResponse
        }
        set (tokenResponse) {

            val oldAuthState = authState
            authState = AuthState(serverConfiguration)
            if (oldAuthState != null) {
                authState!!.update(oldAuthState.lastRegistrationResponse)
            }

            if (tokenResponse?.idToken != null) {
                idToken = tokenResponse.idToken
            }

            if (tokenResponse == null) {
                idToken = null
            } else {
                authState!!.update(tokenResponse, null)
            }
        }

    fun isRegistered(): Boolean {
        return authState?.lastRegistrationResponse != null
    }
}
