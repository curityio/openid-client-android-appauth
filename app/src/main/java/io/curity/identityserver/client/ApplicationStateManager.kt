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

import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenResponse

/*
 * Wraps the AuthState class from the AppAuth library
 * Some or all of the auth state can be persisted to a secure location such as Encrypted Shared Preferences
 */
class ApplicationStateManager() {

    private var authState: AuthState? = null
    var idToken: String? = null

    /*
     * Manage storing or updating the token response
     */
    fun saveTokens(tokenResponse: TokenResponse) {

        // When refreshing tokens, the Curity Identity Server does not issue a new ID token
        // The AppAuth code does not allow us to update the token response with the original ID token
        // Therefore we store the ID token separately
        if (tokenResponse.idToken != null) {
            this.idToken = tokenResponse.idToken
        }

        this.authState!!.update(tokenResponse, null)
    }

    /*
    * Clear tokens upon logout or when the session expires
    */
    fun clearTokens() {

        val metadata = this.authState?.authorizationServiceConfiguration
        this.authState = AuthState(metadata!!)
        this.idToken = null
    }

    var metadata: AuthorizationServiceConfiguration?
    get () {
        return this.authState?.authorizationServiceConfiguration
    }
    set (configuration) {
        this.authState = AuthState(configuration!!)
    }

    val tokenResponse: TokenResponse?
    get () {
        return this.authState?.lastTokenResponse
    }
}
