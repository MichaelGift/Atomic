package com.myth.atomic.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** The app's card: a flat raised surface with large corners and no elevation shadow. */
@Composable
fun AtomicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable () -> Unit = { Column(Modifier.padding(contentPadding), content = content) }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large, color = color, content = body)
    } else {
        Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = color, content = body)
    }
}

/** A small uppercase label above a group of content, with an optional count and action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    action: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = color)
        if (count != null) {
            Box(
                Modifier.background(color.copy(alpha = 0.14f), CircleShape).padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
        Box(Modifier.weight(1f))
        action()
    }
}

/** A compact pill for metadata such as a goal name or a due date. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    Row(
        modifier = modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) Box(Modifier.size(8.dp).background(dotColor, CircleShape))
        Text(text, style = MaterialTheme.typography.labelMedium, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
