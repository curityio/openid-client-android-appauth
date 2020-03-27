package io.curity.identityserver.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_error.*
import kotlin.error

class ErrorActivity : AppCompatActivity() {
    companion object {
        const val ERROR_DESCRIPTION_KEY = "error_description"
        const val ERROR_KEY = "error"
        const val GENERIC_ERROR = "Unknown Error"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        if (intent.extras != null) {
            val error = intent.extras?.get(ERROR_KEY) ?: GENERIC_ERROR
            val errorDescription = intent.extras?.get(ERROR_DESCRIPTION_KEY) ?: ""
            setErrorText(error as String, errorDescription as String)
        } else {
            setErrorText(GENERIC_ERROR, "")
        }
    }

    private fun setErrorText(error: String, errorDescription: String) {
        findViewById<TextView>(R.id.error).text = error
        findViewById<TextView>(R.id.errorDescription).text = errorDescription
    }
}
