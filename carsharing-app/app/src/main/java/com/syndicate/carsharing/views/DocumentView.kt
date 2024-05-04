package com.syndicate.carsharing.views

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.database.models.User
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.viewmodels.CodeViewModel
import com.syndicate.carsharing.viewmodels.DocumentViewModel
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Document(
    fileName: String,
    documentViewModel: DocumentViewModel = hiltViewModel(),
    navigation: NavHostController
) {
    val image = BitmapFactory.decodeFile("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare/${fileName}.jpeg")
    val matrix = Matrix()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val userStore = documentViewModel.userStore

    matrix.postRotate(90f)

    Box(
        modifier = Modifier
            .background(Color(0xFFF9F9FB))
            .padding(
                top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
            )
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
                .padding(
                    top = 20.dp,
                    start = 15.dp,
                    end = 15.dp,
                    bottom = 20.dp + WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()
                )
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            AutoShareButton(
                text = "Переснять",
                isNegative = true,
                modifier = Modifier
                    .weight(1f)
            ) {
                navigation.popBackStack()
            }
            AutoShareButton(
                text = "Готово",
                modifier = Modifier
                    .weight(1f)
            ) {
                when (fileName) {
                    "passport" -> {
                        val file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare/passport.jpeg")
                        scope.launch {
                            val user = userStore.getUser().first()
                            val token = userStore.getToken().first()
                            HttpClient.client.post("${HttpClient.url}/account/upload/?type=passport") {
                                setBody(
                                    MultiPartFormDataContent(
                                        formData {
                                            append("image", file.readBytes(), Headers.build {
                                                append(HttpHeaders.ContentType, "image/jpeg")
                                                append(HttpHeaders.ContentDisposition, "filename=\"${user.id}_passport.jpeg\"")
                                            })
                                        }
                                    ))
                                headers["Authorization"] = "Bearer $token"
                            }
                            userStore.updateToken()
                            navigation.navigate("documentIntro/false/true") {
                                popUpTo(0)
                            }
                        }
                    }
                    "selfie" -> {
                        val file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare/selfie.jpeg")
                        scope.launch {
                            val user = userStore.getUser().first()
                            val token = userStore.getToken().first()
                            HttpClient.client.post("${HttpClient.url}/account/upload/?type=selfie") {
                                setBody(
                                    MultiPartFormDataContent(
                                        formData {
                                            append("image", file.readBytes(), Headers.build {
                                                append(HttpHeaders.ContentType, "image/jpeg")
                                                append(HttpHeaders.ContentDisposition, "filename=\"${user.id}_selfie.jpeg\"")
                                            })
                                        }
                                    ))
                                headers["Authorization"] = "Bearer $token"
                            }
                            userStore.updateToken()
                            navigation.navigate("documentIntro/false/false") {
                                popUpTo(0)
                            }
                        }
                    }
                    else -> {
                        val file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/AutoShare/license.jpeg")
                        scope.launch {
                            val user = userStore.getUser().first()
                            val token = userStore.getToken().first()
                            HttpClient.client.post("${HttpClient.url}/account/upload/?type=driver_license") {
                                setBody(
                                    MultiPartFormDataContent(
                                        formData {
                                            append("image", file.readBytes(), Headers.build {
                                                append(HttpHeaders.ContentType, "image/jpeg")
                                                append(HttpHeaders.ContentDisposition, "filename=\"${user.id}_license.jpeg\"")
                                            })
                                        }
                                    ))
                                headers["Authorization"] = "Bearer $token"
                            }
                            userStore.updateToken()
                            AlertDialog.Builder(context)
                                .setMessage("Ваш аккаунт будет верифицирован в течение 3 рабочих дней")
                                .setPositiveButton("ok") { _, _ -> run { } }
                                .show()
                            navigation.navigate("main") {
                                popUpTo(0)
                            }
                        }
                    }
                }
            }
        }
    }
}
