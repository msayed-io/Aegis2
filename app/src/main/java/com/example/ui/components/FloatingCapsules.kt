package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue

private val CapsuleShape = RoundedCornerShape(percent = 50)

/**
 * A floating header with independent action capsules at the logical start/end edges.
 * The title stays centered and is deliberately not wrapped in a continuous toolbar surface.
 */
@Composable
fun FloatingHeaderCapsule(
    title: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FloatingHeaderFrame(modifier = modifier) {
        if (leadingIcon != null) {
            FloatingCapsuleSurface(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
            }
        }

        if (trailingIcon != null) {
            FloatingCapsuleSurface(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    trailingIcon()
                }
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 64.dp),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = Label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A top row made of two separately elevated capsules, with no shared background. */
@Composable
fun FloatingCapsuleHeader(
    startContent: (@Composable BoxScope.() -> Unit)? = null,
    endContent: (@Composable BoxScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FloatingHeaderFrame(modifier = modifier) {
        if (startContent != null) {
            FloatingCapsuleSurface(
                modifier = Modifier.align(Alignment.CenterStart),
                content = startContent
            )
        }

        if (endContent != null) {
            FloatingCapsuleSurface(
                modifier = Modifier.align(Alignment.CenterEnd),
                content = endContent
            )
        }
    }
}

@Composable
private fun FloatingHeaderFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp),
        content = content
    )
}

/** Solid dark capsule with a fine specular edge and a restrained two-layer ambient shadow. */
@Composable
private fun FloatingCapsuleSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .shadow(
                elevation = 18.dp,
                shape = CapsuleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.40f),
                spotColor = Color.Black.copy(alpha = 0.46f)
            )
            .shadow(
                elevation = 2.dp,
                shape = CapsuleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.56f),
                spotColor = Color.Black.copy(alpha = 0.62f)
            )
            .clip(CapsuleShape)
            .background(SecondarySystemBackground, CapsuleShape)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = CapsuleShape
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
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
        FloatingCapsuleSurface(
            modifier = Modifier
                .widthIn(max = 384.dp)
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingNavItem(
                    icon = Icons.Filled.Shield,
                    label = "المصادقة",
                    isActive = currentTab == 0,
                    onClick = onAuthClick,
                    testTag = "tab_auth",
                    modifier = Modifier.weight(1f)
                )

                FloatingNavItem(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = "مسح رمز",
                    isActive = currentTab == 1,
                    onClick = onScanClick,
                    testTag = "tab_qr_scanner",
                    modifier = Modifier.weight(1f)
                )

                FloatingNavItem(
                    icon = Icons.Outlined.Settings,
                    label = "الإعدادات",
                    isActive = currentTab == 2,
                    onClick = onSettingsClick,
                    testTag = "tab_settings",
                    modifier = Modifier.weight(1f)
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
    testTag: String,
    modifier: Modifier = Modifier
) {
    val tint = if (isActive) SystemBlue else SecondaryLabel

    Row(
        modifier = modifier
            .height(48.dp)
            .testTag(testTag)
            .clip(CircleShape)
            .background(if (isActive) SystemBlue.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        if (isActive) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = tint,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TopVignetteOverlay() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.verticalGradient(
                    0.0f to SystemBackground.copy(alpha = 0.96f),
                    0.38f to SystemBackground.copy(alpha = 0.62f),
                    0.70f to SystemBackground.copy(alpha = 0.20f),
                    1.0f to Color.Transparent
                )
            )
    )
}
