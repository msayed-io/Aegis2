package com.example.crypto

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

/**
 * RFC 6238 (TOTP) over RFC 4226 (HOTP). No silent fallbacks: an invalid secret or parameter
 * produces [TotpException] / `null`, never a fake code.
 */
object TotpGenerator {

    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    val SUPPORTED_ALGORITHMS = listOf("SHA-1", "SHA-256", "SHA-512")
    val SUPPORTED_DIGITS = listOf(6, 8)

    class TotpException(message: String) : Exception(message)

    /** Strict Base32 (RFC 4648) decode: whitespace/dashes ignored, any other foreign char is an error. */
    fun decodeBase32(base32: String): ByteArray {
        val sanitized = base32.trim().uppercase().replace(" ", "").replace("-", "").trimEnd('=')
        if (sanitized.isEmpty()) throw TotpException("المفتاح السري فارغ")
        val bytes = ArrayList<Byte>(sanitized.length * 5 / 8)
        var buffer = 0
        var bitsLeft = 0
        for (c in sanitized) {
            val charIndex = BASE32_CHARS.indexOf(c)
            if (charIndex < 0) throw TotpException("المفتاح السري يحتوي على حرف غير صالح: '$c'")
            buffer = (buffer shl 5) or charIndex
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bytes.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        if (bytes.isEmpty()) throw TotpException("المفتاح السري قصير جداً")
        return bytes.toByteArray()
    }

    /** True when [secret] decodes to at least one byte of key material. */
    fun isValidSecret(secret: String): Boolean = runCatching { decodeBase32(secret) }.isSuccess

    /** Canonical algorithm name ("SHA-1"/"SHA-256"/"SHA-512") or null if unsupported. */
    fun normalizeAlgorithm(raw: String?): String? = when (raw?.uppercase()?.replace("-", "")?.replace("_", "")) {
        null, "", "SHA1" -> "SHA-1"
        "SHA256" -> "SHA-256"
        "SHA512" -> "SHA-512"
        else -> null
    }

    /** Generates the code or throws [TotpException]. */
    fun generateTotp(
        secretBase32: String,
        timeSeconds: Long = System.currentTimeMillis() / 1000L,
        period: Int = 30,
        digits: Int = 6,
        algorithm: String = "SHA-1"
    ): String {
        if (period <= 0) throw TotpException("فترة غير صالحة: $period")
        if (digits !in SUPPORTED_DIGITS) throw TotpException("عدد أرقام غير مدعوم: $digits")
        val macAlgorithm = when (normalizeAlgorithm(algorithm)) {
            "SHA-256" -> "HmacSHA256"
            "SHA-512" -> "HmacSHA512"
            "SHA-1" -> "HmacSHA1"
            else -> throw TotpException("خوارزمية غير مدعومة: $algorithm")
        }
        val keyBytes = decodeBase32(secretBase32)
        val timeStep = Math.floorDiv(timeSeconds, period.toLong())
        val timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array()

        val hash = try {
            Mac.getInstance(macAlgorithm).apply { init(SecretKeySpec(keyBytes, macAlgorithm)) }.doFinal(timeBytes)
        } catch (e: Exception) {
            throw TotpException("فشل حساب HMAC: ${e.message}")
        }

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
        val otp = binary % 10.0.pow(digits).toInt()
        return otp.toString().padStart(digits, '0')
    }

    /** Null-returning variant for UI code paths. */
    fun generateTotpOrNull(
        secretBase32: String,
        timeSeconds: Long,
        period: Int,
        digits: Int,
        algorithm: String
    ): String? = try {
        generateTotp(secretBase32, timeSeconds, period, digits, algorithm)
    } catch (_: Exception) {
        null
    }

    /** Human-readable placeholder shown instead of a code when the account is broken. */
    const val INVALID_CODE_LABEL = "رمز غير صالح"

    fun formatCodeWithDot(code: String): String = when (code.length) {
        6 -> "${code.substring(0, 3)} · ${code.substring(3, 6)}"
        8 -> "${code.substring(0, 4)} · ${code.substring(4, 8)}"
        else -> code
    }

    fun getRemainingSeconds(period: Int = 30, currentTimeSeconds: Long = System.currentTimeMillis() / 1000L): Int {
        if (period <= 0) return 0
        val rem = period - (currentTimeSeconds % period).toInt()
        return if (rem == 0) period else rem
    }

    fun getProgressFraction(period: Int = 30, currentTimeSeconds: Long = System.currentTimeMillis() / 1000L): Float {
        if (period <= 0) return 0f
        return getRemainingSeconds(period, currentTimeSeconds).toFloat() / period.toFloat()
    }
}
