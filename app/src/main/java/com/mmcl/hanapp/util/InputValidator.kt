package com.mmcl.hanapp.util

// Central place for input validation rules, so the same checks are reused everywhere
// (login now, post form and claim form later) instead of being re-written per screen.
object InputValidator {

    // Result of validating a name field: either valid, or invalid with a reason to show the user.
    sealed class Result {
        object Valid : Result()
        data class Invalid(val reason: String) : Result()
    }

    // Validates a typed display name.
    // Trims surrounding spaces first, then enforces: not blank, and within a sane length range.
    // This prevents empty or absurdly long identities from being used to tag posts/claims.
    fun validateName(rawInput: String): Result {
        val name = rawInput.trim()

        return when {
            name.isEmpty() ->
                Result.Invalid("Please enter your name")
            name.length < MIN_NAME_LENGTH ->
                Result.Invalid("Name is too short")
            name.length > MAX_NAME_LENGTH ->
                Result.Invalid("Name is too long")
            else ->
                Result.Valid
        }
    }

    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 30
}