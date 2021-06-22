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

package io.curity.identityserver.client;

import android.content.Context
import androidx.databinding.BaseObservable
import io.curity.identityserver.client.error.ApplicationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthenticatedFragmentViewModel(
    private val context: Context,
    private val runLogoutInActivity: () -> Unit) : BaseObservable() {

    private val appauth = AppAuthHandler(context)
    var subject = ""
    var authenticationDescription = ""

    fun refreshToken() {

        CoroutineScope(Dispatchers.IO).launch {

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
                    // TODO: handle this in main activity's error fragment
                    println(exception)
                }
            }
        }
    }

    fun startLogout() {
        this.runLogoutInActivity()
    }
}
