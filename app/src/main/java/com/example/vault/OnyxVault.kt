package com.example.vault

import com.example.model.AegisAccount
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Onyx Vault — our own file format, extension ".ox".
 *
 * Binary layout (big-endian):
 *   magic   "OX"            2 bytes
 *   version 1               1 byte
 *   salt                    16 bytes   (PBKDF2 salt)
 *   iv                      12 bytes   (AES-GCM nonce)
 *   ciphertext+tag          rest       (AES-256-GCM over UTF-8 JSON payload)
 *
 * Key derivation: PBKDF2WithHmacSHA256, 200 000 iterations, 256-bit key.
 * A wrong password / tampered file fails GCM tag verification -> [VaultException].
 */
object OnyxVault {
    const val EXTENSION = "ox"
    const val MIME_TYPE = "application/octet-stream"
    private val MAGIC = byteArrayOf('O'.code.toByte(), 'X'.code.toByte())
    private const val VERSION: Byte = 1
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val ITERATIONS = 200_000
    private const val KEY_BITS = 256

    class VaultException(message: String) : Exception(message)

    fun defaultFileName(): String {
        val ts = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US).format(java.util.Date())
        return "Onyx-Vault-$ts.$EXTENSION"
    }

    fun export(accounts: List<AegisAccount>, password: CharArray): ByteArray {
        val payload = JSONObject().apply {
            put("app", "Onyx")
            put("format", 1)
            put("exportedAt", System.currentTimeMillis())
            put("accounts", JSONArray().apply {
                accounts.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id); put("issuer", a.issuer); put("accountName", a.accountName)
                        put("secret", a.secret); put("algorithm", a.algorithm)
                        put("digits", a.digits); put("period", a.period); put("addedDate", a.addedDate)
                    })
                }
            })
        }.toString().toByteArray(Charsets.UTF_8)

        val rnd = SecureRandom()
        val salt = ByteArray(SALT_LEN).also(rnd::nextBytes)
        val iv = ByteArray(IV_LEN).also(rnd::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(payload)

        val out = ByteArrayOutputStream()
        DataOutputStream(out).use { d ->
            d.write(MAGIC); d.writeByte(VERSION.toInt()); d.write(salt); d.write(iv); d.write(encrypted)
        }
        return out.toByteArray()
    }

    fun import(bytes: ByteArray, password: CharArray): List<AegisAccount> {
        if (bytes.size < MAGIC.size + 1 + SALT_LEN + IV_LEN + 16) throw VaultException("الملف ليس خزنة Onyx صالحة")
        val d = DataInputStream(bytes.inputStream())
        val magic = ByteArray(2).also { d.readFully(it) }
        if (!magic.contentEquals(MAGIC)) throw VaultException("الملف ليس خزنة Onyx (.ox)")
        val version = d.readByte()
        if (version != VERSION) throw VaultException("إصدار خزنة غير مدعوم ($version)")
        val salt = ByteArray(SALT_LEN).also { d.readFully(it) }
        val iv = ByteArray(IV_LEN).also { d.readFully(it) }
        val encrypted = d.readBytes()

        val plain = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(128, iv))
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            throw VaultException("كلمة المرور غير صحيحة أو الملف تالف")
        }

        val json = JSONObject(String(plain, Charsets.UTF_8))
        val arr = json.getJSONArray("accounts")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            AegisAccount(
                id = o.optString("id", "acc_${System.currentTimeMillis()}_$i"),
                issuer = o.optString("issuer"),
                accountName = o.optString("accountName"),
                secret = o.getString("secret"),
                algorithm = o.optString("algorithm", "SHA-1"),
                digits = o.optInt("digits", 6),
                period = o.optInt("period", 30),
                addedDate = o.optString("addedDate", "")
            )
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(key, "AES")
    }
}
