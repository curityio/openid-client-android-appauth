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

package io.curity.identityserver.client.views;

import androidx.databinding.BaseObservable

class AuthenticatedFragmentViewModel(private val runLogoutInActivity: () -> Unit) : BaseObservable() {

    val subject = "SUBJECT"
    val authenticationDescription = "DESCRIPTION"

    // TODO: receive main view model events

    /*
    val idToken = ApplicationStateManager.tokenResponse?.idToken
    */

    /*private fun viewDataFromIdToken(idToken: String?) {

        val jwtConsumer = JwtConsumerBuilder()
            .setSkipSignatureVerification() // Not required in code flow, since the token is fetched from the server using TLS
            .setRequireSubject()
            .setAllowedClockSkewInSeconds(30)
            .setExpectedIssuer(ApplicationStateManager.serverConfiguration.discoveryDoc?.issuer)
            .setExpectedAudience(ApplicationStateManager.registrationResponse.clientId)
            .build()

        val jwtClaims = try {
            jwtConsumer.processToClaims(idToken)
        } catch (e: InvalidJwtException) {
            throw InvalidIdTokenException(e.message ?: "Failed to parse id token")
        }

        val greeting = getString(R.string.authenticated_greeting)
        val subject = jwtClaims.subject

        val descriptionPart1 = getString(R.string.authn_description1)
        val descriptionPart2 = getString(R.string.authn_description2)
        val time = jwtClaims.getNumericDateClaimValue("auth_time")
        val acr = jwtClaims.getClaimValueAsString("acr")

        this.binding.model!!.subject = "$greeting $subject"
        this.binding.model!!.authenticationDescription = "$descriptionPart1 $time $descriptionPart2 $acr"
    }*/

    fun refreshToken() {

        /*CoroutineScope(Dispatchers.IO).launch {

            val model = this@AuthenticatedFragmentViewModel
            try {
                val response = appauth.refreshAccessToken(
                    ApplicationStateManager.tokenResponse!!.refreshToken!!,
                    ApplicationStateManager.serverConfiguration,
                    ApplicationStateManager.registrationResponse
                )

                withContext(Dispatchers.Main) {
                    if (response != null) {
                        ApplicationStateManager.tokenResponse = response
                    }
                }

            } catch (exception: ApplicationException) {

                withContext(Dispatchers.Main) {
                    println(exception)
                }
            }
        }*/
    }

    fun startLogout() {
        this.runLogoutInActivity()
    }
}
