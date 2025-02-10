package com.yannick

import androidx.paging.PagingConfig
import com.yannick.featurehome.data.paging.ChainsPagingSource
import com.yannick.featurehome.data.paging.FriendsPagingSource
import com.yannick.featurehome.data.repositories.ChainsRepositoryImpl
import com.yannick.featurehome.data.repositories.FriendsRepositoryImpl
import com.yannick.featurehome.data.repositories.PAGE_SIZE
import com.yannick.featurehome.domain.repositories.ChainsRepository
import com.yannick.featurehome.domain.repositories.FriendsRepository
import com.yannick.featurehome.presentation.chains.ChainsViewModel
import com.yannick.featurehome.presentation.createchain.CreateChainViewModel
import com.yannick.featurehome.presentation.friends.FriendsViewModel
import com.yannick.featurehome.presentation.home.HomeViewModel
import com.yannick.featurehome.presentation.profile.ProfileViewModel
import com.yannick.featurehome.presentation.searchfriends.SearchFriendViewModel
import com.yannick.featurehome.presentation.shared.PhotosHolder
import com.yannick.featurehome.presentation.viewphotos.ViewPhotosViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { HomeViewModel(authRepository = get()) }
    viewModel {
        ProfileViewModel(
            authRepository = get(),
            firestoreService = get(),
            coroutinesDispatcherProvider = get(),
        )
    }
    single {
        FriendsPagingSource(
            firebaseFirestore = get()
        )
    }
    single {
        ChainsPagingSource(
            firebaseFireStore = get()
        )
    }
    single<FriendsRepository> {
        FriendsRepositoryImpl(
            source = get(),
            config = PagingConfig(
                PAGE_SIZE.toInt(),
            ),
        )
    }
    single<ChainsRepository> {
        ChainsRepositoryImpl(
            source = get(),
            config = PagingConfig(
                PAGE_SIZE.toInt(),
            ),
        )
    }
    viewModel {
        FriendsViewModel(
            friendsRepository = get(),
            firestoreService = get(),
            authRepository = get(),
        )
    }

    single {
        PhotosHolder()
    }

    viewModel {
        ViewPhotosViewModel(holder = get())
    }

    viewModel {
        ChainsViewModel(
            chainsRepository = get(),
            firestoreService = get(),
            coroutinesDispatcherProvider = get(),
            photosHolder = get(),
        )
    }

    viewModel {
        CreateChainViewModel(
            coroutinesDispatcherProvider = get(),
            fireStoreService = get(),
            authRepository = get(),
        )
    }

    viewModel {
        SearchFriendViewModel(
            firestoreService = get(),
            authRepository = get(),
        )
    }
}

val HomeModules = listOf(homeModule)
