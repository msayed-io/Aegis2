package com.example

import com.example.crypto.TotpGenerator
import com.example.model.OnyxAccount
import com.example.vault.OnyxVault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpEngineTest {

    // RFC 6238 Appendix B test vectors (8 digits). Seeds are the ASCII "1234567890..." keys in Base32.
    private val seedSha1 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
    private val seedSha256 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZA"
    private val seedSha512 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGE"

    @Test
    fun `rfc6238 vectors sha1`() {
        assertEquals("94287082", TotpGenerator.generateTotp(seedSha1, 59L, 30, 8, "SHA-1"))
        assertEquals("07081804", TotpGenerator.generateTotp(seedSha1, 1111111109L, 30, 8, "SHA-1"))
        assertEquals("89005924", TotpGenerator.generateTotp(seedSha1, 1234567890L, 30, 8, "SHA-1"))
    }

    @Test
    fun `rfc6238 vectors sha256 and sha512`() {
        assertEquals("46119246", TotpGenerator.generateTotp(seedSha256, 59L, 30, 8, "SHA-256"))
        assertEquals("90693936", TotpGenerator.generateTotp(seedSha512, 59L, 30, 8, "SHA-512"))
    }

    @Test
    fun `six digit code is the last six digits of the eight digit code`() {
        assertEquals("287082", TotpGenerator.generateTotp(seedSha1, 59L, 30, 6, "SHA-1"))
    }

    @Test
    fun `invalid secret throws instead of returning a fake code`() {
        assertThrows(TotpGenerator.TotpException::class.java) { TotpGenerator.generateTotp("", 59L) }
        assertThrows(TotpGenerator.TotpException::class.java) { TotpGenerator.generateTotp("NOT*VALID!", 59L) }
        assertNull(TotpGenerator.generateTotpOrNull("0189", 59L, 30, 6, "SHA-1"))
        assertFalse(TotpGenerator.isValidSecret("1234"))
        assertTrue(TotpGenerator.isValidSecret("jbsw y3dp ehpk 3pxp"))
    }

    @Test
    fun `vault export then import round trips all fields and rejects wrong password`() {
        val accounts = listOf(
            OnyxAccount("a1", "GitHub", "dev@example.com", "JBSWY3DPEHPK3PXP", "SHA-1", 6, 30, "1 يناير 2026"),
            OnyxAccount("a2", "Bank", "user", seedSha256, "SHA-256", 8, 60, "")
        )
        val bytes = OnyxVault.export(accounts, "correct horse".toCharArray())
        assertEquals('O'.code.toByte(), bytes[0]); assertEquals('X'.code.toByte(), bytes[1])
        val restored = OnyxVault.import(bytes, "correct horse".toCharArray())
        assertEquals(accounts, restored)
        assertThrows(OnyxVault.VaultException::class.java) { OnyxVault.import(bytes, "wrong".toCharArray()) }
    }
}
