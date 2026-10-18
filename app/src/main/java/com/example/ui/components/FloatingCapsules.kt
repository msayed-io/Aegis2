package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
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

/** Unified card shape: smooth 24dp corners on all four sides (never a pill). */
val CardShape = RoundedCornerShape(24.dp)

/**
 * Header made of a leading (start/right in RTL) capsule that carries the screen TITLE
 * together with an optional back/cancel icon, plus an optional trailing action capsule.
 * The title is never floated in the middle of the screen.
 */
@Composable
fun FloatingHeaderCapsule(
    title: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FloatingHeaderFrame(modifier = modifier) {
        FloatingCapsuleSurface(modifier = Modifier.align(Alignment.CenterStart)) {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .padding(start = if (leadingIcon != null) 12.dp else 18.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) { leadingIcon() }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }
        }

        if (trailingIcon != null) {
            FloatingCapsuleSurface(modifier = Modifier.align(Alignment.CenterEnd)) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) { trailingIcon() }
            }
        }
    }
}

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp),
        content = content
    )
}

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
    currentTab: Int,
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
        // FIXED: Shrunk for 2 items only (was 384dp for 3 items → now 220dp for 2)
        FloatingCapsuleSurface(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
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
            .height(44.dp)
            .testTag(testTag)
            .clip(CircleShape)
            .background(if (isActive) SystemBlue.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
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
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
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
            .height(110.dp)
            .background(
                Brush.verticalGradient(
                    0.0f to SystemBackground.copy(alpha = 0.98f),
                    0.42f to SystemBackground.copy(alpha = 0.65f),
                    0.72f to SystemBackground.copy(alpha = 0.18f),
                    1.0f to Color.Transparent
                )
            )
    )
}
