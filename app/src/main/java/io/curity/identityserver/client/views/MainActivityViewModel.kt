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

import android.content.Context
import androidx.lifecycle.ViewModel
import io.curity.identityserver.client.AppAuthHandler
import io.curity.identityserver.client.ApplicationStateManager
import io.curity.identityserver.client.configuration.ApplicationConfig
import java.lang.ref.WeakReference

class MainActivityViewModel() : ViewModel() {

    lateinit var context: WeakReference<Context>
    lateinit var appauth: AppAuthHandler

    fun initialize(activity: WeakReference<Context>) {
        this.context = activity
        val config = ApplicationConfig()
        ApplicationStateManager.load(activity.get()!!)
        this.appauth = AppAuthHandler(config, context.get()!!)
    }

    fun save() {
        if (this.context.get() != null) {
            ApplicationStateManager.save(this.context.get()!!)
        }
    }
}