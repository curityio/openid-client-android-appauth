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
