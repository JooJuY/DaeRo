package com.ssafy.daero.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ssafy.daero.application.App
import com.ssafy.daero.base.BaseViewModel
import com.ssafy.daero.data.repository.UserRepository
import com.ssafy.daero.utils.constant.FAIL
import com.ssafy.daero.utils.constant.SUCCESS

class LoginViewModel : BaseViewModel() {
    private val userRepository = UserRepository.get()

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean>
        get() = _showProgress

    val responseState = MutableLiveData<Int>()

    fun jwtLogin() {
        _showProgress.postValue(true)

        addDisposable(
            userRepository.jwtLogin()
                .subscribe(
                    { response ->
                        // userSeq 저장
                        App.prefs.userSeq = response.body()?.user_seq ?: 0

                        _showProgress.postValue(false)
                        responseState.postValue(SUCCESS)
                    },
                    {throwable ->
                        _showProgress.postValue(false)
                        responseState.postValue(FAIL)
                    })
        )
    }
}