package com.example.currency.ui.history

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currency.R
import com.example.currency.core.Black
import com.example.currency.core.mediaQueryWidth
import com.example.currency.core.normal
import com.example.currency.core.small
import com.example.currency.model.history.History
import org.koin.androidx.compose.koinViewModel

private lateinit var viewModel: ShowHistoryViewModel
private var historyData: List<History>? = null

@Composable
fun ShowHistory() {
    viewModel = koinViewModel<ShowHistoryViewModel>()
    viewModel.getHistory()
    GetHistoryResponse()
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
private fun GetHistoryResponse() {
    when {
        viewModel.isSuccess.value -> {
            historyData = viewModel.historyData
            ShowHistoryList()
        }
        viewModel.isError.value -> {
            historyData = emptyList()
            Text(
                text = viewModel.message.value,
                color = Black,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    25.sp
                } else {
                    30.sp
                },
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ShowHistoryList() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .offset(y = (-45).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Image(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = "Clear history",
            colorFilter = ColorFilter.tint(Black),
            modifier = Modifier
                .size(
                    if (mediaQueryWidth() <= small) {
                        30.dp
                    } else if (mediaQueryWidth() <= normal) {
                        40.dp
                    } else {
                        50.dp
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                   viewModel.deleteAllHistory()
                }
        )
    }
    LazyColumn(
        modifier = Modifier
            .wrapContentSize()
            .padding(top = 20.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(historyData?.size ?: 0) {
            Text(
                text = historyData?.get(it)?.date.toString(),
                color = Black,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    20.sp
                } else if (mediaQueryWidth() <= normal) {
                    25.sp
                } else {
                    30.sp
                },
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = "${historyData?.get(it)?.from} to ${historyData?.get(it)?.to}",
                color = Black,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    15.sp
                } else if (mediaQueryWidth() <= normal) {
                    20.sp
                } else {
                    25.sp
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = "${historyData?.get(it)?.fromAmount} to ${historyData?.get(it)?.toAmount}",
                color = Black,
                fontSize =
                if (mediaQueryWidth() <= small) {
                    15.sp
                } else if (mediaQueryWidth() <= normal) {
                    20.sp
                } else {
                    25.sp
                },
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(10.dp))
            HorizontalDivider(
                color = Black,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.padding(10.dp))
        }
    }
}