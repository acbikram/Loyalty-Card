package com.universalwallet.loyalty.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.universalwallet.loyalty.core.theme.Dimensions
import com.universalwallet.loyalty.core.theme.WalletIcons

/**
 * A single-line search input with leading search icon and a clear button that
 * appears once there is text. Used for the fast card-search workflow.
 */
@Composable
fun WalletSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search cards",
    onClear: () -> Unit = { onQueryChange("") },
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimensions.searchBarHeight)
            .semantics { contentDescription = placeholder },
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(WalletIcons.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(WalletIcons.Close, contentDescription = "Clear search")
                }
            }
        },
    )
}
