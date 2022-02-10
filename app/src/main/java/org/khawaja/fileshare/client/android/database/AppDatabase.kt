
package org.khawaja.fileshare.client.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.khawaja.fileshare.client.android.database.model.SafFolder
import org.khawaja.fileshare.client.android.database.model.SharedText
import org.khawaja.fileshare.client.android.database.model.Transfer
import org.khawaja.fileshare.client.android.database.model.TransferDetail
import org.khawaja.fileshare.client.android.database.model.UClient
import org.khawaja.fileshare.client.android.database.model.UClientAddress
import org.khawaja.fileshare.client.android.database.model.UTransferItem
import org.khawaja.fileshare.client.android.database.model.WebClient
import org.khawaja.fileshare.client.android.database.model.WebTransfer

@Database(
    entities = [
        UClient::class,
        UClientAddress::class,
        UTransferItem::class,
        SafFolder::class,
        SharedText::class,
        Transfer::class,
        WebClient::class,
        WebTransfer::class,
    ],
    views = [TransferDetail::class],
    version = 1
)
@TypeConverters(
    ClientTypeConverter::class, IOTypeConverter::class, DirectionTypeConverter::class,
    TransferItemStateTypeConverter::class, WebTransferTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao

    abstract fun clientAddressDao(): ClientAddressDao

    abstract fun safFolderDao(): SafFolderDao

    abstract fun sharedTextDao(): SharedTextDao

    abstract fun transferDao(): TransferDao

    abstract fun transferItemDao(): TransferItemDao

    abstract fun webClientDao(): WebClientDao

    abstract fun webTransferDao(): WebTransferDao



}


