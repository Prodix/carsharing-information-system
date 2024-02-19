package com.syndicate.carsharing.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R

@Composable
fun UserCursorButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
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