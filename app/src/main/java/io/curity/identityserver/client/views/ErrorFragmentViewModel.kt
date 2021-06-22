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

import androidx.databinding.BaseObservable
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.errors.GENERIC_ERROR

class ErrorFragmentViewModel : BaseObservable() {

    var title = ""
    var description = ""

    fun setErrorDetails(ex: ApplicationException) {
        this.title = ex.errorTitle
        this.description = ex.errorDescription ?: GENERIC_ERROR
        this.notifyChange()
    }
}