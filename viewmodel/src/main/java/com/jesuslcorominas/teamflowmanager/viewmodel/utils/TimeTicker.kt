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
            val now = System.currentTimeMillis()

            val rounded = (now / 1000) * 1000

            emit(rounded)

            delay(1000)
        }
    }
}
