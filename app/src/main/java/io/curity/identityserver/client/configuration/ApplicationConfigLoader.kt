package io.curity.identityserver.client.configuration

import android.content.Context
import com.google.gson.Gson
import io.curity.identityserver.client.R
import okio.Buffer
import okio.buffer
import okio.source
import java.nio.charset.Charset

class ApplicationConfigLoader {

    /*
     * Load configuration from the resource
     */
    fun load(context: Context): ApplicationConfig {

        // Get the raw resource
        val stream = context.resources.openRawResource(R.raw.config)
        val configSource = stream.source().buffer()

        // Read it as JSON text
        val configBuffer = Buffer()
        configSource.readAll(configBuffer)
        val configJson = configBuffer.readString(Charset.forName("UTF-8"))

        // Deserialize it into objects
        return Gson().fromJson(configJson, ApplicationConfig::class.java)
    }
}