package com.example.model

data class OnyxAccount(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val algorithm: String = "SHA-1",
    val digits: Int = 6,
    val period: Int = 30,
    val addedDate: String = ""
) {
    val badgeLetter: String
        get() = when {
            issuer.equals("GitLab", ignoreCase = true) -> "GL"
            issuer.isNotBlank() -> issuer.take(1).uppercase()
            accountName.isNotBlank() -> accountName.take(1).uppercase()
            else -> "A"
        }
}
