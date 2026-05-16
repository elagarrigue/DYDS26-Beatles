package edu.dyds.movies.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
fun installMainDispatcher(): TestDispatcher {
    return UnconfinedTestDispatcher().also(Dispatchers::setMain)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun resetMainDispatcher() {
    runCatching { Dispatchers.resetMain() }
}



