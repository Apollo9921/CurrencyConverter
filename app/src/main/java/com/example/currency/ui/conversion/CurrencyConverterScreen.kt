package com.example.currency.ui.conversion

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.currency.R
import com.example.currency.core.BackgroundColor
import com.example.currency.core.Black
import com.example.currency.core.TopBarColor
import com.example.currency.core.mediaQueryHeight
import com.example.currency.core.mediaQueryWidth
import com.example.currency.core.normal
import com.example.currency.core.small
import com.example.currency.core.status
import com.example.currency.model.currencyList.Currency
import com.example.currency.network.ConnectivityObserver
import com.example.currency.network.NetworkConnectivityObserver
import com.example.currency.ui.history.ShowHistory
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat

private lateinit var connectivityObserver: ConnectivityObserver
private var applicationContext: Context? = null
private var amountToConvert = mutableStateOf("")
private var amountConverted = mutableStateOf("")
private lateinit var viewModel: CurrencyConverterViewModel
private var currencies: Currency? = null
private var currencyToBeConvert = mutableStateOf("")
private var currencyConverted = mutableStateOf("")
private var showHistory = mutableStateOf(false)
private var openDialogFrom = mutableStateOf(false)
private var openDialogTo = mutableStateOf(false)
private var fromCurrency = mutableStateOf("")
private var toCurrency = mutableStateOf("")

@Composable
fun CurrencyConverterScreen(
    from: String,
    to: String,
    amount: String,
    navController: NavHostController
) {
    fromCurrency.value = from
    toCurrency.value = to
    InitializeValues(amount)
    BackHandler {
        clearValues()
        navController.navigateUp()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CurrenciesScreenConverterTopBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            if (showHistory.value) {
                ShowModalBottomSheet()
            }
            GetCurrency()
        }
    }
}

@Composable
private fun InitializeValues(amount: String) {
    var isConnected by remember { mutableStateOf(false) }
    applicationContext = LocalContext.current.applicationContext
    connectivityObserver = NetworkConnectivityObserver(applicationContext ?: return)
    status = connectivityObserver.observe().collectAsState(
        initial = status
    ).value
    viewModel = koinViewModel<CurrencyConverterViewModel>()
    if (status == ConnectivityObserver.Status.Available && !isConnected) {
        viewModel.getCurrencies(status)
        isConnected = true
    }
    amountToConvert.value = amount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowModalBottomSheet() {
    ModalBottomSheet(
        onDismissRequest = { showHistory.value = false },
        containerColor = White,
        modifier = Modifier.fillMaxHeight(0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = stringResource(id = R.string.conversion_history),
                color = Black,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    25.sp
                } else {
                    30.sp
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(10.dp))
            ShowHistory()
        }
    }
}

@Composable
private fun GetCurrency() {
    when {
        viewModel.isLoading.value -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TopBarColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = White
                )
            }
        }

        viewModel.isError.value -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TopBarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.messageError.value,
                    color = White,
                    fontSize =
                    if (mediaQueryWidth() <= small) {
                        20.sp
                    } else if (mediaQueryWidth() <= normal) {
                        24.sp
                    } else {
                        28.sp
                    },
                    textAlign = TextAlign.Center
                )
            }
        }

        viewModel.isSuccess.value -> {
            if (viewModel.conversion.value == null) {
                currencies = viewModel.currencies.value
                defineCurrencies()
            } else if (viewModel.conversion.value != null) {
                convertAmount()
            }
            ConverterScreen()
        }
    }
}

private fun convertAmount() {
    amountConverted.value = viewModel.conversion.value?.rates?.conversion?.values.toString()
    amountConverted.value = amountConverted.value.replace("[", "").replace("]", "")
    amountConverted.value =
        DecimalFormat("#.##").format(amountConverted.value.toDouble())
}

private fun defineCurrencies() {
    if (currencies?.currencies?.containsKey(toCurrency.value) == true) {
        currencyConverted.value = currencies?.currencies?.get(toCurrency.value) ?: ""
    }
    if (currencies?.currencies?.containsKey(fromCurrency.value) == true) {
        currencyToBeConvert.value = currencies?.currencies?.get(fromCurrency.value) ?: ""
    }
}

@Composable
private fun CurrenciesScreenConverterTopBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarColor)
            .padding(top = 60.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.arrow_back),
            contentDescription = "Arrow back",
            colorFilter = ColorFilter.tint(White),
            modifier = Modifier
                .size(
                    if (mediaQueryWidth() <= small) {
                        40.dp
                    } else if (mediaQueryWidth() <= normal) {
                        50.dp
                    } else {
                        60.dp
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    clearValues()
                    navController.navigateUp()
                }
        )
        Image(
            painter = painterResource(id = R.drawable.history),
            contentDescription = "Arrow back",
            colorFilter = ColorFilter.tint(White),
            modifier = Modifier
                .size(
                    if (mediaQueryWidth() <= small) {
                        40.dp
                    } else if (mediaQueryWidth() <= normal) {
                        50.dp
                    } else {
                        60.dp
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showHistory.value = true
                }
        )
    }
}

