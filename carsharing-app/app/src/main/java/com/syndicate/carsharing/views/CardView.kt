package com.syndicate.carsharing.views

import android.app.AlertDialog
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.syndicate.carsharing.R
import com.syndicate.carsharing.database.HttpClient
import com.syndicate.carsharing.database.models.Card
import com.syndicate.carsharing.database.models.DefaultResponse
import com.syndicate.carsharing.models.CardModel
import com.syndicate.carsharing.shared_components.AutoShareButton
import com.syndicate.carsharing.viewmodels.CardViewModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs



@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CardView(
    navigation: NavHostController,
    cardViewModel: CardViewModel = hiltViewModel()
) {
    val cardState by cardViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = density.density * configuration.screenWidthDp
    var lastValue by remember {
        mutableFloatStateOf(0f)
    }

    val cardPagerState = rememberPagerState {
        cardState.cards.size + 1
    }

    val textFieldPagerState = rememberPagerState {
        2
    }

    LaunchedEffect(key1 = cardPagerState.currentPageOffsetFraction) {
        val offset = cardPagerState.getOffsetFractionForPage(cardPagerState.pageCount - 1)
        Log.d("offset", "offset = $offset lastvalue = $lastValue")
        if (offset >= -1f) {
            if (offset > lastValue) {
                val step = screenWidth * abs(abs(offset) - abs(lastValue)) / 2f
                for (i in 1..2)
                    textFieldPagerState.scrollBy(step)
            } else {
                val step = screenWidth * -abs(abs(offset) - abs(lastValue)) / 2f
                for (i in 1..2)
                    textFieldPagerState.scrollBy(step)
            }
        }
        lastValue = offset
    }

    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(
                top = WindowInsets.statusBarsIgnoringVisibility
                    .asPaddingValues()
                    .calculateTopPadding(),
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
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
            Text(
                text = "Карты",
                style = MaterialTheme.typography.titleMedium
            )
        }
        HorizontalPager(
            state = cardPagerState,
            contentPadding = PaddingValues(
                horizontal = 40.dp,
                vertical = 20.dp
            ),
            pageSpacing = 20.dp
        ) {
            if (it == cardState.cards.size)
                AddCardTemplate()
            else
                BankCard(card = cardState.cards[it])
        }
        HorizontalPager(
            state = textFieldPagerState,
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top,
            beyondBoundsPageCount = 1
        ) {
            if (it == 1 || cardPagerState.pageCount == 1) {
                AddCardContent(
                    cardPager = cardPagerState,
                    textFieldPager = textFieldPagerState,
                    cardState = cardState,
                    cardViewModel = cardViewModel
                )
            } else {
                Column (
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = cardState.money.toString(),
                        onValueChange = {
                            cardViewModel.updateMoney(if (it.isEmpty()) 0 else it.toInt())
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.displayMedium,
                        placeholder = { Text(text = "Сумма", style = MaterialTheme.typography.displayMedium) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color(0xFFB5B5B5),
                            unfocusedPlaceholderColor = Color(0xFFB5B5B5),
                            unfocusedBorderColor = Color(0xFFB5B5B5),
                            focusedBorderColor = Color(0xFFB5B5B5),
                            errorBorderColor = Color(0xFFBB3E3E),
                            errorCursorColor = Color(0xFFBB3E3E),
                            errorSupportingTextColor = Color(0xFFBB3E3E)
                        )
                    )
                    AutoShareButton(
                        text = "Пополнить баланс"
                    ) {
                        scope.launch {
                            val user = cardViewModel.userStore.getUser().first()
                            val token = cardViewModel.userStore.getToken().first()
                            val response = HttpClient.client.post(
                                "${HttpClient.url}/account/deposit?id=${user.id}&amount=${cardState.money}"
                            ) {
                                headers["Authorization"] = "Bearer $token"
                            }.body<DefaultResponse>()
                            AlertDialog.Builder(context)
                                .setMessage(response.message)
                                .setPositiveButton("ok") { _, _ -> run { } }
                                .show()
                            cardViewModel.mainViewModel.updateUser()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddCardContent(
    cardPager: PagerState,
    textFieldPager: PagerState,
    cardState: CardModel,
    cardViewModel: CardViewModel
) {
    val context = LocalContext.current
    var isOpen by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = cardState.cardNumber,
            onValueChange = {
                if (it.length <= 16) {
                    cardViewModel.updateCardNumber(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.displayMedium,
            placeholder = { Text(text = "Номер карты", style = MaterialTheme.typography.displayMedium) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedPlaceholderColor = Color(0xFFB5B5B5),
                unfocusedPlaceholderColor = Color(0xFFB5B5B5),
                unfocusedBorderColor = Color(0xFFB5B5B5),
                focusedBorderColor = Color(0xFFB5B5B5),
                errorBorderColor = Color(0xFFBB3E3E),
                errorCursorColor = Color(0xFFBB3E3E),
                errorSupportingTextColor = Color(0xFFBB3E3E)
            )
        )
        OutlinedTextField(
            value = cardState.cvc,
            onValueChange = {
                if (it.length <= 3) {
                    cardViewModel.updateCvc(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.displayMedium,
            placeholder = { Text(text = "CVC", style = MaterialTheme.typography.displayMedium) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedPlaceholderColor = Color(0xFFB5B5B5),
                unfocusedPlaceholderColor = Color(0xFFB5B5B5),
                unfocusedBorderColor = Color(0xFFB5B5B5),
                focusedBorderColor = Color(0xFFB5B5B5),
                errorBorderColor = Color(0xFFBB3E3E),
                errorCursorColor = Color(0xFFBB3E3E),
                errorSupportingTextColor = Color(0xFFBB3E3E)
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                readOnly = true,
                value = cardState.expireDate.format(DateTimeFormatter.ISO_DATE),
                label = { Text("Дата истечения") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(10.dp),
                onValueChange = {},
                textStyle = MaterialTheme.typography.displayMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color(0xFFB5B5B5),
                    unfocusedPlaceholderColor = Color(0xFFB5B5B5),
                    unfocusedBorderColor = Color(0xFFB5B5B5),
                    focusedBorderColor = Color(0xFFB5B5B5),
                    errorBorderColor = Color(0xFFBB3E3E),
                    errorCursorColor = Color(0xFFBB3E3E),
                    errorSupportingTextColor = Color(0xFFBB3E3E)
                )
            )
            IconButton(
                onClick = {
                    isOpen = true
                }
            ) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calendar")
            }
        }
        AutoShareButton(
            text = "Добавить"
        ) {
            scope.launch {
                val user = cardViewModel.userStore.getUser().first()
                val token = cardViewModel.userStore.getToken().first()
                val response = HttpClient.client.post(
                    "${HttpClient.url}/account/card/add?userId=${user.id}&cardNumber=${cardState.cardNumber}&cvc=${cardState.cvc}&expireDate=${cardState.expireDate.format(DateTimeFormatter.ISO_DATE)}"
                ) {
                    headers["Authorization"] = "Bearer $token"
                }.body<DefaultResponse>()
                if (response.status_code != 200) {
                    AlertDialog.Builder(context)
                        .setMessage(response.message)
                        .setPositiveButton("ok") { _, _ -> run { } }
                        .show()
                } else {
                    AlertDialog.Builder(context)
                        .setMessage(response.message)
                        .setPositiveButton("ok") { _, _ -> run { } }
                        .show()
                    cardViewModel.updateCards()
                    cardPager.scrollToPage(0)
                    textFieldPager.scrollToPage(0)
                }
            }
        }
    }
    if (isOpen) {
        CustomDatePickerDialog(
            onAccept = {
                isOpen = false

                if (it != null) {
                    cardViewModel.updateDate(
                        Instant
                            .ofEpochMilli(it)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate())
                }
            },
            onCancel = {
                isOpen = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onAccept: (Long?) -> Unit,
    onCancel: () -> Unit
) {
    val state = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = { onAccept(state.selectedDateMillis) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent
                )) {
                Text("Сохранить", color = Color.Black)
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
fun AddCardTemplate() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(
                Color.White,
                RoundedCornerShape(10.dp)
            )
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFB2B2B2),
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10f, 10f)
                        )
                    ),
                    cornerRadius = CornerRadius(x = 10.dp.toPx(), y = 10.dp.toPx())
                )
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(
                style = MaterialTheme.typography.displaySmall,
                color = Color.Black,
                text = "+"
            )
            Text(
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                text = "Добавить карту"
            )
        }
    }
}


@Composable
fun BankCard(
    card: Card
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .height(175.dp)
            .fillMaxWidth()
            .background(Color(102, 153, 204, 168))
            .padding(20.dp)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(card.cardNumber.getPaymentSystem()),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.End)
        )
        Text(
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 26.sp
            ),
            text = card.cardNumber.substring(0..3) + " " + card.cardNumber.substring(4..7) + " " + card.cardNumber.substring(8..11) + " " + card.cardNumber.substring(12..15),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier
                .align(Alignment.End)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "Дата ист."
                )
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "${if (card.expireDate.monthValue < 10) "0${card.expireDate.monthValue}" else card.expireDate.monthValue}/${if (card.expireDate.year < 10) "0${card.expireDate.year.toString()[3]}" else card.expireDate.year.toString().substring(2)}"
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "CVC"
                )
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = card.cvc
                )
            }
        }
    }
}

private fun String.getPaymentSystem() = when {
    this.startsWith("2") -> R.drawable.svg_mir
    this.matches(Regex("""3[068].+""")) -> R.drawable.svg_diners_club
    this.matches(Regex("""3[15].+""")) -> R.drawable.svg_jcb
    this.matches(Regex("""3[47].+""")) -> R.drawable.svg_american_express
    this.startsWith("4") -> R.drawable.svg_visa
    this.matches(Regex("""5[0678].+""")) -> R.drawable.svg_maestro
    this.matches(Regex("""5[12345].+""")) -> R.drawable.svg_master_card
    this.startsWith("60") -> R.drawable.svg_discover
    this.startsWith("62") -> R.drawable.svg_union_pay
    this.matches(Regex("""6[37].+""")) -> R.drawable.svg_maestro
    else -> 0
}