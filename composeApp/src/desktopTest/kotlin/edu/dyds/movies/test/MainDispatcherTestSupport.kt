package edu.dyds.movies.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
fun installMainDispatcher(testScheduler: TestCoroutineScheduler): TestDispatcher {
    return StandardTestDispatcher(testScheduler).also(Dispatchers::setMain)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun resetMainDispatcher() {
    runCatching { Dispatchers.resetMain() }
}



