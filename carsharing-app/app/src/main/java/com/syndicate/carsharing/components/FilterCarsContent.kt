package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.viewmodels.MainViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalLayoutApi::class
)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun FilterCarsContent(
    mainViewModel: MainViewModel
) {
    val mainState by mainViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Фильтр поиска",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Column {
            Slider(
                value = mainState.carType,
                steps = 1,
                valueRange = 1f..3f,
                onValueChange = {
                    mainViewModel.updateCarType(it)
                },
                colors = SliderDefaults.colors(
                    activeTickColor = Color(0xFF6699CC),
                    inactiveTickColor = Color.Transparent,
                    inactiveTrackColor = Color(0x806699CC),
                    activeTrackColor = Color(0xFF6699CC),
                    thumbColor = Color(0xFF34699D)
                ),
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = MutableInteractionSource(),
                        thumbSize = DpSize(30.dp, 30.dp)
                    )
                }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = "Эконом",
                    fontSize = 12.sp,
                    color = if (mainState.carType == 1f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "Комфорт",
                    fontSize = 12.sp,
                    color = if (mainState.carType == 2f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "Бизнес",
                    fontSize = 12.sp,
                    color = if (mainState.carType == 3f) Color.Black else Color(0xFFC2C2C2)
                )
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "Опции",
            fontSize = 12.sp,
            color = Color(0xFFC2C2C2)
        )

        FlowRow (
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            for (i in mainState.listTags.indices) {
                FilterChip(
                    selected = mainState.listTags[i].isSelected,
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        mainViewModel.updateTags(i)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = if (mainState.listTags[i].tag == "CHILD_CHAIR") R.drawable.child_icon else R.drawable.transponder),
                            contentDescription = null,
                            tint = if (mainState.listTags[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E)
                        )
                    },
                    colors = ChipDefaults.filterChipColors(
                        backgroundColor = Color.Transparent,
                        selectedBackgroundColor = Color(0x266699CC)
                    ),
                    interactionSource = MutableInteractionSource(),
                    border = BorderStroke(1.dp, if (mainState.listTags[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E))
                ) {
                    Text(
                        text = if (mainState.listTags[i].tag == "CHILD_CHAIR") "Детское кресло" else "Транспондер",
                        color = if (mainState.listTags[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E)
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    mainViewModel.updateFiltered(true)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color(0xFFB5B5B5)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .weight(2f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Фильтр по моделям")
            }
            if (mainState.isFiltered) {
                Button(
                    onClick = {
                        mainViewModel.updateFiltered(false)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6699CC),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color(0xFFB5B5B5)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Сбросить")
                }
            }
        }
    }
}