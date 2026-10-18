package com.example.crypto

import android.util.Base64
import com.example.model.OnyxAccount

/**
 * Decoder for Google Authenticator "Export accounts" QR codes:
 *     otpauth-migration://offline?data=<url-safe base64 of protobuf MigrationPayload>
 *
 * The protobuf wire format is parsed by hand (no protobuf dependency). Schema:
 *   MigrationPayload { repeated OtpParameters otp_parameters = 1; int32 version = 2;
 *                      int32 batch_size = 3; int32 batch_index = 4; int32 batch_id = 5; }
 *   OtpParameters     { bytes secret = 1; string name = 2; string issuer = 3;
 *                      Algorithm algorithm = 4; DigitCount digits = 5; OtpType type = 6; int64 counter = 7; }
 *   Algorithm: 0 UNSPECIFIED, 1 SHA1, 2 SHA256, 3 SHA512, 4 MD5
 *   DigitCount: 0 UNSPECIFIED, 1 SIX, 2 EIGHT
 *   OtpType: 0 UNSPECIFIED, 1 HOTP, 2 TOTP
 */
object MigrationPayloadDecoder {

    private const val PREFIX = "otpauth-migration://offline?data="

    class DecodeException(message: String) : Exception(message)

    data class Result(
        val accounts: List<OnyxAccount>,
        /** HOTP / unsupported entries that were skipped. */
        val skipped: Int,
        val batchIndex: Int,
        val batchSize: Int
    )

    fun isMigrationUri(raw: String): Boolean = raw.trim().startsWith("otpauth-migration://", ignoreCase = true)

    fun decode(raw: String): Result {
        val text = raw.trim()
        if (!text.startsWith(PREFIX, ignoreCase = true)) {
            throw DecodeException("هذا ليس رمز نقل حسابات (otpauth-migration)")
        }
        val encoded = java.net.URLDecoder.decode(text.substring(PREFIX.length), "UTF-8")
        val bytes = try {
            Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } catch (e: IllegalArgumentException) {
            try { Base64.decode(encoded, Base64.DEFAULT) } catch (e2: IllegalArgumentException) {
                throw DecodeException("بيانات الرمز تالفة (Base64 غير صالح)")
            }
        }
        if (bytes.isEmpty()) throw DecodeException("رمز النقل فارغ")

        val accounts = ArrayList<OnyxAccount>()
        var skipped = 0
        var batchIndex = 0
        var batchSize = 1
        val now = System.currentTimeMillis()

        ProtoReader(bytes).forEachField { field, wire, reader ->
            when (field) {
                1 -> if (wire == ProtoReader.WIRE_LEN) {
                    val params = parseOtpParameters(reader.readBytes())
                    if (params == null) skipped++ else accounts += params.toAccount("mig_${now}_${accounts.size}")
                } else reader.skip(wire)
                3 -> if (wire == ProtoReader.WIRE_VARINT) batchSize = reader.readVarint().toInt() else reader.skip(wire)
                4 -> if (wire == ProtoReader.WIRE_VARINT) batchIndex = reader.readVarint().toInt() else reader.skip(wire)
                else -> reader.skip(wire)
            }
        }
        if (accounts.isEmpty() && skipped == 0) throw DecodeException("لم يُعثر على أي حساب داخل الرمز")
        return Result(accounts, skipped, batchIndex, batchSize)
    }

