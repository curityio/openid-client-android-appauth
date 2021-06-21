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

package io.curity.identityserver.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import io.curity.identityserver.client.databinding.ActivityMainBinding
import io.curity.identityserver.client.error.ApplicationException
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_AUTHORIZATION_INTENT = 100
    private val REQUEST_CODE_END_SESSION_INTENT   = 101

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val model: MainActivityViewModel by viewModels()
        model.initialize(this)

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.binding.model = model
        this.binding.model!!.registerIfRequired()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_AUTHORIZATION_INTENT && data != null) {
            this.binding.model!!.endLogin(data)
        }
        else if (requestCode == REQUEST_CODE_END_SESSION_INTENT && data != null) {
            this.binding.model!!.endLogout(data)
        }
    }

    fun startLoginRedirect(intent: Intent) {
        this.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION_INTENT)
    }

    fun onLoginSuccess() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(R.id.fragment_authenticated)
    }

    fun startLogoutRedirect(intent: Intent) {
        this.startActivityForResult(intent, REQUEST_CODE_END_SESSION_INTENT)
    }

    fun onLogoutSuccess() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(R.id.fragment_unauthenticated)
    }

    fun handleError(exception: ApplicationException) {
        val errorFragment = this.supportFragmentManager.findFragmentById(R.id.fragment_error) as ErrorFragment
        errorFragment.reportError(exception)
    }
}
