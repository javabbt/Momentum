package com.yannick.featurehome.presentation.chains

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.turbine.test
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.core.utils.CoroutinesDispatcherProvider
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.Chain
import com.yannick.domain.utils.Result
import com.yannick.featurehome.domain.repositories.ChainsRepository
import com.yannick.featurehome.presentation.shared.PhotosHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChainsViewModelTest {

    private lateinit var viewModel: ChainsViewModel
    private lateinit var chainsRepository: ChainsRepository
    private lateinit var firestoreService: FirestoreService
    private lateinit var dispatcherProvider: CoroutinesDispatcherProvider
    private lateinit var photosHolder: PhotosHolder

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        chainsRepository = mockk()
        firestoreService = mockk()
        photosHolder = mockk()
        dispatcherProvider = mockk {
            every { io } returns testDispatcher
            every { main } returns testDispatcher
        }

        // Default mocks
        every { photosHolder.photos } returns mutableStateOf(emptyList())
        every { chainsRepository.getChains() } returns flowOf(PagingData.empty())

        viewModel = ChainsViewModel(
            chainsRepository = chainsRepository,
            firestoreService = firestoreService,
            coroutinesDispatcherProvider = dispatcherProvider,
            photosHolder = photosHolder,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setPhotos updates photos holder`() = mainCoroutineRule.runTest {
        val photos = listOf("photo1.jpg", "photo2.jpg")
        val photosFlow = mutableStateOf<List<String>>(emptyList())
        every { photosHolder.photos } returns photosFlow

        viewModel.setPhotos(photos)

        assertEquals(photos, photosFlow.value)
    }

    @Test
    fun `onImageSelected updates state and emits side effect`() = runTest {
        val testImage = "test.jpg"
        val testChain = Chain()

        coEvery {
            firestoreService.uploadImageAndUpdateChain(testImage, testChain)
        } returns Result.Success(Unit)

        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            viewModel.onImageSelected(testImage, testChain)

            assertTrue(awaitItem().isLoading) // Loading starts
            assertFalse(awaitItem().isLoading) // Loading ends
        }

        viewModel.sideEffect.test {
            viewModel.onImageSelected(testImage, testChain)
            assertEquals(SideEffect.UploadDone, awaitItem())
        }

        coVerify {
            firestoreService.uploadImageAndUpdateChain(testImage, testChain)
        }
    }

    @Test
    fun `reloadChains creates new paging data flow`() = runTest {
        val pagingData1 = PagingData.empty<Chain>()
        val pagingData2 = PagingData.empty<Chain>()

        every { chainsRepository.getChains() } returns flowOf(pagingData1)

        var collectedData1: PagingData<Chain>? = null
        viewModel.chains.test { collectedData1 = awaitItem() }

        every { chainsRepository.getChains() } returns flowOf(pagingData2)
        viewModel.reloadChains()

        var collectedData2: PagingData<Chain>? = null
        viewModel.chains.test { collectedData2 = awaitItem() }

        assertTrue(collectedData1 != collectedData2)
    }
}
