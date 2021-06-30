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

import android.content.Context
import android.content.Context.MODE_PRIVATE
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

    fun load(context: Context) {

        val prefs = context.getSharedPreferences("authState", MODE_PRIVATE)
        val json = prefs.getString("json", null)
        val idToken = prefs.getString("idToken", null)
        if (json != null) {
            this.authState = AuthState.jsonDeserialize(json)
            this.idToken = idToken
        }
    }

    fun save(context: Context) {

        if (authState?.lastRegistrationResponse != null) {

            val authStateToSave = AuthState()
            authStateToSave.update(this.authState!!.lastRegistrationResponse)

            val prefs = context.getSharedPreferences("authState", MODE_PRIVATE)
            prefs.edit()
                .putString("json", authStateToSave.jsonSerializeString())
                .putString("idToken", this.idToken)
                .apply()
        }
    }

    var metadata: AuthorizationServiceConfiguration?
        get () {
            return authState?.authorizationServiceConfiguration
        }
        set (configuration) {
            authState = AuthState(configuration!!)
        }

    var registrationResponse: RegistrationResponse?
        get () {
            return authState?.lastRegistrationResponse
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
            authState = AuthState(metadata!!)
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
}
