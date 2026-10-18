package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.BiometricAuthenticator
import com.example.security.findFragmentActivity
import com.example.ui.components.AegisEmblem
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.SystemRed
import kotlinx.coroutines.launch

/**
 * Minimal lock screen: emblem, app name, one unlock control.
 * The native Android BiometricPrompt (biometrics or device credential) is shown automatically;
 * the fingerprint button simply re-opens it.
 */
@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val emblemScale = remember { Animatable(1f) }
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val requestUnlock: () -> Unit = {
        val activity = context.findFragmentActivity()
        if (activity != null) {
            errorMessage = null
            BiometricAuthenticator.authenticate(
                activity = activity,
                title = "Onyx",
                subtitle = "",
                onSuccess = {
                    scope.launch {
                        emblemScale.animateTo(1.1f, tween(180))
                        emblemScale.animateTo(1f, tween(140))
                        onUnlocked()
                    }
                },
                onFailure = { msg -> errorMessage = msg }
            )
        }
    }

    LaunchedEffect(Unit) { requestUnlock() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(88.dp).scale(emblemScale.value), contentAlignment = Alignment.Center) {
                AegisEmblem(size = 76.dp, tint = SystemBlue, strokeWidth = 3.4f, showOuterRing = true)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Onyx",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Label,
                letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = SystemRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                )
            }
            IconButton(
                onClick = requestUnlock,
                modifier = Modifier
                    .testTag("biometric_unlock_button")
                    .size(64.dp)
                    .background(SecondarySystemBackground, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "فتح",
                    tint = SystemBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
