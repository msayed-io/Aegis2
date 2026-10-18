package com.example

import com.example.crypto.TotpGenerator
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
    private val seedSha512 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQGEZDGNA"

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
}
