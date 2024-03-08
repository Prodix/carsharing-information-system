package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SelectableChipColors
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
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
import com.syndicate.carsharing.components.BottomSheetWithPages
import com.syndicate.carsharing.components.LeftMenu
import com.syndicate.carsharing.components.RadarContent
import com.syndicate.carsharing.components.RadarFindingContent
import com.syndicate.carsharing.components.UserCursorButton
import com.syndicate.carsharing.models.MainModel
import com.syndicate.carsharing.data.Tag
import com.syndicate.carsharing.modifiers.withShadow
import com.syndicate.carsharing.utility.Shadow
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
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

    val page = remember {
        mutableStateOf("radarIntro")
    }

    val carType = remember {
        mutableFloatStateOf(1f)
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

    val walkMinutes = remember {
        mutableIntStateOf(1)
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

                if (!sheetState.isVisible)
                    circle.value?.geometry = Circle(currentLocation.value, 400f * walkMinutes.value)

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
                page.value = "radarIntro"
                scope.launch {
                    map.mapWindow.map.move(CameraPosition(Point(currentLocation.value.latitude, currentLocation.value.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { }
                    circle.value = map.mapWindow.map.mapObjects.addCircle(Circle(currentLocation.value, 400f * walkMinutes.value))
                    circle.value?.fillColor = Color(0x4A92D992).toArgb()
                    circle.value?.strokeColor = Color(0xFF99CC99).toArgb()
                    circle.value?.strokeWidth = 1.5f
                    sheetState.show()
                }
            },
            onClickFilter = {
                page.value = "filter"
                scope.launch {
                    sheetState.show()
                }
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

    val bottomSheetPages: Map<String, @Composable () -> Unit> = mapOf(
        "radarIntro" to { RadarContent(
            circle = circle,
            currentLocation = currentLocation,
            isGesturesEnabled = isGesturesEnabled,
            page = page,
            mem = mem,
            walkMinutes = walkMinutes
        )},
        "radar" to { RadarFindingContent(
            page = page,
            isGesturesEnabled = isGesturesEnabled,
            circle = circle,
            mem = mem,
            currentLocation = currentLocation,
            walkMinutes = walkMinutes
        )},
        "filter" to { FilterCarsContent(
            carType = carType,
            mainViewModel
        )}
    )

    BottomSheetWithPages(
        sheetState = sheetState,
        isGesturesEnabled = isGesturesEnabled,
        page = page,
        sheetComposableList = bottomSheetPages,
        walkMinutes = walkMinutes
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalLayoutApi::class
)
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun FilterCarsContent(
    carType: MutableFloatState,
    mainViewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Фильтр поиска",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Column {
            Slider(
                value = carType.floatValue,
                steps = 1,
                valueRange = 1f..3f,
                onValueChange = {
                    carType.floatValue = it
                },
                colors = SliderDefaults.colors(
                    activeTickColor = Color(0xFF6699CC),
                    inactiveTickColor = Color.Transparent,
                    inactiveTrackColor = Color(0x806699CC),
                    activeTrackColor = Color(0xFF6699CC),
                    thumbColor = Color(0xFF34699D)
                ),
                thumb = {
                    SliderDefaults.Thumb(
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
                    text = "Эконом",
                    fontSize = 12.sp,
                    color = if (carType.floatValue == 1f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "Комфорт",
                    fontSize = 12.sp,
                    color = if (carType.floatValue == 2f) Color.Black else Color(0xFFC2C2C2)
                )
                Text(
                    text = "Бизнес",
                    fontSize = 12.sp,
                    color = if (carType.floatValue == 3f) Color.Black else Color(0xFFC2C2C2)
                )
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "Опции",
            fontSize = 12.sp,
            color = Color(0xFFC2C2C2)
        )

        FlowRow (
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val list by mainViewModel.listTags.collectAsState()

            for (i in list.indices) {
                FilterChip(
                    selected = list[i].isSelected,
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        mainViewModel.updateTags(i)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.child_icon),
                            contentDescription = null,
                            tint = if (list[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E)
                        )
                    },
                    colors = ChipDefaults.filterChipColors(
                        backgroundColor = Color.Transparent,
                        selectedBackgroundColor = Color(0x266699CC)
                    ),
                    interactionSource = MutableInteractionSource(),
                    border = BorderStroke(1.dp, if (list[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E))
                ) {
                    Text(
                        text = "Детское кресло",
                        color = if (list[i].isSelected) Color(0xFF6699CC) else Color(0xFF9E9E9E)
                    )
                }
            }
        }




        Button(
            onClick = {
                //TODO: Сделать фильтрацию автомобилей
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
            Text(text = "Фильтр по моделям")
        }
    }
}








