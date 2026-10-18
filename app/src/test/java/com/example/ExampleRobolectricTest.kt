package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.OtpAuthParser
import com.example.crypto.TotpGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OtpAuthParserTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("Onyx", context.getString(R.string.app_name))
  }

  @Test
  fun `parses full otpauth uri with all parameters`() {
    val p = OtpAuthParser.parse(
      "otpauth://totp/ACME%20Co:john@example.com?secret=JBSWY3DPEHPK3PXP&issuer=ACME%20Co&algorithm=SHA256&digits=8&period=60"
    )
    assertEquals("ACME Co", p.issuer)
    assertEquals("john@example.com", p.accountName)
    assertEquals("JBSWY3DPEHPK3PXP", p.secret)
    assertEquals("SHA-256", p.algorithm)
    assertEquals(8, p.digits)
    assertEquals(60, p.period)
  }

  @Test
  fun `defaults apply when optional parameters are missing`() {
    val p = OtpAuthParser.parse("otpauth://totp/Example:alice@google.com?secret=JBSWY3DPEHPK3PXP")
    assertEquals("Example", p.issuer)
    assertEquals("SHA-1", p.algorithm)
    assertEquals(6, p.digits)
    assertEquals(30, p.period)
  }

  @Test
  fun `rejects hotp migration and malformed codes`() {
    assertThrows(OtpAuthParser.ParseException::class.java) { OtpAuthParser.parse("otpauth://hotp/x?secret=JBSWY3DPEHPK3PXP&counter=1") }
    assertThrows(OtpAuthParser.ParseException::class.java) { OtpAuthParser.parse("otpauth-migration://offline?data=abc") }
    assertThrows(OtpAuthParser.ParseException::class.java) { OtpAuthParser.parse("https://example.com") }
    assertThrows(OtpAuthParser.ParseException::class.java) { OtpAuthParser.parse("otpauth://totp/x?secret=NOT!B32") }
    assertThrows(OtpAuthParser.ParseException::class.java) { OtpAuthParser.parse("otpauth://totp/x?secret=JBSWY3DPEHPK3PXP&digits=7") }
    assertThrows(OtpAuthParser.ParseException::class.java) { OtpAuthParser.parse("otpauth://totp/x?secret=JBSWY3DPEHPK3PXP&algorithm=MD5") }
  }

  @Test
  fun `formatCodeWithDot formats 6 digits with center dot`() {
    assertEquals("482 · 913", TotpGenerator.formatCodeWithDot("482913"))
  }
}
