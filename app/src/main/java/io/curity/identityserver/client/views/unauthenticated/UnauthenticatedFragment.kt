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

package io.curity.identityserver.client.views.unauthenticated

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import io.curity.identityserver.client.databinding.FragmentUnauthenticatedBinding
import io.curity.identityserver.client.views.MainActivity
import io.curity.identityserver.client.views.MainActivityViewModel
import io.curity.identityserver.client.views.error.ErrorFragmentViewModel

class UnauthenticatedFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentUnauthenticatedBinding

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            this.binding.model!!.endLogin(result.data!!)
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
        val viewModel = mainViewModel.getUnauthenticatedViewModel(errorViewModel)

        // Handle events sent from the view model
        viewModel.loginStarted.observe(this, { event ->
            event?.getData()?.let {
                startLoginRedirect(it)
            }
        })
        viewModel.loginCompleted.observe(this, { event ->
            event?.getData()?.let {
                onLoggedIn()
            }
        })

        // Complete the view setup
        this.binding = FragmentUnauthenticatedBinding.inflate(inflater, container, false)
        this.binding.model = viewModel
        return this.binding.root
    }

    private fun startLoginRedirect(intent: Intent) {
        this.loginLauncher.launch(intent)
    }

    private fun onLoggedIn() {
        val mainActivity = this.activity as MainActivity
        mainActivity.onLoggedInNavigate()
    }
}