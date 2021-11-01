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

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    val config: ApplicationConfig = ApplicationConfigLoader().load(app.applicationContext)
    val state: ApplicationStateManager = ApplicationStateManager()
    val appauth: AppAuthHandler = AppAuthHandler(this.config, app.applicationContext)

    fun dispose() {
        this.appauth.dispose()
    }
}