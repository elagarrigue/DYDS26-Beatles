package edu.dyds.movies.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
fun installMainDispatcher() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
}

@OptIn(ExperimentalCoroutinesApi::class)
fun resetMainDispatcher() {
    runCatching { Dispatchers.resetMain() }
}



