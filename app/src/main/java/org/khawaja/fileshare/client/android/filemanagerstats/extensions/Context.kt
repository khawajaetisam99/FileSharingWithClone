package org.khawaja.fileshare.client.android.filemanagerstats.extensions

import android.content.Context
import com.simplemobiletools.commons.extensions.isPathOnOTG
import com.simplemobiletools.commons.extensions.isPathOnSD
import org.khawaja.fileshare.client.android.filemanagerstats.helpers.Config

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.isPathOnRoot(path: String) = !(path.startsWith(config.internalStoragePath) || isPathOnOTG(path) || (isPathOnSD(path)))
