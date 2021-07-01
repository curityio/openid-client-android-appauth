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

package io.curity.identityserver.client.views.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.curity.identityserver.client.databinding.FragmentErrorBinding

class ErrorFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewModel: ErrorFragmentViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
        )

        this.binding = FragmentErrorBinding.inflate(inflater, container, false)
        this.binding.model = viewModel
        return this.binding.root
    }
}