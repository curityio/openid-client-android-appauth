package io.curity.identityserver.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.curity.identityserver.client.databinding.FragmentErrorBinding
import io.curity.identityserver.client.error.ApplicationException

class ErrorFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.binding = FragmentErrorBinding.inflate(inflater, container, false)
        this.binding.model = ErrorFragmentViewModel()
        return this.binding.root
    }

    fun reportError(exception: ApplicationException) {
        this.binding.model!!.setErrorDetails(exception)
    }
}