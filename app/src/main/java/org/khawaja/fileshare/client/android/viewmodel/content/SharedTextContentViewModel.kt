
package org.khawaja.fileshare.client.android.viewmodel.content

import org.khawaja.fileshare.client.android.database.model.SharedText

class SharedTextContentViewModel(sharedText: SharedText) {
    val text = sharedText.text

    val dateCreated = sharedText.created
}
