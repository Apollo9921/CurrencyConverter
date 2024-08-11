package com.example.currency.ui.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currency.core.Black
import com.example.currency.core.mediaQueryWidth
import com.example.currency.core.normal
import com.example.currency.core.small


@Composable
fun ShowHistory() {
    LazyColumn(
        modifier = Modifier
            .wrapContentSize()
            .padding(top = 20.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(15) {
            Text(
                text = "11/08/2024",
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
                text = "EUR to USD",
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
                text = "1.0 to 1.97",
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