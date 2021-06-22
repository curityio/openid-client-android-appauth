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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import io.curity.identityserver.client.databinding.FragmentAuthenticatedBinding
import io.curity.identityserver.client.error.ApplicationException
import io.curity.identityserver.client.error.InvalidIdTokenException
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder

class AuthenticatedFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentAuthenticatedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val mainViewModel: MainActivityViewModel by activityViewModels()

        this.binding = FragmentAuthenticatedBinding.inflate(inflater, container, false)
        this.binding.model = AuthenticatedFragmentViewModel(this.requireContext(), mainViewModel::startLogout)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val idToken = ApplicationStateManager.tokenResponse?.idToken
            viewDataFromIdToken(idToken)

        } catch (e: ApplicationException) {
            // handleError(this, e.errorTitle, e.errorDescription)
        }
    }

    private fun viewDataFromIdToken(idToken: String?) {

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
    }
}