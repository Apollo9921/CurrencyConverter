package com.example.currency.koin

import com.example.currency.ktor.CurrencyApi
import com.example.currency.ktor.KtorClient
import com.example.currency.ui.CurrenciesScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        KtorClient.httpClient
    }
    single {
        CurrencyApi()
    }
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(get())
    }
    viewModel {
        CurrenciesScreenViewModel(get())
    }
}