package com.syndicate.carsharing.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Transport
import com.syndicate.carsharing.viewmodels.MainViewModel
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Date
import kotlin.random.Random

@Composable
fun CheckContent(
    mainViewModel: MainViewModel,
    navigation: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timer by mainViewModel.timer.collectAsState()
    val stopwatchOnChecking by mainViewModel.stopwatchChecking.collectAsState()
    val placemark by mainViewModel.lastSelectedPlacemark.collectAsState()
    val transportInfo = placemark?.userData as Transport

    val damagesNameList = remember {
        mutableStateOf<List<String>?>(null)
    }
    //TODO: ускоряется таймер при платном осмотре и добавлении фото
    LaunchedEffect(key1 = context) {
        mainViewModel.updateReserving(false)
        mainViewModel.updateSession(null)
        mainViewModel.updateChecking(true)

        damagesNameList.value = HttpClient.client.get(
            "${HttpClient.url}/transport/get/damage?id=${transportInfo.id}"
        ).body<List<String>>()

        stopwatchOnChecking.clear()
        timer.onTimerEnd = {
            mainViewModel.viewModelScope.launch {
                stopwatchOnChecking.start()
            }
        }

        if (timer.defaultMinutes != 5) {
            timer.changeStartTime(5,0)
        }

        if (!timer.isStarted) {
            mainViewModel.viewModelScope.launch {
                timer.start()
            }
        }
    }

    val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    val activityResultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    context,
                    "Вы должны выдать разрешения использования камеры!",
                    Toast.LENGTH_SHORT).show()
            }
        }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Column (
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Недостатки, о которых мы уже знаем"
        )
        if (damagesNameList.value != null) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(damagesNameList.value!!) {
                    SubcomposeAsyncImage(
                        model = "${HttpClient.url}/transport/get/damage/image?name=$it",
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .width(190.dp)
                            .height(115.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    ) {
                        val state = painter.state
                        if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                            Loader()
                        } else {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }
        }
        Box (
            modifier = Modifier
                .padding(4.dp)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0x666699CC),
                        RoundedCornerShape(10.dp)
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFF6699CC),
                            style = Stroke(
                                width = 4f,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f)
                                )
                            ),
                            cornerRadius = CornerRadius(x = 10.dp.toPx(), y = 10.dp.toPx())
                        )
                    }
                    .clickable {
                        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            //TODO: Привязать к id юзера

                            navigation.navigate("camera/damage_${transportInfo.id}_${Random.nextInt()}${Date().time}")
                        } else {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    .padding(vertical = 25.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.camera),
                        contentDescription = null
                    )
                    Text(
                        text = "Добавить фото"
                    )
                }
            }
        }
        Text(
            text = "Если вы не обнаружили новые повреждения и ознакомились с правилами пользователя, то можно отправляться в путь",
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = "Правила пользователя",
            modifier = Modifier
                .fillMaxWidth()
        )
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = if (stopwatchOnChecking.isStarted)
                    "Платный осмотр"
                else
                    "Время бесплатного осмотра закончится через"
            )
            Text(
                text = if (stopwatchOnChecking.isStarted)
                    stopwatchOnChecking.toString()
                else
                    timer.toString()
            )
        }
        Button(
            onClick = {
                stopwatchOnChecking.stop()
                mainViewModel.updatePage("rentPage")
                mainViewModel.updateChecking(false)
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Начать аренду"
            )
        }
    }
}