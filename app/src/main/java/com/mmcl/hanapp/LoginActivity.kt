package com.mmcl.hanapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivityLoginBinding
import com.mmcl.hanapp.util.InputValidator

// Entry screen. Collects a display name, validates it, stores it as the session identity,
// then hands off to MainActivity (the tabs). If a user is already logged in from a previous
// launch, this screen is skipped entirely.
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // If someone's already logged in, skip login and go straight to the app.
        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonContinue.setOnClickListener { attemptLogin() }
    }

    // Validates the entered name; on success saves it and proceeds, on failure shows an inline error.
    private fun attemptLogin() {
        val rawName = binding.editTextName.text?.toString().orEmpty()

        when (val result = InputValidator.validateName(rawName)) {
            is InputValidator.Result.Valid -> {
                // Store the trimmed name as the active identity for this session.
                session.setCurrentUser(rawName.trim())
                goToMain()
            }
            is InputValidator.Result.Invalid -> {
                // Show the reason directly under the field, the standard Material error pattern.
                binding.inputLayoutName.error = result.reason
            }
        }
    }

    // Launches the main tabbed screen and closes login so back-press won't return here.
    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}