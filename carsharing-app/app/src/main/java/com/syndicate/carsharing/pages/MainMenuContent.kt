package com.syndicate.carsharing.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.viewmodels.MainViewModel

@Composable
fun MainMenuContent(
    mainViewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(bottom = 10.dp)
                .clickable {
                    mainViewModel.updatePage("profile")
                }
        ) {
            Image(
                ImageBitmap.imageResource(id = R.drawable.driver_license),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "email@gmail.com",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5B5B5B)
                )
                Text(
                    text = "Рейтинг 75 баллов",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF6699CC)
                )
            }
            Spacer(
                modifier = Modifier
                    .weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.arrow),
                contentDescription = null
            )
        }
        HorizontalDivider()
        Text(
            text = "Правила и соглашения",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF5B5B5B),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        HorizontalDivider()
        Text(
            text = "Выбор темы",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF5B5B5B),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        HorizontalDivider()
        Text(
            text = "Поддержка",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF5B5B5B),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        HorizontalDivider()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 75.dp),
            text = "AutoShare",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6699CC)
        )
    }
}