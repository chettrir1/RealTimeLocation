package com.raju.realtimelocation.di

import com.raju.realtimelocation.core.data.networking.HttpClientFactory
import com.raju.realtimelocation.main.MainViewModel
import com.raju.realtimelocation.main.data.networking.RemoteMainDataSource
import com.raju.realtimelocation.main.domain.MainDataSource
import io.ktor.client.engine.cio.CIO
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    single { HttpClientFactory.create(CIO.create()) }

    singleOf(::RemoteMainDataSource).bind<MainDataSource>()

    viewModel { MainViewModel(get()) }

}