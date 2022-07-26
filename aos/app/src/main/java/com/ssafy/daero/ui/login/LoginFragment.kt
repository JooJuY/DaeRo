package com.ssafy.daero.ui.login

import android.graphics.Paint
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.daero.R
import com.ssafy.daero.application.App
import com.ssafy.daero.base.BaseFragment
import com.ssafy.daero.databinding.FragmentLoginBinding
import com.ssafy.daero.utils.constant.DEFAULT
import com.ssafy.daero.utils.constant.FAIL
import com.ssafy.daero.utils.constant.SUCCESS
import com.ssafy.daero.utils.view.setStatusBarOrigin
import com.ssafy.daero.utils.view.setStatusBarTransparent

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun init() {
        initViews()
        setOnClickListeners()
        observeData()
        jwtLogin()
    }

    private fun initViews() {
        binding.textLoginSignup.paintFlags = Paint.UNDERLINE_TEXT_FLAG;
    }

    private fun setOnClickListeners() {
        binding.buttonLoginEmailLogin.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_emailLoginFragment)
        }
        binding.textLoginSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupEmailFragment)
        }
    }

    private fun jwtLogin() {
        App.prefs.jwt?.let {
            loginViewModel.jwtLogin()
        }
    }

    private fun observeData() {
        loginViewModel.responseState.observe(viewLifecycleOwner) {
            when(it) {
                SUCCESS -> {
                    findNavController().navigate(R.id.action_loginFragment_to_rootFragment)
                    loginViewModel.responseState.value = DEFAULT
                }
                FAIL -> {
                    // jwt 토큰, user_seq 삭제
                    App.prefs.jwt = null
                    App.prefs.userSeq = 0
                    loginViewModel.responseState.value = DEFAULT
                }
            }
        }
        loginViewModel.showProgress.observe(viewLifecycleOwner) {
            binding.progressBarLoginLoading.isVisible = it
        }
    }

    private fun setStatusBarTransParent() {
        binding.constraintLoginInnerContainer.setStatusBarTransparent(requireActivity())
    }

    private fun setStatusBarOrigin() {
        requireActivity().setStatusBarOrigin()
    }

    override fun onStart() {
        super.onStart()
        setStatusBarTransParent()
    }

    override fun onStop() {
        super.onStop()
        setStatusBarOrigin()
    }
}