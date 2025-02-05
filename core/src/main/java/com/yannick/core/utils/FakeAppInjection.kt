package com.yannick.core.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
fun provideFakeCoroutinesDispatcherProvider(
    dispatcher: TestDispatcher?,
): CoroutinesDispatcherProvider {
    val sharedTestCoroutineDispatcher = UnconfinedTestDispatcher()
    return CoroutinesDispatcherProvider(
        dispatcher ?: sharedTestCoroutineDispatcher,
        dispatcher ?: sharedTestCoroutineDispatcher,
        dispatcher ?: sharedTestCoroutineDispatcher,
    )
}
