package com.yannick.data

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.data.datasource.FirebaseAuthDataSource
import com.yannick.data.repositories.AuthRepositoryImpl
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.ProfileStats
import com.yannick.domain.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var authDataSource: FirebaseAuthDataSource
    private lateinit var firestoreService: FirestoreService

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authDataSource = mockk(relaxed = true)
        firestoreService = mockk(relaxed = true)
        repository = AuthRepositoryImpl(authDataSource, firestoreService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle when successful should update profile and return user`() = runBlocking {
        // Arrange
        val credential = mockk<AuthCredential>()
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "http://photo.url"

        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "test-uid"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseUser.photoUrl } returns mockUri

        coEvery { authDataSource.signInWithCredential(credential) } returns firebaseUser
        coEvery {
            firestoreService.updateProfileOfUser(
                any(),
                any(),
            )
        } coAnswers { Result.Success(Unit) }

        // Act
        val result = repository.signInWithGoogle(credential)

        // Assert
        assert(result == firebaseUser)
        coVerify {
            firestoreService.updateProfileOfUser(
                userId = "test-uid",
                profileStats = ProfileStats(
                    friends = 0,
                    chains = 0,
                    streaks = 0,
                    userName = "Test User",
                    profilePicture = "http://photo.url",
                    uid = "test-uid",
                ),
            )
        }
    }

    @Test
    fun `signInWithGoogle when failed should return null`() = runBlocking {
        // Arrange
        val credential = mockk<AuthCredential>()
        coEvery { authDataSource.signInWithCredential(credential) } returns null

        // Act
        val result = repository.signInWithGoogle(credential)

        // Assert
        assert(result == null)
        coVerify(exactly = 0) { firestoreService.updateProfileOfUser(any(), any()) }
    }

    @Test
    fun `getCurrentUser should delegate to authDataSource`() {
        // Arrange
        val mockUser = mockk<FirebaseUser>()
        every { authDataSource.getCurrentUser() } returns mockUser

        // Act
        val result = repository.getCurrentUser()

        // Assert
        assert(result == mockUser)
        verify { authDataSource.getCurrentUser() }
    }

    @Test
    fun `isLoggedIn should return true when user exists`() {
        // Arrange
        every { authDataSource.getCurrentUser() } returns mockk()

        // Act
        val result = repository.isLoggedIn()

        // Assert
        assert(result)
        verify { authDataSource.getCurrentUser() }
    }

    @Test
    fun `isLoggedIn should return false when user is null`() {
        // Arrange
        every { authDataSource.getCurrentUser() } returns null

        // Act
        val result = repository.isLoggedIn()

        // Assert
        assert(!result)
        verify { authDataSource.getCurrentUser() }
    }

    @Test
    fun `logout should delegate to authDataSource`() = runBlocking {
        // Act
        repository.logout()

        // Assert
        verify { authDataSource.signOut() }
    }
}
