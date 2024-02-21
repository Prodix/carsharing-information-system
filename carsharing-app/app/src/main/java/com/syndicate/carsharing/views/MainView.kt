package com.syndicate.carsharing.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    lateinit var map: MapView
    val mainState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    lateinit var currentLocation: Point
    val scope = rememberCoroutineScope()
    lateinit var userLocationLayer: UserLocationLayer
    val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
            userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(map.mapWindow).apply {
                this.isVisible = true
                this.isAutoZoomEnabled = true
            }

            userLocationLayer.setObjectListener(object : UserLocationObjectListener {
                @SuppressLint("MissingPermission")
                override fun onObjectAdded(p0: UserLocationView) {
                    p0.arrow.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
                    p0.pin.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
                    p0.accuracyCircle.isVisible = false


                    val loc = location.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    currentLocation = Point(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0)

                    map.setNoninteractive(true)
                    map.mapWindow.map.move(CameraPosition(currentLocation, 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                    { map.setNoninteractive(false) }

                }

                override fun onObjectRemoved(p0: UserLocationView) {
                    //TODO("Not yet implemented")
                }

                override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
                    //TODO("Not yet implemented")
                }

            })
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

        MapKitFactory.setApiKey(com.syndicate.carsharing.BuildConfig.MAPKIT_KEY)
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
                    sheetState.show()
                }
            },
            onClickFilter = {

            }
        )



        LeftMenu(
            modifier = Modifier
                .padding(16.dp)
                .withShadow(
                    Shadow(
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        radius = 4.dp,
                        color = Color(0, 0, 0, 40)
                    ),
                    RoundedCornerShape(10.dp)
                )
                .background(Color.White, RoundedCornerShape(10.dp))
                .align(Alignment.TopStart)
        )

        BalanceMenu(
            modifier = Modifier
                .padding(16.dp)
                .withShadow(
                    Shadow(
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        radius = 4.dp,
                        color = Color(0, 0, 0, 40)
                    ),
                    RoundedCornerShape(10.dp)
                )
                .background(Color.White, RoundedCornerShape(10.dp))
                .align(Alignment.TopEnd)
        )

        UserCursorButton(
            modifier = Modifier
                .padding(16.dp)
                .withShadow(
                    Shadow(
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        radius = 4.dp,
                        color = Color(0, 0, 0, 40)
                    ),
                    RoundedCornerShape(10.dp)
                )
                .background(Color.White, RoundedCornerShape(10.dp))
                .align(Alignment.CenterEnd)
        ) {
            map.mapWindow.map.move(CameraPosition(Point(currentLocation.latitude, currentLocation.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
            { }
        }

        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            scrimColor = Color.Transparent,
            sheetGesturesEnabled = false,
            sheetContent = {
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
                    Slider(value = 0f, onValueChange = {})
                    Row (
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(
                            text = "5 мин",
                            fontSize = 12.sp,
                            color = Color(0xFFC2C2C2)
                        )
                        Text(
                            text = "10 мин",
                            fontSize = 12.sp,
                            color = Color(0xFFC2C2C2)
                        )
                        Text(
                            text = "15 мин",
                            fontSize = 12.sp,
                            color = Color(0xFFC2C2C2)
                        )
                        Text(
                            text = "20 мин",
                            fontSize = 12.sp,
                            color = Color(0xFFC2C2C2)
                        )
                    }
                    androidx.compose.material3.Button(
                        onClick = { /*TODO*/ },
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
        ) {

        }
    }
}



