
package org.khawaja.fileshare.client.android.viewmodel.content

import org.monora.uprotocol.core.protocol.Client

class SenderClientContentViewModel(val client: Client?) {
    val hasClient = client != null

    val nickname = client?.clientNickname
}
