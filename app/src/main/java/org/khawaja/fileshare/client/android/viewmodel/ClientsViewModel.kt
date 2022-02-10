package org.khawaja.fileshare.client.android.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.khawaja.fileshare.client.android.data.ClientRepository
import org.khawaja.fileshare.client.android.data.OnlineClientRepository
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject internal constructor(
    clientRepository: ClientRepository,
    onlineClientRepository: OnlineClientRepository
) : ViewModel() {
    val onlineClients = onlineClientRepository.getOnlineClients()

    val clients = clientRepository.getAll()
}
