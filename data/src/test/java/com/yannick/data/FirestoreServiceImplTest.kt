package com.yannick.data

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.data.services.CHAINS
import com.yannick.data.services.CHAINS_ROW
import com.yannick.data.services.FOLLOWERS_ROW
import com.yannick.data.services.FRIENDS_ROW
import com.yannick.data.services.FirestoreServiceImpl
import com.yannick.data.services.PROFILE_PICTURE_ROW
import com.yannick.data.services.STREAKS_ROW
import com.yannick.data.services.UID
import com.yannick.data.services.USERS_COLLECTION
import com.yannick.data.services.USER_NAME_ROW
import com.yannick.domain.models.Chain
import com.yannick.domain.models.Friend
import com.yannick.domain.utils.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("Some things still need to be done")
class FirestoreServiceImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var service: FirestoreServiceImpl

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        firestore = mockk()
        storage = mockk()
        service = FirestoreServiceImpl(firestore, storage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProfileOfUser returns success with existing user`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val documentSnapshot = mockk<DocumentSnapshot>()
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        val data = mapOf(
            FRIENDS_ROW to 5,
            CHAINS_ROW to 3,
            STREAKS_ROW to 1,
            USER_NAME_ROW to "Test User",
            PROFILE_PICTURE_ROW to "profile.jpg",
            UID to userId,
        )

        // Setup mocks
        every { firestore.collection(USERS_COLLECTION) } returns mockk {
            every { document(userId) } returns documentReference
        }
        every { documentReference.get() } returns task
        coEvery { task.await() } returns documentSnapshot // Uncommented and fixed this line
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.data } returns data

        // When
        val result = service.getProfileOfUser(userId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Test User", (result as Result.Success).output.userName)
        assertEquals(5, result.output.friends)
        verify { firestore.collection(USERS_COLLECTION) }
    }

    @Test
    fun `uploadImageAndUpdateChain returns success`() = mainCoroutineRule.runTest {
        // Given
        val photoUrl = "content://test/photo.jpg"
        val chain = Chain(
            id = "chainId",
            createdBy = "userId",
            photos = listOf(),
        )

        val storageRef = mockk<StorageReference>()
        val imagesRef = mockk<StorageReference>()
        val spaceRef = mockk<StorageReference>()
        val uploadTask = mockk<UploadTask.TaskSnapshot>()
        val urlTask = mockk<Task<Uri>>()
        val collectionReference = mockk<CollectionReference>()
        val documentReference = mockk<DocumentReference>()
        val updateTask = mockk<Task<Void>>()

        // Setup mocks
        every { storage.reference } returns storageRef
        every { storageRef.child("images") } returns imagesRef
        every { imagesRef.child(any()) } returns spaceRef
        every { spaceRef.putFile(any()) } returns mockk {
            coEvery { await() } returns uploadTask
        }
        every { uploadTask.storage } returns spaceRef
        every { spaceRef.downloadUrl } returns urlTask
        coEvery { urlTask.await() } returns mockk {
            every { toString() } returns "https://firebasestorage.url/photo.jpg"
        }
        every { firestore.collection(CHAINS) } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference
        every { documentReference.update(any<String>(), any()) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        // When
        val result = service.uploadImageAndUpdateChain(photoUrl, chain)

        // Then
        assertTrue(result is Result.Success)
        verify {
            storage.reference
            spaceRef.putFile(any())
            firestore.collection(CHAINS)
        }
    }

    @Test
    fun `getFriendsOfUser returns success`() = runBlocking {
        // Given
        val userId = "testUserId"
        val querySnapshot = mockk<QuerySnapshot>(relaxed = true, relaxUnitFun = true)
        val documentReference = mockk<DocumentReference>(relaxed = true, relaxUnitFun = true)
        val collectionReference = mockk<CollectionReference>(relaxed = true, relaxUnitFun = true)
        val task = mockk<Task<QuerySnapshot>>(relaxed = true, relaxUnitFun = true)
        val friend = Friend("friendId", "friendName", "friendPhoto")
        val documentSnapshot = mockk<DocumentSnapshot>(relaxed = true, relaxUnitFun = true)

        // Setup mocks
        every { firestore.collection(FOLLOWERS_ROW) } returns collectionReference
        every { collectionReference.document(userId) } returns documentReference
        every { documentReference.collection(FOLLOWERS_ROW) } returns collectionReference
        every { collectionReference.get() } returns task
        coEvery { task.await() } returns querySnapshot
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.toObject(Friend::class.java) } returns friend

        // When
        val result = service.getFriendsOfUser(userId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).output.size)
        assertEquals("friendId", result.output[0].uid)
        verify {
            firestore.collection(FOLLOWERS_ROW)
            documentSnapshot.toObject(Friend::class.java)
        }
    }
}
