/*
 *  Copyright 2020 Curity AB
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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.ContentValues.TAG
import android.view.View
import android.widget.Button
import android.widget.TextView
import io.curity.identityserver.client.ErrorActivity.Companion.handleError
import org.json.JSONObject

class AuthenticatedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticated)


        if (intent.extras == null) return showError("No data in intent")
        if (intent.extras?.get("id_token_claims") == null) {
            return showError("Do not find id token claims")
        }

        val jsonString = intent.extras?.get("id_token_claims") as String
        val jsonObject = JSONObject(jsonString)
        val subject = jsonObject["sub"]
        val authTime = jsonObject["auth_time"]
        val acr = jsonObject["acr"]
        val title = findViewById<TextView>(R.id.hello_subject)
        title.text = getString(R.string.hello_subject, subject)

        val authnDescription = findViewById<TextView>(R.id.authn_description)
        authnDescription.text = getString(R.string.authn_description, authTime, acr)

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener(logout())

    }

    private fun logout(): (View) -> Unit = {
        val logoutIntent = Intent(applicationContext, MainActivity::class.java)
        startActivity(logoutIntent)
    }

    private fun showError(error: String) {
        handleError(this, ErrorActivity.GENERIC_ERROR, error)
    }
}
