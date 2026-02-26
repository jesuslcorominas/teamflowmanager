package com.jesuslcorominas.teamflowmanager.data.remote.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Phase 1: iOS remote data sources are not yet implemented.
 * Firebase SDK and Ktor Darwin engine will be wired in Phase 2
 * using the GitLive Firebase KMP SDK (dev.gitlive:firebase-*).
 */
actual val dataRemoteModule: Module = module {
    // TODO Phase 2: add GitLive Firebase + Darwin Ktor engine
}
