package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun UserCursorButton(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .then(
                if (sheetState.isVisible) {
                    Modifier.alpha(0f)
                } else {
                    Modifier
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
                        .alpha(1f)
                }
            )
            .padding(10.dp)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null
            ) {
                onClick()
            }
    ) {
        Image(imageVector = ImageVector.vectorResource(id = R.drawable.usercursor), contentDescription = null)
    }
}