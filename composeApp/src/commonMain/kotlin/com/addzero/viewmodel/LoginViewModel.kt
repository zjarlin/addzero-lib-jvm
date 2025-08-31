package com.addzero.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.assist.api
import com.addzero.core.network.AddHttpClient
import com.addzero.entity.CheckSignInput
import com.addzero.entity.SecondLoginDTO
import com.addzero.entity.SignInStatus
import com.addzero.generated.api.ApiProvider.loginApi
import com.addzero.generated.api.ApiProvider.sysUserCenterApi
import com.addzero.generated.isomorphic.SysUserIso
import com.addzero.settings.SettingContext4Compose
import com.addzero.ui.infra.model.navigation.RecentTabsManager
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val defaultUsername = "admin"


/**
 * 登录状态管理
 */
@KoinViewModel
class LoginViewModel : ViewModel(), KoinComponent {

    // 注入最近标签页管理器
    private val recentTabsManager: RecentTabsManager by inject()

    var singinStatus: SignInStatus by mutableStateOf(
        SignInStatus.None
    )
    var checkSignInput by mutableStateOf(CheckSignInput.USERNAME)


    //登录页面状态
    var loginContext by mutableStateOf(defaultUsername)

    var password by mutableStateOf("")

    //注册页面状态
    var confirmPassword by mutableStateOf("")
    var userRegFormState by mutableStateOf(SysUserIso())


    // 当前登录用户
    var currentToken by mutableStateOf<SysUserIso?>(null)
    var permissonFlag by mutableStateOf(false)
        private set

    // 登录状态
    var isLoading by mutableStateOf(false)
        private set

    fun updateUserRegFormStateByContextAct(loginContextAct: CheckSignInput, loginContext: String) {
        userRegFormState = when (loginContextAct) {
            CheckSignInput.PHONE -> userRegFormState.copy(phone = loginContext)
            CheckSignInput.EMAIL -> userRegFormState.copy(email = loginContext)
            CheckSignInput.USERNAME -> userRegFormState.copy(username = loginContext)
        }
    }

    fun updateCheckSignInput(signin: SignInStatus) {
        //清理掉旧的状态
        if (signin is SignInStatus.Notregister) {
            checkSignInput = signin.loginContextAct
            userRegFormState = SysUserIso()
            updateUserRegFormStateByContextAct(signin.loginContextAct, loginContext)
        } else if (signin is SignInStatus.Alredyregister) {
            checkSignInput = signin.loginContextAct
            val sysUserIso = signin.sysUserIso
            //清理掉旧的状态
            userRegFormState = sysUserIso
            updateUserRegFormStateByContextAct(signin.loginContextAct, loginContext)
        }

    }

    /**
     * 初步登录
     */
    fun signFirst() {
        api(isLoading, onLodingChange = { isLoading = it }) {
            //超管直接登
            if (loginContext == defaultUsername) {
                //todo 这里要设置真实token
                AddHttpClient.setToken(loginContext)
                //设置当前登录人即可进入主界面
                currentToken = SysUserIso().copy(username = defaultUsername, nickname = "超级管理员")
                return@api
            }
            val signin = loginApi.signin(loginContext)

            //更新表单
            updateCheckSignInput(signin)
            singinStatus = signin

        }
    }

    /**
     * 登出
     */
    fun logout() {
        api {
            sysUserCenterApi.logout()
            currentToken = null
            AddHttpClient.setToken(null)

            // 清空最近的标签页
            recentTabsManager.clear()
        }
    }

    /**
     * 检查权限
     */
    fun hasPermission(permissionCode: String): Boolean {
        api {

            val hasPermition1 = loginApi.hasPermition(permissionCode)
            permissonFlag = hasPermition1
        }
        return permissonFlag
    }

    fun onForgetPassword() {
        //todo  忘记密码找回界面
//        todo 接入手机短信验证码,邮箱验证码
        TODO("Not yet implemented")
    }

    //    注册
    fun register() {
        api {
            if (userRegFormState.password == confirmPassword) {
                val signup = loginApi.signup(userRegFormState)
                if (signup) {
                    singinStatus = SignInStatus.Alredyregister(
                        checkSignInput,
                        sysUserIso = userRegFormState
                    )
                    onSecondLogin()
                }
            } else {
                com.addzero.component.toast.ToastManager.error("两次输入的密码不一致")
            }

        }
    }

    fun onSecondLogin() {
        api {
            val secondLoginDTO = SecondLoginDTO(userRegFormState)
            val signinSecond = loginApi.signinSecond(secondLoginDTO)
            currentToken = signinSecond.sysUserIso
            val token = signinSecond.token

            if (token.isNotBlank()) {
                com.addzero.component.toast.ToastManager.success(SettingContext4Compose.WELCOME_MSG)
            }

            AddHttpClient.setToken(null)
            AddHttpClient.setToken(token)
        }
    }
}
