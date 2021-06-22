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

package io.curity.identityserver.client.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import io.curity.identityserver.client.R
import io.curity.identityserver.client.databinding.ActivityMainBinding
import io.curity.identityserver.client.errors.ApplicationException
import kotlinx.android.synthetic.main.activity_main.toolbar

/*
 * The main activity is just a container of fragments
 */
class MainActivity : AppCompatActivity(), MainActivityEvents {

    private lateinit var binding: ActivityMainBinding

    val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            this.binding.model!!.endLogin(result.data!!)
        }
    }

    val logoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            this.binding.model!!.endLogout(result.data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val model: MainActivityViewModel by viewModels()
        model.initialize(this, this)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.binding.model = model
        this.binding.model!!.registerIfRequired()
    }

    override fun startLoginRedirect(intent: Intent) {
        loginLauncher.launch(intent)
    }

    override fun onLoginSuccess() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(R.id.fragment_authenticated)
    }

    override fun startLogoutRedirect(intent: Intent) {
        logoutLauncher.launch(intent)
    }

    override fun onLogoutSuccess() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(R.id.fragment_unauthenticated)
    }

    override fun handleError(ex: ApplicationException) {
        val errorFragment = this.supportFragmentManager.findFragmentById(R.id.fragment_error) as ErrorFragment
        errorFragment.reportError(ex)
    }
}
