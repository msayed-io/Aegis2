package com.example.crypto

import android.net.Uri

/**
 * Full parser for Key-URI format `otpauth://totp/LABEL?secret=...&issuer=...&algorithm=...&digits=...&period=...`
 * (https://github.com/google/google-authenticator/wiki/Key-Uri-Format).
 *
 * Only TOTP is accepted; HOTP, migration payloads and anything malformed are rejected with a
 * precise Arabic error so the UI can show it.
 */
object OtpAuthParser {

    data class Parsed(
        val issuer: String,
        val accountName: String,
        val secret: String,
        val algorithm: String,
        val digits: Int,
        val period: Int
    )

    class ParseException(message: String) : Exception(message)

    fun parse(raw: String): Parsed {
        val text = raw.trim()
        if (text.startsWith("otpauth-migration://", ignoreCase = true)) {
            throw ParseException("هذا رمز نقل من Google Authenticator وليس حساباً واحداً. أضف الحساب من رمز QR الخاص بالخدمة نفسها.")
        }
        if (!text.startsWith("otpauth://", ignoreCase = true)) {
            throw ParseException("رمز QR ليس رمز مصادقة (otpauth)")
        }
        val uri = Uri.parse(text)
        val type = uri.host?.lowercase()
        when (type) {
            "totp" -> Unit
            "hotp" -> throw ParseException("رموز HOTP (العدّاد) غير مدعومة، Onyx يدعم TOTP فقط")
            else -> throw ParseException("نوع otpauth غير معروف: ${type ?: "فارغ"}")
        }

        val secret = uri.getQueryParameter("secret")?.trim().orEmpty()
        if (secret.isEmpty()) throw ParseException("الرمز لا يحتوي على مفتاح سري (secret)")
        if (!TotpGenerator.isValidSecret(secret)) throw ParseException("المفتاح السري في الرمز ليس Base32 صالحاً")

        val algorithm = TotpGenerator.normalizeAlgorithm(uri.getQueryParameter("algorithm"))
            ?: throw ParseException("خوارزمية غير مدعومة: ${uri.getQueryParameter("algorithm")}")

        val digitsParam = uri.getQueryParameter("digits")
        val digits = if (digitsParam.isNullOrBlank()) 6 else digitsParam.toIntOrNull()
            ?: throw ParseException("قيمة digits غير صالحة: $digitsParam")
        if (digits !in TotpGenerator.SUPPORTED_DIGITS) throw ParseException("عدد الأرقام غير مدعوم: $digits (المدعوم 6 أو 8)")

        val periodParam = uri.getQueryParameter("period")
        val period = if (periodParam.isNullOrBlank()) 30 else periodParam.toIntOrNull()
            ?: throw ParseException("قيمة period غير صالحة: $periodParam")
        if (period <= 0 || period > 3600) throw ParseException("الفترة الزمنية غير صالحة: $period ثانية")

        // Label: "Issuer:account" or just "account" (URL-decoded by Uri.path).
        val label = uri.path?.removePrefix("/")?.trim().orEmpty()
        val labelIssuer = if (label.contains(":")) label.substringBefore(":").trim() else ""
        val labelAccount = if (label.contains(":")) label.substringAfter(":").trim() else label
        val issuer = uri.getQueryParameter("issuer")?.trim().takeUnless { it.isNullOrEmpty() } ?: labelIssuer
        if (issuer.isEmpty() && labelAccount.isEmpty()) throw ParseException("الرمز لا يحتوي على اسم الخدمة أو الحساب")

        return Parsed(
            issuer = issuer.ifEmpty { labelAccount },
            accountName = labelAccount,
            secret = secret.uppercase().replace(" ", ""),
            algorithm = algorithm,
            digits = digits,
            period = period
        )
    }
}
