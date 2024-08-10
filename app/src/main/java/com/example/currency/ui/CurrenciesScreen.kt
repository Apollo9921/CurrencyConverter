package com.example.currency.ui

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.currency.core.BackgroundColor
import com.example.currency.core.Black
import com.example.currency.core.TextColor
import com.example.currency.core.TopBarColor
import com.example.currency.core.mediaQueryHeight
import com.example.currency.core.mediaQueryWidth
import com.example.currency.core.normal
import com.example.currency.core.small
import com.example.currency.main.keepSplashOpened
import com.example.currency.model.rates.RateList
import com.example.currency.model.rates.Rates
import com.example.currency.network.ConnectivityObserver
import com.example.currency.network.NetworkConnectivityObserver
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat

private lateinit var connectivityObserver: ConnectivityObserver
private lateinit var status: ConnectivityObserver.Status
private var applicationContext: Context? = null
private lateinit var viewModel: CurrenciesScreenViewModel
private var rate = mutableStateOf<Rates?>(null)
private var rateValue = derivedStateOf { rate }
private var allCurrencyRates: ArrayList<RateList> = arrayListOf()
private var openDialog = mutableStateOf(false)
private var selectedOption = mutableStateOf("")

@Composable
fun CurrenciesScreen(navController: NavHostController) {
    applicationContext = LocalContext.current.applicationContext
    connectivityObserver = NetworkConnectivityObserver(applicationContext ?: return)
    status = connectivityObserver.observe().collectAsState(
        initial = ConnectivityObserver.Status.Unavailable
    ).value
    viewModel = koinViewModel<CurrenciesScreenViewModel>()

    if (allCurrencyRates.isEmpty())
        viewModel.getAllCurrencies(status, selectedOption.value.ifEmpty { "EUR" })

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
            GetCurrencyRates()
        }
    }

    keepSplashOpened = false
}

@Composable
private fun GetCurrencyRates() {
    when {
        viewModel.isLoading.value -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Black
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
            if (allCurrencyRates.isEmpty()) {
                val jsonString = Json.encodeToString(rate.value?.rates)
                val jsonElement = Json.parseToJsonElement(jsonString)
                val map = jsonElement.jsonObject.toMap()
                val hashMap = HashMap(map)
                hashMap.forEach { (key, value) ->
                    val rateValue = (value as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                    allCurrencyRates.add(RateList(key, rateValue))
                }
                allCurrencyRates.sortBy { it.base }
            }
            RatesList()
        }
    }
}

@Composable
private fun CurrenciesScreenTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarColor)
            .padding(top = 60.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
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
            Text("Change Currency")
        }
    }
}

@Composable
private fun ChangeCurrency() {
    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        title = { Text("Choose an option") },
        text = {
            LazyColumn {
                items(allCurrencyRates.size) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOption.value == allCurrencyRates[index].base,
                            onClick = { selectedOption.value = allCurrencyRates[index].base }
                        )
                        Text(allCurrencyRates[index].base)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                openDialog.value = false
                viewModel.isLoading.value = true
                viewModel.isSuccess.value = false
                allCurrencyRates.clear()
                viewModel.getAllCurrencies(status, selectedOption.value)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = { openDialog.value = false }) {
                Text("Cancel")
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(mediaQueryHeight() / 1.5f)
    )
}

@Composable
private fun RatesList() {
    if (openDialog.value) {
        ChangeCurrency()
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
        items(allCurrencyRates.size) { index ->
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
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = allCurrencyRates[index].base,
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
                    val rate = allCurrencyRates[index].rate
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
    }
}