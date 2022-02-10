package org.khawaja.fileshare.client.android.util

import org.khawaja.fileshare.client.android.database.model.UClient
import org.khawaja.fileshare.client.android.database.model.UClientAddress
import org.khawaja.fileshare.client.android.model.ClientRoute
import org.monora.uprotocol.core.CommunicationBridge

val CommunicationBridge.clientRoute: ClientRoute
    get() {
        val client = this.remoteClient
        val address = this.remoteClientAddress

        check(client is UClient) {
            "Unsupported client wrapper class: ${client.javaClass.simpleName}"
        }

        check(address is UClientAddress) {
            "Unsupported client address wrapper class: ${address.javaClass.simpleName}"
        }

        return ClientRoute(client, address)
    }
