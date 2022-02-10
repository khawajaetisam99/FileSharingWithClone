
package org.khawaja.fileshare.client.android.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.khawaja.fileshare.client.android.data.ExtrasRepository
import javax.inject.Inject

@HiltViewModel
class CrashLogViewModel @Inject internal constructor(
    private val extrasRepository: ExtrasRepository,
): ViewModel() {
    val crashLog by lazy {
        extrasRepository.getCrashLog()
    }

    val shouldShowCrashLog
        get() = extrasRepository.shouldShowCrashLog()

    fun clearCrashLog() = extrasRepository.clearCrashLog()
}
