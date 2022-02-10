package org.khawaja.fileshare.client.android.activity

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import org.khawaja.fileshare.client.android.R

fun FragmentActivity.navController(id: Int): NavController {
    return (supportFragmentManager.findFragmentById(id) as NavHostFragment).navController
}
