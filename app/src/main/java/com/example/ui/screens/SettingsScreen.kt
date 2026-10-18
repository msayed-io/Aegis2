package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.ui.components.FloatingBottomNavCapsule
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.IosSwitch
import com.example.ui.components.CardShape
import com.example.ui.theme.*
import com.example.BuildConfig

@Composable
fun SettingsScreen(
    biometricEnabled: Boolean,
    autoLockOption: String,
    hideCodesWhenLocked: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onAutoLockChange: (String) -> Unit,
    onToggleHideCodes: (Boolean) -> Unit,
    onNavigateToBackupSync: () -> Unit,
    onBack: () -> Unit,
    onOpenWelcome: () -> Unit,
    onOpenTotpMath: () -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
            ) {
                // Profile Banner Card - Android Professional
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(TertiarySystemBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "م",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Label
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "محمد",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Label
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = SystemGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Onyx لأندرويد · الحماية البيومترية نشطة",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = SecondaryLabel
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = TertiaryLabel,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: الأمان - Android Biometric
                SectionHeader("الأمان والخصوصية")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SystemBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Fingerprint, null, tint = SystemBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "القفل البيومتري", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                                Text(text = "بصمة الإصبع والوجه لأندرويد", fontFamily = InterFontFamily, fontSize = 11.sp, color = SecondaryLabel)
                            }
                        }
                        IosSwitch(
                            checked = biometricEnabled,
                            onCheckedChange = onToggleBiometric,
                            testTag = "switch_biometric"
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                onAutoLockChange(if (autoLockOption == "فوراً") "بعد دقيقة واحدة" else "فوراً")
                            }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Timer, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "القفل التلقائي", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = autoLockOption, fontFamily = InterFontFamily, fontSize = 15.sp, color = SecondaryLabel)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.VisibilityOff, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "إخفاء الرموز في القائمة", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        IosSwitch(
                            checked = hideCodesWhenLocked,
                            onCheckedChange = onToggleHideCodes,
                            testTag = "switch_hide_codes"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: المزامنة والنسخ الاحتياطي - Android Native
                SectionHeader("المزامنة والنسخ الاحتياطي")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("settings_backup_row")
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onNavigateToBackupSync() }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Security, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "خزنة Onyx المشفرة", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Label)
                                Text(text = "ملفات .ox محلية بتشفير AES-256", fontFamily = InterFontFamily, fontSize = 11.sp, color = SecondaryLabel)
                            }
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(18.dp))
                    }

                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الحسابات مشفّرة على الجهاز عبر Android Keystore، وملفات .ox مشفّرة بكلمة مرورك قبل الحفظ.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TertiaryLabel,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader("التجربة والدليل")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("settings_welcome_row")
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onOpenWelcome() }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "شاشة الترحيب (الثقة، بالأرقام)", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(16.dp))
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .testTag("settings_totp_math_row")
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onOpenTotpMath() }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "شرح TOTP (الرياضيات، ببساطة)", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader("حول التطبيق")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, CardShape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "الإصدار", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = SecondaryLabel)
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .testTag("settings_licenses_row")
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { onOpenLicenses() }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Description, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "التراخيص مفتوحة المصدر", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بلا حسابات · بلا خوادم · كل شيء على جهازك",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryLabel
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Onyx Authenticator © 2025",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = TertiaryLabel
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            TopVignetteOverlay()

            // Title lives in its own capsule on the leading (right) edge; navigation is via the bottom bar.
            FloatingHeaderCapsule(
                title = "الإعدادات",
                modifier = Modifier.align(Alignment.TopCenter)
            )

            FloatingBottomNavCapsule(
                currentTab = 2,
                onAuthClick = onBack,
                onScanClick = {},
                onSettingsClick = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = InterFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = SecondaryLabel,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
}
