package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.geometry.Circle

//TODO: Подгрузка инфы из базы

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun RateContent(
    mainViewModel: MainViewModel
) {
    val rate by mainViewModel.lastSelectedRate.collectAsState()
    val rentHours by mainViewModel.rentHours.collectAsState()

    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = rate!!.rateName)
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.close),
                contentDescription = null
            )
        }
        if (String.format("%.2f", rate!!.parkingPrice) == String.format("%.2f", rate!!.onRoadPrice)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Стоимость в час"
                )
                Text(
                    text = "${String.format("%.2f", rate!!.parkingPrice * 60)} P/час"
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Итого"
                )
                Text(
                    text = "${String.format("%.2f", rate!!.parkingPrice * rentHours * 60)} P"
                )
            }
            Column {
                Slider(
                    value = rentHours.toFloat(),
                    steps = 2,
                    valueRange = 2f..8f,
                    onValueChange = {
                        mainViewModel.updateRentHours(it.toInt())
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
                        text = "2 часа",
                        fontSize = 12.sp,
                        color = if (rentHours.toFloat() == 2f) Color.Black else Color(0xFFC2C2C2)
                    )
                    Text(
                        text = "4 часа",
                        fontSize = 12.sp,
                        color = if (rentHours.toFloat() == 4f) Color.Black else Color(0xFFC2C2C2)
                    )
                    Text(
                        text = "6 часов",
                        fontSize = 12.sp,
                        color = if (rentHours.toFloat() == 6f) Color.Black else Color(0xFFC2C2C2)
                    )
                    Text(
                        text = "8 часов",
                        fontSize = 12.sp,
                        color = if (rentHours.toFloat() == 8f) Color.Black else Color(0xFFC2C2C2)
                    )
                }
            }
        } else {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.wheel),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF6699CC))
                    )
                    Text(
                        text = "Стоимость в пути"
                    )
                }
                Text(
                    text = "${String.format("%.2f", rate!!.onRoadPrice)} P/мин"
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.parking),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF6699CC))
                    )
                    Text(
                        text = "Парковка"
                    )
                }
                Text(
                    text = "${String.format("%.2f", rate!!.parkingPrice)} P/мин"
                )
            }
        }
        Button(
            onClick = {
                mainViewModel.updatePage("reservationPage")
                mainViewModel.updateSession(
                    mainViewModel.pedestrianRouter.value!!.requestRoutes(
                        mainViewModel.points.value,
                        mainViewModel.options,
                        mainViewModel.routeListener
                    )
                )
                if (String.format("%.2f", rate!!.parkingPrice) == String.format("%.2f", rate!!.onRoadPrice)) {
                    mainViewModel.updateIsFixed(true)
                } else {
                    mainViewModel.updateIsFixed(false)
                }
                mainViewModel.updateReserving(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Забронировать")
        }
    }
}