@Composable
private fun ConverterScreen() {
    val context = LocalContext.current
    if (openDialogFrom.value || openDialogTo.value) {
        ChangeCurrency()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(TopBarColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currencyToBeConvert.value,
                    color = White,
                    fontSize =
                    if (mediaQueryWidth() <= small) {
                        25.sp
                    } else if (mediaQueryWidth() <= normal) {
                        30.sp
                    } else {
                        35.sp
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500
                )
                Spacer(modifier = Modifier.padding(20.dp))
                TextField(
                    value = amountToConvert.value,
                    onValueChange = { newValue -> typeAmount(newValue) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedIndicatorColor = White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = White
                    ),
                    textStyle = TextStyle(
                        fontSize =
                        if (mediaQueryWidth() <= small) {
                            50.sp
                        } else if (mediaQueryWidth() <= normal) {
                            150.sp
                        } else {
                            250.sp
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = White
                    )
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = fromCurrency.value,
                    color = White,
                    fontSize =
                    if (mediaQueryWidth() <= small) {
                        20.sp
                    } else if (mediaQueryWidth() <= normal) {
                        25.sp
                    } else {
                        30.sp
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TopBarColor,
                            BackgroundColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_top_currency),
                    contentDescription = "Arrow top currency",
                    colorFilter = ColorFilter.tint(White),
                    modifier = Modifier
                        .size(
                            if (mediaQueryWidth() <= small) {
                                50.dp
                            } else if (mediaQueryWidth() <= normal) {
                                150.dp
                            } else {
                                250.dp
                            }
                        )
                        .padding(start = 20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            openDialogFrom.value = true
                        }
                )
                val noInternet = stringResource(id = R.string.no_internet)
                Image(
                    painter = painterResource(id = R.drawable.arrow_down),
                    contentDescription = "Arrow bottom",
                    colorFilter = ColorFilter.tint(White),
                    modifier = Modifier
                        .size(
                            if (mediaQueryWidth() <= small) {
                                75.dp
                            } else if (mediaQueryWidth() <= normal) {
                                175.dp
                            } else {
                                275.dp
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (status == ConnectivityObserver.Status.Unavailable) {
                                Toast
                                    .makeText(context, noInternet, Toast.LENGTH_SHORT)
                                    .show()
                                return@clickable
                            }
                            viewModel.makeConversion(
                                status,
                                fromCurrency.value,
                                toCurrency.value,
                                amountToConvert.value
                            )
                        }
                )
                Image(
                    painter = painterResource(id = R.drawable.arrow_bottom_currency),
                    contentDescription = "Arrow bottom currency",
                    colorFilter = ColorFilter.tint(White),
                    modifier = Modifier
                        .size(
                            if (mediaQueryWidth() <= small) {
                                50.dp
                            } else if (mediaQueryWidth() <= normal) {
                                150.dp
                            } else {
                                250.dp
                            }
                        )
                        .padding(end = 20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            openDialogTo.value = true
                        }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(BackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = toCurrency.value,
                    color = White,
                    fontSize =
                    if (mediaQueryWidth() <= small) {
                        25.sp
                    } else if (mediaQueryWidth() <= normal) {
                        30.sp
                    } else {
                        35.sp
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = amountConverted.value,
                    color = White,
                    fontSize =
                    if (mediaQueryWidth() <= small) {
                        50.sp
                    } else if (mediaQueryWidth() <= normal) {
                        150.sp
                    } else {
                        250.sp
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(20.dp))
                Text(
                    text = currencyConverted.value,
                    color = White,
                    fontSize =
                    if (mediaQueryWidth() <= small) {
                        25.sp
                    } else if (mediaQueryWidth() <= normal) {
                        30.sp
                    } else {
                        35.sp
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

private fun typeAmount(newValue: String) {
    val input = newValue.replace(",", "")
    if (input.isEmpty()) {
        amountToConvert.value = "0.00"
    } else {
        val isValid = input.matches(Regex("^[0-9]*[.]?[0-9]{0,2}$"))
        if (isValid) {
            val number = input.toDoubleOrNull()
            if (number != null && number <= 99999999.99) {
                amountToConvert.value = input
            }
        }
    }
}

private fun clearValues() {
    amountToConvert.value = ""
    amountConverted.value = ""
    currencyToBeConvert.value = ""
    currencyConverted.value = ""
    fromCurrency.value = ""
    toCurrency.value = ""
    viewModel.conversion.value = null
}

@Composable
private fun ChangeCurrency() {
    val context = LocalContext.current
    var filterSize: List<String>?
    var selectedOption by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {
            openDialogFrom.value = false
            openDialogTo.value = false
        },
        title = { Text(stringResource(id = R.string.select_option)) },
        text = {
            filterSize = currencies?.currencies?.keys?.filter {
                !it.contains(fromCurrency.value) && !it.contains(toCurrency.value)
            }
            LazyColumn {
                items(filterSize?.size ?: 0) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOption == filterSize?.elementAt(index),
                            onClick = {
                                selectedOption = filterSize?.elementAt(index) ?: ""
                            }
                        )
                        Text(filterSize?.elementAt(index) ?: "")
                    }
                }
            }
        },
        confirmButton = {
            val selectOption = stringResource(id = R.string.select_option)
            val ok = stringResource(id = R.string.ok)
            Button(onClick = {
                if (selectedOption.isEmpty()) {
                    Toast.makeText(context, selectOption, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (openDialogFrom.value) {
                    fromCurrency.value = selectedOption
                } else if (openDialogTo.value) {
                    toCurrency.value = selectedOption
                }
                defineCurrencies()
                viewModel.conversion.value = null
                amountConverted.value = ""
                openDialogFrom.value = false
                openDialogTo.value = false
            }) {
                Text(ok)
            }
        },
        dismissButton = {
            val cancel = stringResource(id = R.string.cancel)
            Button(onClick = {
                openDialogFrom.value = false
                openDialogTo.value = false
            }) {
                Text(cancel)
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(mediaQueryHeight() / 1.5f)
    )
}