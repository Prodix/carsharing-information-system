package com.syndicate.carsharing.views

import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.Penalty
import com.syndicate.carsharing.database.models.RentHistory
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.viewmodels.HistoryViewModel
import com.syndicate.carsharing.viewmodels.PenaltyViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent.headers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PenaltyView(
    navigation: NavHostController,
    penaltyViewModel: PenaltyViewModel = hiltViewModel()
) {
    val penaltyState by penaltyViewModel.uiState.collectAsState()

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
                text = "Штрафы",
                style = MaterialTheme.typography.titleMedium
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding(), top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(penaltyState.penalty) {
                PenaltyCard(
                    penalty = it,
                    navigation = navigation,
                    penaltyViewModel = penaltyViewModel
                )
            }
        }
    }
}

@Composable
fun PenaltyCard(
    penalty: Penalty,
    navigation: NavHostController,
    penaltyViewModel: PenaltyViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
                text = "Штраф",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Рейтинг: -${penalty.ratingPenalty}",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.width(50.dp))
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.dollar),
                    contentDescription = null
                )
                Text(
                    text = "${String.format("%.2f", penalty.price)} ₽",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Text(
                text = penalty.description,
                style = MaterialTheme.typography.displaySmall
            )
            if (!penalty.isPaid) {
                Button(
                    onClick = {
                        scope.launch {
                            val response = HttpClient.client.post(
                                "${HttpClient.url}/account/penalty/pay?id=${penalty.id}"
                            ) { }.body<DefaultResponse>()
                            if (response.status_code != 200) {
                                AlertDialog.Builder(context)
                                    .setMessage(response.message)
                                    .setPositiveButton("ok") { _, _ -> run { } }
                                    .show()
                            } else {
                                penaltyViewModel.mainViewModel.updateUser()
                                AlertDialog.Builder(context)
                                    .setMessage("Штраф оплачен")
                                    .setPositiveButton("ok") { _, _ -> run { } }
                                    .show()
                                navigation.popBackStack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF99CC99)
                    ),
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "Оплатить",
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}