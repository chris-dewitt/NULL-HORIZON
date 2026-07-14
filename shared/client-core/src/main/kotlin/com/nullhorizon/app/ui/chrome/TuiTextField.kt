package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/**
 * Box-drawing text input for terminal surfaces. Long-form editable text stays
 * plain: no glow, no typewriter, no animated effects.
 */
@Composable
fun TuiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    supportingText: String? = null,
    contentDescription: String = label,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val fontFamily = NhTheme.fontFamily
    val borderColor = when {
        isError -> NhColors.PhosphorRed
        enabled -> NhColors.PhosphorGreen
        else -> NhColors.PhosphorDim
    }
    val textColor = if (enabled) NhColors.PhosphorWhite else NhColors.PhosphorDim

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.trim().uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = borderColor,
            fontFamily = fontFamily,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            cursorBrush = SolidColor(NhColors.PhosphorGreen),
            textStyle = textStyle.copy(
                color = textColor,
                fontFamily = fontFamily,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .drawTuiBorder(color = borderColor)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .semantics { this.contentDescription = contentDescription },
            decorationBox = { inner ->
                if (value.isEmpty() && enabled && singleLine) {
                    BlockCursor(color = NhColors.PhosphorGreen)
                }
                inner()
            },
        )
        supportingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) NhColors.PhosphorRed else NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
        }
    }
}
