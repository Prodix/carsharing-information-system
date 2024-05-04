package com.syndicate.carsharing.pages

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.zIndex
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.shared_components.Loader
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first


@Composable
fun ProfileContent(
    mainViewModel: MainViewModel,
    navigation: NavHostController
) {
    val user by mainViewModel.userStore.getUser().collectAsState(initial = User())

    LaunchedEffect(key1 = Unit) {
        while (user.id == 0)
            delay(100)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
            SubcomposeAsyncImage(
                model = "${HttpClient.url}/account/get/selfie/?id=${user.selfieId}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    Loader()
                } else {
                    SubcomposeAsyncImageContent()
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5B5B5B)
                )
                Text(
                    text = "Рейтинг ${user.rating} ${when {
                        user.rating in 5..20 -> "баллов"
                        user.rating % 10 == 1 -> "балл"
                        user.rating % 10 in (2..4) -> "балла"
                        else -> "баллов"
                    }}",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF6699CC)
                )
            }
        }
        Box (
            modifier = Modifier
                .withShadow(
                    shadow = Shadow(0.dp, 0.dp, 4.dp, Color(0x40000000)),
                    shape = RoundedCornerShape(10.dp)
                )
                .fillMaxWidth()
                .height(85.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    color = Color.White
                )
        ){
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.background_triangles),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ){
                Text(
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color(0xFFC2C2C2),
                        fontWeight = FontWeight.ExtraBold
                    ),
                    text = "Ваш баланс"
                )
                Text(
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF6699CC)
                    ),
                    text = "${String.format("%.2f", user.balance)} ₽"
                )
            }
        }
        Column {
            Column(
                modifier = Modifier
                    .clickable {
                        navigation.navigate("history")
                    }
            ) {
                HorizontalDivider()
                Text(
                    text = "История поездок",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFF5B5B5B),
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth()
                        .height(25.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
                HorizontalDivider()
            }
            Column {
                Text(
                    text = "Штрафы",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFF5B5B5B),
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth()
                        .height(25.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
                HorizontalDivider()
            }
            Column(
                modifier = Modifier
                    .clickable {
                        navigation.navigate("web/agreement")
                    }
            ) {
                Text(
                    text = "Пользовательское соглашение",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFF5B5B5B),
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth()
                        .height(25.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
                HorizontalDivider()
            }
            Column(
                modifier = Modifier
                    .clickable {
                        navigation.navigate("web/rules")
                    }
            ) {
                Text(
                    text = "Правила использования",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFF5B5B5B),
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth()
                        .height(25.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
                HorizontalDivider()
            }
        }
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
