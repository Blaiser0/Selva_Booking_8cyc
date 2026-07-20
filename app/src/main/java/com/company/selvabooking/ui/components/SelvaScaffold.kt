package com.company.selvabooking.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.company.selvabooking.ui.navigation.DrawerMenuIconButton
import com.company.selvabooking.ui.navigation.LocalDrawerController
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.LogoBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelvaTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    showDrawerMenu: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val drawerController = LocalDrawerController.current

    TopAppBar(
        title = {
            if (subtitle != null) {
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            }
        },
        navigationIcon = {
            when {
                navigationIcon != null -> navigationIcon()
                showDrawerMenu && drawerController?.showMenuButton == true -> {
                    DrawerMenuIconButton(onClick = drawerController.open)
                }
            }
        },
        actions = actions,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LogoBackground,
            titleContentColor = ForestGreen,
            navigationIconContentColor = ForestGreen,
            actionIconContentColor = ForestGreen
        )
    )
}

@Composable
fun SelvaScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        content = content
    )
}
