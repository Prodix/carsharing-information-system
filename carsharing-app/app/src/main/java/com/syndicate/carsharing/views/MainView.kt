package com.syndicate.carsharing.views

import android.util.Log
import android.widget.ToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.database.managers.CarManager
import com.syndicate.carsharing.viewmodels.MainViewModel
import com.syndicate.carsharing.viewmodels.SignInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    navigation: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    val mainState by mainViewModel.uiState.collectAsState()

    var first by remember {
        mutableStateOf(true)
    }

    var second by remember {
        mutableStateOf(true)
    }

    var third by remember {
        mutableStateOf(true)
    }


    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ){
        OutlinedTextField(
            value = mainState.search,
            onValueChange = { value -> mainViewModel.search(value)},
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(15.dp),
            textStyle = TextStyle(
                fontSize = 16.sp
            ),
            placeholder = { Text(text = "Поиск") },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF6699CC),
                placeholderColor = Color(0xFFB5B5B5),
                unfocusedBorderColor = Color(0xFFB5B5B5),
                focusedBorderColor = Color(0xFFB5B5B5),
            )
        )
        Row (
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ){

            val action: (String, Boolean) -> Unit = { text, state ->
                if (state)
                    mainViewModel.addToParams(text)
                else
                    mainViewModel.removeFromParams(text)
            }

            ToggleButton(text = "Full", action = action)
            ToggleButton(text = "Half", action = action)
            ToggleButton(text = "Empty", action = action)

        }
        LazyColumn {
            items(mainState.cars) {
                car -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp)),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ){
                    Row (
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        Text(
                            text = car.brand.toString()
                        )
                        Text(
                            text = car.model.toString()
                        )
                    }
                    Text(
                        text = "Fuel: ${car.fuelLevel}"
                    )
                    Text(
                        text = car.carPlate.toString()
                    )
                }
            }
        }
    }
}

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


@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun GreetingPreview() {
}
