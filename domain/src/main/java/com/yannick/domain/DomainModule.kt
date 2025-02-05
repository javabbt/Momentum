package com.yannick.domain

import com.yannick.domain.usecases.SignInWithGoogleUseCase
import org.koin.dsl.module

val domainModule = module {
    single {
        SignInWithGoogleUseCase(get())
    }
}

val DomainModules = listOf(domainModule)
