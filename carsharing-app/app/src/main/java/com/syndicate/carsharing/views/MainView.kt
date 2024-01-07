package com.syndicate.carsharing.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    val mainState by mainViewModel.uiState.collectAsState()
}
/*
@Composable
fun ToggleButton(
    modifier: Modifier = Modifier,
    text: String,
    action: (String, Boolean) -> Unit
) {
    var isToggled by remember {
        mutableStateOf(false)
    }

    val background = if (isToggled) Color(0xFFF0F5FA) else Color.White
    val border = if (isToggled) Color.Transparent else Color.LightGray

    Box(
        modifier = Modifier
            .width(90.dp)
            .height(50.dp)
            .border(2.dp, border, RoundedCornerShape(10.dp))
            .background(background, RoundedCornerShape(10.dp))
            .toggleable(
                value = isToggled,
                onValueChange = {
                    isToggled = it
                    action(text, it)
                }
            )
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = text,
            fontSize = 20.sp
        )
    }
}
*/

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun GreetingPreview() {
}
