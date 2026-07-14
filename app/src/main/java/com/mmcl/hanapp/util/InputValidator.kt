package com.mmcl.hanapp.util

// Central place for input validation rules, reused across login, sign-up,
// and the post/claim forms.
object InputValidator {

    sealed class Result {
        object Valid : Result()
        data class Invalid(val reason: String) : Result()
    }

    // Validates a typed display name / username.
    fun validateName(rawInput: String): Result {
        val name = rawInput.trim()
        return when {
            name.isEmpty() -> Result.Invalid("Please enter your name")
            name.length < MIN_NAME_LENGTH -> Result.Invalid("Name is too short")
            name.length > MAX_NAME_LENGTH -> Result.Invalid("Name is too long")
            else -> Result.Valid
        }
    }

    // Validates a sign-up password against a standard complexity policy:
    // at least 8 characters, one uppercase letter, one lowercase letter,
    // one digit, and one special character. Collects every unmet
    // requirement into a single, clear message rather than stopping at
    // the first failure, so the user sees everything they need to fix
    // in one pass instead of a frustrating one-at-a-time loop.
    fun validatePassword(rawInput: String): Result {
        if (rawInput.isEmpty()) {
            return Result.Invalid("Please enter a password")
        }

        val problems = mutableListOf<String>()

        if (rawInput.length < MIN_PASSWORD_LENGTH) {
            problems.add("at least $MIN_PASSWORD_LENGTH characters")
        }
        if (rawInput.none { it.isUpperCase() }) {
            problems.add("an uppercase letter")
        }
        if (rawInput.none { it.isLowerCase() }) {
            problems.add("a lowercase letter")
        }
        if (rawInput.none { it.isDigit() }) {
            problems.add("a number")
        }
        if (rawInput.none { !it.isLetterOrDigit() }) {
            problems.add("a special character")
        }

        return if (problems.isEmpty()) {
            Result.Valid
        } else {
            Result.Invalid("Password must include " + problems.joinToString(", "))
        }
    }

    // Validates the item name field on the post form.
    fun validateItemName(rawInput: String): Result {
        val name = rawInput.trim()
        return when {
            name.isEmpty() -> Result.Invalid("Item name is required")
            name.length > 60 -> Result.Invalid("Item name must be 60 characters or fewer")
            else -> Result.Valid
        }
    }

    // Validates the description field on the post form.
    fun validateDescription(rawInput: String): Result {
        val description = rawInput.trim()
        return when {
            description.isEmpty() -> Result.Invalid("Description is required")
            description.length > 300 -> Result.Invalid("Description must be 300 characters or fewer")
            else -> Result.Valid
        }
    }

    // Validates a contact number: exactly 11 digits, numbers only.
// Matches the standard PH mobile number length (e.g. 09171234567).
    fun validateContactNumber(rawInput: String): Result {
        val number = rawInput.trim()
        return when {
            number.isEmpty() -> Result.Invalid("Contact number is required")
            !number.all { it.isDigit() } -> Result.Invalid("Contact number must contain only digits")
            number.length != 11 -> Result.Invalid("Contact number must be exactly 11 digits")
            else -> Result.Valid
        }
    }

    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 30
    private const val MIN_PASSWORD_LENGTH = 8
}