package com.myth.atomic.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.myth.atomic.presentation.theme.AtomicTheme

/** Height of the floating bar; screens pad their content by [FloatingBarClearance] to scroll clear of it. */
val FloatingBarHeight = 64.dp
val FloatingBarClearance = FloatingBarHeight + 24.dp

data class NavBarItem(val icon: ImageVector, val label: String)

/** A floating pill of destinations; the selected one widens to show its label on the accent. */
@Composable
fun FloatingNavBar(
    items: List<NavBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(FloatingBarHeight),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier.padding(8.dp).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val background by animateColorAsState(if (selected) AtomicTheme.colors.accent else Color.Transparent, label = "tab")
                val content by animateColorAsState(
                    if (selected) AtomicTheme.colors.onAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tabContent",
                )
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(background)
                        .selectable(selected = selected, onClick = { onSelect(index) }, role = Role.Tab)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, contentDescription = if (selected) null else item.label, tint = content)
                    AnimatedVisibility(selected, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                        Text(item.label, style = MaterialTheme.typography.labelLarge, color = content, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
