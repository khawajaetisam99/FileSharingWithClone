package org.khawaja.fileshare.client.android.content

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.MediaStore.Audio.*
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import android.annotation.TargetApi
import java.io.File


class AudioStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getAlbums(): List<Album> {
        return try {
            loadAlbums(
                context.contentResolver.query(
                    Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        Albums._ID,
                        Albums.ARTIST,
                        Albums.LAST_YEAR,
                        Albums.ALBUM,
                    ),
                    null,
                    null,
                    Albums.ALBUM
                )
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAlbums(artist: Artist): List<Album> {
        return try {
            loadAlbums(
                context.contentResolver.query(
                    Artists.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL, artist.id),
                    arrayOf(
                        Albums._ID,
                        Albums.ARTIST,
                        Albums.LAST_YEAR,
                        Albums.ALBUM,
                    ),
                    null,
                    null,
                    Albums.ALBUM
                )
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getArtists(): List<Artist> {
        try {
            context.contentResolver.query(
                Artists.EXTERNAL_CONTENT_URI,
                arrayOf(
                    Artists._ID,
                    Artists.ARTIST,
                    Artists.NUMBER_OF_ALBUMS,
                ),
                null,
                null,
                Albums.ARTIST
            )?.use {
                if (it.moveToFirst()) {
                    val idIndex: Int = it.getColumnIndex(Artists._ID)
                    val artistIndex: Int = it.getColumnIndex(Artists.ARTIST)
                    val numberOfAlbumsIndex: Int = it.getColumnIndex(Artists.NUMBER_OF_ALBUMS)

                    val result = ArrayList<Artist>(it.count)

                    do {
                        try {
                            val id = it.getLong(idIndex)

                            result.add(
                                Artist(
                                    id,
                                    it.getString(artistIndex),
                                    it.getInt(numberOfAlbumsIndex),
                                    ContentUris.withAppendedId(Artists.EXTERNAL_CONTENT_URI, id),
                                )
                            )
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    } while (it.moveToNext())

                    return result
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return emptyList()
    }



    val mimeType = "application/pdf"

    fun getPdfList(): ArrayList<String>? {
        val pdfList: ArrayList<String> = ArrayList()
        val projection=arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE)
        val sortOrder: String = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection: String = MediaStore.Files.FileColumns.MIME_TYPE + " =?"
        val mimeType: String? = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)
        val collection:Uri=if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder).use { cursor ->
            val idColumn= cursor!!.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            val nameColumn=cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            if(cursor!=null) {
                while (cursor.moveToFirst()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                }
            }
        }
        return pdfList
    }


    fun Search_Dir(dir: File) {
        val pdfPattern = ".pdf"
        val FileList = dir.listFiles()
        if (FileList != null) {
            for (i in FileList.indices) {
                if (FileList[i].isDirectory) {
                    Search_Dir(FileList[i])
                } else {
                    if (FileList[i].name.endsWith(pdfPattern)) {
                        //here you have that file.
                    }
                }
            }
        }
    }







    fun getSongs(selection: String, selectionArgs: Array<String>): List<Song> {
        try {
            context.contentResolver.query(
                Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    Media._ID,
                    Media.ARTIST,
                    Media.ALBUM,
                    Media.ALBUM_ID,
                    Media.TITLE,
                    Media.DISPLAY_NAME,
                    Media.MIME_TYPE,
                    Media.SIZE,
                    Media.DATE_MODIFIED
                ),
                selection,
                selectionArgs,
                Media.TITLE
            )?.use {
                if (it.moveToFirst()) {
                    val idIndex: Int = it.getColumnIndex(Media._ID)
                    val artistIndex: Int = it.getColumnIndex(Media.ARTIST)
                    val albumIndex: Int = it.getColumnIndex(Media.ALBUM)
                    val albumIdIndex = it.getColumnIndex(Media.ALBUM_ID)
                    val titleIndex: Int = it.getColumnIndex(Media.TITLE)
                    val displayNameIndex: Int = it.getColumnIndex(Media.DISPLAY_NAME)
                    val mimeTypeIndex: Int = it.getColumnIndex(Media.MIME_TYPE)
                    val sizeIndex: Int = it.getColumnIndex(Media.SIZE)
                    val dateModifiedIndex: Int = it.getColumnIndex(Media.DATE_MODIFIED)

                    val result = ArrayList<Song>(it.count)

                    do {
                        try {
                            val id = it.getLong(idIndex)

                            result.add(
                                Song(
                                    id,
                                    it.getString(artistIndex),
                                    it.getString(albumIndex),
                                    it.getString(titleIndex),
                                    it.getString(displayNameIndex),
                                    it.getString(mimeTypeIndex),
                                    it.getLong(sizeIndex),
                                    it.getLong(dateModifiedIndex),
                                    ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id),
                                    ContentUris.withAppendedId(Albums.EXTERNAL_CONTENT_URI, it.getLong(albumIdIndex))
                                )
                            )
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    } while (it.moveToNext())

                    return result
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return emptyList()
    }

    private fun loadAlbums(cursor: Cursor?): List<Album> {
        if (cursor != null && cursor.moveToFirst()) {
            val idIndex: Int = cursor.getColumnIndex(Albums._ID)
            val artistIndex: Int = cursor.getColumnIndex(Albums.ARTIST)
            val albumIndex: Int = cursor.getColumnIndex(Albums.ALBUM)
            val lastYearIndex: Int = cursor.getColumnIndex(Albums.LAST_YEAR)

            val result = ArrayList<Album>(cursor.count)

            do {
                try {
                    val id = cursor.getLong(idIndex)

                    result.add(
                        Album(
                            id,
                            cursor.getString(artistIndex),
                            cursor.getString(albumIndex),
                            cursor.getInt(lastYearIndex),
                            ContentUris.withAppendedId(Albums.EXTERNAL_CONTENT_URI, id),
                        )
                    )
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            } while (cursor.moveToNext())

            return result
        }

        return emptyList()
    }
}





@Parcelize
data class Album(
    val id: Long,
    val artist: String,
    val title: String,
    val year: Int,
    val uri: Uri,
) : Parcelable

@Parcelize
data class Artist(
    val id: Long,
    val name: String,
    val numberOfAlbums: Int,
    val uri: Uri,
) : Parcelable

@Parcelize
data class Song(
    val id: Long,
    val artist: String,
    val album: String,
    val title: String,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val dateModified: Long,
    val uri: Uri,
    val albumUri: Uri,
) : Parcelable {
    @IgnoredOnParcel
    var isSelected = false

    override fun equals(other: Any?): Boolean {
        return other is Song && uri == other.uri
    }
}


@Parcelize
data class Pdf(
    val id: Long,
    val name: String,
    val numberOfAlbums: Int,
    val uri: Uri,
) : Parcelable
