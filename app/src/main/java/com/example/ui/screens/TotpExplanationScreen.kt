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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.TotpGenerator
import com.example.ui.components.AegisEmblem
import com.example.ui.components.CountdownRing
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.CardShape
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.TertiarySystemBackground

@Composable
fun TotpExplanationScreen(
    currentTimeSeconds: Long,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingSeconds = TotpGenerator.getRemainingSeconds(30, currentTimeSeconds)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Scrollable Content - padded to avoid floating header
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Onyx Emblem
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SecondarySystemBackground, CardShape),
                    contentAlignment = Alignment.Center
                ) {
                    AegisEmblem(size = 32.dp, tint = SystemBlue, strokeWidth = 2.5f)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "الرياضيات، ببساطة.",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Label,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "يعمل بروتوكول TOTP محليًا على جهازك دون اتصال بالسحابة، عبر دمج مفتاحك السري اللحظي مع عداد الثواني الدقيق.",
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = SecondaryLabel,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4 Connected Mathematical Flow Modules
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    // Module 1: Shared Key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Key, null, tint = Label, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "مفتاح مشترك", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                                Text(text = "المفتاح السري المشفر (K)", fontFamily = InterFontFamily, fontSize = 12.sp, color = SecondaryLabel)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(TertiarySystemBackground, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "K 7·R 9", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = Label)
                        }
                    }

                    ConnectorBar("+")

                    // Module 2: Time Parameter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Schedule, null, tint = Label, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "الوقت", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                                Text(text = "نافذة الخطوة الزمنية (t)", fontFamily = InterFontFamily, fontSize = 12.sp, color = SecondaryLabel)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CountdownRing(
                                remainingSeconds = remainingSeconds,
                                period = 30,
                                size = 20.dp,
                                strokeWidth = 2.dp,
                                showNumberInside = false
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(TertiarySystemBackground, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "${remainingSeconds}s", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = Label)
                            }
                        }
                    }

                    ConnectorBar("↓")

                    // Module 3: HMAC-SHA-1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Functions, null, tint = Label, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "HMAC-SHA-1", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                                Text(text = "تجزئة تشفير أحادية الاتجاه", fontFamily = InterFontFamily, fontSize = 12.sp, color = SecondaryLabel)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(TertiarySystemBackground, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "f(K,t)", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = Label)
                        }
                    }

                    ConnectorBar("=")

                    // Module 4: Generated 6-Digit Code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TertiarySystemBackground)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SystemBlue, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Lock, null, tint = Label, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "٦ أرقام", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                                Text(text = "رمز المصادقة الصالح فورًا", fontFamily = InterFontFamily, fontSize = 12.sp, color = SecondaryLabel)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SecondarySystemBackground, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SystemGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "482 · 913",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = SystemBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, null, tint = SystemGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "متوافق تمامًا مع معيار RFC 6238 دون مغادرة جهازك",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryLabel
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Actions - now inside scroll with correct spacing
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .testTag("totp_math_continue_button")
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
                        text = "متابعة",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Label
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تخطي",
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = SecondaryLabel,
                    modifier = Modifier
                        .testTag("totp_math_skip_button")
                        .clickable { onSkip() }
                        .padding(6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top Vignette Overlay
            TopVignetteOverlay()

            // Floating Top Header Capsule - NEW CAPSULE SYSTEM
            FloatingHeaderCapsule(
                title = "كيف يعمل؟",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = "رجوع",
                        tint = SystemBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onBack() }
                    )
                },
                trailingIcon = {
                    Text(
                        text = "Onyx Core 2.4",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = SecondaryLabel,
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun ConnectorBar(symbol: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(SecondarySystemBackground),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(TertiarySystemBackground, CircleShape)
                .padding(horizontal = 8.dp, vertical = 1.dp)
        ) {
            Text(
                text = symbol,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = SecondaryLabel
            )
        }
    }
}
