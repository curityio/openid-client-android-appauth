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

package io.curity.identityserver.client.views

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.curity.identityserver.client.AppAuthHandler
import io.curity.identityserver.client.ApplicationStateManager
import io.curity.identityserver.client.configuration.ApplicationConfig
import io.curity.identityserver.client.configuration.ApplicationConfigLoader
import io.curity.identityserver.client.views.authenticated.AuthenticatedFragmentViewModel
import io.curity.identityserver.client.views.error.ErrorFragmentViewModel
import io.curity.identityserver.client.views.unauthenticated.UnauthenticatedFragmentViewModel

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    // Global objects
    private val config: ApplicationConfig = ApplicationConfigLoader().load(app.applicationContext)
    private val state: ApplicationStateManager = ApplicationStateManager()
    private val appauth: AppAuthHandler = AppAuthHandler(this.config, app.applicationContext)

    // Child view models
    private var unauthenticatedViewModel: UnauthenticatedFragmentViewModel? = null
    private var authenticatedViewModel:   AuthenticatedFragmentViewModel? = null

    /*
     * Create child view models the first time
     */
    fun getUnauthenticatedViewModel(errorViewModel: ErrorFragmentViewModel): UnauthenticatedFragmentViewModel {

        if (this.unauthenticatedViewModel == null) {
            this.unauthenticatedViewModel = UnauthenticatedFragmentViewModel(
                this.state,
                this.appauth,
                errorViewModel
            )
        }

        return this.unauthenticatedViewModel!!
    }

    fun getAuthenticatedViewModel(errorViewModel: ErrorFragmentViewModel): AuthenticatedFragmentViewModel {

        if (this.authenticatedViewModel == null) {
            this.authenticatedViewModel = AuthenticatedFragmentViewModel(
                this.config,
                this.state,
                this.appauth,
                errorViewModel
            )
        }

        return this.authenticatedViewModel!!
    }

    fun dispose() {
        this.appauth.dispose()
    }
}