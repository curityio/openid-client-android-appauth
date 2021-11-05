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
import androidx.fragment.app.viewModels
import io.curity.identityserver.client.databinding.FragmentAuthenticatedBinding
import io.curity.identityserver.client.views.MainActivity
import io.curity.identityserver.client.views.MainActivityViewModel
import io.curity.identityserver.client.views.error.ErrorFragmentViewModel

class AuthenticatedFragment : androidx.fragment.app.Fragment() {

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

        // Create the view model the first time the view is created
        val mainViewModel: MainActivityViewModel by activityViewModels()
        val errorViewModel: ErrorFragmentViewModel by viewModels()
        val viewModel = mainViewModel.getAuthenticatedViewModel(errorViewModel)

        // Handle events sent from the view model
        viewModel.logoutStarted.observe(this, { event ->
            event?.getData()?.let {
                startLogoutRedirect(it)
            }
        })
        viewModel.logoutCompleted.observe(this, { event ->
            event?.getData()?.let {
                onLoggedOut()
            }
        })

        // Complete the view setup
        this.binding = FragmentAuthenticatedBinding.inflate(inflater, container, false)
        this.binding.model = viewModel
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.binding.model!!.processTokens()
    }

    private fun startLogoutRedirect(intent: Intent) {
        this.logoutLauncher.launch(intent)
    }

    private fun onLoggedOut() {
        val mainActivity = this.activity as MainActivity
        mainActivity.onLoggedOutNavigate()
    }
}