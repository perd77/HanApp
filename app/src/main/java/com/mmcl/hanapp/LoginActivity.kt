package com.mmcl.hanapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mmcl.hanapp.data.repository.AuthRepository
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivityLoginBinding
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

// Entry screen. Collects a username + password, authenticates against real
// Supabase accounts, and stores the resulting session. If already logged in
// from a previous launch, this screen is skipped entirely.
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // must be called before super.onCreate()
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener { attemptLogin() }
        binding.textGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    // Basic presence check before hitting the network — the real validity
    // check (does this account/password combo exist) happens server-side.
    private fun attemptLogin() {
        val username = binding.editTextUsername.text?.toString().orEmpty().trim()
        val password = binding.editTextPassword.text?.toString().orEmpty()

        if (username.isEmpty()) {
            binding.inputLayoutUsername.error = "Please enter your username"
            return
        }
        binding.inputLayoutUsername.error = null

        if (password.isEmpty()) {
            binding.inputLayoutPassword.error = "Please enter your password"
            return
        }
        binding.inputLayoutPassword.error = null

        performLogin(username, password)
    }

    private fun performLogin(username: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> {
                    session.saveSession(
                        accessToken = result.data.accessToken,
                        refreshToken = result.data.refreshToken,
                        userId = result.data.user.id,
                        username = username
                    )
                    goToMain()
                }
                is NetworkResult.Error -> {
                    setLoading(false)
                    binding.inputLayoutPassword.error = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressLogin.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !loading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}