package com.yannick.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.data.datasource.FirebaseAuthDataSourceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthDataSourceImplTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAuthDataSource: FirebaseAuthDataSourceImpl

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        firebaseAuth = mockk(relaxed = true)
        firebaseAuthDataSource = FirebaseAuthDataSourceImpl(firebaseAuth)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithCredential returns null when exception occurs`() = mainCoroutineRule.runTest {
        // Arrange
        val credential = mockk<AuthCredential>()
        coEvery { firebaseAuth.signInWithCredential(credential) } throws Exception("Sign in failed")

        // Act
        val result = firebaseAuthDataSource.signInWithCredential(credential)

        // Assert
        assertNull(result)
        coVerify(exactly = 1) { firebaseAuth.signInWithCredential(credential) }
    }

    @Test
    fun `getCurrentUser returns current user from FirebaseAuth`() {
        // Arrange
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseAuth.currentUser } returns firebaseUser

        // Act
        val result = firebaseAuthDataSource.getCurrentUser()

        // Assert
        assertEquals(firebaseUser, result)
        verify(exactly = 1) { firebaseAuth.currentUser }
    }

    @Test
    fun `getCurrentUser returns null when no user is signed in`() {
        // Arrange
        every { firebaseAuth.currentUser } returns null

        // Act
        val result = firebaseAuthDataSource.getCurrentUser()

        // Assert
        assertNull(result)
        verify(exactly = 1) { firebaseAuth.currentUser }
    }

    @Test
    fun `signOut calls FirebaseAuth signOut`() {
        // Act
        firebaseAuthDataSource.signOut()

        // Assert
        verify(exactly = 1) { firebaseAuth.signOut() }
    }
}
