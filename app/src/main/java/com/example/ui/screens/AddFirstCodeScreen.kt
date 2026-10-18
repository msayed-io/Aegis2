package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.TotpGenerator
import com.example.ui.components.AegisEmblem
import com.example.ui.components.CountdownRing
import com.example.ui.components.FloatingCapsuleHeader
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.TertiarySystemBackground

@Composable
fun AddFirstCodeScreen(
    currentTimeSeconds: Long,
    onAddFirst: () -> Unit,
    onDoLater: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingSeconds = TotpGenerator.getRemainingSeconds(30, currentTimeSeconds)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 96.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Central Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "أضف رمزك الأول",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Label,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Demo Account TOTP Card Preview (Elevated 1: SecondarySystemBackground)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondarySystemBackground, RoundedCornerShape(percent = 50))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Right in RTL: Avatar + Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Label
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        text = "Google",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color = Label
                                    )
                                    Text(
                                        text = "يتجدد خلال $remainingSeconds ثانية",
                                        fontFamily = InterFontFamily,
                                        fontSize = 13.sp,
                                        color = SecondaryLabel
                                    )
                                }
                            }
                        }

                        // Left in RTL: Code, Countdown Ring, Copy
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "482 · 913",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Label
                                )
                            }

                            CountdownRing(
                                remainingSeconds = remainingSeconds,
                                period = 30,
                                size = 32.dp,
                                strokeWidth = 2.5.dp
                            )

                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                tint = SecondaryLabel,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onAddFirst,
                        modifier = Modifier
                            .testTag("add_first_code_primary_button")
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SystemBlue,
                            contentColor = Label
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "أضف حسابك الأول",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Label
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "سأفعل ذلك لاحقًا",
                        fontFamily = InterFontFamily,
                        fontSize = 15.sp,
                        color = SystemBlue,
                        modifier = Modifier
                            .testTag("add_first_code_later_button")
                            .clickable { onDoLater() }
                            .padding(6.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(136.dp)
                            .height(5.dp)
                            .background(Color.White.copy(alpha = 0.35f), CircleShape)
                    )
                }
            }

            // Top Vignette Overlay
            TopVignetteOverlay()

            // NEW CAPSULE SYSTEM - Two floating capsules like MainScreen
            FloatingCapsuleHeader(
                startContent = {
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AegisEmblem(size = 20.dp, tint = SystemBlue, strokeWidth = 3f)
                        Text(
                            text = "Onyx",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Label
                        )
                    }
                },
                endContent = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("add_first_code_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "الإعدادات",
                            tint = SecondaryLabel,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
