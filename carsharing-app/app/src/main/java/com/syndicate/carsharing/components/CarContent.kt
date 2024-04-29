package com.syndicate.carsharing.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


//TODO: Загрузка изображения и информации
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CarContent(
    mainViewModel: MainViewModel
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val transportInfo = mainState.lastSelectedPlacemark?.userData as Transport

    val context = LocalContext.current
    val file = File("${context.cacheDir.absolutePath}/${transportInfo.carImagePath}")
    var isImageInitialized by remember {
        mutableStateOf(false)
    }

    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(
                text = transportInfo.carName
            )
            Box(
                modifier = Modifier
                    .background(Color(0x80A3C2E0), RoundedCornerShape(5.dp))
            ) {
                Text(
                    modifier = Modifier
                        .padding(5.dp),
                    text = "${transportInfo.carNumber[0]} ${transportInfo.carNumber.subSequence(1, 4)} ${transportInfo.carNumber.subSequence(4, 6)} ${transportInfo.carNumber.subSequence(6, transportInfo.carNumber.length)}"
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.fuel),
                    contentDescription = null
                )
                Text(
                    text = "${((transportInfo.gasLevel / (transportInfo.gasConsumption / 100.0))).toInt()} км • ${((transportInfo.gasLevel / transportInfo.tankCapacity.toDouble()) * 100).toInt()}%"
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.car),
                    contentDescription = null
                )
                Text(
                    text = when (transportInfo.transportType) {
                        "BASE" -> "Базовый"
                        "COMFORT" -> "Комфорт"
                        "BUSINESS" -> "Бизнес"
                        else -> "Неизвестно"
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.insurance),
                    contentDescription = null
                )
                Text(
                    text = transportInfo.insuranceType
                )
            }
        }

        SubcomposeAsyncImage(
            model = "${HttpClient.url}/transport/get/image?name=${transportInfo.carImagePath}",
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                Loader()
            } else {
                SubcomposeAsyncImageContent()
            }
        }

        if (!transportInfo.functions.isEmpty()) {
            Text(
                text = "Что есть в машине?"
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
                            Text(text = if (String.format("%.2f", it.onRoadPrice) == String.format("%.2f", it.parkingPrice))
                                    "${String.format("%.2f", it.onRoadPrice * 60)} P/час"
                                else
                                    "${String.format("%.2f", it.onRoadPrice)} P/мин")
                            Text(text = it.rateName)
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