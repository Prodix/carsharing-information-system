package com.syndicate.carsharing.pages

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syndicate.carsharing.R
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel


//TODO: Добавить стили и функциональность кнопок
@Composable
fun ProfileContent(
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
                    .weight(1f)
            )
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.close),
                contentDescription = null
            )
        }
        Row {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.phone), 
                contentDescription = null
            )
            Text(text = "Введите номер телефона")
        }
        Row {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.email),
                contentDescription = null
            )
            Text(text = "Введите электронную почту")
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .withShadow(
                    shadow = Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                    shape = RoundedCornerShape(10.dp)
                )
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
                    Text(text = "Ваш баланс")
                    Text(text = "10000, 94 ₽")
                }
                Image(
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    imageVector = ImageVector.vectorResource(R.drawable.background_triangles),
                    contentDescription = null
                )
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { /*TODO*/ }
        ) {
            Text(text = "Пополнить баланс")
        }
        HorizontalDivider()
        Text(
            text = "История поездок",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF5B5B5B),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        HorizontalDivider()
        Text(
            text = "Банковские карты",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF5B5B5B),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        HorizontalDivider()
        Text(
            text = "Штрафы",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFF5B5B5B),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        HorizontalDivider()
        Text(
            text = "Рейтинг водителя",
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
