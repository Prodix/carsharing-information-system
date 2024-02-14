package com.syndicate.carsharing.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun Document(
    fileName: String,
    navigation: NavHostController
) {
    val image = BitmapFactory.decodeFile("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare/${fileName}.jpeg")
    val matrix = Matrix()
    matrix.postRotate(90f)

    Box(
        modifier = Modifier
            .background(Color(0xFFF9F9FB))
    ) {
        Image(
            bitmap = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.Center

        )
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    spotColor = Color(0x336699CC),
                    ambientColor = Color(0x336699CC)
                )
                .background(Color.White, RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                .padding(15.dp, 20.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Button(
                onClick = {
                    navigation.popBackStack()
                },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFAF0F0)
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    text = "Переснять",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFBB3E3E)
                )
            }
            Button(
                onClick = {
                    when (fileName) {
                        "passport" -> {
                            /* TODO: Отправка паспорта */
                            navigation.navigate("documentIntro/false/true")
                        }
                        "selfie" -> {
                            /* TODO: Отправка селфи */
                            navigation.navigate("documentIntro/false/false")
                        }
                        else -> {
                            /* TODO: Отправка удостоверения */
                            /* TODO: Показать предупреждение о длительности верификации аккаунта */
                            navigation.navigate("main")
                        }
                    }
                },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6699CC)
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    text = "Готово",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
