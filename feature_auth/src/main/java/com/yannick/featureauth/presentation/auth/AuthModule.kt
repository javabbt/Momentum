package com.yannick.featureauth.presentation.auth

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val AuthModule = module {
    viewModel {
        AuthViewModel(
            signInWithGoogleUseCase = get(),
            coroutinesDispatcherProvider = get(),
        )
    }
}
