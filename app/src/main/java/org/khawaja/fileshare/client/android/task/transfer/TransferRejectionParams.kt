package org.khawaja.fileshare.client.android.task.transfer

import org.khawaja.fileshare.client.android.database.model.Transfer
import org.monora.uprotocol.core.protocol.Client

class TransferRejectionParams(
    val transfer: Transfer,
    val client: Client,
)
