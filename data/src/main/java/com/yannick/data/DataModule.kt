package com.yannick.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.yannick.data.datasource.FirebaseAuthDataSource
import com.yannick.data.datasource.FirebaseAuthDataSourceImpl
import com.yannick.data.repositories.AuthRepositoryImpl
import com.yannick.data.services.FirestoreService
import com.yannick.data.services.FirestoreServiceImpl
import com.yannick.domain.repositories.AuthRepository
import org.koin.dsl.module

val dataModule = module {
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single<FirebaseStorage> { Firebase.storage }
    single<FirebaseAuthDataSource> { FirebaseAuthDataSourceImpl(firebaseAuth = get()) }
    single<AuthRepository> { AuthRepositoryImpl(authDataSource = get(), firestoreService = get()) }
    single { FirebaseFirestore.getInstance() }
    single<FirestoreService> { FirestoreServiceImpl(firestore = get(), firebaseStorage = get()) }
}

val DataModules = listOf(dataModule)
