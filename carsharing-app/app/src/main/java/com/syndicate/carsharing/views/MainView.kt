package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import android.util.MutableFloat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.BuildConfig
import com.syndicate.carsharing.R
import com.syndicate.carsharing.components.BalanceMenu
import com.syndicate.carsharing.components.BottomMenu
import com.syndicate.carsharing.components.LeftMenu
import com.syndicate.carsharing.components.UserCursorButton
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.Layer
import com.yandex.mapkit.layers.LayerOptions
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.layers.TileFormat
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.tiles.TileProvider
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import io.ktor.client.plugins.convertLongTimeoutToLongWithInfiniteAsZero
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    lateinit var map: MapView
    val mainState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val currentLocation: MutableState<Point> = remember {
        mutableStateOf(Point())
    }
    val scope = rememberCoroutineScope()
    val mem = remember {
        mutableFloatStateOf(1f)
    }
    val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val circle: MutableState<CircleMapObject?> = remember {
        mutableStateOf(null)
    }
    lateinit var userPlacemark: PlacemarkMapObject

    val isGesturesEnabled = remember {
        mutableStateOf(true)
    }

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = {
            if (it == ModalBottomSheetValue.Hidden) {
                if (circle.value != null)
                    map.mapWindow.map.mapObjects.remove(circle.value!!)
                circle.value = null
            }

            true
        }
    )

    // TODO: Добавить проверку интернета и геолокации
    fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val loc = location.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            currentLocation.value = Point(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0)

            userPlacemark = map.mapWindow.map.mapObjects.addPlacemark()
            userPlacemark.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
            userPlacemark.geometry = currentLocation.value
            if (currentLocation.value.latitude == 0.0 && currentLocation.value.longitude == 0.0) {
                userPlacemark.isVisible = false
            }
            else {
                map.setNoninteractive(true)
                map.mapWindow.map.move(CameraPosition(currentLocation.value, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                { map.setNoninteractive(false) }
            }

            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f) {
                currentLocation.value = Point(it.latitude, it.longitude)
                userPlacemark.geometry = currentLocation.value
                circle.value?.geometry = Circle(currentLocation.value, 400f * mem.floatValue)

                if (!userPlacemark.isVisible) {
                    userPlacemark.isVisible = true
                    map.setNoninteractive(true)
                    map.mapWindow.map.move(CameraPosition(currentLocation.value, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { map.setNoninteractive(false) }
                }
            }
        }
    }

    val locationPermissions =
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    val activityResultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in locationPermissions && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                AlertDialog.Builder(context)
                    .setMessage("Вы должны выдать разрешения использования геолокации чтобы пользоваться приложением!")
                    .setPositiveButton("ok") { _, _ -> exitProcess(0) }
                    .show()
            } else {
                enableLocation()
            }
        }

    LaunchedEffect(key1 = context) {
        activityResultLauncher.launch(locationPermissions)
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_KEY)
        AndroidView(factory = {
            MapView(it).apply {
                map = this
                MapKitFactory.initialize(it)
                MapKitFactory.getInstance().onStart()
                this.onStart()
            }

        }, modifier = Modifier) {

        }

        BottomMenu(
            modifier = Modifier
                .padding(25.dp)
                .withShadow(
                    Shadow(
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        radius = 4.dp,
                        color = Color(0, 0, 0, 40)
                    ),
                    RoundedCornerShape(16.dp)
                )
                .background(Color.White, RoundedCornerShape(16.dp))
                .align(Alignment.BottomCenter),
            onClickRadar = {
                scope.launch {
                    map.mapWindow.map.move(CameraPosition(Point(currentLocation.value.latitude, currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { }
                    circle.value = map.mapWindow.map.mapObjects.addCircle(Circle(currentLocation.value, 400f * mem.floatValue))
                    circle.value?.fillColor = Color(0x4A92D992).toArgb()
                    circle.value?.strokeColor = Color(0xFF99CC99).toArgb()
                    circle.value?.strokeWidth = 1.5f
                    sheetState.show()
                }
            },
            onClickFilter = {

            }
        )

        LeftMenu(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            sheetState = sheetState
        )

        BalanceMenu(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            sheetState = sheetState
        )

        UserCursorButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterEnd),
            sheetState = sheetState
        ) {
            map.mapWindow.map.move(CameraPosition(Point(currentLocation.value.latitude, currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
            { }
        }
    }



    val page = remember {
        mutableIntStateOf(0)
    }

    val bottomSheetPages: List<@Composable () -> Unit> = listOf({RadarContent(
        circle = circle,
        currentLocation = currentLocation,
        isGesturesEnabled = isGesturesEnabled,
        page = page,
        mem = mem
    )}, {RadarFindingContent(page, isGesturesEnabled, circle, mem, currentLocation)})

    BottomSheetWithPages(sheetState, isGesturesEnabled, page, bottomSheetPages)
}

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetWithPages(
    sheetState: ModalBottomSheetState,
    isGesturesEnabled: MutableState<Boolean>,
    page: MutableState<Int>,
    test: List<@Composable () -> Unit>
) {
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.Transparent,
        sheetGesturesEnabled = isGesturesEnabled.value,
        sheetContent = {
            Box {
                AnimatedContent(
                    targetState = page.value,
                    label = "test"
                ) {
                    test[it].invoke()
                }
            }
        }
    ) {
    }
    Box(
        modifier = Modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .then(
                    if (!isGesturesEnabled.value) {
                        Modifier
                            .clickable(
                                indication = null,
                                interactionSource = MutableInteractionSource()
                            ) { }
                    } else {
                        Modifier
                    }
                )
        ) {
        }
    }
}

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarContent(
    circle: MutableState<CircleMapObject?>,
    currentLocation: MutableState<Point>,
    isGesturesEnabled: MutableState<Boolean>,
    page: MutableState<Int>,
    mem: MutableFloatState
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Радар",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Сколько мне идти",
            fontSize = 12.sp,
            color = Color(0xFFC2C2C2)
        )
        Column {
            androidx.compose.material3.Slider(
                value = mem.floatValue,
                steps = 2,
                valueRange = 1f..4f,
                onValueChange = {
                    mem.floatValue = it
                    circle.value?.geometry = Circle(currentLocation.value, 400f * it)
                },
                colors = androidx.compose.material3.SliderDefaults.colors(
                    activeTickColor = Color(0xFF6699CC),
                    inactiveTickColor = Color.Transparent,
                    inactiveTrackColor = Color(0x806699CC),
                    activeTrackColor = Color(0xFF6699CC),
                    thumbColor = Color(0xFF34699D)
                ),
                thumb = {
                    androidx.compose.material3.SliderDefaults.Thumb(
                        interactionSource = MutableInteractionSource(),
                        thumbSize = DpSize(30.dp, 30.dp)
                    )
                }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = "5 мин",
                    fontSize = 12.sp,
                    color = if (mem.value == 1f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "10 мин",
                    fontSize = 12.sp,
                    color = if (mem.value == 2f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "15 мин",
                    fontSize = 12.sp,
                    color = if (mem.value == 3f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "20 мин",
                    fontSize = 12.sp,
                    color = if (mem.value == 4f) Color.Black else Color(0xFFC2C2C2)
                )
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        androidx.compose.material3.Button(
            onClick = {
                isGesturesEnabled.value = false
                page.value++
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6699CC),
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color(0xFFB5B5B5)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Начать поиск")
        }
    }
}

@Composable
fun RadarFindingContent(
    page: MutableState<Int>,
    isGesturesEnabled: MutableState<Boolean>,
    circle: MutableState<CircleMapObject?>,
    mem: MutableFloatState,
    currentLocation: MutableState<Point>
) {
    // TODO: Сделать таймер
    // TODO: Реализовать поиск автомобиля
    // TODO: Сделать подстановку информации о минутах пешком

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = isGesturesEnabled.value) {
        scope.launch {
            val max = mem.floatValue
            var isIncreasing = false
            while (true) {
                if (isIncreasing)
                    mem.floatValue += max*0.05f
                else
                    mem.floatValue -= max*0.05f

                if (mem.floatValue <= 0.04f)
                    isIncreasing = true

                if (mem.floatValue >= max)
                    isIncreasing = false

                circle.value?.geometry = Circle(currentLocation.value, 400f * mem.floatValue)

                delay(170)
            }
        }
    }

    isGesturesEnabled.value = false

    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Радар",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = "Высокий спрос в вашей зоне, машин поблизости нет",
            fontSize = 12.sp,
            color = Color(0xFFC2C2C2)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Ищем ближайший автомобиль",
                fontSize = 12.sp,
                color = Color(0xFFC2C2C2)
            )
            Text(
                text = "10 МИНУТ ПЕШКОМ",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Осталось 29 минут",
                fontSize = 12.sp,
                color = Color(0xFFC2C2C2)
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        androidx.compose.material3.Button(
            onClick = {
                isGesturesEnabled.value = true
                page.value--
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6699CC),
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color(0xFFB5B5B5)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Назад")
        }
    }
}


