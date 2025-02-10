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
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

    @BeforeEach
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

    @AfterEach
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
        // Create test chains
        val chain1 = Chain(id = "1")
        val chain2 = Chain(id = "2")

        viewModel.chains.test {
            // Skip initial emission from setup
            skipItems(1)

            every { chainsRepository.getChains() } returns flowOf(PagingData.from(listOf(chain1)))
            viewModel.reloadChains()
            awaitItem()

            every { chainsRepository.getChains() } returns flowOf(PagingData.from(listOf(chain2)))
            viewModel.reloadChains()
            awaitItem()

            ensureAllEventsConsumed()
        }
        coVerify(exactly = 2) { chainsRepository.getChains() }
    }
}
