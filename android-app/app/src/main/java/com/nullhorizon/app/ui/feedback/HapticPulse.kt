package com.nullhorizon.app.ui.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Fires a single haptic pulse when [key] first appears or changes. Android-only
 * — desktop has no haptics, so this lives in the app module rather than shared.
 */
@Composable
fun HapticPulse(
    key: Any?,
    type: HapticFeedbackType = HapticFeedbackType.LongPress,
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(key) {
        if (key != null) haptics.performHapticFeedback(type)
    }
}
