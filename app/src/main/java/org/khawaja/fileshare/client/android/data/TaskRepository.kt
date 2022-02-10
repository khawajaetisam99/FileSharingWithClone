
package org.khawaja.fileshare.client.android.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.backend.Backend
import org.khawaja.fileshare.client.android.backend.TaskFilter
import org.khawaja.fileshare.client.android.backend.TaskRegistry
import org.khawaja.fileshare.client.android.backend.TaskSubscriber
import org.khawaja.fileshare.client.android.protocol.isIncoming
import org.khawaja.fileshare.client.android.service.backgroundservice.Task
import org.khawaja.fileshare.client.android.task.transfer.TransferParams
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val backend: Backend,
) {
    fun cancelMatching(filter: TaskFilter) = backend.cancelMatchingTasks(filter)

    fun contains(filter: TaskFilter) = backend.hasTask(filter)

    fun containsAny() = backend.hasTasks()

    fun <T : Any> register(name: String, params: T, registry: TaskRegistry<T>): Task = backend.register(
        name, params, registry
    )

    fun register(
        params: TransferParams,
        taskRegistry: TaskRegistry<TransferParams>
    ) = register(
        context.getString(if (params.transfer.direction.isIncoming) R.string.receiving else R.string.sending),
        params,
        taskRegistry
    ).also {
        params.job = it.job
    }

    fun <T : Any> subscribeToTask(condition: TaskSubscriber<T>) = backend.subscribeToTask(condition)

    fun <T : Any> subscribeToTasks(condition: TaskSubscriber<T>) = backend.subscribeToTasks(condition)
}
