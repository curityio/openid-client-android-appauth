package io.curity.identityserver.client

import androidx.databinding.BaseObservable
import io.curity.identityserver.client.error.ApplicationException
import io.curity.identityserver.client.error.GENERIC_ERROR

class ErrorFragmentViewModel : BaseObservable() {

    var title = ""
    var description = ""

    fun setErrorDetails(exception: ApplicationException) {
        this.title = exception.errorTitle
        this.description = exception.errorDescription ?: GENERIC_ERROR
        this.notifyChange()
    }
}