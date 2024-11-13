package org.codeslu.wifiextender.app.di

import org.codeslu.wifiextender.ui.main.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module{
    viewModelOf(::MainViewModel)
}