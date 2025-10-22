package com.jesuslcorominas.teamflowmanager.viewmodel.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface TimeTicker {
    val timeFlow: Flow<Long>
}

internal class RealTimeTicker : TimeTicker {
    override val timeFlow: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }
}
