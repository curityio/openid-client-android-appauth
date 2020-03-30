package io.curity.identityserver.client.error

const val GENERIC_ERROR = "Unknown Error"

open class ApplicationException(val errorTitle: String,
                                val errorDescription: String?) : RuntimeException()

class ServerCommunicationException(errorTitle: String, errorDescription: String?) :
    ApplicationException(errorTitle, errorDescription)

class IllegalApplicationStateException(errorDescription: String) :
    ApplicationException(GENERIC_ERROR, errorDescription)
