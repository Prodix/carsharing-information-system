package com.syndicate.carsharing.shared_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.ui.theme.buttonColors
import com.syndicate.carsharing.ui.theme.buttonColorsNegative

@Composable
fun AutoShareButton(
    modifier: Modifier = Modifier,
    text: String,
    isNegative: Boolean = false,
    enabled: Boolean = true,
    border: BorderStroke? = null,
    onClick: () -> Unit
) {
    val colors = if (isNegative) buttonColorsNegative() else buttonColors()

    Button(
        onClick = onClick,
        colors = colors,
        modifier = Modifier
            .then(
                modifier
            )
            .height(60.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = border,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if (enabled) colors.contentColor else colors.disabledContentColor,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}