package com.syndicate.carsharing.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.viewmodels.MainViewModel

//TODO: Подгрузка инфы из базы

@Composable
fun RateContent(
    mainViewModel: MainViewModel
) {
    val rate by mainViewModel.lastSelectedRate.collectAsState()

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
                mainViewModel.updateReserving(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Забронировать")
        }
    }
}