package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun BottomMenu(
    modifier: Modifier = Modifier,
    onClickRadar: () -> Unit,
    onClickFilter: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(25.dp),
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = {
                        onClickRadar()
                    }
                )
        ) {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.radar), contentDescription = null)
            Text(
                text = "Радар",
                fontSize = 12.sp,
                style = TextStyle(
                    lineHeightStyle = LineHeightStyle(
                        trim = LineHeightStyle.Trim.Both,
                        alignment = LineHeightStyle.Alignment.Center
                    )
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = {
                        onClickFilter()
                    }
                )
        ) {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.filter), contentDescription = null)
            Text(
                text = "Фильтр",
                fontSize = 12.sp,
                style = TextStyle(
                    lineHeightStyle = LineHeightStyle(
                        trim = LineHeightStyle.Trim.Both,
                        alignment = LineHeightStyle.Alignment.Center
                    )
                )
            )
        }
    }
}