package com.syndicate.carsharing.views

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import kotlin.system.exitProcess

@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    lateinit var map: MapView
    val mainState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    lateinit var currentLocation: com.yandex.mapkit.location.Location
    lateinit var userLocationLayer: UserLocationLayer
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
            }

            MapKitFactory.getInstance().createLocationManager().requestSingleUpdate(
                object : com.yandex.mapkit.location.LocationListener {

                    override fun onLocationUpdated(p0: com.yandex.mapkit.location.Location) {
                        currentLocation = p0
                        map.setNoninteractive(true)
                        map.mapWindow.map.move(CameraPosition(Point(p0.position.latitude, p0.position.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
                        { map.setNoninteractive(false) }
                    }

                    override fun onLocationStatusUpdated(p0: LocationStatus) {

                    }

                }
            )

            userLocationLayer.setObjectListener(object : UserLocationObjectListener {
                override fun onObjectAdded(p0: UserLocationView) {
                    p0.arrow.setIcon(ImageProvider.fromResource(context, R.drawable.userpoint))
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
                .align(Alignment.BottomCenter)
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
            map.mapWindow.map.move(CameraPosition(Point(currentLocation.position.latitude, currentLocation.position.longitude), 13f, 0f, 0f), Animation(Animation.Type.SMOOTH, 0.5f))
            { }
        }
    }
}