package com.example.currency.koin

import com.example.currency.ktor.CurrencyApi
import com.example.currency.ktor.KtorClient
import com.example.currency.model.history.HistoryDatabase
import com.example.currency.model.history.HistoryRepository
import com.example.currency.ui.conversion.CurrencyConverterViewModel
import com.example.currency.ui.currency.CurrenciesScreenViewModel
import com.example.currency.ui.history.ShowHistoryViewModel
import org.koin.android.ext.koin.androidContext
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
    single {
        HistoryDatabase.getDatabase(androidContext())
    }
    single {
        get<HistoryDatabase>().historyDao()
    }
    single {
        HistoryRepository(get())
    }
    viewModel { CurrenciesScreenViewModel(get()) }
    viewModel { CurrencyConverterViewModel(get(), get()) }
    viewModel { ShowHistoryViewModel(get()) }
}