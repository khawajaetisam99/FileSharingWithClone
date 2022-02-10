package org.khawaja.fileshare.client.android.service.web.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.khawaja.fileshare.client.android.backend.Services
import org.khawaja.fileshare.client.android.data.FileRepository
import org.khawaja.fileshare.client.android.data.WebDataRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WebEntryPoint {
    fun fileRepository(): FileRepository

    fun services(): Services

    fun webDataRepository(): WebDataRepository
}
