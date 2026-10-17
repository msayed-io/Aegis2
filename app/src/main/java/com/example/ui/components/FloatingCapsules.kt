package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FloatingHeaderCapsule(
    title: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .height(44.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(9999.dp), ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f))
                .background(LiquidGlassNav, RoundedCornerShape(9999.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    leadingIcon?.invoke()
                }

                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Label
                )

                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    trailingIcon?.invoke()
                }
            }
        }
    }
}

@Composable
fun FloatingBottomNavCapsule(
    currentTab: Int, // 0: Auth, 1: Scan, 2: Settings
    onScanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAuthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 384.dp)
                .fillMaxWidth()
                .height(48.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(9999.dp), ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
                .background(LiquidGlassNav, RoundedCornerShape(9999.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 0: المصادقة
                FloatingNavItem(
                    icon = Icons.Filled.Shield,
                    label = "المصادقة",
                    isActive = currentTab == 0,
                    onClick = onAuthClick,
                    testTag = "tab_auth"
                )

                // Tab 1: مسح رمز
                FloatingNavItem(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = "مسح رمز",
                    isActive = currentTab == 1,
                    onClick = onScanClick,
                    testTag = "tab_qr_scanner"
                )

                // Tab 2: الإعدادات
                FloatingNavItem(
                    icon = Icons.Outlined.Settings,
                    label = "الإعدادات",
                    isActive = currentTab == 2,
                    onClick = onSettingsClick,
                    testTag = "tab_settings"
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val tint = if (isActive) SystemBlue else SecondaryLabel
    Row(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(9999.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        if (isActive) {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = tint
            )
        }
    }
}

@Composable
fun TopVignetteOverlay() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SystemBackground.copy(alpha = 0.95f),
                        SystemBackground.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )
            )
    )
}
