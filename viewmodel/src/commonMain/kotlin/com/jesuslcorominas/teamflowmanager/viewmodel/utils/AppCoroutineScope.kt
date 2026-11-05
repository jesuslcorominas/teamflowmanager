package com.jesuslcorominas.teamflowmanager.viewmodel.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * A scope that lives as long as the process, for work a screen starts but must not own.
 *
 * `viewModelScope` is the right default and should stay the default: work tied to what the user is
 * looking at ought to stop when they look away. This is for the exception — work that is triggered
 * from a screen only because that is where the trigger happens to be, and whose correctness does
 * not survive being cut in half.
 *
 * The splash is the case it exists for. Every route out of it pops the splash off the back stack,
 * which destroys its `NavBackStackEntry` and cancels its `viewModelScope`, and that pop is driven
 * by the very state emission the work waits for. Anything launched there races a scope that is
 * already dying, and loses at its first suspension point.
 *
 * [SupervisorJob] so one failed job never takes the others down with it, and [Dispatchers.Default]
 * because nothing here touches the UI.
 */
interface AppCoroutineScope : CoroutineScope

fun createAppCoroutineScope(): AppCoroutineScope = RealAppCoroutineScope()

internal class RealAppCoroutineScope :
    AppCoroutineScope,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default)
