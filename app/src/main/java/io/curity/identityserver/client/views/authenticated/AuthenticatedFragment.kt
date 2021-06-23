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

package io.curity.identityserver.client.views.authenticated

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import io.curity.identityserver.client.databinding.FragmentAuthenticatedBinding
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.views.MainActivity
import io.curity.identityserver.client.views.MainActivityViewModel

class AuthenticatedFragment : androidx.fragment.app.Fragment(), AuthenticatedFragmentEvents {

    private lateinit var binding: FragmentAuthenticatedBinding

    private val logoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            this.binding.model!!.endLogout(result.data!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val mainViewModel: MainActivityViewModel by activityViewModels()

        this.binding = FragmentAuthenticatedBinding.inflate(inflater, container, false)
        this.binding.model = AuthenticatedFragmentViewModel(this, mainViewModel.appauth)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.binding.model!!.processTokens()
    }

    override fun startLogoutRedirect(intent: Intent) {
        logoutLauncher.launch(intent)
    }

    override fun onLogoutSuccess() {
        val mainActivity = this.activity as MainActivity
        mainActivity.postLogoutNavigate()
    }

    override fun handleError(ex: ApplicationException) {
        val mainActivity = this.activity as MainActivity
        mainActivity.handleError(ex)
    }
}