package com.yannick

import androidx.paging.PagingConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yannick.data.services.FOLLOWERS_ROW
import com.yannick.data.services.USER_CHAINS
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
            queryFriends = FirebaseFirestore.getInstance().collection(FOLLOWERS_ROW)
                .document(FirebaseAuth.getInstance().uid!!)
                .collection(FOLLOWERS_ROW)
                .orderBy("userName", Query.Direction.ASCENDING)
                .limit(PAGE_SIZE),
        )
    }
    single {
        ChainsPagingSource(
            queryChains = FirebaseFirestore.getInstance().collection(USER_CHAINS)
                .document(FirebaseAuth.getInstance().uid!!)
                .collection(USER_CHAINS)
                .orderBy("deadline", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE),
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
