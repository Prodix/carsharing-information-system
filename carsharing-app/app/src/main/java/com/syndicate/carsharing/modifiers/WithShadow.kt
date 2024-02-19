package com.syndicate.carsharing.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import com.syndicate.carsharing.utility.Shadow

fun Modifier.withShadow(
    shadow: Shadow,
    shape: Shape,
) = drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            this.color = Color.Transparent.toArgb()
            setShadowLayer(
                shadow.radius.toPx(),
                shadow.offsetX.toPx(),
                shadow.offsetY.toPx(),
                shadow.color.toArgb(),
            )
        }
        val outline = shape.createOutline(size, layoutDirection, this)
        canvas.drawOutline(outline, paint)
    }
}