package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.TotpGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Onyx", appName)
  }

  @Test
  fun `generate TOTP code produces valid 6-digit output`() {
    val code = TotpGenerator.generateTotp(
      secretBase32 = "JBSWY3DPEHPK3PXP",
      timeSeconds = 1710000000L,
      period = 30,
      digits = 6,
      algorithm = "SHA-1"
    )
    assertNotNull(code)
    assertEquals(6, code.length)
  }

  @Test
  fun `formatCodeWithDot formats 6 digits with center dot`() {
    val formatted = TotpGenerator.formatCodeWithDot("482913")
    assertEquals("482 · 913", formatted)
  }
}
