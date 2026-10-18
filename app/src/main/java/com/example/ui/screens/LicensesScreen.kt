package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CapsuleCloseButton
import com.example.ui.components.CardShape
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.Separator
import com.example.ui.theme.SystemBackground

/** Open-source licenses actually shipped in the APK (mirrors app/build.gradle.kts). */
private val LICENSES = listOf(
    "AndroidX (Compose, Activity, Lifecycle, Biometric, Security-Crypto, DocumentFile, CameraX, Core)" to "Apache License 2.0",
    "Kotlin & kotlinx.coroutines — JetBrains" to "Apache License 2.0",
    "Google ML Kit Barcode Scanning" to "Google APIs Terms of Service",
    "Accompanist Permissions — Google" to "Apache License 2.0",
    "Inter typeface — Rasmus Andersson" to "SIL Open Font License 1.1",
    "JetBrains Mono typeface — JetBrains" to "SIL Open Font License 1.1",
    "Material Symbols / Icons — Google" to "Apache License 2.0"
)

@Composable
fun LicensesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = modifier.fillMaxSize().background(SystemBackground)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Onyx مبني على المكونات مفتوحة المصدر التالية",
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = SecondaryLabel,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
                Column(modifier = Modifier.fillMaxWidth().background(SecondarySystemBackground, CardShape)) {
                    LICENSES.forEachIndexed { index, (name, license) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(text = name, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = license, fontFamily = InterFontFamily, fontSize = 13.sp, color = SecondaryLabel)
                        }
                        if (index < LICENSES.size - 1) {
                            HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
            TopVignetteOverlay()
            FloatingHeaderCapsule(
                title = "التراخيص",
                trailingIcon = { CapsuleCloseButton(onClick = onBack, testTag = "licenses_back_button") },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
