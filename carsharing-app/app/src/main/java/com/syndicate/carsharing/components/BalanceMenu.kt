package com.syndicate.carsharing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BalanceMenu(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState
) {
    Box(
        modifier = modifier
            .then(
                if (sheetState.targetValue == ModalBottomSheetValue.Expanded) {
                    Modifier.alpha(0f)
                } else {
                    Modifier.alpha(1f)
                }
            )
            .withShadow(
                Shadow(
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    radius = 4.dp,
                    color = Color(0, 0, 0, 40)
                ),
                RoundedCornerShape(10.dp)
            )
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 13.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(end = 5.dp)
        ) {
            Text(
                text = "10000,94",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold)
            Text(
                text = "₽",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF6699CC),
                            radius = this.size.maxDimension / 2f
                        )
                    }
            )
        }
    }
}