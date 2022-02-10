

package org.khawaja.fileshare.client.android.data

import org.khawaja.fileshare.client.android.util.NsdDaemon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineClientRepository @Inject constructor(
    private val nsdDaemon: NsdDaemon
){
    fun getOnlineClients() = nsdDaemon.onlineClients
}