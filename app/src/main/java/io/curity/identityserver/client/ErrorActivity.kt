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

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import io.curity.identityserver.client.error.GENERIC_ERROR

class ErrorActivity : AppCompatActivity() {
    companion object {
        const val ERROR_DESCRIPTION_KEY = "error_description"
        const val ERROR_KEY = "error"

        fun handleError(context: Context,
                        error: String?,
                        errorDescription: String?) {
            val viewError = Intent(context, ErrorActivity::class.java)
            viewError.putExtra(ERROR_KEY, error ?: GENERIC_ERROR)
            viewError.putExtra(ERROR_DESCRIPTION_KEY, errorDescription ?: "")
            startActivity(context, viewError, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val error = intent.extras?.getString(ERROR_KEY) ?: GENERIC_ERROR
        val errorDescription = intent.extras?.getString(ERROR_DESCRIPTION_KEY) ?: GENERIC_ERROR

        setErrorText(error, errorDescription)
    }

    private fun setErrorText(error: String, errorDescription: String) {
        findViewById<TextView>(R.id.error).text = error
        findViewById<TextView>(R.id.errorDescription).text = errorDescription
    }
}
