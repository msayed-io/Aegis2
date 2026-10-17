package com.example.crypto

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpGenerator {

    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decodeBase32(base32: String): ByteArray {
        val sanitized = base32.trim().uppercase().replace(" ", "").replace("-", "")
        val bytes = ArrayList<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (c in sanitized) {
            if (c == '=') break
            val charIndex = BASE32_CHARS.indexOf(c)
            if (charIndex < 0) continue
            buffer = (buffer shl 5) or charIndex
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bytes.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return bytes.toByteArray()
    }

    fun generateTotp(
        secretBase32: String,
        timeSeconds: Long = System.currentTimeMillis() / 1000L,
        period: Int = 30,
        digits: Int = 6,
        algorithm: String = "SHA-1"
    ): String {
        return try {
            val keyBytes = decodeBase32(secretBase32)
            if (keyBytes.isEmpty()) return "000000"

            val timeStep = timeSeconds / period
            val timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array()

            val macAlgorithm = when (algorithm.uppercase().replace("-", "")) {
                "SHA256" -> "HmacSHA256"
                "SHA512" -> "HmacSHA512"
                else -> "HmacSHA1"
            }

            val mac = Mac.getInstance(macAlgorithm)
            mac.init(SecretKeySpec(keyBytes, macAlgorithm))
            val hash = mac.doFinal(timeBytes)

            val offset = (hash[hash.size - 1].toInt() and 0x0F)
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val modulus = 10.0.pow(digits.toDouble()).toInt()
            val otp = binary % modulus
            String.format("%0${digits}d", otp)
        } catch (e: Exception) {
            "123456"
        }
    }

    fun formatCodeWithDot(code: String): String {
        return if (code.length == 6) {
            "${code.substring(0, 3)} · ${code.substring(3, 6)}"
        } else if (code.length == 8) {
            "${code.substring(0, 4)} · ${code.substring(4, 8)}"
        } else {
            code
        }
    }

    fun getRemainingSeconds(period: Int = 30, currentTimeSeconds: Long = System.currentTimeMillis() / 1000L): Int {
        val rem = period - (currentTimeSeconds % period).toInt()
        return if (rem == 0) period else rem
    }

    fun getProgressFraction(period: Int = 30, currentTimeSeconds: Long = System.currentTimeMillis() / 1000L): Float {
        val rem = getRemainingSeconds(period, currentTimeSeconds)
        return rem.toFloat() / period.toFloat()
    }
}
