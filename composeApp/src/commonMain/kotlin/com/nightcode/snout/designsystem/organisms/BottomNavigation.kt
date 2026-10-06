package com.nightcode.snout.designsystem.organisms

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.nightcode.snout.designsystem.tokens.SnoutTheme

/** One destination of the [BottomNavigation]. */
internal data class BottomNavigationItem(val label: String, val icon: Painter)

/**
 * Five destinations on `surfaceContainer` with a tonal `secondaryContainer` indicator pill. The active icon is
 * `onSecondaryContainer`, never `primary`; labels are always visible.
 */
@Composable
internal fun BottomNavigation(
    items: List<BottomNavigationItem>,
    selectedIndex: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier, containerColor = SnoutTheme.colors.surfaceContainer) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = { Icon(painter = item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = SnoutTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SnoutTheme.colors.secondaryContainer,
                    selectedIconColor = SnoutTheme.colors.onSecondaryContainer,
                    selectedTextColor = SnoutTheme.colors.onSurface,
                    unselectedIconColor = SnoutTheme.colors.onSurfaceVariant,
                    unselectedTextColor = SnoutTheme.colors.onSurfaceVariant,
                ),
            )
        }
    }
}
