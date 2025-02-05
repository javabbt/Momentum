package com.yannick.core

import com.yannick.core.theme.ThemePreferences
import com.yannick.core.utils.CoroutinesDispatcherProvider
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { CoroutinesDispatcherProvider(main = Dispatchers.Main, io = Dispatchers.IO, computation = Dispatchers.Default) }
    single { ThemePreferences(androidContext()) }
}

val CoreModules = listOf(coreModule)
