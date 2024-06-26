package com.syndicate.carsharing.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.syndicate.carsharing.shared_components.AutoShareButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionView(
    navigation: NavHostController
) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(
                top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding(),
                start = 20.dp,
                end = 20.dp
            )
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(50.dp),
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(
                text = "Отсутствует интернет соединение или геолокация",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
            AutoShareButton(
                text = "Попробовать снова"
            ) {
                navigation.navigate("splash")
            }
        }
    }
}