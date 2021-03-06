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

import io.curity.identityserver.client.error.IllegalApplicationStateException
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.TokenResponse

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

    var tokenResponse: TokenResponse
        get() {
            return authState.lastTokenResponse
                ?: throw IllegalApplicationStateException("No recent tokens")
        }
        set(tokenResponse) {
            authState.update(tokenResponse, null)
        }

    fun isRegistered(): Boolean {
        return authState.lastRegistrationResponse != null
    }
}

