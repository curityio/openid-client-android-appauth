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

package io.curity.identityserver.client.configuration

import android.net.Uri

/*
 * Standard mobile OAuth configuration settings
 */
class ApplicationConfig {
    lateinit var issuer: String
    lateinit var clientID: String
    lateinit var redirectUri: String
    lateinit var postLogoutRedirectUri: String
    lateinit var scope: String

    fun getIssuerUri(): Uri {
        return Uri.parse(issuer)
    }

    fun getRedirectUri(): Uri {
        return Uri.parse(redirectUri)
    }

    fun getPostLogoutRedirectUri(): Uri {
        return Uri.parse(postLogoutRedirectUri)
    }
}
