package com.syndicate.carsharing.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BalanceMenu(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val user by mainViewModel.userStore.getUser().collectAsState(initial = User())


    Box(
        modifier = modifier
            .then(
                if (mainState.sheetState!!.targetValue == ModalBottomSheetValue.Expanded) {
                    Modifier.alpha(0f)
                } else {
                    Modifier.alpha(1f)
                }
            )
            .withShadow(
                Shadow(
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    radius = 4.dp,
                    color = Color(0, 0, 0, 40)
                ),
                RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable {
                //TODO: Клик на баланс
            }
            .padding(horizontal = 10.dp, vertical = 13.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(end = 5.dp)
        ) {
            Text(
                text = String.format("%.2f", user.balance),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "₽",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF6699CC),
                            radius = this.size.maxDimension / 2f
                        )
                    }
            )
        }
    }
}