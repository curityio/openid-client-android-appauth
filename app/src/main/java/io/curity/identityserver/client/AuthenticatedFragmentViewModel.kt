package io.curity.identityserver.client;

import android.content.Context
import androidx.databinding.BaseObservable
import io.curity.identityserver.client.error.ApplicationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthenticatedFragmentViewModel(
    private val context: Context,
    private val runLogoutInActivity: () -> Unit) : BaseObservable() {

    private val appauth = AppAuthHandler(context)
    var subject = ""
    var authenticationDescription = ""

    fun refreshToken() {

        CoroutineScope(Dispatchers.IO).launch {

            val model = this@AuthenticatedFragmentViewModel
            try {
                val response = appauth.refreshAccessToken(
                    ApplicationStateManager.tokenResponse!!.refreshToken!!,
                    ApplicationStateManager.serverConfiguration,
                    ApplicationStateManager.registrationResponse
                )

                withContext(Dispatchers.Main) {
                    if (response != null) {
                        ApplicationStateManager.tokenResponse = response
                    }
                }

            } catch (exception: ApplicationException) {

                withContext(Dispatchers.Main) {
                    // TODO: handle this in main activity's error fragment
                    println(exception)
                }
            }
        }
    }

    fun startLogout() {
        this.runLogoutInActivity()
    }
}
