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
import io.curity.identityserver.client.R
import io.curity.identityserver.client.databinding.FragmentUnauthenticatedBinding
import io.curity.identityserver.client.errors.ApplicationException
import io.curity.identityserver.client.views.MainActivity
import io.curity.identityserver.client.views.MainActivityViewModel
import io.curity.identityserver.client.views.error.ErrorFragment

class UnauthenticatedFragment : androidx.fragment.app.Fragment(), UnauthenticatedFragmentEvents {

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

        val mainViewModel: MainActivityViewModel by activityViewModels()

        this.binding = FragmentUnauthenticatedBinding.inflate(inflater, container, false)
        this.binding.model = UnauthenticatedFragmentViewModel(this, mainViewModel.appauth)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.binding.model!!.registerIfRequired()
    }

    override fun startLoginRedirect(intent: Intent) {
        loginLauncher.launch(intent)
    }

    override fun onLoginSuccess() {
        val mainActivity = this.activity as MainActivity
        mainActivity.postLoginNavigate()
    }

    override fun handleError(ex: ApplicationException) {
        val errorFragment = this.childFragmentManager.findFragmentById(R.id.fragment_unauthenticated_error) as ErrorFragment
        errorFragment.reportError(ex)
    }
}