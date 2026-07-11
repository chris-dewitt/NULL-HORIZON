package com.nullhorizon.pc.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class PcViewModel {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun clear() {
        viewModelScope.cancel()
    }
}
