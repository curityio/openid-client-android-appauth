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

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.curity.identityserver.client.ErrorActivity.Companion.handleError
import io.curity.identityserver.client.config.ApplicationConfig
import io.curity.identityserver.client.error.ApplicationException
import io.curity.identityserver.client.error.GENERIC_ERROR
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_AUTHORIZATION_INTENT = 100
    }

    private lateinit var appauth: AppAuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        appauth = AppAuthController(this)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startAuthorizationRedirect()
        }

        fetchMetadataAndRegister()
    }

    private fun fetchMetadataAndRegister() {

        CoroutineScope(Dispatchers.IO).launch {

            val that = this@MainActivity
            try {
                val serverConfiguration = appauth.fetchMetadata(ApplicationConfig.issuer)
                if (!ApplicationStateManager.isRegistered()) {
                    val registrationResponse = appauth.registerClient(serverConfiguration)

                    withContext(Dispatchers.Main) {
                        ApplicationStateManager.serverConfiguration = serverConfiguration
                        ApplicationStateManager.registrationResponse = registrationResponse
                    }
                }

            } catch (exception: ApplicationException) {
                withContext(Dispatchers.Main) {
                    handleError(that, exception.errorTitle,exception.errorDescription ?: GENERIC_ERROR)
                }
            }
        }
    }

    private fun startAuthorizationRedirect() {

        val intent = Intent(this, WaitingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_AUTHORIZATION_INTENT, intent, 0)

        appauth.startAuthorizationRedirect(
            ApplicationStateManager.serverConfiguration,
            ApplicationStateManager.registrationResponse,
            pendingIntent)
    }
}
