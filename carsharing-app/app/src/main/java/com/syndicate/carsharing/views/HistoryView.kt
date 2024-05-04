package com.syndicate.carsharing.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.models.RentHistory
import com.syndicate.carsharing.viewmodels.HistoryViewModel
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryView(
    navigation: NavHostController,
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val historyState by historyViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(
                top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding(),
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp,
                ),
                onClick = {
                    navigation.popBackStack()
                }
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.back_arrow),
                    contentDescription = null
                )
            }
            Text(
                text = "История поездок",
                style = MaterialTheme.typography.titleMedium
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding(), top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(historyState.history) {
                HistoryCard(
                    it.second,
                    it.first
                )
            }
        }
    }
}

@Composable
fun HistoryCard(
    rentHistory: RentHistory,
    carName: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Color(102, 153, 204, 168))
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = carName,
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.clock),
                    contentDescription = null
                )
                Text(
                    text = rentHistory.rentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.width(50.dp))
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.dollar),
                    contentDescription = null
                )
                Text(
                    text = "${String.format("%.2f", rentHistory.price)} ₽",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.calendar),
                    contentDescription = null
                )
                Text(
                    text = rentHistory.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Text(
                text = "Рейтинг: +${(rentHistory.rentTime.hour * 60 + rentHistory.rentTime.minute) / 10}",
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}