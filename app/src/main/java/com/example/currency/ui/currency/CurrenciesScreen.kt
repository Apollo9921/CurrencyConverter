package com.example.currency.ui.currency

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
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
import com.example.currency.core.TextColor
import com.example.currency.core.TopBarColor
import com.example.currency.core.mediaQueryHeight
import com.example.currency.core.mediaQueryWidth
import com.example.currency.core.normal
import com.example.currency.core.small
import com.example.currency.core.status
import com.example.currency.main.keepSplashOpened
import com.example.currency.model.currencyList.Currency
import com.example.currency.model.rates.Rates
import com.example.currency.navigation.Destination
import com.example.currency.network.ConnectivityObserver
import com.example.currency.network.NetworkConnectivityObserver
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat

private lateinit var connectivityObserver: ConnectivityObserver
private var applicationContext: Context? = null
private lateinit var viewModel: CurrenciesScreenViewModel
private var rate = mutableStateOf<Rates?>(null)
private var rateValue = derivedStateOf { rate }
private var allCurrencyRates: Currency? = null
private var openDialog = mutableStateOf(false)
private var isConnected by mutableStateOf(false)
private var searchByCurrency = mutableStateOf("")
private var searchByCurrencyValue = derivedStateOf { searchByCurrency }

@Composable
fun CurrenciesScreen(navController: NavHostController) {
    applicationContext = LocalContext.current.applicationContext
    connectivityObserver = NetworkConnectivityObserver(applicationContext ?: return)
    status = connectivityObserver.observe().collectAsState(
        initial = ConnectivityObserver.Status.Unavailable
    ).value
    viewModel = koinViewModel<CurrenciesScreenViewModel>()
    if (status == ConnectivityObserver.Status.Available && !isConnected) {
        viewModel.getAllCurrencies(status)
        isConnected = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CurrenciesScreenTopBar() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            GetCurrencyRates(navController)
        }
    }

    keepSplashOpened = false
}

@Composable
private fun GetCurrencyRates(navController: NavHostController) {
    when {
        viewModel.isLoading.value -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor),
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
                    .background(BackgroundColor),
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
            rate.value = viewModel.rates.value
            allCurrencyRates = viewModel.currencies.value
            RatesList(navController)
        }
    }
}

@Composable
private fun CurrenciesScreenTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarColor)
            .padding(top = 60.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${rateValue.value.value?.base ?: "EUR"} ${rateValue.value.value?.amount ?: "0.0"}",
                color = White,
                fontWeight = FontWeight.W400,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    24.sp
                } else {
                    28.sp
                }
            )
            Button(onClick = { openDialog.value = true }) {
                Text(stringResource(id = R.string.change_currency))
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        TextField(
            value = searchByCurrency.value,
            onValueChange = {
                if (it.length <= 10) {
                    searchByCurrency.value = it
                }
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_by_currency),
                    color = White
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    15.sp
                } else if (mediaQueryWidth() <= normal) {
                    20.sp
                } else {
                    25.sp
                },
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Start,
                color = White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}

@Composable
private fun ChangeCurrency() {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        title = { Text(stringResource(id = R.string.select_option)) },
        text = {
            LazyColumn {
                items(allCurrencyRates?.currencies?.size ?: 0) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOption == allCurrencyRates?.currencies?.keys?.elementAt(
                                index
                            ),
                            onClick = {
                                selectedOption =
                                    allCurrencyRates?.currencies?.keys?.elementAt(index) ?: ""
                            }
                        )
                        Text(allCurrencyRates?.currencies?.keys?.elementAt(index) ?: "")
                    }
                }
            }
        },
        confirmButton = {
            val selectOption = stringResource(id = R.string.select_option)
            val noInternetConnection = stringResource(id = R.string.no_internet)
            val ok = stringResource(id = R.string.ok)
            Button(onClick = {
                if (selectedOption.isEmpty()) {
                    Toast.makeText(context, selectOption, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (status == ConnectivityObserver.Status.Unavailable) {
                    Toast.makeText(context, noInternetConnection, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                searchByCurrency.value = ""
                openDialog.value = false
                viewModel.isLoading.value = true
                viewModel.isSuccess.value = false
                allCurrencyRates?.currencies?.remove(selectedOption)
                viewModel.getAllCurrencies(status, selectedOption)
            }) {
                Text(ok)
            }
        },
        dismissButton = {
            val cancel = stringResource(id = R.string.cancel)
            Button(onClick = { openDialog.value = false }) {
                Text(cancel)
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(mediaQueryHeight() / 1.5f)
    )
}

@Composable
private fun RatesList(navController: NavHostController) {
    if (openDialog.value) {
        ChangeCurrency()
    }
    var sortedCurrencies: Map<String, String>? = null
    if (searchByCurrencyValue.value.value.isNotEmpty()) {
        val filteredCurrencies =
            allCurrencyRates?.currencies?.filterKeys {
                it.contains(
                    searchByCurrencyValue.value.value,
                    ignoreCase = true
                )
            }
        sortedCurrencies = filteredCurrencies?.toSortedMap()
    }
    val state = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = state,
        verticalItemSpacing = 15.dp,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(25.dp)
    ) {
        if (sortedCurrencies != null) {
            items(sortedCurrencies.size) {
                FilterCurrencyItem(it, sortedCurrencies, navController)
            }
        } else {
            items(allCurrencyRates?.currencies?.size ?: 0) { index ->
                CurrencyItem(index, navController)
            }
        }
    }
}

@Composable
private fun FilterCurrencyItem(
    index: Int,
    sortedCurrencies: Map<String, String>,
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = White,
                shape = RoundedCornerShape(13.dp)
            )
            .border(
                border = BorderStroke(3.dp, Black),
                shape = RoundedCornerShape(13.dp)
            )
            .padding(20.dp)
            .clickable {
                searchByCurrency.value = ""
                navController.navigate(
                    Destination.E2(
                        from = rateValue.value.value?.base ?: "EUR",
                        to = sortedCurrencies.keys.elementAt(index),
                        amount = rateValue.value.value?.amount.toString()
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = sortedCurrencies.keys.elementAt(index),
                color = TextColor,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    24.sp
                } else {
                    28.sp
                },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W500
            )
            Spacer(modifier = Modifier.padding(10.dp))
            val rate = sortedCurrencies.values.elementAt(index).toString().toDouble()
            val rateValue = DecimalFormat("#.##").format(rate)
            Text(
                text = rateValue,
                color = TextColor,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    24.sp
                } else {
                    28.sp
                },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CurrencyItem(index: Int, navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = White,
                shape = RoundedCornerShape(13.dp)
            )
            .border(
                border = BorderStroke(3.dp, Black),
                shape = RoundedCornerShape(13.dp)
            )
            .padding(20.dp)
            .clickable {
                searchByCurrency.value = ""
                navController.navigate(
                    Destination.E2(
                        from = rateValue.value.value?.base ?: "EUR",
                        to = allCurrencyRates?.currencies?.keys?.elementAt(index) ?: "",
                        amount = rateValue.value.value?.amount.toString()
                    )
                ) {
                    launchSingleTop = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = allCurrencyRates?.currencies?.keys?.elementAt(index) ?: "",
                color = TextColor,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    24.sp
                } else {
                    28.sp
                },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W500
            )
            Spacer(modifier = Modifier.padding(10.dp))
            val rate =
                allCurrencyRates?.currencies?.values?.elementAt(index).toString()
                    .toDouble()
            val rateValue = DecimalFormat("#.##").format(rate)
            Text(
                text = rateValue,
                color = TextColor,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    24.sp
                } else {
                    28.sp
                },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}