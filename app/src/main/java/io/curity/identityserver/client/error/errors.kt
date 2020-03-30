package io.curity.identityserver.client.error

const val GENERIC_ERROR = "Unknown Error"

open class ApplicationException(val errorTitle: String,
                                val errorDescription: String?) : RuntimeException()

class ServerCommunicationException(errorTitle: String, errorDescription: String?) :
    ApplicationException(errorTitle, errorDescription)

class IllegalClientStateException(errorTitle: String, errorDescription: String?) :
    ApplicationException(errorTitle, errorDescription) {
    constructor(errorDescription: String) : this(GENERIC_ERROR, errorDescription)
}
