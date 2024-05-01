package com.syndicate.carsharing.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.pages.components.CarPresenter
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel


//TODO: Загрузка изображения и информации
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CarContent(
    mainViewModel: MainViewModel
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val transportInfo = mainState.lastSelectedPlacemark?.userData as Transport

    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
        CarPresenter(transportInfo)
        if (transportInfo.functions.isNotEmpty()) {
            Text(
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                text = "Что есть в машине?",
                color = Color(0xFFC2C2C2)
            )
            for (function in transportInfo.functions) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(if (function.functionData == "CHILD_CHAIR") R.drawable.child_icon else R.drawable.transponder),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF6699CC))
                    )
                    Text(
                        style = MaterialTheme.typography.displaySmall,
                        text = when (function.functionData) {
                            "CHILD_CHAIR" -> "Детское кресло"
                            "TRANSPONDER" -> "Транспондер"
                            else -> "Неизвестно"
                        }
                    )
                }
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            items(transportInfo.rates) {
                Box(
                    modifier = Modifier
                        .width(217.dp)
                        .height(85.dp)
                        .padding(5.dp)
                        .withShadow(
                            shadow = Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            mainViewModel.updateSelectedRate(it);
                            if (it.onRoadPrice != it.parkingPrice) {
                                mainViewModel.updateIsFixed(false)
                            } else {
                                mainViewModel.updateIsFixed(true)
                            }
                            mainViewModel.updatePage("rateInfo")
                        }
                ) {
                    Box (
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                    ){
                        Column(
                            modifier = Modifier
                                .padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ){
                            Text(
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                text = if (String.format("%.2f", it.onRoadPrice) == String.format("%.2f", it.parkingPrice))
                                    "${String.format("%.2f", it.onRoadPrice * 60)} ₽/час"
                                else
                                    "${String.format("%.2f", it.onRoadPrice)} ₽/мин"
                            )
                            Text(
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                text = it.rateName,
                                color = Color(0xFFC2C2C2)
                            )
                        }
                        Image(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(15.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.info),
                            contentDescription = null
                        )
                        Image(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width(60.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.background_triangles),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}