package org.khawaja.fileshare.client.android.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import org.khawaja.fileshare.client.android.backend.Backend
import javax.inject.Inject


@RequiresApi(api = Build.VERSION_CODES.N)
@AndroidEntryPoint
class TogglingTileService : TileService() {
    @Inject
    lateinit var backend: Backend

    private val observer = Observer<Boolean> { activated ->
        qsTile?.let { tile ->
            tile.state = if (activated) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        backend.tileState.observeForever(observer)
    }

    override fun onStopListening() {
        super.onStopListening()
        backend.tileState.removeObserver(observer)
    }

    override fun onClick() {
        super.onClick()
        backend.takeBgServiceFgThroughTogglingTile()
    }
}