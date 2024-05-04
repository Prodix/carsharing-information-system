package com.syndicate.carsharing.views

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WebView(
    navigation: NavHostController,
    documentName: String
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(
                top = WindowInsets.statusBarsIgnoringVisibility
                    .asPaddingValues()
                    .calculateTopPadding(),
                start = 15.dp,
                end = 15.dp,
                bottom = WindowInsets.navigationBarsIgnoringVisibility
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent
            ),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp,
            ),
            onClick = {
                navigation.popBackStack()
            }
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.back_arrow),
                contentDescription = null
            )
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    this.webViewClient = WebViewClient()
                    this.clearCache(true)
                    this.loadUrl("${HttpClient.url.substring(0 until HttpClient.url.length - 3)}${documentName}")
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .fillMaxSize()
        )
    }
}