package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OnyxAccount
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemOrange
import com.example.ui.theme.SystemRed
import com.example.ui.theme.TertiarySystemBackground

/**
 * Lean account row: badge · issuer · code · countdown. No buttons, no e-mail.
 *  - single tap  -> copy (haptic + caller shows the toast)
 *  - double tap  -> open details
 */
@Composable
fun AccountCodeCard(
    account: OnyxAccount,
    formattedCode: String,
    remainingSeconds: Int,
    isCopied: Boolean,
    onCopy: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val pressScale by animateFloatAsState(
        targetValue = if (isCopied) 0.985f else 1f,
        animationSpec = tween(120),
        label = "card_press"
    )
    val codeColor = when {
        remainingSeconds <= 1 -> SystemRed
        remainingSeconds <= 5 -> SystemOrange
        else -> Label
    }

    Row(
        modifier = modifier
            .testTag("account_item_${account.id}")
            .fillMaxWidth()
            .scale(pressScale)
            .pointerInput(account.id) {
                detectTapGestures(
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCopy()
                    },
                    onDoubleTap = { onOpenDetails() }
                )
            }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TertiarySystemBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.badgeLetter,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Label
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = account.issuer,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Label,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = formattedCode,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp,
                    letterSpacing = 1.sp,
                    color = codeColor,
                    maxLines = 1
                )
            }

            CountdownRing(
                remainingSeconds = remainingSeconds,
                period = account.period,
                size = 30.dp,
                strokeWidth = 2.6.dp
            )
        }
    }
}

/**
 * Apple-style minimal confirmation: a small dark pill with plain text (no icon, no emoji),
 * sliding up from the bottom. Visibility is driven by the caller (e.g. `copiedCodeId != null`).
 */
@Composable
fun CopiedToast(
    visible: Boolean,
    text: String = "تم النسخ",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)) + slideInVertically(tween(200)) { it / 2 },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(220)) { it / 2 },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 92.dp)
                .shadow(14.dp, CircleShape, ambientColor = Color.Black.copy(0.4f))
                .background(SecondarySystemBackground, CircleShape)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Label
            )
        }
    }
}
