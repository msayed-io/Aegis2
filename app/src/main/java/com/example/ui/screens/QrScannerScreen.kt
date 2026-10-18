package com.example.ui.screens

import android.Manifest
import com.example.ui.theme.SystemRed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.crypto.OtpAuthParser
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.QrScannerEngine
import com.example.ui.components.CapsuleCloseButton
import com.example.ui.components.FloatingHeaderCapsule
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SecondarySystemBackground
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBlue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onClose: () -> Unit,
    onManualEntry: () -> Unit,
    /** Receives a fully parsed otpauth account; returns null on success or an error message. */
    onScannedResult: (parsed: OtpAuthParser.Parsed) -> String?,
    modifier: Modifier = Modifier
) {
    var scanError by remember { mutableStateOf<String?>(null) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffsetRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (cameraPermissionState.status.isGranted) {
            // FIXED: Shared PreviewView instance - was creating two different views causing black screen
            val previewView = remember {
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            }

            val engine = remember(lifecycleOwner, previewView) {
                var engineRef: QrScannerEngine? = null
                QrScannerEngine(lifecycleOwner, previewView) { rawBarcode ->
                    // Full otpauth:// validation. Invalid codes are rejected with a reason and scanning resumes.
                    val error = try {
                        onScannedResult(OtpAuthParser.parse(rawBarcode))
                    } catch (e: OtpAuthParser.ParseException) {
                        e.message
                    } catch (e: Exception) {
                        "تعذّر قراءة الرمز"
                    }
                    if (error != null) {
                        scanError = error
                        engineRef?.rescan()
                    }
                }.also { engineRef = it }
            }

            // Robust lifecycle: start when composed, shutdown when disposed
            LaunchedEffect(previewView) {
                engine.startCamera()
            }

            DisposableEffect(lifecycleOwner) {
                onDispose { engine.shutdown() }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // FIXED: Use the SAME PreviewView that engine is bound to
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                scanError?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(3500)
                        if (scanError == msg) scanError = null
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 150.dp)
                            .background(SystemRed.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Unified header: title capsule (right) + circular close capsule (left)
                FloatingHeaderCapsule(
                    title = "مسح رمز QR",
                    trailingIcon = {
                        CapsuleCloseButton(onClick = onClose, testTag = "scanner_close_button", contentDescription = "إغلاق")
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Viewfinder Reticle Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 3.5.dp.toPx()
                            val cornerLen = 32.dp.toPx()
                            val radius = 20.dp.toPx()
                            val w = size.width
                            val h = size.height

                            // Top Right
                            drawLine(SystemBlue, Offset(w - cornerLen, 0f), Offset(w - radius, 0f), stroke)
                            drawLine(SystemBlue, Offset(w, radius), Offset(w, cornerLen), stroke)
                            drawArc(SystemBlue, useCenter = false, startAngle = 270f, sweepAngle = 90f, topLeft = Offset(w - radius*2, 0f), size = androidx.compose.ui.geometry.Size(radius*2, radius*2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))

                            // Top Left
                            drawLine(SystemBlue, Offset(radius, 0f), Offset(cornerLen, 0f), stroke)
                            drawLine(SystemBlue, Offset(0f, radius), Offset(0f, cornerLen), stroke)
                            drawArc(SystemBlue, useCenter = false, startAngle = 180f, sweepAngle = 90f, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(radius*2, radius*2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))

                            // Bottom Right
                            drawLine(SystemBlue, Offset(w - cornerLen, h), Offset(w - radius, h), stroke)
                            drawLine(SystemBlue, Offset(w, h - radius), Offset(w, h - cornerLen), stroke)
                            drawArc(SystemBlue, useCenter = false, startAngle = 0f, sweepAngle = 90f, topLeft = Offset(w - radius*2, h - radius*2), size = androidx.compose.ui.geometry.Size(radius*2, radius*2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))

                            // Bottom Left
                            drawLine(SystemBlue, Offset(radius, h), Offset(cornerLen, h), stroke)
                            drawLine(SystemBlue, Offset(0f, h - radius), Offset(0f, h - cornerLen), stroke)
                            drawArc(SystemBlue, useCenter = false, startAngle = 90f, sweepAngle = 90f, topLeft = Offset(0f, h - radius*2), size = androidx.compose.ui.geometry.Size(radius*2, radius*2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))
                        }

                        val laserY = 16.dp + (228.dp * laserOffsetRatio)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .offset(y = laserY - 130.dp)
                                .height(2.5.dp)
                                .shadow(8.dp, CircleShape, spotColor = SystemBlue)
                                .background(SystemBlue, CircleShape)
                        )
                    }

                }

                // Bottom Action Area
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(percent = 50))
                            .background(SecondarySystemBackground, RoundedCornerShape(percent = 50))
                            .clickable { onManualEntry() }
                            .testTag("scanner_manual_entry_link")
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Keyboard,
                                contentDescription = null,
                                tint = SystemBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الإدخال اليدوي بدلًا من ذلك",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Label
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(SystemBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "الكاميرا مطلوبة لمسح رموز QR",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Label
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يرجى السماح بالوصول للكاميرا لمسح الرموز بأمان على جهازك",
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        color = SecondaryLabel
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = SystemBlue)
                    ) {
                        Text(text = "منح الإذن", fontFamily = InterFontFamily, color = Label)
                    }
                }
            }
        }
    }
}
