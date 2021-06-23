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

package io.curity.identityserver.client.errors

const val GENERIC_ERROR = "Unknown Error"

open class ApplicationException(val errorTitle: String,
                                val errorDescription: String?,
                                val errorCode: String? = null) : RuntimeException()

class ServerCommunicationException(errorTitle: String, errorDescription: String?, errorCode: String?) :
    ApplicationException(errorTitle, errorDescription, errorCode)

class IllegalApplicationStateException(errorDescription: String) :
    ApplicationException("Invalid Application State", errorDescription)

class InvalidIdTokenException(errorDescription: String) :
    ApplicationException("Invalid ID Token", errorDescription)

