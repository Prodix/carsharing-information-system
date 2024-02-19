package com.syndicate.carsharing.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.R

@Composable
fun BottomMenu(
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 5.dp)
        ) {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.radar), contentDescription = null)
            Text(
                text = "Радар",
                fontSize = 12.sp
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 5.dp)
        ) {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.filter), contentDescription = null)
            Text(
                text = "Фильтр",
                fontSize = 12.sp
            )
        }
    }
}