package com.syndicate.carsharing.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R


@Composable
fun DocumentIntro(
    isPassport: Boolean,
    navigation: NavHostController,
    modifier: Modifier = Modifier
) {
    val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    val context = LocalContext.current

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



    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFFF9F9FB))
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(2f)
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(if (isPassport) R.drawable.passport else R.drawable.driver_license),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    contentScale = ContentScale.FillWidth

                )
            }
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = 10.dp,
                        spotColor = Color(0x336699CC),
                        ambientColor = Color(0x336699CC)
                    )
                    .background(Color.White, RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                    .padding(15.dp, 20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {

                if (isPassport) {
                    Text(
                        text = "Фото паспорта",
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "Для подтверждения личности вам нужно сделать фотографию паспорта в развёрнутом виде - страница с фотографией и регистрацией!",
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Фото водительского удостоверения",
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "Также для подтверждения права на управление транспортом вам нужно сфотографировать страницу с фотографией водительского удостоверения",
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                            if (isPassport)
                                navigation.navigate("camera/passport")
                            else
                                navigation.navigate("camera/license")
                        } else {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6699CC)
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text = "Сделать фото",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
