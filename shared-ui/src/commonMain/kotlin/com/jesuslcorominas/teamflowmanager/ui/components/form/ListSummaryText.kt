package com.jesuslcorominas.teamflowmanager.ui.components.form

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.list_multiple
import teamflowmanager.shared_ui.generated.resources.list_single

@Composable
fun ListSummaryText(
    items: List<String>,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val text = when {
        items.isEmpty() -> ""
        items.size == 1 -> stringResource(Res.string.list_single, items.first())
        else -> stringResource(Res.string.list_multiple, items.first(), items.size - 1)
    }

    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
