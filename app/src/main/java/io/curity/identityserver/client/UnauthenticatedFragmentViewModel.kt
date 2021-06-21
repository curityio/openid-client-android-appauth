package io.curity.identityserver.client;

import androidx.databinding.BaseObservable

class UnauthenticatedFragmentViewModel(private val runLoginInActivity: () -> Unit) : BaseObservable() {

    fun startLogin() {
        this.runLoginInActivity()
    }
}
