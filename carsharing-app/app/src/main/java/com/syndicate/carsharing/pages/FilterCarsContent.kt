package com.syndicate.carsharing.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.ui.theme.filterChipColors
import com.syndicate.carsharing.ui.theme.sliderColors
import com.syndicate.carsharing.ui.theme.sliderThumb
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
        Box(modifier = Modifier
            .fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .width(30.dp)
                    .height(4.dp)
                    .background(
                        Color(0xFFB5B5B5),
                        shape = CircleShape
                    )
                    .align(Alignment.Center)
            )
        }
        Text(
            text = "Фильтр поиска",
            style = MaterialTheme.typography.titleMedium

        )
        Column {
            Slider(
                value = mainState.carType,
                steps = 1,
                valueRange = 1f..3f,
                onValueChange = {
                    mainViewModel.updateCarType(it)
                },
                colors = sliderColors(),
                thumb = {
                    sliderThumb(it)
                }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = "Эконом",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (mainState.carType == 1f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "Комфорт",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (mainState.carType == 2f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "Бизнес",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (mainState.carType == 3f) Color.Black else Color(0xFFC2C2C2)
                )
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "Опции",
            style = MaterialTheme.typography.displayMedium,
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
                    colors = filterChipColors(),
                    interactionSource = MutableInteractionSource(),
                    border = BorderStroke(1.dp, if (mainState.listTags[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E))
                ) {
                    Text(
                        text = if (mainState.listTags[i].tag == "CHILD_CHAIR") "Детское кресло" else "Транспондер",
                        color = if (mainState.listTags[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E),
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AutoShareButton(
                modifier = Modifier
                    .weight(2f),
                text = "Фильтр по моделям"
            ) {
                mainViewModel.updateFiltered(true)
            }
            if (mainState.isFiltered) {
                AutoShareButton(
                    modifier = Modifier
                        .weight(1f),
                    text = "Сбросить"
                ) {
                    mainViewModel.updateFiltered(false)
                }
            }
        }
    }
}