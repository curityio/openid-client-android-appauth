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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.error.ApplicationException
import io.curity.identityserver.client.error.GENERIC_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
/*
class WaitingActivity : AppCompatActivity() {

    private lateinit var appauth: AppAuthMessages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        appauth = AppAuthMessages(this)
        handleAuthorizationResponse(intent)
    }

    private fun handleAuthorizationResponse(intent: Intent) {

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@WaitingActivity
            try {

                val response = AuthorizationResponse.fromIntent(intent)
                val error = AuthorizationException.fromIntent(intent)
                val tokenResponse = appauth.handleAuthorizationResponse(response, error, ApplicationStateManager.registrationResponse)

                withContext(Dispatchers.Main) {
                    ApplicationStateManager.tokenResponse = tokenResponse
                    startActivity(Intent(that, AuthenticatedActivity::class.java))
                }

            } catch (exception: ApplicationException) {

                withContext(Dispatchers.Main) {
                    ErrorActivity.handleError(that, exception.errorTitle, exception.errorDescription ?: GENERIC_ERROR)
                }
            }
        }
    }
}
*/