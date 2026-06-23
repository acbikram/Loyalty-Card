package com.universalwallet.loyalty.core.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Central registry of icons used across the app. Referencing icons through a
 * single object keeps icon choices consistent and makes a future swap (e.g. to
 * a custom icon set) a one-file change.
 */
object WalletIcons {
    val Add: ImageVector = Icons.Rounded.Add
    val Back: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack
    val Close: ImageVector = Icons.Rounded.Close
    val Card: ImageVector = Icons.Rounded.CreditCard
    val Delete: ImageVector = Icons.Rounded.Delete
    val Edit: ImageVector = Icons.Rounded.Edit
    val Error: ImageVector = Icons.Rounded.Error
    val Home: ImageVector = Icons.Rounded.Home
    val Scan: ImageVector = Icons.Rounded.QrCodeScanner
    val Search: ImageVector = Icons.Rounded.Search
    val Settings: ImageVector = Icons.Rounded.Settings
    val Wallet: ImageVector = Icons.Rounded.Wallet
    val Statistics: ImageVector = Icons.Rounded.BarChart
    val Info: ImageVector = Icons.Rounded.Info
    val Star: ImageVector = Icons.Rounded.Star
    val StarOutline: ImageVector = Icons.Rounded.StarBorder
    val Copy: ImageVector = Icons.Rounded.ContentCopy
    val Share: ImageVector = Icons.Rounded.Share
    val Filter: ImageVector = Icons.Rounded.FilterList
    val Sort: ImageVector = Icons.AutoMirrored.Rounded.Sort
    val Grid: ImageVector = Icons.Rounded.GridView
    val ListView: ImageVector = Icons.AutoMirrored.Rounded.ViewList
    val Language: ImageVector = Icons.Rounded.Language
    val Lock: ImageVector = Icons.Rounded.Lock
    val Palette: ImageVector = Icons.Rounded.Palette
    val Store: ImageVector = Icons.Rounded.Store
    val ChevronRight: ImageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight
}
