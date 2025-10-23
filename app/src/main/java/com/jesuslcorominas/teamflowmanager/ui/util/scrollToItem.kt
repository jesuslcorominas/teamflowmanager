package com.jesuslcorominas.teamflowmanager.ui.util

import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun scrollToItem(key: String, listState: LazyListState, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        delay(100)

        val headerIndex = listState.layoutInfo
            .visibleItemsInfo
            .find { it.key == key }
            ?.index
        headerIndex?.let {
            listState.animateScrollToItem(it)
        }
    }
}
