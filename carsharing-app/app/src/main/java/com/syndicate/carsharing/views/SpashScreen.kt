package com.syndicate.carsharing.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syndicate.carsharing.UserStore
import com.syndicate.carsharing.database.models.User
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigateToDestination: (String) -> Unit,
    userStore: UserStore
) {
    val user by userStore.getUser().collectAsState(initial = User())

    LaunchedEffect(key1 = Unit) {
        delay(1500)
        if (user.email == "") {
            navigateToDestination("signIn")
        } else if (user.passportId == 0) {
            navigateToDestination("documentIntro/true/false")
        } else if (user.driverLicenseId == 0) {
            navigateToDestination("documentIntro/false/false")
        } else if (user.selfieId == 0) {
            navigateToDestination("documentIntro/false/true")
        } else {
            navigateToDestination("main")
        }
    }
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {

    }
}