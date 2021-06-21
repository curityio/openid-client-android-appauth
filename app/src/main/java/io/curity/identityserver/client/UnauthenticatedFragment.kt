package io.curity.identityserver.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import io.curity.identityserver.client.databinding.FragmentUnauthenticatedBinding

class UnauthenticatedFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentUnauthenticatedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val mainViewModel: MainActivityViewModel by activityViewModels()

        this.binding = FragmentUnauthenticatedBinding.inflate(inflater, container, false)
        this.binding.model = UnauthenticatedFragmentViewModel(mainViewModel::startLogin)
        return this.binding.root
    }
}