    private class OtpParams(
        var secret: ByteArray = ByteArray(0),
        var name: String = "",
        var issuer: String = "",
        var algorithm: Int = 0,
        var digits: Int = 0,
        var type: Int = 0
    ) {
        fun toAccount(id: String): OnyxAccount {
            val labelIssuer = if (name.contains(":")) name.substringBefore(":").trim() else ""
            val labelName = if (name.contains(":")) name.substringAfter(":").trim() else name.trim()
            val finalIssuer = issuer.ifBlank { labelIssuer }.ifBlank { labelName }
            return OnyxAccount(
                id = id,
                issuer = finalIssuer,
                accountName = labelName.ifBlank { finalIssuer },
                secret = encodeBase32(secret),
                algorithm = when (algorithm) { 2 -> "SHA-256"; 3 -> "SHA-512"; else -> "SHA-1" },
                digits = if (digits == 2) 8 else 6,
                period = 30,
                addedDate = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("ar")).format(java.util.Date())
            )
        }
    }

    /** Returns null for entries Onyx cannot represent (HOTP, MD5, empty secret). */
    private fun parseOtpParameters(bytes: ByteArray): OtpParams? {
        val p = OtpParams()
        ProtoReader(bytes).forEachField { field, wire, reader ->
            when {
                field == 1 && wire == ProtoReader.WIRE_LEN -> p.secret = reader.readBytes()
                field == 2 && wire == ProtoReader.WIRE_LEN -> p.name = String(reader.readBytes(), Charsets.UTF_8)
                field == 3 && wire == ProtoReader.WIRE_LEN -> p.issuer = String(reader.readBytes(), Charsets.UTF_8)
                field == 4 && wire == ProtoReader.WIRE_VARINT -> p.algorithm = reader.readVarint().toInt()
                field == 5 && wire == ProtoReader.WIRE_VARINT -> p.digits = reader.readVarint().toInt()
                field == 6 && wire == ProtoReader.WIRE_VARINT -> p.type = reader.readVarint().toInt()
                else -> reader.skip(wire)
            }
        }
        val isTotp = p.type == 2 || p.type == 0
        if (!isTotp || p.algorithm == 4 || p.secret.isEmpty()) return null
        return p
    }

    /** RFC 4648 Base32 (no padding) — the format Onyx stores and [TotpGenerator] decodes. */
    fun encodeBase32(data: ByteArray): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val sb = StringBuilder((data.size * 8 + 4) / 5)
        var buffer = 0
        var bits = 0
        for (b in data) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bits += 8
            while (bits >= 5) {
                sb.append(alphabet[(buffer shr (bits - 5)) and 0x1F])
                bits -= 5
            }
        }
        if (bits > 0) sb.append(alphabet[(buffer shl (5 - bits)) and 0x1F])
        return sb.toString()
    }

    /** Minimal protobuf wire-format reader. */
    private class ProtoReader(private val buf: ByteArray) {
        private var pos = 0

        fun forEachField(block: (field: Int, wire: Int, reader: ProtoReader) -> Unit) {
            while (pos < buf.size) {
                val tag = readVarint()
                val field = (tag ushr 3).toInt()
                val wire = (tag and 0x7).toInt()
                if (field == 0) throw DecodeException("بنية الرمز غير صالحة")
                block(field, wire, this)
            }
        }

        fun readVarint(): Long {
            var result = 0L
            var shift = 0
            while (true) {
                if (pos >= buf.size) throw DecodeException("بيانات الرمز مقطوعة")
                val b = buf[pos++].toInt()
                result = result or ((b and 0x7F).toLong() shl shift)
                if (b and 0x80 == 0) return result
                shift += 7
                if (shift > 63) throw DecodeException("بنية الرمز غير صالحة")
            }
        }

        fun readBytes(): ByteArray {
            val len = readVarint().toInt()
            if (len < 0 || pos + len > buf.size) throw DecodeException("بيانات الرمز مقطوعة")
            val out = buf.copyOfRange(pos, pos + len)
            pos += len
            return out
        }

        fun skip(wire: Int) {
            when (wire) {
                WIRE_VARINT -> readVarint()
                WIRE_64 -> pos += 8
                WIRE_LEN -> readBytes()
                WIRE_32 -> pos += 4
                else -> throw DecodeException("نوع حقل غير مدعوم في الرمز")
            }
            if (pos > buf.size) throw DecodeException("بيانات الرمز مقطوعة")
        }

        companion object {
            const val WIRE_VARINT = 0
            const val WIRE_64 = 1
            const val WIRE_LEN = 2
            const val WIRE_32 = 5
        }
    }
}
