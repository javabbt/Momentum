package com.yannick.momentum

import com.yannick.momentum.ui.AppViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppViewModel(themePreferences = get(), authRepository = get()) }
}

val appModules = listOf(appModule)
