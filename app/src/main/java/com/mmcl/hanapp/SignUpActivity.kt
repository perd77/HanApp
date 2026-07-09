package com.mmcl.hanapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mmcl.hanapp.data.repository.AuthRepository
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivitySignUpBinding
import com.mmcl.hanapp.util.InputValidator
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import android.widget.Toast

// Account creation screen. Collects a username + password, creates a real
// Supabase account behind a generated placeholder email, and logs the user
// in immediately since email confirmation is disabled.
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var session: SessionManager
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.buttonSignUp.setOnClickListener { attemptSignUp() }
        binding.textGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Validates both fields, then creates the account if they pass.
    private fun attemptSignUp() {
        val username = binding.editTextUsername.text?.toString().orEmpty().trim()
        val password = binding.editTextPassword.text?.toString().orEmpty()

        val usernameResult = InputValidator.validateName(username)
        val passwordResult = InputValidator.validatePassword(password)

        var hasError = false
        if (usernameResult is InputValidator.Result.Invalid) {
            binding.inputLayoutUsername.error = usernameResult.reason
            hasError = true
        } else {
            binding.inputLayoutUsername.error = null
        }
        if (passwordResult is InputValidator.Result.Invalid) {
            binding.inputLayoutPassword.error = passwordResult.reason
            hasError = true
        } else {
            binding.inputLayoutPassword.error = null
        }
        if (hasError) return

        performSignUp(username, password)
    }

    // Calls the repository and reacts to loading/success/error.
    private fun performSignUp(username: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            when (val result = authRepository.signUp(username, password)) {
                is NetworkResult.Success -> {
                    // Deliberately NOT saving the session here — even though Supabase
                    // returns one immediately (since email confirmation is off), we
                    // send the user to log in manually rather than auto-signing them in.
                    Toast.makeText(
                        this@SignUpActivity,
                        getString(R.string.signup_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    goToLogin()
                }
                is NetworkResult.Error -> {
                    setLoading(false)
                    binding.inputLayoutPassword.error = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressSignUp.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonSignUp.isEnabled = !loading
    }

}