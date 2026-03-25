package com.jesuslcorominas.teamflowmanager.ui.components.form

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R

@Composable
fun ListSummaryText(
    items: List<String>,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val context = LocalContext.current
    val text =
        when {
            items.isEmpty() -> ""
            items.size == 1 -> context.getString(R.string.list_single, items.first())
            else -> context.getString(R.string.list_multiple, items.first(), items.size - 1)
        }

    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Preview(showBackground = true)
@Composable
private fun ListSummaryTextPreview() {
    MaterialTheme {
        ListSummaryText(listOf("Forward", "Midfielder", "Defender"))
    }
}
