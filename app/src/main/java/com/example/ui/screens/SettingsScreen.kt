package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
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
import com.example.ui.components.AegisEmblem
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.components.TopVignetteOverlay
import com.example.ui.components.IosSwitch
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    faceIdEnabled: Boolean,
    autoLockOption: String,
    hideCodesWhenLocked: Boolean,
    iCloudSyncEnabled: Boolean,
    onToggleFaceId: (Boolean) -> Unit,
    onAutoLockChange: (String) -> Unit,
    onToggleHideCodes: (Boolean) -> Unit,
    onToggleICloudSync: (Boolean) -> Unit,
    onNavigateToBackupSync: () -> Unit,
    onBack: () -> Unit,
    onOpenWelcome: () -> Unit,
    onOpenTotpMath: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SystemBackground)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 76.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                // Profile Banner Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
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
                                imageVector = Icons.Outlined.CloudDone,
                                contentDescription = null,
                                tint = SystemBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Aegis لـ iPhone · متصل بـ iCloud",
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

                // Section: الأمان
                SectionHeader("الأمان")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Face, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "القفل باستخدام Face ID", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        IosSwitch(
                            checked = faceIdEnabled,
                            onCheckedChange = onToggleFaceId,
                            testTag = "switch_face_id"
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.VisibilityOff, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "إخفاء الرموز عند القفل", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        IosSwitch(
                            checked = hideCodesWhenLocked,
                            onCheckedChange = onToggleHideCodes,
                            testTag = "switch_hide_codes"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: المزامنة والنسخ الاحتياطي
                SectionHeader("المزامنة والنسخ الاحتياطي")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CloudSync, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "مزامنة iCloud", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(SystemGreen.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "مفعلة", fontFamily = InterFontFamily, fontSize = 12.sp, color = SystemGreen, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IosSwitch(
                                checked = iCloudSyncEnabled,
                                onCheckedChange = onToggleICloudSync,
                                testTag = "switch_icloud_sync"
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .testTag("settings_export_vault_row")
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onNavigateToBackupSync() }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LockReset, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "تصدير مشفر", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Aegis Vault (.aegis)", fontFamily = InterFontFamily, fontSize = 14.sp, color = SecondaryLabel)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يتم تشفير جميع النسخ الاحتياطية باستخدام خوارزمية AES-256 قبل مغادرة هذا الجهاز.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = TertiaryLabel,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section: أيقونة التطبيق
                SectionHeader("المظهر")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Shield, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "أيقونة التطبيق", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(TertiarySystemBackground, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AegisEmblem(size = 15.dp, tint = SystemBlue, strokeWidth = 1.6f)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = TertiaryLabel, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: صفحات التجربة
                SectionHeader("التجربة والدليل")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .testTag("settings_welcome_row")
                            .fillMaxWidth()
                            .height(48.dp)
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
                            .height(48.dp)
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

                // Section: حول التطبيق
                SectionHeader("حول التطبيق")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, null, tint = Label, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "الإصدار", fontFamily = InterFontFamily, fontSize = 16.sp, color = Label)
                        }
                        Text(text = "2.4.1 (180)", fontFamily = JetBrainsMonoFontFamily, fontSize = 14.sp, color = SecondaryLabel)
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Separator, modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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

                // Footer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        Text(text = "سياسة الخصوصية", fontFamily = InterFontFamily, fontSize = 12.sp, color = SystemBlue)
                        Text(text = " · ", fontFamily = InterFontFamily, fontSize = 12.sp, color = TertiaryLabel)
                        Text(text = "شروط الخدمة", fontFamily = InterFontFamily, fontSize = 12.sp, color = SystemBlue)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "جميع البيانات مشفرة محلياً بواسطة Secure Enclave",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = SecondaryLabel
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Aegis Authenticator © 2025",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = TertiaryLabel
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Top Vignette Overlay
            TopVignetteOverlay()

            // Floating Top Header Capsule
            FloatingHeaderCapsule(
                title = "الإعدادات",
                leadingIcon = {
                    Row(
                        modifier = Modifier
                            .testTag("settings_back_button")
                            .clickable { onBack() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = "رجوع",
                            tint = SystemBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                trailingIcon = {
                    Text(
                        text = "تم",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SystemBlue,
                        modifier = Modifier
                            .testTag("settings_done_button")
                            .clickable { onBack() }
                    )
                },
                modifier = Modifier.align(Alignment.TopCenter)
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
